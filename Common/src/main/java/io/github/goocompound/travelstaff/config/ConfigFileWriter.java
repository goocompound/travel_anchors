package io.github.goocompound.travelstaff.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.Strictness;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

/** Edits config values while preserving comments and unknown fields. */
final class ConfigFileWriter {
    private static final Gson GSON = new GsonBuilder().setStrictness(Strictness.LENIENT).create();

    private ConfigFileWriter() {}

    static void write(Path path, String defaults, Object values) throws IOException {
        String source = Files.exists(path) ? Files.readString(path, StandardCharsets.UTF_8) : defaults;
        String updated;
        try {
            updated = update(source, GSON.toJsonTree(values).getAsJsonObject());
        } catch (RuntimeException exception) {
            throw new IOException("Cannot safely update config " + path, exception);
        }
        if (Files.exists(path) && updated.equals(source)) return;
        Files.createDirectories(path.getParent());
        Path temporary = Files.createTempFile(path.getParent(), path.getFileName().toString(), ".tmp");
        try {
            Files.writeString(temporary, updated, StandardCharsets.UTF_8);
            try {
                Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    static String update(String source, JsonObject values) {
        JsonObject expected = GSON.fromJson(source, JsonObject.class);
        if (expected == null) throw new IllegalArgumentException("Expected a config object");
        values.entrySet().forEach(entry -> expected.add(entry.getKey(), entry.getValue()));
        List<Token> tokens = tokens(source);
        if (tokens.isEmpty() || !tokens.getFirst().text.equals("{") || !tokens.getLast().text.equals("}")) {
            throw new IllegalArgumentException("Expected a config object");
        }
        List<Edit> edits = new ArrayList<>();
        var found = new HashSet<String>();
        int index = 1;
        int lastValueEnd = -1;
        boolean separator = false;
        while (index < tokens.size() - 1) {
            String key = GSON.fromJson(tokens.get(index++).text, String.class);
            String delimiter = tokens.get(index++).text;
            if (!delimiter.equals(":") && !delimiter.equals("=") && !delimiter.equals("=>")) {
                throw new IllegalArgumentException("Expected a property separator");
            }
            Token first = tokens.get(index++);
            int depth = first.text.equals("{") || first.text.equals("[") ? 1 : 0;
            while (depth > 0) {
                String token = tokens.get(index++).text;
                if (token.equals("{") || token.equals("[")) depth++;
                if (token.equals("}") || token.equals("]")) depth--;
            }
            lastValueEnd = tokens.get(index - 1).end;
            if (values.has(key)) {
                found.add(key);
                String replacement = GSON.toJson(values.get(key));
                if (!values.get(key).equals(GSON.fromJson(source.substring(first.start, lastValueEnd), JsonElement.class))) {
                    edits.add(new Edit(first.start, lastValueEnd, replacement));
                }
            }
            separator = tokens.get(index).text.equals(",") || tokens.get(index).text.equals(";");
            if (separator) index++;
        }
        String newline = source.contains("\r\n") ? "\r\n" : "\n";
        List<String> missing = values.entrySet().stream().filter(entry -> !found.contains(entry.getKey()))
                .map(entry -> "  " + GSON.toJson(entry.getKey()) + ": " + GSON.toJson(entry.getValue())).toList();
        if (!missing.isEmpty()) {
            int end = tokens.getLast().start;
            String prefix = "";
            if (lastValueEnd >= 0 && !separator) {
                if (lastValueEnd == end) prefix = ",";
                else edits.add(new Edit(lastValueEnd, lastValueEnd, ","));
            }
            edits.add(new Edit(end, end, prefix + newline + String.join("," + newline, missing) + newline));
        }
        StringBuilder result = new StringBuilder(source);
        edits.sort(Comparator.comparingInt(Edit::start).reversed());
        for (Edit edit : edits) result.replace(edit.start, edit.end, edit.text);
        String updated = result.toString();
        // Verify that only the requested values changed.
        if (!expected.equals(GSON.fromJson(updated, JsonObject.class))) {
            throw new IllegalArgumentException("Config update did not preserve the document");
        }
        return updated;
    }

    private static List<Token> tokens(String source) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        while (i < source.length()) {
            char c = source.charAt(i);
            if (Character.isWhitespace(c) || c == '\ufeff') { i++; continue; }
            if (tokens.isEmpty() && source.startsWith(")]}'\n", i)) { i += 5; continue; }
            if (c == '#' || source.startsWith("//", i)) {
                while (i < source.length() && source.charAt(i) != '\n' && source.charAt(i) != '\r') i++;
                continue;
            }
            if (source.startsWith("/*", i)) {
                int end = source.indexOf("*/", i + 2);
                if (end < 0) throw new IllegalArgumentException("Unterminated comment");
                i = end + 2;
                continue;
            }
            int start = i++;
            if (c == '"' || c == '\'') {
                while (i < source.length()) {
                    char next = source.charAt(i++);
                    if (next == '\\') i++;
                    else if (next == c) break;
                }
            } else if (c == '=' && i < source.length() && source.charAt(i) == '>') {
                i++;
            } else if ("{}[],:;=".indexOf(c) < 0) {
                while (i < source.length() && !Character.isWhitespace(source.charAt(i))
                        && "{}[],:;=/#".indexOf(source.charAt(i)) < 0) i++;
            }
            tokens.add(new Token(start, i, source.substring(start, i)));
        }
        return tokens;
    }

    private record Token(int start, int end, String text) {}
    private record Edit(int start, int end, String text) {}
}
