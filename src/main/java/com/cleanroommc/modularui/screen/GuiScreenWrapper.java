package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.GuiErrorHandler;
import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.widget.IGuiElement;
import com.cleanroommc.modularui.api.widget.IVanillaSlot;
import com.cleanroommc.modularui.core.mixin.GuiContainerAccessor;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.sizer.Area;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class GuiScreenWrapper extends GuiContainer {

    private final ModularScreen screen;
    private boolean init = true;
    private char lastChar;

    public GuiScreenWrapper(ModularContainer container, ModularScreen screen) {
        super(container);
        this.screen = screen;
        this.screen.construct(this, container.isClientOnly() ? null : container.getSyncHandler());
    }

    @Override
    public void initGui() {
        GuiErrorHandler.INSTANCE.clear();
        super.initGui();
        if (this.init) {
            this.screen.onOpen();
            this.init = false;
        }
        this.screen.onResize(width, height);
    }

    public void updateArea(Area mainViewport) {
        this.guiLeft = mainViewport.x;
        this.guiTop = mainViewport.y;
        this.xSize = mainViewport.width;
        this.ySize = mainViewport.height;
    }

    public GuiContainerAccessor getAccessor() {
        return (GuiContainerAccessor) this;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int i = this.guiLeft;
        int j = this.guiTop;

        this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        this.screen.drawScreen(mouseX, mouseY, partialTicks);

        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        // mainly for invtweaks compat
        drawVanillaElements(mouseX, mouseY, partialTicks);
        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        getAccessor().setHoveredSlot(null);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GlStateManager.enableRescaleNormal();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.disableStandardItemLighting();
        this.drawGuiContainerForegroundLayer(mouseX, mouseY);
        this.screen.drawForeground(partialTicks);
        RenderHelper.enableGUIStandardItemLighting();

        getAccessor().setHoveredSlot(null);
        IGuiElement hovered = this.screen.context.getHovered();
        if (hovered instanceof IVanillaSlot) {
            getAccessor().setHoveredSlot(((IVanillaSlot) hovered).getVanillaSlot());
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.translate(i, j, 0);
        MinecraftForge.EVENT_BUS.post(new GuiContainerEvent.DrawForeground(this, mouseX, mouseY));
        GlStateManager.popMatrix();

        InventoryPlayer inventoryplayer = this.mc.player.inventory;
        ItemStack itemstack = getAccessor().getDraggedStack().isEmpty() ? inventoryplayer.getItemStack() : getAccessor().getDraggedStack();
        GlStateManager.translate((float) i, (float) j, 0.0F);
        if (!itemstack.isEmpty()) {
            int k2 = getAccessor().getDraggedStack().isEmpty() ? 8 : 16;
            String s = null;

            if (!getAccessor().getDraggedStack().isEmpty() && getAccessor().getIsRightMouseClick()) {
                itemstack = itemstack.copy();
                itemstack.setCount(MathHelper.ceil((float) itemstack.getCount() / 2.0F));
            } else if (this.dragSplitting && this.dragSplittingSlots.size() > 1) {
                itemstack = itemstack.copy();
                itemstack.setCount(getAccessor().getDragSplittingRemnant());

                if (itemstack.isEmpty()) {
                    s = "" + TextFormatting.YELLOW + "0";
                }
            }

            this.drawItemStack(itemstack, mouseX - i - 8, mouseY - j - k2, s);
        }

        if (!getAccessor().getReturningStack().isEmpty()) {
            float f = (float) (Minecraft.getSystemTime() - getAccessor().getReturningStackTime()) / 100.0F;

            if (f >= 1.0F) {
                f = 1.0F;
                getAccessor().setReturningStack(ItemStack.EMPTY);
            }

            int l2 = getAccessor().getReturningStackDestSlot().xPos - getAccessor().getTouchUpX();
            int i3 = getAccessor().getReturningStackDestSlot().yPos - getAccessor().getTouchUpY();
            int l1 = getAccessor().getTouchUpX() + (int) ((float) l2 * f);
            int i2 = getAccessor().getTouchUpY() + (int) ((float) i3 * f);
            this.drawItemStack(getAccessor().getReturningStack(), l1, i2, null);
        }

        GlStateManager.popMatrix();

        if (ModularUIConfig.guiDebugMode) {
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            drawDebugScreen();
            GlStateManager.color(1f, 1f, 1f, 1f);
        }
        GuiErrorHandler.INSTANCE.drawErrors(0, 0);

        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableStandardItemLighting();
    }

    private void drawItemStack(ItemStack stack, int x, int y, String altText) {
        GlStateManager.translate(0.0F, 0.0F, 32.0F);
        this.zLevel = 200.0F;
        this.itemRender.zLevel = 200.0F;
        FontRenderer font = stack.getItem().getFontRenderer(stack);
        if (font == null) font = fontRenderer;
        this.itemRender.renderItemAndEffectIntoGUI(stack, x, y);
        this.itemRender.renderItemOverlayIntoGUI(font, stack, x, y - (getAccessor().getDraggedStack().isEmpty() ? 0 : 8), altText);
        this.zLevel = 0.0F;
        this.itemRender.zLevel = 0.0F;
    }

    protected void drawVanillaElements(int mouseX, int mouseY, float partialTicks) {
        for (GuiButton guiButton : this.buttonList) {
            guiButton.drawButton(this.mc, mouseX, mouseY, partialTicks);
        }
        for (GuiLabel guiLabel : this.labelList) {
            guiLabel.drawLabel(this.mc, mouseX, mouseY);
        }
    }

    public void drawDebugScreen() {
        GuiContext context = screen.context;
        int mouseX = context.getAbsMouseX(), mouseY = context.getAbsMouseY();
        int screenW = this.screen.getViewport().width, screenH = this.screen.getViewport().height;
        int color = Color.rgb(180, 40, 115);
        int lineY = screenH - 13;
        drawString(fontRenderer, "Mouse Pos: " + mouseX + ", " + mouseY, 5, lineY, color);
        lineY -= 11;
        //drawString(fontRenderer, "FPS: " + fps, 5, screenSize.height - 24, color);
        lineY -= 11;
        IGuiElement hovered = this.screen.context.getHovered();
        if (hovered != null) {
            Area area = hovered.getArea();
            IGuiElement parent = hovered.getParent();

            GuiDraw.drawBorder(area.x, area.y, area.width, area.height, color, 1f);
            if (parent != null) {
                GuiDraw.drawBorder(parent.getArea().x, parent.getArea().y, parent.getArea().width, parent.getArea().height, Color.withAlpha(color, 0.3f), 1f);
            }
            GuiDraw.drawText("Pos: " + area.x + ", " + area.y, 5, lineY, 1, color, false);
            lineY -= 11;
            GuiDraw.drawText("Size: " + area.width + ", " + area.height, 5, lineY, 1, color, false);
            lineY -= 11;
            if (parent != null) {
                GuiDraw.drawText("Parent: " + parent, 5, lineY, 1, color, false);
                lineY -= 11;
            }
            GuiDraw.drawText("Class: " + hovered, 5, lineY, 1, color, false);
        }
        color = Color.withAlpha(color, 25);
        for (int i = 5; i < screenW; i += 5) {
            drawVerticalLine(i, 0, screenH, color);
        }

        for (int i = 5; i < screenH; i += 5) {
            drawHorizontalLine(0, screenW, i, color);
        }
        drawRect(mouseX, mouseY, mouseX + 1, mouseY + 1, Color.withAlpha(Color.GREEN.normal, 0.8f));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.screen.onUpdate();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        this.screen.onClose();
    }

    public ModularScreen getScreen() {
        return screen;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (this.screen.onMousePressed(mouseButton)) return;
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (this.screen.onMouseRelease(state)) return;
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (this.screen.onMouseDrag(clickedMouseButton, timeSinceLastClick)) return;
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    /**
     * This replicates vanilla behavior while also injecting custom behavior for consistency
     */
    @Override
    public void handleKeyboardInput() throws IOException {
        char c0 = Keyboard.getEventCharacter();
        int key = Keyboard.getEventKey();
        boolean state = Keyboard.getEventKeyState();

        if (state) {
            this.lastChar = c0;
            if (this.screen.onKeyPressed(c0, key)) return;
            keyTyped(c0, key);
        } else {
            // when the key is released, the event char is empty
            if (this.screen.onKeyRelease(this.lastChar, key)) return;
            if (key == 0 && c0 >= ' ') {
                keyTyped(c0, key);
            }
        }

        this.mc.dispatchKeypresses();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        // debug mode C + CTRL + SHIFT + ALT
        if (keyCode == 46 && isCtrlKeyDown() && isShiftKeyDown() && isAltKeyDown()) {
            ModularUIConfig.guiDebugMode = !ModularUIConfig.guiDebugMode;
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    public boolean isDragSplitting() {
        return dragSplitting;
    }

    public Set<Slot> getDragSlots() {
        return dragSplittingSlots;
    }

    public RenderItem getItemRenderer() {
        return itemRender;
    }

    public float getZ() {
        return zLevel;
    }

    public void setZ(float z) {
        this.zLevel = z;
    }

    public FontRenderer getFontRenderer() {
        return fontRenderer;
    }
}
