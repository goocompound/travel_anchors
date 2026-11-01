// Modified by goocompound.
package io.github.goocompound.travelstaff.mixin;

import io.github.goocompound.travelstaff.platform.Services;
import io.github.goocompound.travelstaff.util.EventHandlers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow @Nullable public HitResult hitResult;

    @Shadow @Nullable public LocalPlayer player;

    @Inject(at = @At("HEAD"), method = "startUseItem()V")
    private void startUseItem(CallbackInfo ci) {
        if (player == null) return;

        for(InteractionHand interactionhand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(interactionhand);
            if (stack.isEmpty() && (hitResult == null || hitResult.getType() == HitResult.Type.MISS)){
                EventHandlers.onEmptyClick(player, interactionhand, stack);
            }
        }
    }
}