// Modified by goocompound.
package io.github.goocompound.travelstaff.block;

import io.github.goocompound.travelstaff.TravelAnchorRegistry;
import io.github.goocompound.travelstaff.TravelStaffMain;
import io.github.goocompound.travelstaff.item.ItemTravelStaff;
import io.github.goocompound.travelstaff.util.GuiHelper;
import io.github.goocompound.travelstaff.util.TeleportHandler;
import io.github.goocompound.travelstaff.util.TravelAnchorList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockTravelAnchor extends Block implements EntityBlock {

    private static final VoxelShape SHAPE = Shapes.box(0.01, 0.01, 0.01, 0.99, 0.99, 0.99);

    public BlockTravelAnchor() {
        super(BlockBehaviour.Properties.of(Material.METAL).requiresCorrectToolForDrops().strength(2.0F, 6.0F));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return TravelAnchorRegistry.TRAVEL_ANCHOR_TILE.get().create(pos, state);
    }

    static Random rand = new Random();
    @Override
    public void onPlace(BlockState $$0, Level level, BlockPos pos, BlockState $$3, boolean $$4) {
        super.onPlace($$0, level, pos, $$3, $$4);
        if (!level.isClientSide()){
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof TileTravelAnchor) {
                if (((TileTravelAnchor) tile).getName().isEmpty()){
                    ((TileTravelAnchor) tile).setName("untitled " + rand.nextInt(10000));
                    tile.setChanged();
                }
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getLightBlock(@Nonnull BlockState state, BlockGetter level, @Nonnull BlockPos pos) {
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof TileTravelAnchor) {
            BlockState mimic = ((TileTravelAnchor) tile).getMimic();
            if (mimic != null) {
                return mimic.getLightBlock(level, pos);
            }
        }
        return super.getLightBlock(state, level, pos);
    }
    
    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(@Nonnull BlockState state, BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof TileTravelAnchor) {
            BlockState mimic = ((TileTravelAnchor) tile).getMimic();
            if (mimic != null && !mimic.is(TravelAnchorRegistry.TRAVEL_ANCHOR.get())) {
                return mimic.getShape(level, pos, context);
            }
        }
        return Shapes.block();
    }

    // since i cant access hasCollision directly to set based on mimic i must to this. because saplings getShape makes it think you can walk on them
    @Override
    public VoxelShape getCollisionShape(BlockState $$0, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof TileTravelAnchor) {
            BlockState mimic = ((TileTravelAnchor) tile).getMimic();
            if (mimic != null && !mimic.is(TravelAnchorRegistry.TRAVEL_ANCHOR.get())) {
                return mimic.getCollisionShape(level, pos, context);
            }
        }
        return super.getCollisionShape($$0, level, pos, context);
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOcclusionShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos) {
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof TileTravelAnchor) {
            BlockState mimic = ((TileTravelAnchor) tile).getMimic();
            if (mimic != null) {
                return mimic.getBlockSupportShape(level, pos);
            }
        }
        return SHAPE;
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, @Nonnull TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.travelstaff.travel_anchor_block"));
    }

    @Nonnull
    @Override
    public InteractionResult use(@Nonnull BlockState state, Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        if (!level.isClientSide) {
            if (TeleportHandler.canItemTeleport(player, hand) && TeleportHandler.anchorTeleport(level, player, player.blockPosition().immutable().below(), hand)) {
                return InteractionResult.SUCCESS;
            }

            ItemStack item = player.getItemInHand(InteractionHand.OFF_HAND);
            if (!item.isEmpty() && item.getItem() instanceof BlockItem && player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() && hand == InteractionHand.MAIN_HAND) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof TileTravelAnchor) {
                    BlockState mimicState = ((BlockItem) item.getItem()).getBlock().getStateForPlacement(new BlockPlaceContext(player, InteractionHand.OFF_HAND, item, hit));
                    if (mimicState == null || mimicState.getBlock() == this) {
                        ((TileTravelAnchor) be).setMimic(null);
                    } else {
                        ((TileTravelAnchor) be).setMimic(mimicState);
                    }
                }
                return InteractionResult.SUCCESS;
            }
            super.use(state, level, pos, player, hand, hit);
        } else {
            if (!TeleportHandler.canItemTeleport(player, InteractionHand.MAIN_HAND) && (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() || !(player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof BlockItem))){
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof TileTravelAnchor && !(player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ItemTravelStaff)) {
                    String name = ((TileTravelAnchor) be).getName();

                    // when you relog it doesnt sync the actual tile so do this.
                    if (name.isEmpty()){
                        name = TravelAnchorList.get(level).getAnchor(pos);
                        if (name == null) name = "";
                    }

                    GuiHelper.openAnchorGui(name, pos);
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
        TravelAnchorList.get(level).setAnchor(level, pos, null, this.defaultBlockState());
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public RenderShape getRenderShape(@Nonnull BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}

