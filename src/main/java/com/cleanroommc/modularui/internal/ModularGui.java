package com.cleanroommc.modularui.internal;

import com.cleanroommc.modularui.api.IVanillaSlot;
import com.cleanroommc.modularui.mixin.GuiContainerAccess;
import com.google.common.primitives.Ints;
import com.cleanroommc.modularui.ModularUIMod;
import com.cleanroommc.modularui.api.IWidgetParent;
import com.cleanroommc.modularui.api.Interactable;
import com.cleanroommc.modularui.api.math.GuiArea;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.widget.IWidgetDrawable;
import com.cleanroommc.modularui.widget.Widget;
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

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

@SideOnly(Side.CLIENT)
public class ModularGui extends GuiContainer implements GuiContainerAccess {

    private final ModularUI gui;
    private long lastClick = -1;
    private long lastFocusedClick = -1;
    private Interactable focused;
    public boolean debugMode = true;
    private int drawCalls = 0;
    private long drawTime = 0;
    private int fps = 0;

    public ModularGui(ModularUIContainer container) {
        super(container);
        this.gui = container.getGui();
        this.gui.setScreen(this);
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        this.gui.onResize(new Size(scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight()));
        this.gui.initialise();
    }

    public ModularUI getGui() {
        return gui;
    }

    @Override
    public void onResize(Minecraft mc, int w, int h) {
        super.onResize(mc, w, h);
        gui.onResize(new Size(w, h));
        ModularUIMod.LOGGER.info("Resized screen");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (debugMode) {
            GlStateManager.disableRescaleNormal();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.colorMask(true, true, true, true);
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

        IWidgetParent.forEachByLayer(gui, widget -> {
            widget.onFrameUpdate();
            if (widget.isEnabled() && widget instanceof IWidgetDrawable) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(widget.getAbsolutePos().x, widget.getAbsolutePos().y, 0);
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
        IWidgetParent.forEachByLayer(gui, widget -> {
            if (widget.isEnabled() && widget instanceof IWidgetDrawable) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(widget.getAbsolutePos().x, widget.getAbsolutePos().y, 0);
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

        IWidgetParent.forEachByLayer(gui, widget -> {
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

        int color = 0xDB69D2;
        Widget widget = topWidget.get();
        if (widget != null) {
            GuiArea area = widget.getArea();

            //drawRect(10f, 10, 700, 400, color);

            drawBorder(area.x0, area.y0, area.width, area.height, color, 1);
            //drawRect((int) (area.x0 - 1), (int) (area.y0 - 1), (int) (area.x1 + 1), (int) (area.y1 + 1), color);
            //drawHorizontalLine((int) area.x0 - 1, (int) area.x1 + 1, (int) area.y0 - 2, color);
            //drawHorizontalLine((int) area.x0 - 1, (int) area.x1 + 1, (int) area.y1 + 2, color);
            //drawVerticalLine((int) area.x0 - 1, (int) area.y0 - 1, (int) area.y1 + 2, color);
            //drawVerticalLine((int) area.x1 + 1, (int) area.y0 - 1, (int) area.y1 + 2, color);
            //fontRenderer.drawString(info, area.x0, area.y0 - 12, color, false);
            drawText("Class: " + widget.getClass().getSimpleName(), area.x0, area.y0 - 18, 0.5f, color, false);
            drawText("Size: " + area.getSize(), area.x0, area.y0 - 12, 0.5f, color, false);
            drawText("Pos: " + area.getTopLeft(), area.x0, area.y0 - 6, 0.5f, color, false);
        }

        drawString(fontRenderer, "FPS: " + fps, 5, (int) (gui.getScaledScreenSize().height - 24), color);
        drawString(fontRenderer, "Mouse Pos: " + gui.getMousePos(), 5, (int) (gui.getScaledScreenSize().height - 13), color);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        gui.update();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        long time = Minecraft.getSystemTime();
        int diff = Ints.saturatedCast(time - lastClick);
        lastClick = time;
        Pos2d mousePos = gui.getMousePos();
        for (Interactable interactable : gui.getListeners()) {
            interactable.onClick(mousePos, mouseButton, diff);
        }
        Interactable interactable = gui.getTopInteractable(gui.getMousePos());
        if (interactable != null) {
            if (focused == interactable) {
                diff = Ints.saturatedCast(time - lastFocusedClick);
            } else {
                diff = Integer.MAX_VALUE;
            }
            interactable.onClick(mousePos, mouseButton, diff);
        }
        lastFocusedClick = time;
        focused = interactable;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        Pos2d mousePos = gui.getMousePos();
        for (Interactable interactable : gui.getListeners()) {
            interactable.onClickReleased(mousePos, mouseButton);
        }
        if (isFocusedValid()) {
            focused.onClickReleased(mousePos, mouseButton);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
        Pos2d mousePos = gui.getMousePos();
        for (Interactable interactable : gui.getListeners()) {
            interactable.onMouseDragged(mousePos, mouseButton, timeSinceLastClick);
        }
        if (isFocusedValid()) {
            focused.onMouseDragged(mousePos, mouseButton, timeSinceLastClick);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        // debug mode C + CTRL + SHIFT + ALT
        if (keyCode == 46 && isCtrlKeyDown() && isShiftKeyDown() && isAltKeyDown()) {
            this.debugMode = !this.debugMode;
        }
        for (Interactable interactable : gui.getListeners()) {
            interactable.onKeyPressed(typedChar, keyCode);
        }
        if (isFocusedValid()) {
            focused.onKeyPressed(typedChar, keyCode);
        }
    }

    public boolean isFocusedValid() {
        return focused != null && ((Widget) focused).isEnabled();
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
        GlStateManager.color(1, 1, 1, 1);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(left, bottom, 0.0D).color(r, g, b, a).endVertex();
        bufferbuilder.pos(right, bottom, 0.0D).color(r, g, b, a).endVertex();
        bufferbuilder.pos(right, top, 0.0D).color(r, g, b, a).endVertex();
        bufferbuilder.pos(left, top, 0.0D).color(r, g, b, a).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    @Override
    public Slot getSlotAt(float x, float y) {
        Interactable interactable = getGui().getTopInteractable(new Pos2d(x, y));
        if (interactable instanceof IVanillaSlot) {
            return ((IVanillaSlot) interactable).getMcSlot();
        }
        return null;
    }

    @Override
    public boolean isOverSlot(Slot slot, float x, float y) {
        return isPointInRegion(slot.xPos, slot.yPos, 16, 16, (int) x, (int) y);
    }
}
