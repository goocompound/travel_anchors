// Modified by goocompound.
package io.github.goocompound.travelstaff.render;

import io.github.goocompound.travelstaff.TravelAnchorRegistry;
import io.github.goocompound.travelstaff.platform.Services;
import io.github.goocompound.travelstaff.util.TeleportHandler;
import io.github.goocompound.travelstaff.util.TravelAnchorList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;

import javax.annotation.Nullable;

public class TravelAnchorRenderer {
    public static final RenderType BOLD_LINES = Services.PLATFORM.createLines("bold_lines", 3);
    public static final RenderType VERLY_BOLD_LINES = Services.PLATFORM.createLines("very_bold_lines", 5);

    public static BakedModel MODEL = Minecraft.getInstance().getBlockRenderer().getBlockModel(TravelAnchorRegistry.TRAVEL_ANCHOR.get().defaultBlockState());

    public static void renderAnchors(PoseStack poseStack, float partialTick) {
        ClientLevel level = Minecraft.getInstance().level;
        LocalPlayer player = Minecraft.getInstance().player;
        if (level != null && player != null) {
            if (TeleportHandler.canBlockTeleport(player) || TeleportHandler.canItemTeleport(player, InteractionHand.MAIN_HAND)
                    || TeleportHandler.canItemTeleport(player, InteractionHand.OFF_HAND)) {
                double maxDistanceSq = TeleportHandler.getMaxDistance(player);
                maxDistanceSq = maxDistanceSq * maxDistanceSq;
                TravelAnchorList list = TravelAnchorList.get(Minecraft.getInstance().level);
                double posX = Mth.lerp(partialTick, player.xo, player.getX());
                double posY = Mth.lerp(partialTick, player.yo, player.getY());
                double posZ = Mth.lerp(partialTick, player.zo, player.getZ());
                Vec3 projection = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
                double projPosX = projection.x;
                double projPosY = projection.y;
                double projPosZ = projection.z;
                Pair<BlockPos, String> pair = TeleportHandler.getAnchorToTeleport(level, player, player.blockPosition().below());
                for (BlockPos pos : list.anchors.keySet()) {
                    double distanceSq = pos.distToLowCornerSqr(posX, posY, posZ);
                    if (distanceSq <= maxDistanceSq) {
                        TravelAnchorList.Entry entry = list.getEntry(pos);
                        if (entry != null) {
                            int light;
                            //noinspection deprecation
                            if (level.hasChunkAt(pos)) {
                                light = LightTexture.pack(level.getBrightness(LightLayer.BLOCK, pos), level.getBrightness(LightLayer.SKY, pos));
                            } else {
                                light = LightTexture.pack(15, 15);
                            }
                            boolean active = pair != null && pos.equals(pair.getLeft());
                            boolean directText = distanceSq <= 15 * 15;
                            poseStack.pushPose();
                           loadProjection(poseStack, pos);
                            if (distanceSq > 10 * 10) {
                                double distance = Math.sqrt(distanceSq);
                                poseStack.translate(0.5, 0.5, 0.5);
                                double log = Math.log(distance) / 2.3;
                                float scale = (float) (log * log * log);
                                poseStack.scale(scale, scale, scale);
                                poseStack.translate(-0.5, -0.5, -0.5);
                            }
                            renderAnchor(poseStack, OutlineBuffer.INSTANCE, directText ? entry.name : null, entry.state, light, true, active, distanceSq);
                            poseStack.popPose();
                            if (!directText && !entry.name.trim().isEmpty()) {
                                // Blit the text at the correct location
                                poseStack.pushPose();

                                double blockScale = Math.sqrt(0.0035 * Math.sqrt(distanceSq));
                                if (blockScale < 0.1f) {
                                    blockScale = 0.1f;
                                }
                                blockScale = blockScale * (Math.sin(Math.toRadians(Minecraft.getInstance().options.fov().get() / 4d)));
                                if (active) {
                                    blockScale *= 1.3;
                                }

                               loadProjection(poseStack, projPosX, projPosY, projPosZ);
                                CircleRotation rot = rotateCircle(projPosX - (pos.getX() + 0.5), projPosY - (pos.getY() + 0.5 + (0.5 * blockScale)), projPosZ - (pos.getZ() + 0.5));
                                rot.apply(poseStack);
                                poseStack.translate(0, 5, 0);

                                double doubleScale = 0.1;
                                doubleScale = doubleScale * (Math.sin(Math.toRadians(Minecraft.getInstance().options.fov().get() / 4d)));
                                if (active) {
                                    doubleScale *= 1.3;
                                }
                                float scale = (float) doubleScale;

                                rot.reverse(poseStack);
                                poseStack.translate(0, 0.05 + (doubleScale * Minecraft.getInstance().font.lineHeight), 0);
                                poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
                                poseStack.scale(-scale, -scale, scale);

                                int color = 0xFFFFFF;
                                if (active) {
                                    color = ChatFormatting.GOLD.getColor() == null ? 0xFFFFFF : ChatFormatting.GOLD.getColor();
                                }

                                Matrix4f matrix4f = poseStack.last().pose();
                                Component tc = Component.literal(entry.name.trim());

                                float textOpacitySetting = Minecraft.getInstance().options.getBackgroundOpacity(0.5f);
                                int alpha = (int) (textOpacitySetting * 255) << 24;
                                float halfWidth = (float) (-Minecraft.getInstance().font.width(tc) / 2);

                                Minecraft.getInstance().font.drawInBatch(tc, halfWidth, 0, color, false, matrix4f, OutlineBuffer.INSTANCE, true, alpha, LightTexture.pack(15, 15));
                                Minecraft.getInstance().font.drawInBatch(tc, halfWidth, 0, color, false, matrix4f, OutlineBuffer.INSTANCE, false, 0, LightTexture.pack(15, 15));

                                poseStack.popPose();
                            }
                        }
                    }
                }
                Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
            }
        }
    }

