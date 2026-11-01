// Modified by goocompound.
package io.github.goocompound.travelstaff.block;

import io.github.goocompound.travelstaff.TravelAnchorRegistry;
import io.github.goocompound.travelstaff.render.TravelAnchorRenderer;
import io.github.goocompound.travelstaff.util.TravelAnchorList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class RenderTravelAnchor implements BlockEntityRenderer<TileTravelAnchor, RenderTravelAnchor.State> {
    private static final BlockDisplayContext DISPLAY_CONTEXT = BlockDisplayContext.create();
    private final BlockModelResolver modelResolver;

    public RenderTravelAnchor(BlockEntityRendererProvider.Context context) {
        this.modelResolver = context.blockModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileTravelAnchor anchor, State state, float partialTick, Vec3 cameraPosition,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(anchor, state, partialTick, cameraPosition, crumbling);
        LocalPlayer player = Minecraft.getInstance().player;
        state.visible = player == null || !TravelAnchorRenderer.canSeeAnchors(player)
                || (anchor.getLevel() != null && TravelAnchorList.get(anchor.getLevel()).getAnchor(anchor.getBlockPos()) == null);
        state.model.clear();
        if (state.visible) {
            BlockState mimic = anchor.getMimic();
            this.modelResolver.update(state.model,
                    mimic == null ? TravelAnchorRegistry.TRAVEL_ANCHOR.get().defaultBlockState() : mimic, DISPLAY_CONTEXT);
        }
    }

    @Override
    public void submit(State state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.visible) state.model.submit(poses, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
    }

    public static final class State extends BlockEntityRenderState {
        private final BlockModelRenderState model = new BlockModelRenderState();
        private boolean visible;
    }
}
