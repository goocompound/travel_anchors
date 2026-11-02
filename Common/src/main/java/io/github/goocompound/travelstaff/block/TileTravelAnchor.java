// Modified by goocompound.
package io.github.goocompound.travelstaff.block;

import io.github.goocompound.travelstaff.TravelAnchorRegistry;
import io.github.goocompound.travelstaff.util.TravelAnchorList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileTravelAnchor extends BlockEntity {
    private BlockState mimic;
    private String name = "";

    public TileTravelAnchor(BlockPos pos, BlockState state) {
        super(TravelAnchorRegistry.TRAVEL_ANCHOR_TILE.get(), pos, state);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        // NBT may have been restored before this entity was attached to a level.
        if (!level.isClientSide()) {
            TravelAnchorList.get(level).setAnchor(level, worldPosition, name, mimic);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("travel_anchor_name", name);
        output.store("mimic", BlockState.FULL_CODEC,
                mimic == null ? TravelAnchorRegistry.TRAVEL_ANCHOR.get().defaultBlockState() : mimic);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        name = input.getStringOr("travel_anchor_name", "");
        BlockState state = input.read("mimic", BlockState.CODEC)
                .orElse(TravelAnchorRegistry.TRAVEL_ANCHOR.get().defaultBlockState());
        mimic = state.is(TravelAnchorRegistry.TRAVEL_ANCHOR.get()) ? null : state;
        if (level != null) {
            TravelAnchorList.get(level).setAnchor(level, worldPosition, name, mimic);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        if (level != null) {
            TravelAnchorList.get(level).setAnchor(level, worldPosition, name, mimic);
        }
    }

    public BlockState getMimic() {
        return mimic;
    }

    public void setMimic(BlockState mimic) {
        this.mimic = mimic;
        if (level != null) {
            TravelAnchorList.get(level).setAnchor(level, worldPosition, name, mimic);
        }
        setChanged();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && !level.isClientSide()) {
            TravelAnchorList.get(level).setAnchor(level, pos, null,
                    TravelAnchorRegistry.TRAVEL_ANCHOR.get().defaultBlockState());
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
