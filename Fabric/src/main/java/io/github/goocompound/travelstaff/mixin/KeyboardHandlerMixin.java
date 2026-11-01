// Modified by goocompound.
package io.github.goocompound.travelstaff.mixin;

import io.github.goocompound.travelstaff.util.EventHandlers;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(at = @At("HEAD"), method = "keyPress(JIIII)V")
    private void keyPress(long p_90894_, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen == null) {
            if (action == GLFW.GLFW_PRESS && Minecraft.getInstance().options.keyJump.matches(key, scanCode)){
                EventHandlers.onJump(Minecraft.getInstance().player);
            }
            if (action == GLFW.GLFW_PRESS && Minecraft.getInstance().options.keyShift.matches(key, scanCode)){
                EventHandlers.onSneak(Minecraft.getInstance().player);
            }
        }
    }
}