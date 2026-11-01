// Modified by goocompound.
package io.github.goocompound.travelstaff.util;

import io.github.goocompound.travelstaff.TravelAnchorRegistry;
import io.github.goocompound.travelstaff.TravelStaffMain;
import io.github.goocompound.travelstaff.block.TileTravelAnchor;
import io.github.goocompound.travelstaff.item.ItemTravelStaff;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Optional;


// TODO: use tags instead of directly comparing?

public class TeleportHandler {

    public static boolean anchorTeleport(Level level, Player player, @Nullable BlockPos except, @Nullable InteractionHand hand) {
        Pair<BlockPos, String> anchor = getAnchorToTeleport(level, player, except);
        return teleportPlayer(player, anchor, hand);
    }

    public static Pair<BlockPos, String> getAnchorToTeleport(Level level, Player player, @Nullable BlockPos except) {
        boolean allow = !player.isShiftKeyDown() || canBlockTeleport(player);
        if (allow) {
            double maxDistance = getMaxDistance(player);
            Vec3 positionVec = player.position().add(0, player.getEyeHeight(), 0);
            Optional<Pair<BlockPos, String>> anchor = TravelAnchorList.get(level).getAnchorsAround(player.position(), Math.pow(maxDistance, 2))
                    .filter(pair -> except == null || !except.equals(pair.getLeft()))
                    .min((p1, p2) -> {
                        double angle1 = Math.abs(getAngleRadians(positionVec, p1.getLeft(), player.getYRot(), player.getXRot()));
                        double angle2 = Math.abs(getAngleRadians(positionVec, p2.getLeft(), player.getYRot(), player.getXRot()));
                        return Double.compare(angle1, angle2);
                    }).filter(p -> Math.abs(getAngleRadians(positionVec, p.getLeft(), player.getYRot(), player.getXRot())) <= Math.toRadians(TravelStaffMain.getConfig(level).maxAngle))
                    .filter(p -> canTeleportTo(level, p.getLeft()));
            return anchor.orElse(null);
        } else {
            return null;
        }
    }
    
    public static boolean teleportPlayer(Player player, @Nullable Pair<BlockPos, String> anchor, @Nullable InteractionHand hand) {
        if (anchor != null) {
            if (!player.level().isClientSide()) {
                Vec3 teleportVec = checkTeleport(player, anchor.getLeft().above());
                if (teleportVec == null) {
                    return false;
                }
                player.teleportTo(teleportVec.x()+0.5, teleportVec.y(), teleportVec.z()+0.5);
            }
            player.fallDistance = 0;
            if (hand != null) {
                player.swing(hand, SwingAnimation.DEFAULT, true);
            }
            playTeleportSound(player);
            if (!player.level().isClientSide()) {
                player.sendOverlayMessage(Component.translatable("travelstaff.tp.success", anchor.getRight()));
            }
            return true;
        } else {
            if (!player.level().isClientSide()) {
                player.sendOverlayMessage(Component.translatable("travelstaff.tp.fail"));
            }
            return false;
        }
    }

    public static boolean shortTeleport(Level level, Player player, InteractionHand hand) {
        Vec3 targetVec = player.position().add(0, player.getEyeHeight(), 0);
        Vec3 lookVec = player.getLookAngle();
        BlockPos target = null;
        for (double i = TravelStaffMain.getConfig(level).shortTeleportDistance; i >= 2; i -= 0.5) {
            Vec3 v3d = targetVec.add(lookVec.multiply(i, i, i));
            target = BlockPos.containing(Math.round(v3d.x), Math.round(v3d.y), Math.round(v3d.z));
            if (canTeleportTo(level, target.below())) { //to use the same check as the anchors use the position below
                break;
            } else {
                target = null;
            }
        }
        if (target != null) {
            if (!player.level().isClientSide()) {
                Vec3 teleportVec = checkTeleport(player, target);
                if (teleportVec == null) {
                    return false;
                }
                player.teleportTo(teleportVec.x()+0.5, teleportVec.y(), teleportVec.z()+0.5);
            }
            player.fallDistance = 0;
            player.swing(hand, SwingAnimation.DEFAULT, true);
            playTeleportSound(player);
            return true;
        } else {
            if (!player.level().isClientSide()) {
                player.sendOverlayMessage(Component.translatable("travelstaff.hop.fail"));
            }
            return false;
        }
    }

    public static boolean canTeleportTo(BlockGetter level, BlockPos target) {
        return !fullBlockAt(level, target.immutable().above(1))
                && !fullBlockAt(level, target.immutable().above(2))
                && target.getY() >= level.getMinY();
    }

    private static boolean fullBlockAt(BlockGetter level, BlockPos target){
        return Block.isShapeFullBlock(level.getBlockState(target).getShape(level, target));
    }

    public static boolean canPlayerTeleport(Player player, InteractionHand hand) {
        if (player != null) {
            return canItemTeleport(player, hand) || canBlockTeleport(player);
        }
        return false;
    }

    public static BlockPos down(Player player){
        return BlockPos.containing(player.getBoundingBox().getCenter().x, player.getY(), player.getBoundingBox().getCenter().z).below();
    }

