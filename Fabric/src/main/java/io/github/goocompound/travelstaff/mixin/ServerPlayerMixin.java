// Modified by goocompound.
package io.github.goocompound.travelstaff.mixin;

import io.github.goocompound.travelstaff.platform.Services;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Inject(at = @At("RETURN"), method = "changeDimension(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/entity/Entity;")
    private void init(ServerLevel serverLevel, CallbackInfoReturnable<Entity> cir) {
        if (cir.getReturnValue() != null){
            Services.NETWORK.sendAnchorListToClient((ServerPlayer) cir.getReturnValue());
        }
    }
}