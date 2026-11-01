// Modified by goocompound.
package io.github.goocompound.travelstaff.mixin;

import io.github.goocompound.travelstaff.util.EventHandlers;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(at = @At("HEAD"), method = "keyPress(JILnet/minecraft/client/input/KeyEvent;)V")
    private void keyPress(long window, int action, KeyEvent event, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gui.screen() == null && action == InputConstants.PRESS) {
            if (minecraft.options.keyJump.matches(event)) {
                EventHandlers.onJump(minecraft.player);
            }
            if (minecraft.options.keyShift.matches(event)) {
                EventHandlers.onSneak(minecraft.player);
            }
        }
    }
}
