// Modified by goocompound.
package io.github.goocompound.travelstaff.util;

import io.github.goocompound.travelstaff.TravelAnchorRegistry;
import io.github.goocompound.travelstaff.TravelStaffMain;
import io.github.goocompound.travelstaff.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.resources.Identifier;
import com.mojang.serialization.Codec;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class TravelAnchorList extends SavedData {
    public static final Codec<TravelAnchorList> CODEC = CompoundTag.CODEC.xmap(
            TravelAnchorList::new, anchors -> anchors.save(new CompoundTag()));
    public static final SavedDataType<TravelAnchorList> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(TravelStaffMain.MOD_ID, "anchors"), TravelAnchorList::new, CODEC, null);

    private static final TravelAnchorList clientInstance = new TravelAnchorList();

    public static TravelAnchorList get(Level level) {
        if (!level.isClientSide()) {
            return ((ServerLevel) level).getDataStorage().computeIfAbsent(TYPE);
        } else {
            return clientInstance;
        }
    }

    public static void clearClient() {
        clientInstance.anchors.clear();
    }

    public TravelAnchorList() {
        
    }

    public TravelAnchorList(CompoundTag nbt) {
        this();
        this.load(nbt);
    }

    public final HashMap<BlockPos, Entry> anchors = new HashMap<>();

    public void load(@Nonnull CompoundTag nbt) {
        this.anchors.clear();
        if (nbt.getList("anchors").isPresent()) {
            boolean anyOldFormat = false;
            ListTag list = nbt.getListOrEmpty("anchors");
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entryNBT = list.getCompoundOrEmpty(i);
                if (entryNBT.contains("x") && entryNBT.contains("y") && entryNBT.contains("z") && entryNBT.contains("name")) {
                    BlockPos pos = new BlockPos(entryNBT.getIntOr("x", 0), entryNBT.getIntOr("y", 0), entryNBT.getIntOr("z", 0)).immutable();
                    String name = entryNBT.getStringOr("name", "");
                    if (!name.isEmpty()) {
                        BlockState state;

                        if (entryNBT.contains("state2")){
                            state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK, entryNBT.getCompoundOrEmpty("state2"));
                        }
                        else if (entryNBT.contains("state")){  // keep compat
                            state = Block.stateById(entryNBT.getIntOr("state", 0));
                            anyOldFormat = true;
                        }
                        else {
                            state = TravelAnchorRegistry.TRAVEL_ANCHOR.get().defaultBlockState();
                        }

                        this.anchors.put(pos, new Entry(entryNBT.getStringOr("name", ""), state));
                    }
                }
            }

            if (anyOldFormat) this.setDirty();
        }
    }

    @Nonnull
    public CompoundTag save(@Nonnull CompoundTag compound) {
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, Entry> entry : this.anchors.entrySet()) {
            CompoundTag entryNBT = new CompoundTag();
            entryNBT.putInt("x", entry.getKey().getX());
            entryNBT.putInt("y", entry.getKey().getY());
            entryNBT.putInt("z", entry.getKey().getZ());
            entryNBT.putString("name", entry.getValue().name);
            entryNBT.put("state2", NbtUtils.writeBlockState(entry.getValue().state));
            list.add(entryNBT);
        }
        compound.put("anchors", list);
        return compound;
    }

    // Runtime block-state IDs keep the synchronized list smaller than the persistent representation.
    public CompoundTag saveForNetwork() {
        CompoundTag compound = new CompoundTag();
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, Entry> entry : this.anchors.entrySet()) {
            CompoundTag entryNBT = new CompoundTag();
            entryNBT.putInt("x", entry.getKey().getX());
            entryNBT.putInt("y", entry.getKey().getY());
            entryNBT.putInt("z", entry.getKey().getZ());
            entryNBT.putString("name", entry.getValue().name);
            entryNBT.putInt("state", Block.getId(entry.getValue().state));
            list.add(entryNBT);
        }
        compound.put("anchors", list);
        return compound;
    }



    public void setAnchor(Level level, BlockPos pos, @Nullable String name, @Nullable BlockState state) {
        if (!level.isClientSide()) {
            boolean needsUpdate = false;
            BlockPos immutablePos = pos.immutable();
            if (name == null || name.trim().isEmpty()) {
                if (this.anchors.containsKey(immutablePos)) {
                    this.anchors.remove(immutablePos);
                    needsUpdate = true;
                }
            } else {
                if (state == null) state = TravelAnchorRegistry.TRAVEL_ANCHOR.get().defaultBlockState();
                Entry oldEntry = this.anchors.getOrDefault(immutablePos, null);
                if (oldEntry == null || !oldEntry.name.equals(name) || oldEntry.state != state) {
                    this.anchors.put(pos.immutable(), new Entry(name, state));
                    needsUpdate = true;
                }
            }
            this.setDirty();
            if (needsUpdate) {
                Services.NETWORK.sendAnchorListToClients((ServerLevel) level, this);
            }
        }
    }

    public String getAnchor(BlockPos pos) {
        Entry entry = this.getEntry(pos);
        return entry == null ? null : entry.name;
    }
    
    public Entry getEntry(BlockPos pos) {
        return this.anchors.getOrDefault(pos.immutable(), null);
    }

    public Stream<Pair<BlockPos, String>> getAnchorsAround(Vec3 pos, double maxDistanceSq) {
        return this.anchors.entrySet().stream()
                .filter(entry -> entry.getKey().distSqr(BlockPos.containing(pos)) < maxDistanceSq)
                .map(entry -> Pair.of(entry.getKey(), entry.getValue().name));
    }
    
    public static class Entry {
        
        public String name;
        public BlockState state;

        public Entry(String name, BlockState state) {
            this.name = name;
            this.state = state;
        }
    }
}