    public static void renderAnchor(PoseStack poseStack, MultiBufferSource buffer, @Nullable String name, BlockState state, int light, boolean glow, boolean active, double distanceSq) {
        if (state == null || state.getBlock() == TravelAnchorRegistry.TRAVEL_ANCHOR.get()) {
            VertexConsumer vertex = buffer.getBuffer(RenderType.solid());
            //noinspection deprecation
            Minecraft.getInstance().getBlockRenderer().getModelRenderer()
                    .renderModel(poseStack.last(), vertex, state,
                            MODEL, 1, 1, 1, light, OverlayTexture.NO_OVERLAY);
        } else {
            Minecraft.getInstance().getBlockRenderer()
                    .renderSingleBlock(state, poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
        }
        if (glow) {
            RenderType type;
            if (distanceSq > 85 * 85) {
                type = RenderType.lines();
            } else if (distanceSq > 38 * 38) {
                type = BOLD_LINES;
            } else {
                type = VERLY_BOLD_LINES;
            }
            VertexConsumer vertex = buffer.getBuffer(type);
            LevelRenderer.renderLineBox(poseStack, vertex, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1);
        }
        if (name != null && !name.trim().isEmpty()) {
            double doubleScale = Math.sqrt(0.0035 * Math.sqrt(distanceSq));
            if (doubleScale < 0.1f) {
                doubleScale = 0.1f;
            }
            doubleScale = doubleScale * (Math.sin(Math.toRadians(Minecraft.getInstance().options.fov().get() / 4d)));
            if (active) {
                doubleScale *= 1.3;
            }
            float scale = (float) doubleScale;

            poseStack.pushPose();
            poseStack.translate(0.5, 1.05 + (doubleScale * Minecraft.getInstance().font.lineHeight), 0.5);
            poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
            poseStack.scale(-scale, -scale, scale);

            int color = 0xFFFFFF;
            if (active) {
                color = ChatFormatting.GOLD.getColor() == null ? 0xFFFFFF : ChatFormatting.GOLD.getColor();
            }

            Matrix4f matrix4f = poseStack.last().pose();
            Component tc = Component.literal(name.trim());

            float textOpacitySetting = Minecraft.getInstance().options.getBackgroundOpacity(0.5f);
            int alpha = (int) (textOpacitySetting * 255) << 24;
            float halfWidth = (float) (-Minecraft.getInstance().font.width(tc) / 2);

            Minecraft.getInstance().font.drawInBatch(tc, halfWidth, 0, color, false, matrix4f, buffer, true, alpha, LightTexture.pack(15, 15));
            Minecraft.getInstance().font.drawInBatch(tc, halfWidth, 0, color, false, matrix4f, buffer, false, 0, LightTexture.pack(15, 15));
            poseStack.popPose();
        }
    }

    // Y ist the direction pointing directly out of the circle.
    private static CircleRotation rotateCircle(double x, double y, double z) {
        float yr = Float.NaN;
        if (z != 0) {
            yr = (float) (Math.atan2(x, z) + (Math.PI / 2));
        }
        double hor = Math.sqrt((x * x) + (z * z));
        float zr = (float) (Math.atan2(hor, y) + Math.PI);
        return new CircleRotation(yr, zr);
    }

    private record CircleRotation(float y, float z) {

        public void apply(PoseStack poseStack) {
            if (!Float.isNaN(this.y)) {
                poseStack.mulPose(Axis.YP.rotation(this.y));
            }
            poseStack.mulPose(Axis.ZP.rotation(this.z));
        }

        public void reverse(PoseStack poseStack) {
            poseStack.mulPose(Axis.ZP.rotation(-this.z));
            if (!Float.isNaN(this.y)) {
                poseStack.mulPose(Axis.YP.rotation(-this.y));
            }
        }
    }


    /**
     * This is meant to be called in { RenderLevelLastEvent}. This will move the pose stack to the
     * given position in the world. Do not always use this with {@code (0, 0, 0)} and translate to the
     * position you need afterwards as it will be buggy millions of blocks away because of rounding errors.
     */
    public static void loadProjection(PoseStack poseStack, BlockPos pos) {
        loadProjection(poseStack, pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * This is meant to be called in { RenderLevelLastEvent}. This will move the pose stack to the
     * given position in the world. Do not always use this with {@code (0, 0, 0)} and translate to the
     * position you need afterwards as it will be buggy millions of blocks away because of rounding errors.
     */
    public static void loadProjection(PoseStack poseStack, Vec3 pos) {
        loadProjection(poseStack, pos.x, pos.y, pos.z);
    }

    /**
     * This is meant to be called in { RenderLevelLastEvent}. This will move the pose stack to the
     * given position in the world. Do not always use this with {@code (0, 0, 0)} and translate to the
     * position you need afterwards as it will be buggy millions of blocks away because of rounding errors.
     */
    public static void loadProjection(PoseStack poseStack, double x, double y, double z) {
        Vec3 projection = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        poseStack.translate(x - projection.x, y - projection.y, z - projection.z);
    }
}
