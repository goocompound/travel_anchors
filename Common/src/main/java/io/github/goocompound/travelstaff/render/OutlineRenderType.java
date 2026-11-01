// Modified by goocompound.
package io.github.goocompound.travelstaff.render;

import io.github.goocompound.travelstaff.TravelStaffMain;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.minecraft.client.renderer.oit.OitPipelineSet;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import java.util.IdentityHashMap;
import java.util.Map;

/** Keeps the model's materials while drawing the travel overlay through terrain. */
public final class OutlineRenderType extends RenderType {
    private static final Map<RenderType, OutlineRenderType> TYPES = new IdentityHashMap<>();
    private static final Map<OitPipelineSet, OitPipelineSet> OIT_TYPES = new IdentityHashMap<>();
    private final RenderType parent;
    private final RenderPipeline pipeline;

    private OutlineRenderType(RenderType parent) {
        super("travelstaff_overlay_" + parent, parent.state);
        this.parent = parent;
        this.pipeline = new OverlayPipeline(parent.pipeline());
    }

    @Override
    public RenderPipeline pipeline() {
        return this.pipeline;
    }

    @Override
    public PreparedRenderType prepare() {
        PreparedRenderType prepared = this.parent.prepare();
        OitPipelineSet oit = prepared.oitPipelineSet() == null ? null : OIT_TYPES.computeIfAbsent(
                prepared.oitPipelineSet(), source -> new OitPipelineSet(
                        new OverlayPipeline(source.depthBoundsPipeline()),
                        new OverlayPipeline(source.transmittancePipeline()),
                        new OverlayPipeline(source.accumulatePipeline())));
        return new PreparedRenderType(prepared.name(), this.pipeline, oit,
                prepared.dynamicTransforms(), prepared.scissorState(), prepared.textures());
    }

    public static RenderType get(RenderType parent) {
        return parent instanceof OutlineRenderType ? parent : TYPES.computeIfAbsent(parent, OutlineRenderType::new);
    }

    public static void clear() {
        TYPES.clear();
        OIT_TYPES.clear();
    }

    private static final class OverlayPipeline extends RenderPipeline {
        private OverlayPipeline(RenderPipeline parent) {
            super(Identifier.fromNamespaceAndPath(TravelStaffMain.MOD_ID,
                            "overlay/" + parent.getLocation().getNamespace() + "/" + parent.getLocation().getPath()),
                    parent.getShaders(), parent.getShaderDefines(), parent.getBindGroupLayouts(),
                    parent.getColorTargetStates().toArray(ColorTargetState[]::new),
                    new DepthStencilState(CompareOp.ALWAYS_PASS, false), parent.getPolygonMode(), parent.isCull(),
                    parent.getVertexFormatBindings().toArray(VertexFormat[]::new), parent.getPrimitiveTopology(),
                    parent.pushConstantSize(), parent.getSortKey());
        }
    }
}
