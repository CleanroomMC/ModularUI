package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.GuiErrorHandler;
import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.widget.IGuiElement;
import com.cleanroommc.modularui.api.widget.IVanillaSlot;
import com.cleanroommc.modularui.core.mixin.GuiContainerAccessor;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.screen.viewport.LocatedWidget;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

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
import net.minecraft.inventory.ClickType;
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

    private int fps, frameCount = 0;
    private long timer = Minecraft.getSystemTime();
    private boolean doAnimateTransition = true;

    public GuiScreenWrapper(ModularContainer container, ModularScreen screen) {
        super(container);
        this.screen = screen;
        this.screen.construct(this);
    }

    @Override
    public void initGui() {
        GuiErrorHandler.INSTANCE.clear();
        super.initGui();
        if (this.init) {
            this.screen.getWindowManager().resetClosed();
            this.screen.onOpen();
            this.init = false;
        }
        this.screen.onResize(this.width, this.height);
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
        this.frameCount++;
        long time = Minecraft.getSystemTime();
        if (time - this.timer >= 1000) {
            this.fps = this.frameCount;
            this.frameCount = 0;
            this.timer += 1000;
        }

        Stencil.reset();
        Stencil.apply(this.screen.getScreenArea(), null);
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
        IGuiElement hovered = this.screen.getContext().getHovered();
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
                    s = TextFormatting.YELLOW + "0";
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

        Stencil.remove();
    }

    @Override
    public void drawWorldBackground(int tint) {
        if (ModularUI.isBlurLoaded() || this.mc.world == null) {
            super.drawWorldBackground(tint);
            return;
        }
        float alpha = this.doAnimateTransition ? this.screen.getMainPanel().getAlpha() : 1f;
        // vanilla color values as hex
        int color = 0x101010;
        int startAlpha = 0xc0;
        int endAlpha = 0xd0;
        this.drawGradientRect(0, 0, this.width, this.height, Color.withAlpha(color, (int) (startAlpha * alpha)), Color.withAlpha(color, (int) (endAlpha * alpha)));
    }

    private void drawItemStack(ItemStack stack, int x, int y, String altText) {
        GlStateManager.translate(0.0F, 0.0F, 32.0F);
        this.zLevel = 200.0F;
        this.itemRender.zLevel = 200.0F;
        FontRenderer font = stack.getItem().getFontRenderer(stack);
        if (font == null) font = this.fontRenderer;
        GlStateManager.enableDepth();
        this.itemRender.renderItemAndEffectIntoGUI(stack, x, y);
        this.itemRender.renderItemOverlayIntoGUI(font, stack, x, y - (getAccessor().getDraggedStack().isEmpty() ? 0 : 8), altText);
        GlStateManager.disableDepth();
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
        GuiContext context = this.screen.getContext();
        int mouseX = context.getAbsMouseX(), mouseY = context.getAbsMouseY();
        int screenH = this.screen.getScreenArea().height;
        int color = Color.rgb(180, 40, 115);
        int lineY = screenH - 13;
        drawString(this.fontRenderer, "Mouse Pos: " + mouseX + ", " + mouseY, 5, lineY, color);
        lineY -= 11;
        drawString(this.fontRenderer, "FPS: " + this.fps, 5, screenH - 24, color);
        LocatedWidget locatedHovered = this.screen.getWindowManager().getTopWidgetLocated(true);
        if (locatedHovered != null) {
            drawSegmentLine(lineY -= 4, color);
            lineY -= 10;

            IGuiElement hovered = locatedHovered.getElement();
            locatedHovered.applyMatrix(context);
            GlStateManager.pushMatrix();
            context.applyToOpenGl();

            Area area = hovered.getArea();
            IGuiElement parent = hovered.getParent();

            GuiDraw.drawBorder(0, 0, area.width, area.height, color, 1f);
            if (hovered.hasParent()) {
                GuiDraw.drawBorder(-area.rx, -area.ry, parent.getArea().width, parent.getArea().height, Color.withAlpha(color, 0.3f), 1f);
            }
            GlStateManager.popMatrix();
            locatedHovered.unapplyMatrix(context);
            GuiDraw.drawText("Pos: " + area.x + ", " + area.y + "  Rel: " + area.rx + ", " + area.ry, 5, lineY, 1, color, false);
            lineY -= 11;
            GuiDraw.drawText("Size: " + area.width + ", " + area.height, 5, lineY, 1, color, false);
            lineY -= 11;
            GuiDraw.drawText("Class: " + hovered, 5, lineY, 1, color, false);
            if (hovered.hasParent()) {
                drawSegmentLine(lineY -= 4, color);
                lineY -= 10;
                area = parent.getArea();
                GuiDraw.drawText("Parent size: " + area.width + ", " + area.height, 5, lineY, 1, color, false);
                lineY -= 11;
                GuiDraw.drawText("Parent: " + parent, 5, lineY, 1, color, false);
            }
            if (hovered instanceof ItemSlot) {
                drawSegmentLine(lineY -= 4, color);
                lineY -= 10;
                ItemSlot slotWidget = (ItemSlot) hovered;
                ModularSlot slot = slotWidget.getSlot();
                GuiDraw.drawText("Slot Index: " + slot.getSlotIndex(), 5, lineY, 1, color, false);
                lineY -= 11;
                GuiDraw.drawText("Slot Number: " + slot.slotNumber, 5, lineY, 1, color, false);
                lineY -= 11;
                if (slotWidget.isSynced()) {
                    SlotGroup slotGroup = slot.getSlotGroup();
                    boolean allowShiftTransfer = slotGroup != null && slotGroup.allowShiftTransfer();
                    GuiDraw.drawText("Shift-Click Priority: " + (allowShiftTransfer ? slotGroup.getShiftClickPriority() : "DISABLED"), 5, lineY, 1, color, false);
                }
            }
        }
        // dot at mouse pos
        drawRect(mouseX, mouseY, mouseX + 1, mouseY + 1, Color.withAlpha(Color.GREEN.main, 0.8f));
    }

    private void drawSegmentLine(int y, int color) {
        GuiDraw.drawRect(5, y, 140, 1, color);
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
        this.init = true;
        this.doAnimateTransition = true;
    }

    public ModularScreen getScreen() {
        return this.screen;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (this.screen.onMousePressed(mouseButton)) return;
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void clickSlot() {
        try {
            super.mouseClicked(this.screen.getContext().getAbsMouseX(), this.screen.getContext().getAbsMouseY(), this.screen.getContext().getMouseButton());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (this.screen.onMouseRelease(state)) return;
        super.mouseReleased(mouseX, mouseY, state);
    }

    public void releaseSlot() {
        super.mouseReleased(this.screen.getContext().getAbsMouseX(), this.screen.getContext().getAbsMouseY(), this.screen.getContext().getMouseButton());
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (this.screen.onMouseDrag(clickedMouseButton, timeSinceLastClick)) return;
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    public void dragSlot(long timeSinceLastClick) {
        super.mouseClickMove(this.screen.getContext().getAbsMouseX(), this.screen.getContext().getAbsMouseY(), this.screen.getContext().getMouseButton(), timeSinceLastClick);
    }

    /**
     * This replicates vanilla behavior while also injecting custom behavior for consistency
     */
    @Override
    public void handleKeyboardInput() {
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
    protected void keyTyped(char typedChar, int keyCode) {
        // debug mode C + CTRL + SHIFT + ALT
        if (keyCode == 46 && isCtrlKeyDown() && isShiftKeyDown() && isAltKeyDown()) {
            ModularUIConfig.guiDebugMode = !ModularUIConfig.guiDebugMode;
            return;
        }
        if (keyCode == 1 || this.mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode)) {
            this.screen.close();
        }

        this.checkHotbarKeys(keyCode);
        Slot hoveredSlot = getAccessor().getHoveredSlot();
        if (hoveredSlot != null && hoveredSlot.getHasStack()) {
            if (this.mc.gameSettings.keyBindPickBlock.isActiveAndMatches(keyCode)) {
                this.handleMouseClick(hoveredSlot, hoveredSlot.slotNumber, 0, ClickType.CLONE);
            } else if (this.mc.gameSettings.keyBindDrop.isActiveAndMatches(keyCode)) {
                this.handleMouseClick(hoveredSlot, hoveredSlot.slotNumber, isCtrlKeyDown() ? 1 : 0, ClickType.THROW);
            }
        }
    }

    public boolean isDragSplitting() {
        return this.dragSplitting;
    }

    public Set<Slot> getDragSlots() {
        return this.dragSplittingSlots;
    }

    public RenderItem getItemRenderer() {
        return this.itemRender;
    }

    public float getZ() {
        return this.zLevel;
    }

    public void setZ(float z) {
        this.zLevel = z;
    }

    public FontRenderer getFontRenderer() {
        return this.fontRenderer;
    }

    public void setDoAnimateTransition(boolean doAnimateTransition) {
        this.doAnimateTransition = doAnimateTransition;
    }

    public boolean doAnimateTransition() {
        return doAnimateTransition;
    }
}