    public static boolean canBlockTeleport(Player player) {
        return player.level().getBlockState(down(player)).getBlock() == TravelAnchorRegistry.TRAVEL_ANCHOR.get();// && !player.isShiftKeyDown();
    }

    public static boolean canItemTeleport(Player player, InteractionHand hand) {
        return player.getItemInHand(hand).getItem() instanceof ItemTravelStaff
                || getEnchantmentLevel(player, hand, TravelAnchorRegistry.TELEPORTATION) >= 1;
    }

    private static int getEnchantmentLevel(Player player, InteractionHand hand, ResourceKey<Enchantment> enchantment) {
        return EnchantmentHelper.getItemEnchantmentLevel(
                player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment),
                player.getItemInHand(hand));
    }

    private static void playTeleportSound(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSoundPacket(
                    BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.ENDERMAN_TELEPORT),
                    SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1F, 1F,
                    player.getRandom().nextLong()));
        } else if (player.isLocalPlayer()) {
            player.level().playLocalSound(player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1F, 1F, false);
        }
    }

    private static double getAngleRadians(Vec3 positionVec, BlockPos anchor, float yRot, float xRot) {
        Vec3 blockVec = new Vec3(anchor.getX() + 0.5 - positionVec.x, anchor.getY() + 1.0 - positionVec.y, anchor.getZ() + 0.5 - positionVec.z).normalize();
        Vec3 lookVec = Vec3.directionFromRotation(xRot, yRot).normalize();
        return Math.acos(lookVec.dot(blockVec));
    }

    public static double getMaxDistance(Player player) {
        int mainHandLevel = getEnchantmentLevel(player, InteractionHand.MAIN_HAND, TravelAnchorRegistry.RANGE);
        int offHandLevel = getEnchantmentLevel(player, InteractionHand.OFF_HAND, TravelAnchorRegistry.RANGE);
        int lvl = Math.max(mainHandLevel, offHandLevel);
        return TravelStaffMain.getConfig(player.level()).maxTeleportDistance * (1 + (lvl * TravelStaffMain.getConfig(player.level()).rangeEnchantScaling));
    }
    
    public static boolean canElevate(Player player) {
        if (player != null) {
            return player.level().getBlockState(down(player)).getBlock() == TravelAnchorRegistry.TRAVEL_ANCHOR.get();
        }
        return false;
    }
    
    public static boolean elevateUp(Player player) {
        if (!canElevate(player)) {
            return false;
        }
        Level level = player.level();
        BlockPos.MutableBlockPos searchPos = player.blockPosition().immutable().mutable();
        while (!level.isOutsideBuildHeight(searchPos) && (level.getBlockState(searchPos).getBlock() != TravelAnchorRegistry.TRAVEL_ANCHOR.get() || !canTeleportTo(level, searchPos))) {
            searchPos.move(Direction.UP);
        }
        BlockState state = level.getBlockState(searchPos);
        Pair<BlockPos, String> anchor = null;
        if (state.getBlock() == TravelAnchorRegistry.TRAVEL_ANCHOR.get() && canTeleportTo(level, searchPos)) {
            BlockPos target = searchPos.immutable();
            BlockEntity tile = level.getBlockEntity(target);
            if (tile instanceof TileTravelAnchor) {
                String name = ((TileTravelAnchor) tile).getName();
                if (!name.isEmpty()){
                    anchor = Pair.of(target, name);
                }
            }
        }
        return teleportPlayer(player, anchor, null);
    }
    
    public static boolean elevateDown(Player player) {
        if (!canElevate(player)) {
            return false;
        }
        Level level = player.level();
        BlockPos.MutableBlockPos searchPos = player.blockPosition().immutable().below(2).mutable();
        while (!level.isOutsideBuildHeight(searchPos) && (level.getBlockState(searchPos).getBlock() != TravelAnchorRegistry.TRAVEL_ANCHOR.get() || !canTeleportTo(level, searchPos))) {
            searchPos.move(Direction.DOWN);
        }
        BlockState state = level.getBlockState(searchPos);
        Pair<BlockPos, String> anchor = null;
        if (state.getBlock() == TravelAnchorRegistry.TRAVEL_ANCHOR.get() && canTeleportTo(level, searchPos)) {
            BlockPos target = searchPos.immutable();
            BlockEntity tile = level.getBlockEntity(target);
            if (tile instanceof TileTravelAnchor) {
                String name = ((TileTravelAnchor) tile).getName();
                if (!name.isEmpty()){
                    anchor = Pair.of(target, name);
                }
            }
        }
        return teleportPlayer(player, anchor, null);
    }


    @Nullable
    private static Vec3 checkTeleport(Player player, BlockPos target) {
//        EntityTeleportEvent event = new EntityTeleportEvent(player, target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
//        if (MinecraftForge.EVENT_BUS.post(event)) {
//            return null;
//        }
//        return new Vec3(event.getTargetX(), event.getTargetY(), event.getTargetZ());
        return new Vec3(target.getX(), target.getY(), target.getZ());
    }
}
