package io.github.goocompound.travelstaff.config;

import io.github.goocompound.travelstaff.FabricNetworkHandler;
import io.github.goocompound.travelstaff.TravelStaffMain;
import io.github.goocompound.travelstaff.TravelStaffMain.ClientConfigData;
import io.github.goocompound.travelstaff.TravelStaffMain.SyncedConfigData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;
import java.util.Map;

final class TravelStaffConfigScreen extends OptionsSubScreen {
    private final IntegratedServer server;
    private final boolean hasWorld;
    private final ClientConfigData clientValues = new ClientConfigData();
    private final Map<String, String> worldValues = new LinkedHashMap<>();
    private final Map<String, String> originalWorldValues;
    private final Map<String, EditBox> fields = new LinkedHashMap<>();
    private CycleButton<Boolean> elevator;
    private NameScaleSlider nameScale;
    private Button save;
    private Button cancel;
    private Button reset;
    private StringWidget status;
    private boolean saving;

    TravelStaffConfigScreen(Screen parent) {
        super(parent, Minecraft.getInstance().options, text("title"));
        server = minecraft.getSingleplayerServer();
        hasWorld = minecraft.level != null;
        ClientConfigData current = TravelStaffConfig.getClientConfig();
        clientValues.isElevatorMode = current.isElevatorMode;
        clientValues.nameplateScale = Double.isFinite(current.nameplateScale)
                ? Math.clamp(current.nameplateScale, 0.25, 2) : new ClientConfigData().nameplateScale;
        setWorldValues(TravelStaffConfig.getSyncedConfig());
        originalWorldValues = Map.copyOf(worldValues);
    }

    @Override
    protected void addOptions() {
        fields.clear();
        list.addHeader(text("client"));
        elevator = CycleButton.onOffBuilder(new ClientConfigData().isElevatorMode)
                .create(0, 0, 310, 20, text("isElevatorMode"), (button, value) -> clientValues.isElevatorMode = value);
        elevator.setValue(clientValues.isElevatorMode);
        elevator.setTooltip(Tooltip.create(text("isElevatorMode.help")));
        list.addBig(elevator);
        nameScale = new NameScaleSlider();
        list.addBig(nameScale);
        list.addHeader(text(!hasWorld ? "world.closed" : server == null ? "world.remote" : "world"));
        if (hasWorld) {
            worldValues.forEach((key, value) -> {
                StringWidget label = new StringWidget(150, 20, text(key), font);
                label.setTooltip(Tooltip.create(text(key + ".help")));
                EditBox input = new EditBox(font, 0, 0, 150, 20, text(key));
                input.setMaxLength(32);
                input.setValue(value);
                input.setEditable(server != null);
                input.setTooltip(Tooltip.create(text(key + ".help")));
                input.setResponder(changed -> {
                    worldValues.put(key, changed);
                    validateInputs();
                });
                fields.put(key, input);
                list.addSmall(label, input);
            });
        }
    }

    @Override
    protected void addFooter() {
        layout.setFooterHeight(56);
        LinearLayout footer = LinearLayout.vertical().spacing(5);
        footer.defaultCellSetting().alignHorizontallyCenter();
        status = footer.addChild(new StringWidget(310, 12, Component.empty(), font));
        LinearLayout buttons = footer.addChild(LinearLayout.horizontal().spacing(5));
        reset = buttons.addChild(Button.builder(text("reset"), button -> resetDefaults()).width(100).build());
        cancel = buttons.addChild(Button.builder(Component.translatable("gui.cancel"), button -> onClose()).width(100).build());
        save = buttons.addChild(Button.builder(text("save"), button -> save()).width(100).build());
        layout.addToFooter(footer);
        validateInputs();
    }

    private void setWorldValues(SyncedConfigData config) {
        worldValues.put("maxTeleportDistance", Integer.toString(config.maxTeleportDistance));
        worldValues.put("shortTeleportDistance", Integer.toString(config.shortTeleportDistance));
        worldValues.put("staffCooldownTicks", Integer.toString(config.staffCooldownTicks));
        worldValues.put("rangeEnchantScaling", Float.toString(config.rangeEnchantScaling));
        worldValues.put("maxAngle", Double.toString(config.maxAngle));
    }

