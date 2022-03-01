package com.cleanroommc.modularui.common.internal.wrapper;

import com.cleanroommc.modularui.ModularUIMod;
import com.cleanroommc.modularui.api.IVanillaSlot;
import com.cleanroommc.modularui.api.IWidgetParent;
import com.cleanroommc.modularui.api.Interactable;
import com.cleanroommc.modularui.api.math.Color;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.common.internal.ModularUIContext;
import com.cleanroommc.modularui.common.internal.mixin.GuiContainerAccess;
import com.cleanroommc.modularui.common.widget.IWidgetDrawable;
import com.cleanroommc.modularui.common.widget.SlotWidget;
import com.cleanroommc.modularui.common.widget.Widget;
import com.google.common.primitives.Ints;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

@SideOnly(Side.CLIENT)
public class ModularGui extends GuiContainer implements GuiContainerAccess {

    private final ModularUIContext context;
    private Pos2d mousePos = Pos2d.ZERO;

    //private final ModularUI gui;
    private long lastClick = -1;
    private long lastFocusedClick = -1;
    private Widget focused;
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
    }

    public ModularUIContext getContext() {
        return context;
    }

    public Pos2d getMousePos() {
        return mousePos;
    }

    @Override
    public void onResize(Minecraft mc, int w, int h) {
        super.onResize(mc, w, h);
        context.resize(new Size(w, h));
        ModularUIMod.LOGGER.info("Resized screen");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // calculate exact mouse pos
        float x = Mouse.getEventX() * width / (float) mc.displayWidth;
        float y = height - Mouse.getEventY() * height / (float) mc.displayHeight - 1;
        mousePos = new Pos2d(x, y);

        super.drawScreen(mouseX, mouseY, partialTicks);
        if (debugMode) {
            GlStateManager.disableRescaleNormal();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            drawDebugScreen();
            GlStateManager.color(1f, 1f, 1f, 1f);
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            GlStateManager.enableRescaleNormal();
            RenderHelper.enableStandardItemLighting();
        }
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

        drawDefaultBackground();

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        IWidgetParent.forEachByLayer(context.getCurrentWindow(), widget -> {
            widget.onFrameUpdate();
            if (widget.isEnabled() && widget instanceof IWidgetDrawable) {
                GlStateManager.pushMatrix();
                if (widget instanceof SlotWidget) {
                    Slot slot = ((SlotWidget) widget).getMcSlot();
                    GlStateManager.translate(guiLeft + slot.xPos, guiTop + slot.yPos, 0);
                } else {
                    GlStateManager.translate(widget.getAbsolutePos().x, widget.getAbsolutePos().y, 0);
                }
                GlStateManager.enableBlend();
                ((IWidgetDrawable) widget).drawInBackground(partialTicks);
                GlStateManager.popMatrix();
            }
            return false;
        });

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableLighting();
        RenderHelper.enableStandardItemLighting();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(-this.guiLeft, -this.guiTop, 0);
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        float partialTicks = Minecraft.getMinecraft().getTickLength();
        IWidgetParent.forEachByLayer(context.getCurrentWindow(), widget -> {
            if (widget.isEnabled() && widget instanceof IWidgetDrawable) {
                GlStateManager.pushMatrix();
                if (widget instanceof SlotWidget) {
                    Slot slot = ((SlotWidget) widget).getMcSlot();
                    GlStateManager.translate(guiLeft + slot.xPos, guiTop + slot.yPos, 0);
                } else {
                    GlStateManager.translate(widget.getAbsolutePos().x, widget.getAbsolutePos().y, 0);
                }
                GlStateManager.enableBlend();
                ((IWidgetDrawable) widget).drawInForeground(partialTicks);
                GlStateManager.popMatrix();
            }
            return false;
        });

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableLighting();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    public void drawDebugScreen() {
        AtomicReference<Widget> topWidget = new AtomicReference<>();

        IWidgetParent.forEachByLayer(context.getCurrentWindow(), widget -> {
            if (!widget.isUnderMouse()) {
                return;
            }
            if (topWidget.get() == null) {
                topWidget.set(widget);
                return;
            }
            if (widget.getLayer() >= topWidget.get().getLayer()) {
                topWidget.set(widget);
            }
        });

        int color = Color.rgb(180, 40, 115);
        Widget widget = topWidget.get();
        if (widget != null) {
            Size size = widget.getSize();
            Pos2d pos = widget.getAbsolutePos();

            drawBorder(pos.x, pos.y, size.width, size.height, color, 1f);
            drawText("Class: " + widget.getClass().getSimpleName(), pos.x, pos.y - 18, 0.5f, color, false);
            drawText("Size: " + size, pos.x, pos.y - 12, 0.5f, color, false);
            drawText("Pos: " + widget.getPos(), pos.x, pos.y - 6, 0.5f, color, false);
        }

        drawString(fontRenderer, "FPS: " + fps, 5, (int) (context.getScaledScreenSize().height - 24), color);
        drawString(fontRenderer, "Mouse Pos: " + getMousePos(), 5, (int) (context.getScaledScreenSize().height - 13), color);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        context.getCurrentWindow().update();
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
        Pos2d mousePos = getMousePos();
        for (Interactable interactable : context.getCurrentWindow().getInteractionListeners()) {
            interactable.onClick(mouseButton, doubleClick);
        }
        Widget widget = context.getTopWidgetAt(mousePos);
        if (widget != null) {
            if (widget instanceof Interactable) {
                Interactable interactable = (Interactable) widget;
                doubleClick = focused == interactable && isDoubleClick(lastFocusedClick, time);
                interactable.onClick(mouseButton, doubleClick);
            }
            if (widget.shouldGetFocus()) {
                if (focused != null) {
                    focused.onRemoveFocus();
                }
                focused = widget;
            } else {
                focused = null;
            }
        } else if (focused != null) {
            focused.onRemoveFocus();
            focused = null;
        }
        lastFocusedClick = time;
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
        super.keyTyped(typedChar, keyCode);
        // debug mode C + CTRL + SHIFT + ALT
        if (keyCode == 46 && isCtrlKeyDown() && isShiftKeyDown() && isAltKeyDown()) {
            this.debugMode = !this.debugMode;
        }
        for (Interactable interactable : context.getCurrentWindow().getInteractionListeners()) {
            interactable.onKeyPressed(typedChar, keyCode);
        }
        if (isFocusedValid() && focused instanceof Interactable) {
            ((Interactable) focused).onKeyPressed(typedChar, keyCode);
        }
    }

    public boolean isFocusedValid() {
        return focused != null && focused.isEnabled();
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

    @Override
    public Slot getSlotAt(float x, float y) {
        Widget widget = context.getTopWidgetAt(new Pos2d(x, y));
        if (widget instanceof IVanillaSlot) {
            return ((IVanillaSlot) widget).getMcSlot();
        }
        return null;
    }

    @Override
    public boolean isOverSlot(Slot slot, float x, float y) {
        return isPointInRegion(slot.xPos, slot.yPos, 16, 16, (int) x, (int) y);
    }
}
