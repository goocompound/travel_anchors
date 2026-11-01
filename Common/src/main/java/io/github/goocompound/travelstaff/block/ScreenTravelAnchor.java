// Modified by goocompound.
package io.github.goocompound.travelstaff.block;

import io.github.goocompound.travelstaff.platform.Services;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class ScreenTravelAnchor extends Screen {
    private final String name;
    private final BlockPos pos;
    private EditBox textFieldWidget;

    public ScreenTravelAnchor(String name, BlockPos pos) {
        super(Component.literal(name));
        this.name = name;
        this.pos = pos;
    }

    @Override
    protected void init() {
        super.init();
        this.textFieldWidget = this.addRenderableWidget(new EditBox(this.font,
                this.width / 2 - 50, this.height / 2 - 63, 100, 15,
                Component.translatable("screen.travelstaff.search")));
        this.textFieldWidget.setMaxLength(32767);
        this.textFieldWidget.setValue(this.name);
        this.textFieldWidget.setHighlightPos(0);
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.textFieldWidget);
    }

    @Override
    public void removed() {
        super.removed();
        if (Minecraft.getInstance().level != null) {
            Services.NETWORK.sendNameChangeToServer(this.textFieldWidget.getValue().trim(), this.pos);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_RETURN) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }
}
