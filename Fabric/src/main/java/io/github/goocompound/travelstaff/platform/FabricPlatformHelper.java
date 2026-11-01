// Modified by goocompound.
package io.github.goocompound.travelstaff.platform;

import io.github.goocompound.travelstaff.TravelStaffMain;
import io.github.goocompound.travelstaff.platform.services.IPlatformHelper;
import io.github.goocompound.travelstaff.render.TravelAnchorRenderer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.OptionalDouble;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void renderAnchor(PoseStack matrixStack, MultiBufferSource buffer, BlockState mimic, int combinedLight, boolean glow, boolean active, int distanceSq) {
        TravelAnchorRenderer.renderAnchor(matrixStack, buffer, null, mimic, combinedLight, glow, active, distanceSq);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public RenderType createLines(String name, int strength) {
        return RenderType.create(TravelStaffMain.MOD_ID + "_" + name,
                DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 256, false, false,
                RenderType.CompositeState.builder().setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                        .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(strength)))
                        .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                        .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                        .setCullState(RenderStateShard.NO_CULL)
                        .createCompositeState(false)
        );
    }

    @Environment(EnvType.CLIENT)
    @Override
    public boolean accessSortOnUpload(RenderType parent) {
        return parent.sortOnUpload;
    }
}
