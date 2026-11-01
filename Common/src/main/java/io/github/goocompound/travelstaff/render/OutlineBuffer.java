// Modified by goocompound.
package io.github.goocompound.travelstaff.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MeshView;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricOrderedSubmitNodeCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.gizmos.DrawableGizmoPrimitives;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;

import java.util.List;
import java.util.function.Function;

/** Applies the travel overlay pipeline to model submissions, including special block models. */
public final class OutlineBuffer implements SubmitNodeCollector, FabricOrderedSubmitNodeCollector {
    private final SubmitNodeCollector root;
    private final OrderedSubmitNodeCollector delegate;

    public OutlineBuffer(SubmitNodeCollector root) {
        this(root, root);
    }

    private OutlineBuffer(SubmitNodeCollector root, OrderedSubmitNodeCollector delegate) {
        this.root = root;
        this.delegate = delegate;
    }

    @Override
    public OrderedSubmitNodeCollector order(int order) {
        return new OutlineBuffer(this.root, this.root.order(order));
    }

    @Override
    public <S> void submitModel(Model<? super S> model, S state, PoseStack pose, RenderType type,
                                int light, int overlay, int tint, UvMapping uv, int outlineColor) {
        this.delegate.submitModel(model, state, pose, OutlineRenderType.get(type), light, overlay, tint, uv, outlineColor);
    }

    @Override
    public void submitBlockModel(PoseStack pose, RenderType type, List<BlockStateModelPart> parts,
                                 int[] tints, int light, int overlay, int outlineColor) {
        this.delegate.submitBlockModel(pose, OutlineRenderType.get(type), parts, tints, light, overlay, outlineColor);
    }

    @Override
    public void submitBlockModel(PoseStack pose, Function<ChunkSectionLayer, RenderType> types, boolean translucent,
                                 List<BlockStateModelPart> parts, Mesh mesh, int[] tints, int light, int overlay, int outlineColor) {
        // Fabric's default overload drops the mesh. Forward it intact, including per-layer materials.
        ((FabricOrderedSubmitNodeCollector) this.delegate).submitBlockModel(pose,
                types.andThen(type -> type == null ? null : OutlineRenderType.get(type)),
                translucent, parts, mesh, tints, light, overlay, outlineColor);
    }

    @Override
    public void submitShapeOutline(PoseStack pose, VoxelShape shape, RenderType type, int color, float width, boolean afterTerrain) {
        this.delegate.submitShapeOutline(pose, shape, OutlineRenderType.get(type), color, width, afterTerrain);
    }

    @Override
    public void submitCustomGeometry(PoseStack pose, RenderType type, CustomGeometryRenderer renderer) {
        this.delegate.submitCustomGeometry(pose, OutlineRenderType.get(type), renderer);
    }

    @Override
    public <S> void submitCrumblingOverlay(Model<? super S> model, S state, PoseStack pose, RenderType type,
                                          int light, int overlay, int tint, ModelFeatureRenderer.CrumblingOverlay crumbling) {
        this.delegate.submitCrumblingOverlay(model, state, pose, OutlineRenderType.get(type), light, overlay, tint, crumbling);
    }

    @Override
    public void submitText(PoseStack pose, float x, float y, FormattedCharSequence text, boolean shadow,
                           Font.DisplayMode mode, int light, int color, int background, int outlineColor) {
        this.delegate.submitText(pose, x, y, text, shadow, mode, light, color, background, outlineColor);
    }

    @Override
    public void submitTextBackground(PoseStack pose, float x1, float y1, float x2, float y2, int color, Font.DisplayMode mode, int light) {
        this.delegate.submitTextBackground(pose, x1, y1, x2, y2, color, mode, light);
    }

    @Override
    public void submitNameTag(PoseStack pose, Vec3 nameTagAttachment, int offset, Component text, boolean seeThrough, int light, CameraRenderState camera) {
        this.delegate.submitNameTag(pose, nameTagAttachment, offset, text, seeThrough, light, camera);
    }

    @Override
    public void submitShadow(PoseStack pose, float radius, List<EntityRenderState.ShadowPiece> pieces) {
        this.delegate.submitShadow(pose, radius, pieces);
    }

    @Override
    public void submitFlame(PoseStack pose, EntityRenderState state, Quaternionf rotation) {
        this.delegate.submitFlame(pose, state, rotation);
    }

    @Override
    public void submitLeash(PoseStack pose, EntityRenderState.LeashState state) {
        this.delegate.submitLeash(pose, state);
    }

    @Override
    public void submitMovingBlock(PoseStack pose, MovingBlockRenderState state, int outlineColor) {
        this.delegate.submitMovingBlock(pose, state, outlineColor);
    }

    @Override
    public void submitBreakingBlockModel(PoseStack pose, List<BlockStateModelPart> parts, int stage, boolean isBlockTranslucent) {
        this.delegate.submitBreakingBlockModel(pose, parts, stage, isBlockTranslucent);
    }

    @Override
    public void submitBreakingBlockModel(PoseStack pose, List<BlockStateModelPart> parts, Mesh mesh, int stage, boolean isBlockTranslucent) {
        ((FabricOrderedSubmitNodeCollector) this.delegate).submitBreakingBlockModel(pose, parts, mesh, stage, isBlockTranslucent);
    }

    @Override
    public void submitItem(PoseStack pose, ItemDisplayContext context, int light, int overlay, int outlineColor,
                           int[] tints, ItemQuads quads, ItemStackRenderState.FoilType foil) {
        this.delegate.submitItem(pose, context, light, overlay, outlineColor, tints, quads, foil);
    }

    @Override
    public void submitItem(PoseStack pose, ItemDisplayContext context, int light, int overlay, int outlineColor,
                           int[] tints, ItemQuads quads, MeshView mesh, ItemStackRenderState.FoilType foil) {
        ((FabricOrderedSubmitNodeCollector) this.delegate).submitItem(pose, context, light, overlay, outlineColor,
                tints, quads, mesh, foil);
    }

    @Override
    public void submitQuadParticleGroup(QuadParticleRenderState state) {
        this.delegate.submitQuadParticleGroup(state);
    }

    @Override
    public void submitGizmoPrimitives(DrawableGizmoPrimitives.Group group, CameraRenderState camera, boolean alwaysOnTop) {
        this.delegate.submitGizmoPrimitives(group, camera, alwaysOnTop);
    }
}
