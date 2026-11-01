// Modified by goocompound.
package io.github.goocompound.travelstaff.util;

import io.github.goocompound.travelstaff.block.ScreenTravelAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public class GuiHelper {
    public static void openAnchorGui(String name, BlockPos pos) {
        Minecraft.getInstance().gui.setScreen(new ScreenTravelAnchor(name, pos));
    }
}
