// Modified by goocompound.
package io.github.goocompound.travelstaff.mixin;

import io.github.goocompound.travelstaff.render.TravelAnchorRenderer;
import io.github.goocompound.travelstaff.util.EventHandlers;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.joml.Matrix4f;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public class GameRendererMixin {
    @Inject(at = @At("RETURN"), method = "renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;)V")
    private void startUseItem(PoseStack poseStack, float partialTick, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
        TravelAnchorRenderer.renderAnchors(poseStack, partialTick);
    }
}
