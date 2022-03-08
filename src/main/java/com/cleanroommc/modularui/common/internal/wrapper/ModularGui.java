package com.cleanroommc.modularui.common.internal.wrapper;

import com.cleanroommc.modularui.api.IVanillaSlot;
import com.cleanroommc.modularui.api.IWidgetParent;
import com.cleanroommc.modularui.api.Interactable;
import com.cleanroommc.modularui.api.TooltipContainer;
import com.cleanroommc.modularui.api.drawable.TextSpan;
import com.cleanroommc.modularui.api.math.Color;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.common.internal.ModularUIContext;
import com.cleanroommc.modularui.common.internal.mixin.GuiContainerMixin;
import com.cleanroommc.modularui.common.widget.SlotWidget;
import com.cleanroommc.modularui.common.widget.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@SideOnly(Side.CLIENT)
public class ModularGui extends GuiContainer {

    private final ModularUIContext context;
    private Pos2d mousePos = Pos2d.ZERO;

    private long lastClick = -1;
    private long lastFocusedClick = -1;
    private Widget focused;
    private Widget hovered = null;
    private int timeHovered = 0;
    public boolean debugMode = true;
    private int drawCalls = 0;
    private long drawTime = 0;
    private int fps = 0;

    public ModularGui(ModularUIContainer container) {
        super(container);
        this.context = container.getContext();
        this.context.initializeClient(this);
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        this.context.resize(new Size(scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight()));
        this.context.buildWindowOnStart();
        this.context.getCurrentWindow().onOpen();
    }

    public ModularUIContext getContext() {
        return context;
    }

    public Pos2d getMousePos() {
        return mousePos;
    }

    public boolean isFocused(Widget widget) {
        return focused != null && focused == widget;
    }

    public void removeFocus(Widget widget) {
        if (isFocused(widget)) {
            focused.onRemoveFocus();
            focused = null;
        }
    }

    public boolean isHovering(Widget widget) {
        return hovered != null && hovered == widget;
    }

    private void setHovered(Widget widget) {
        if (hovered != widget) {
            hovered = widget;
            timeHovered = 0;
        }
    }

    @Override
    public void onResize(Minecraft mc, int w, int h) {
        super.onResize(mc, w, h);
        context.resize(new Size(w, h));
    }

