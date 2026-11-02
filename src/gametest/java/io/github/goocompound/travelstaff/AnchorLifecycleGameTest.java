package io.github.goocompound.travelstaff;

import io.github.goocompound.travelstaff.block.TileTravelAnchor;
import io.github.goocompound.travelstaff.util.TravelAnchorList;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AnchorLifecycleGameTest {
    @GameTest
    public void pickupRemovesOldDestination(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        placeAnchor(level, pos);
        TravelAnchorList list = TravelAnchorList.get(level);
        helper.assertTrue(list.getEntry(pos) != null, "Placed anchor must be discoverable");

        // Carry On 2.12.0 detaches the block entity before replacing the block.
        level.removeBlockEntity(pos);
        level.removeBlock(pos, false);
        helper.assertTrue(list.getEntry(pos) == null, "Pickup must remove the old destination");
        helper.succeed();
    }

    @GameTest
    public void placementRegistersRestoredNameAndDisguise(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos source = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockPos destination = helper.absolutePos(new BlockPos(3, 1, 1));
        TileTravelAnchor original = placeAnchor(level, source);
        original.setName("Moved anchor");
        BlockState disguise = Blocks.OAK_SLAB.defaultBlockState();
        original.setMimic(disguise);
        CompoundTag saved = original.saveWithFullMetadata(level.registryAccess());
        level.removeBlockEntity(source);
        level.removeBlock(source, false);

        // Carry On restores NBT without a level, then installs the restored entity.
        BlockState state = TravelAnchorRegistry.TRAVEL_ANCHOR.get().defaultBlockState();
        TileTravelAnchor restored = (TileTravelAnchor) BlockEntity.loadStatic(destination, state, saved, level.registryAccess());
        helper.assertTrue(restored != null && restored.getLevel() == null, "Carried entity starts detached");
        level.setBlockAndUpdate(destination, state);
        level.setBlockEntity(restored);
        TravelAnchorList list = TravelAnchorList.get(level);
        TravelAnchorList.Entry entry = list.getEntry(destination);
        helper.assertTrue(entry != null && entry.name.equals("Moved anchor"), "Discovery must use the restored name");
        helper.assertTrue(entry.state == disguise && restored.getMimic() == disguise, "Disguise must survive the move");

        // Chunk unloading also marks entities removed; it must not erase saved destinations.
        restored.setRemoved();
        helper.assertTrue(list.getEntry(destination) != null, "Unloading must retain the destination");
        restored.clearRemoved();
        level.removeBlock(destination, false);
        helper.assertTrue(list.getEntry(destination) == null, "Ordinary removal must still clear discovery");
        helper.succeed();
    }

    @GameTest
    public void chunkLoadRepairsOnlyMissingAnchorsInThatChunk(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos real = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockPos ghost = real.offset((real.getX() & 15) == 15 ? -1 : 1, 0, 0);
        BlockPos unloaded = new BlockPos(-20_000_000, real.getY(), -20_000_000);
        placeAnchor(level, real);
        level.setBlockAndUpdate(ghost, Blocks.AIR.defaultBlockState());
        TravelAnchorList list = TravelAnchorList.get(level);
        list.setAnchor(level, ghost, "Old location", null);
        list.setAnchor(level, unloaded, "Unloaded destination", null);
        list.setDirty(false);
        helper.assertFalse(level.hasChunkAt(unloaded), "Distant chunk must start unloaded");

        ServerChunkEvents.CHUNK_LOAD.invoker().onChunkLoad(level, level.getChunkAt(real), false);
        helper.assertTrue(list.getEntry(ghost) == null, "Chunk load must remove an existing ghost");
        helper.assertTrue(list.getEntry(real) != null, "A real anchor must remain discoverable");
        helper.assertTrue(list.getEntry(unloaded) != null, "An unchecked chunk's destination must be retained");
        helper.assertFalse(level.hasChunkAt(unloaded), "Repair must not force-load other chunks");
        helper.assertTrue(list.isDirty(), "Repair must be saved");
        TravelAnchorList reloaded = new TravelAnchorList(list.save(new CompoundTag()));
        helper.assertTrue(reloaded.getEntry(ghost) == null && reloaded.getEntry(real) != null,
                "Repaired entries must survive serialization");
        list.setAnchor(level, unloaded, null, null);
        helper.succeed();
    }

    private static TileTravelAnchor placeAnchor(ServerLevel level, BlockPos pos) {
        level.setBlockAndUpdate(pos, TravelAnchorRegistry.TRAVEL_ANCHOR.get().defaultBlockState());
        return (TileTravelAnchor) level.getBlockEntity(pos);
    }
}
