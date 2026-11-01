// Modified by goocompound.
package io.github.goocompound.travelstaff.platform.services;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.state.BlockState;

public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    void renderAnchor(PoseStack matrixStack, MultiBufferSource buffer, BlockState mimic, int combinedLight, boolean glow, boolean active, int distanceSq);

    // does things that need the access transformers. because they have different systems that need thier magic gradle so i cant make the common code compile even though i know the things would be accessable under both modloaders
    RenderType createLines(String name, int strength);
    boolean accessSortOnUpload(RenderType parent);
}
