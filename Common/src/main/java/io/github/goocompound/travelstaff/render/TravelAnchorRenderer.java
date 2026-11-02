// Modified by goocompound.
package io.github.goocompound.travelstaff.render;

import io.github.goocompound.travelstaff.TravelAnchorRegistry;
import io.github.goocompound.travelstaff.TravelStaffMain;
import io.github.goocompound.travelstaff.block.RenderTravelAnchor;
import io.github.goocompound.travelstaff.util.TeleportHandler;
import io.github.goocompound.travelstaff.util.TravelAnchorList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public final class TravelAnchorRenderer {
    private static final RenderStateDataKey<Frame> FRAME = RenderStateDataKey.create(() -> "travelstaff:anchors");
    private static final BlockDisplayContext DISPLAY_CONTEXT = BlockDisplayContext.create();
    private static BlockModelResolver modelResolver;

    private TravelAnchorRenderer() {
    }

    public static void init() {
        BlockEntityRendererRegistry.register(TravelAnchorRegistry.TRAVEL_ANCHOR_TILE.get(), RenderTravelAnchor::new);
        LevelExtractionEvents.END_EXTRACTION.register(TravelAnchorRenderer::extractAnchors);
        LevelRenderEvents.COLLECT_SUBMITS.register(TravelAnchorRenderer::submitAnchors);
        InvalidateRenderStateCallback.EVENT.register(OutlineRenderType::clear);
    }

    private static void extractAnchors(LevelExtractionContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = context.level();
        LocalPlayer player = minecraft.player;
        List<Anchor> anchors = new ArrayList<>();
        if (level != null && player != null && canSeeAnchors(player)) {
            if (modelResolver == null) modelResolver = new BlockModelResolver(minecraft.getModelManager());
            double maxDistance = TeleportHandler.getMaxDistance(player);
            double maxDistanceSq = maxDistance * maxDistance;
            float partialTick = context.deltaTracker().getGameTimeDeltaPartialTick(false);
            Vec3 playerPosition = player.getPosition(partialTick);
            Pair<BlockPos, String> selected = TeleportHandler.getAnchorToTeleport(level, player, player.blockPosition().below());
            TravelAnchorList list = TravelAnchorList.get(level);
            for (BlockPos pos : list.anchors.keySet()) {
                double distanceSq = pos.distToLowCornerSqr(playerPosition.x, playerPosition.y, playerPosition.z);
                if (!isWithinOverlayRange(distanceSq, maxDistanceSq)) continue;
                TravelAnchorList.Entry entry = list.getEntry(pos);
                if (entry == null) continue;
                BlockState state = entry.state == null ? TravelAnchorRegistry.TRAVEL_ANCHOR.get().defaultBlockState() : entry.state;
                BlockModelRenderState model = new BlockModelRenderState();
                modelResolver.update(model, state, DISPLAY_CONTEXT);
                Component name = Component.literal(entry.name.trim());
                int light = level.hasChunkAt(pos) ? LightCoordsUtil.getLightCoords(level, pos) : LightCoordsUtil.FULL_BRIGHT;
                anchors.add(new Anchor(pos, model, name.getVisualOrderText(), minecraft.font.width(name),
                        entry.name.trim().isEmpty(), light, selected != null && pos.equals(selected.getLeft()), distanceSq));
            }
        }
        float fovScale = (float) Math.sin(Math.toRadians(minecraft.options.fov().get() / 4d));
        int background = (int) (minecraft.options.getBackgroundOpacity(0.5f) * 255) << 24;
        double nameScale = TravelStaffMain.getClientConfig().nameplateScale;
        if (!Double.isFinite(nameScale)) nameScale = 0.7;
        nameScale = Math.clamp(nameScale, 0.25, 2);
        ((FabricRenderState) context.levelState()).setData(FRAME,
                new Frame(List.copyOf(anchors), fovScale, background, minecraft.font.lineHeight, nameScale));
    }

    public static boolean isWithinOverlayRange(double distanceSq, double maxDistanceSq) {
        return distanceSq <= maxDistanceSq;
    }

    public static boolean canSeeAnchors(LocalPlayer player) {
        return TeleportHandler.canBlockTeleport(player)
                || TeleportHandler.canItemTeleport(player, InteractionHand.MAIN_HAND)
                || TeleportHandler.canItemTeleport(player, InteractionHand.OFF_HAND);
    }

    private static void submitAnchors(LevelRenderContext context) {
        Frame frame = ((FabricRenderState) context.levelState()).getData(FRAME);
        if (frame == null || frame.anchors.isEmpty()) return;
        CameraRenderState camera = context.levelState().cameraRenderState;
        PoseStack poses = context.poseStack();
        SubmitNodeCollector collector = new OutlineBuffer(context.submitNodeCollector());
        for (Anchor anchor : frame.anchors) {
            poses.pushPose();
            poses.translate(anchor.pos.getX() - camera.pos.x, anchor.pos.getY() - camera.pos.y, anchor.pos.getZ() - camera.pos.z);
            anchor.model.submit(poses, collector, anchor.light, OverlayTexture.NO_OVERLAY, 0);
            float lineWidth = anchor.distanceSq > 85 * 85 ? 1 : anchor.distanceSq > 38 * 38 ? 3 : 5;
            collector.submitShapeOutline(poses, Shapes.block(), RenderTypes.lines(), 0xFFFFFFFF, lineWidth, false);
            if (anchor.distanceSq <= 15 * 15 && !anchor.emptyName) {
                double scale = labelScale(anchor, frame);
                poses.pushPose();
                poses.translate(0.5, 1.05 + scale * frame.lineHeight, 0.5);
                poses.rotate(camera.orientation);
                // Negative X would reverse the text's front face.
                poses.scale((float) scale, (float) -scale, (float) scale);
                submitName(poses, collector, frame, anchor);
                poses.popPose();
            }
            poses.popPose();
            if (anchor.distanceSq > 15 * 15 && !anchor.emptyName) {
                // Project distant labels five blocks in front of the camera.
                poses.pushPose();
                double blockScale = labelScale(anchor, frame);
                CircleRotation rotation = rotateCircle(camera.pos.x - (anchor.pos.getX() + 0.5),
                        camera.pos.y - (anchor.pos.getY() + 0.5 + 0.5 * blockScale),
                        camera.pos.z - (anchor.pos.getZ() + 0.5));
                rotation.apply(poses);
                poses.translate(0, 5, 0);
                double scale = frame.nameScale * 0.1 * frame.fovScale * (anchor.active ? 1.3 : 1);
                rotation.reverse(poses);
                poses.translate(0, 0.05 + scale * frame.lineHeight, 0);
                poses.rotate(camera.orientation);
                poses.scale((float) scale, (float) -scale, (float) scale);
                submitName(poses, collector, frame, anchor);
                poses.popPose();
            }
        }
    }

    private static double labelScale(Anchor anchor, Frame frame) {
        return Math.max(0.1, Math.sqrt(0.0035 * Math.sqrt(anchor.distanceSq)))
                * frame.nameScale * frame.fovScale * (anchor.active ? 1.3 : 1);
    }

    private static void submitName(PoseStack poses, SubmitNodeCollector collector, Frame frame, Anchor anchor) {
        int color = anchor.active ? 0xFFFFAA00 : 0xFFFFFFFF;
        float x = -anchor.nameWidth / 2.0f;
        collector.submitText(poses, x, 0, anchor.name, false, Font.DisplayMode.SEE_THROUGH,
                LightCoordsUtil.FULL_BRIGHT, color, frame.background, 0);
        collector.submitText(poses, x, 0, anchor.name, false, Font.DisplayMode.NORMAL,
                LightCoordsUtil.FULL_BRIGHT, color, 0, 0);
    }

    private static CircleRotation rotateCircle(double x, double y, double z) {
        float yr = z == 0 ? Float.NaN : (float) (Math.atan2(x, z) + Math.PI / 2);
        float zr = (float) (Math.atan2(Math.sqrt(x * x + z * z), y) + Math.PI);
        return new CircleRotation(yr, zr);
    }

    private record CircleRotation(float y, float z) {
        private void apply(PoseStack poses) {
            if (!Float.isNaN(this.y)) poses.rotate(Axis.YP, this.y);
            poses.rotate(Axis.ZP, this.z);
        }

        private void reverse(PoseStack poses) {
            poses.rotate(Axis.ZP, -this.z);
            if (!Float.isNaN(this.y)) poses.rotate(Axis.YP, -this.y);
        }
    }

    private record Anchor(BlockPos pos, BlockModelRenderState model, FormattedCharSequence name, int nameWidth,
                          boolean emptyName, int light, boolean active, double distanceSq) {
    }

    private record Frame(List<Anchor> anchors, float fovScale, int background, int lineHeight, double nameScale) {
    }
}