    public GuiContainerMixin getAccessor() {
        return (GuiContainerMixin) this;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        mousePos = new Pos2d(mouseX, mouseY);

        int i = this.guiLeft;
        int j = this.guiTop;
        this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        getAccessor().setHoveredSlot(null);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.disableStandardItemLighting();
        this.drawGuiContainerForegroundLayer(mouseX, mouseY);
        RenderHelper.enableGUIStandardItemLighting();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiContainerEvent.DrawForeground(this, mouseX, mouseY));
        InventoryPlayer inventoryplayer = this.mc.player.inventory;
        ItemStack itemstack = getAccessor().getDraggedStack().isEmpty() ? inventoryplayer.getItemStack() : getAccessor().getDraggedStack();
        GlStateManager.translate((float) i, (float) j, 0.0F);
        if (!itemstack.isEmpty()) {
            int j2 = 8;
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

        if (debugMode) {
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            drawDebugScreen();
            GlStateManager.color(1f, 1f, 1f, 1f);
        }
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

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        if (debugMode) {
            long time = Minecraft.getSystemTime() / 1000;
            if (drawTime != time) {
                fps = drawCalls;
                drawCalls = 0;
                drawTime = time;
            }
            drawCalls++;
        }
        context.getCurrentWindow().frameUpdate(partialTicks);
        drawDefaultBackground();

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        context.getCurrentWindow().drawWidgetsBackGround(partialTicks);

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableLighting();
        RenderHelper.enableStandardItemLighting();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        GlStateManager.pushMatrix();
        //GlStateManager.translate(-this.guiLeft, -this.guiTop, 0);
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        context.getCurrentWindow().drawWidgetsForeGround(Minecraft.getMinecraft().getTickLength());

        if (hovered != null && !(hovered instanceof SlotWidget)) {
            if (hovered.getTooltipShowUpDelay() <= timeHovered) {
                TooltipContainer tooltipContainer = hovered.getHoverText();
                List<TextSpan> additional = hovered.getTooltip();
                if (tooltipContainer == null) {
                    tooltipContainer = new TooltipContainer();
                }
                if (!additional.isEmpty()) {
                    tooltipContainer = tooltipContainer.with(additional);
                }
                tooltipContainer.draw(context);
            }
        }
        // draw vanilla item slot tooltip
        renderHoveredToolTip(mouseX, mouseY);

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableLighting();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    public void drawDebugScreen() {
        int color = Color.rgb(180, 40, 115);
        if (hovered != null) {
            Size size = hovered.getSize();
            Pos2d pos = hovered.getAbsolutePos();

            drawBorder(pos.x, pos.y, size.width, size.height, color, 1f);
            drawText("Class: " + hovered.getClass().getSimpleName(), pos.x, pos.y - 18, 0.5f, color, false);
            drawText("Size: " + size, pos.x, pos.y - 12, 0.5f, color, false);
            drawText("Pos: " + hovered.getPos(), pos.x, pos.y - 6, 0.5f, color, false);
        }

        drawString(fontRenderer, "FPS: " + fps, 5, (int) (context.getScaledScreenSize().height - 24), color);
        drawString(fontRenderer, "Mouse Pos: " + getMousePos(), 5, (int) (context.getScaledScreenSize().height - 13), color);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        context.getCurrentWindow().update();
        setHovered(context.getTopWidgetAt(getMousePos()));
        if (hovered != null) {
            if (hovered instanceof IVanillaSlot) {
                getAccessor().setHoveredSlot(((IVanillaSlot) hovered).getMcSlot());
            }
            timeHovered++;
        }
    }

    private boolean isDoubleClick(long lastClick, long currentClick) {
        return currentClick - lastClick > 500;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        long time = Minecraft.getSystemTime();
        boolean doubleClick = isDoubleClick(lastClick, time);
        lastClick = time;
        for (Interactable interactable : context.getCurrentWindow().getInteractionListeners()) {
            interactable.onClick(mouseButton, doubleClick);
        }

        boolean changedFocus = tryFindFocused();

        if (focused instanceof Interactable) {
            Interactable interactable = (Interactable) focused;
            doubleClick = !changedFocus && isDoubleClick(lastFocusedClick, time);
            interactable.onClick(mouseButton, doubleClick);
        }

        lastFocusedClick = time;
    }

    private boolean tryFindFocused() {
        Widget widget = context.getTopWidgetAt(mousePos);
        boolean changedFocus = false;
        if (widget != null) {
            if (focused == null || focused != widget) {
                if (focused != null) {
                    focused.onRemoveFocus();
                }
                focused = widget.shouldGetFocus() ? widget : null;
                changedFocus = true;
            }
        } else if (focused != null) {
            focused.onRemoveFocus();
            focused = null;
            changedFocus = true;
        }
        return changedFocus;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        for (Interactable interactable : context.getCurrentWindow().getInteractionListeners()) {
            interactable.onClickReleased(mouseButton);
        }
        if (isFocusedValid() && focused instanceof Interactable) {
            ((Interactable) focused).onClickReleased(mouseButton);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
        for (Interactable interactable : context.getCurrentWindow().getInteractionListeners()) {
            interactable.onMouseDragged(mouseButton, timeSinceLastClick);
        }
        if (isFocusedValid() && focused instanceof Interactable) {
            ((Interactable) focused).onMouseDragged(mouseButton, timeSinceLastClick);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        // debug mode C + CTRL + SHIFT + ALT
        if (keyCode == 46 && isCtrlKeyDown() && isShiftKeyDown() && isAltKeyDown()) {
            this.debugMode = !this.debugMode;
        }
        for (Interactable interactable : context.getCurrentWindow().getInteractionListeners()) {
            interactable.onKeyPressed(typedChar, keyCode);
        }
        if (isFocusedValid() && focused instanceof Interactable) {
            if (!((Interactable) focused).onKeyPressed(typedChar, keyCode)) {
                keyTypedSuper(typedChar, keyCode);
            }
        } else {
            keyTypedSuper(typedChar, keyCode);
        }
    }

    private void keyTypedSuper(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1 || this.mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode)) {
            if (context.getMainWindow().onTryClose()) {
                this.mc.player.closeScreen();
            }
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    public boolean isFocusedValid() {
        return focused != null && focused.isEnabled();
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

    @SideOnly(Side.CLIENT)
    public static void drawBorder(float x, float y, float width, float height, int color, float border) {
        drawSolidRect(x - border, y - border, width + 2 * border, border, color);
        drawSolidRect(x - border, y + height, width + 2 * border, border, color);
        drawSolidRect(x - border, y, border, height, color);
        drawSolidRect(x + width, y, border, height, color);
    }

    @SideOnly(Side.CLIENT)
    public static void drawSolidRect(float x, float y, float width, float height, int color) {
        drawRect(x, y, x + width, y + height, color);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableBlend();
    }

    @SideOnly(Side.CLIENT)
    public static void drawText(String text, float x, float y, float scale, int color, boolean shadow) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        GlStateManager.disableBlend();
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 0f);
        float sf = 1 / scale;
        fontRenderer.drawString(text, x * sf, y * sf, color, shadow);
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
    }

    public static void drawRect(float left, float top, float right, float bottom, int color) {
        if (left < right) {
            float i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            float j = top;
            top = bottom;
            bottom = j;
        }

        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(r, g, b, a);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(left, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, top, 0.0D).endVertex();
        bufferbuilder.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}