    private void resetDefaults() {
        ClientConfigData defaults = new ClientConfigData();
        clientValues.isElevatorMode = defaults.isElevatorMode;
        elevator.setValue(defaults.isElevatorMode);
        nameScale.setScale(defaults.nameplateScale);
        if (server != null) {
            setWorldValues(new SyncedConfigData());
            fields.forEach((key, input) -> input.setValue(worldValues.get(key)));
        }
        validateInputs();
    }

    private SyncedConfigData worldDraft() {
        SyncedConfigData draft = new SyncedConfigData();
        draft.maxTeleportDistance = integer("maxTeleportDistance");
        draft.shortTeleportDistance = integer("shortTeleportDistance");
        draft.staffCooldownTicks = integer("staffCooldownTicks");
        draft.rangeEnchantScaling = (float) number("rangeEnchantScaling");
        draft.maxAngle = number("maxAngle");
        TravelStaffConfig.validateServer(draft);
        return draft;
    }

    private int integer(String key) {
        try { return Integer.parseInt(worldValues.get(key).trim()); }
        catch (NumberFormatException exception) { throw new IllegalArgumentException(key); }
    }

    private double number(String key) {
        try { return Double.parseDouble(worldValues.get(key).trim()); }
        catch (NumberFormatException exception) { throw new IllegalArgumentException(key); }
    }

    private boolean worldChanged() {
        return server != null && !worldValues.equals(originalWorldValues);
    }

    private void validateInputs() {
        if (save == null || saving) return;
        try {
            if (worldChanged()) worldDraft();
            save.active = true;
            status.setMessage(Component.empty());
        } catch (IllegalArgumentException exception) {
            save.active = false;
            status.setMessage(Component.translatable("config.travelstaff.invalid", text(exception.getMessage())));
        }
    }

    private void save() {
        SyncedConfigData world = worldChanged() ? worldDraft() : null;
        if (world != null && minecraft.getSingleplayerServer() != server) {
            status.setMessage(text("world.closed"));
            return;
        }
        try {
            TravelStaffConfig.saveClient(clientValues);
        } catch (Exception exception) {
            TravelStaffMain.LOG.error("Could not save client settings", exception);
            status.setMessage(text("save.client_failed"));
            return;
        }
        if (world == null) {
            onClose();
            return;
        }
        setSaving(true);
        server.execute(() -> {
            try {
                TravelStaffConfig.saveServer(server, world);
                server.getPlayerList().getPlayers().forEach(FabricNetworkHandler::sendConfig);
                minecraft.execute(() -> finishSave(null));
            } catch (Exception exception) {
                TravelStaffMain.LOG.error("Could not save world settings", exception);
                minecraft.execute(() -> finishSave(text("save.world_failed")));
            }
        });
    }

    private void finishSave(Component error) {
        if (minecraft.gui.screen() != this) return;
        setSaving(false);
        if (error == null) onClose();
        else status.setMessage(error);
    }

    private void setSaving(boolean saving) {
        this.saving = saving;
        save.active = cancel.active = reset.active = elevator.active = nameScale.active = !saving;
        fields.values().forEach(input -> input.setEditable(!saving && server != null));
        status.setMessage(saving ? text("saving") : Component.empty());
    }

    @Override
    public void onClose() {
        if (!saving) minecraft.gui.setScreen(lastScreen);
    }

    private static Component text(String key) {
        return Component.translatable("config.travelstaff." + key);
    }

    private final class NameScaleSlider extends AbstractSliderButton {
        private NameScaleSlider() {
            super(0, 0, 310, 20, Component.empty(), (clientValues.nameplateScale - 0.25) / 1.75);
            setTooltip(Tooltip.create(text("nameplateScale.help")));
            updateMessage();
        }

        private void setScale(double scale) {
            clientValues.nameplateScale = scale;
            value = (scale - 0.25) / 1.75;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("config.travelstaff.nameplateScale", Math.round(clientValues.nameplateScale * 100)));
        }

        @Override
        protected void applyValue() {
            clientValues.nameplateScale = Math.round((0.25 + value * 1.75) * 100) / 100.0;
        }
    }
}
