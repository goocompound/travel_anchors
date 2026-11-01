// Modified by goocompound.
package io.github.goocompound.travelstaff.block;

import io.github.goocompound.travelstaff.platform.Services;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
    public void init() {
        super.init();
        this.textFieldWidget = new EditBox(this.font, this.width / 2 - 50, this.height / 2 - 63, 100, 15, Component.translatable("screen.travelstaff.search"));
        this.textFieldWidget.setMaxLength(32767);
        this.textFieldWidget.changeFocus(true);
        this.textFieldWidget.setValue(name);
        textFieldWidget.setHighlightPos(0);
    }

    // todo: bring back background and have a help menu that sends you to wiki page
    /*
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/travel_anchor.png");
    private int imageWidth = 180;
    private int imageHeight = 50;

    protected void renderBg(@Nonnull PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        this.blit(poseStack, (int) (this.width - this.imageWidth) / 2, (int) (this.height - this.imageHeight) / 2, 0, 0, this.imageWidth, this.imageHeight);
    }


    protected void renderLabels(@Nonnull PoseStack poseStack, int mouseX, int mouseY) {
        String title = this.title.getString();
        this.font.draw(poseStack, title, (float) (this.imageWidth / 2 - this.font.width(title) / 2), 6.0F, 0x404040);
        boolean empty = this.textFieldWidget.getValue().trim().isEmpty();
        this.font.draw(poseStack, empty ? I18n.get("screen.travelstaff.nameless") : this.name, 8, (float) (this.imageHeight - 126), empty ? 0xB31616 : 0x404040);
    }

     */

    @Override
    public void render(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        // this.renderTooltip(matrixStack, mouseX, mouseY);
        this.textFieldWidget.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    @Nullable
    public GuiEventListener getFocused() {
        return this.textFieldWidget;
    }

    @Override
    public void removed() {
        super.removed();
        if (Minecraft.getInstance().level != null) {
            Services.NETWORK.sendNameChangeToServer(this.textFieldWidget.getValue().trim(), this.pos);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key mapping = InputConstants.getKey(keyCode, scanCode);
        if (keyCode == GLFW.GLFW_KEY_ENTER){
            this.onClose();
            return true;
        }
        //noinspection ConstantConditions
        /*
        if (keyCode != 256 && (this.minecraft.options.keyInventory.isActiveAndMatches(mapping)
                || this.minecraft.options.keyDrop.isActiveAndMatches(mapping))) {
            return true;
        }

         */
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        this.textFieldWidget.tick();
    }
}
