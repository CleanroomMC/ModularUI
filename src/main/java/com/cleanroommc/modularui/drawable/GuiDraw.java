package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.IDrawable;
import com.cleanroommc.modularui.api.IKey;
import com.cleanroommc.modularui.api.IWidget;
import com.cleanroommc.modularui.screen.GuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.MathUtils;
import com.cleanroommc.modularui.widget.sizer.Area;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class GuiDraw {

    private final static Stack<Area> scissors = new Stack<Area>();

    public static void scissor(Area area, GuiContext context) {
        scissor(area.x, area.y, area.width, area.height, context);
    }

    public static void scissor(int x, int y, int w, int h, GuiContext context) {
        scissor(context.globalX(x), context.globalY(y), w, h, context.screen.getViewport().width, context.screen.getViewport().height);
    }

    public static void scissorTransformed(int x, int y, int w, int h, GuiContext context) {
        scissor(x, y, w, h, context.screen.getViewport().width, context.screen.getViewport().height);
    }

    /**
     * Scissor (clip) the screen
     */
    public static void scissor(int x, int y, int w, int h, int sw, int sh) {
        Area scissor = scissors.isEmpty() ? null : scissors.peek();

        /* If it was scissored before, then clamp to the bounds of the last one */
        if (scissor != null) {
            w += Math.min(x - scissor.x, 0);
            h += Math.min(y - scissor.y, 0);
            x = MathUtils.clamp(x, scissor.x, scissor.ex());
            y = MathUtils.clamp(y, scissor.y, scissor.ey());
            w = MathUtils.clamp(w, 0, scissor.ex() - x);
            h = MathUtils.clamp(h, 0, scissor.ey() - y);
        }

        scissor = new Area(x, y, w, h);
        scissorArea(x, y, w, h, sw, sh);
        scissors.add(scissor);
    }

    private static void scissorArea(int x, int y, int w, int h, int sw, int sh) {
        /* Clipping area around scroll area */
        Minecraft mc = Minecraft.getMinecraft();

        float rx = (float) Math.ceil(mc.displayWidth / (double) sw);
        float ry = (float) Math.ceil(mc.displayHeight / (double) sh);

        int xx = (int) (x * rx);
        int yy = (int) (mc.displayHeight - (y + h) * ry);
        int ww = (int) (w * rx);
        int hh = (int) (h * ry);

        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        if (ww == 0 || hh == 0) {
            GL11.glScissor(0, 0, 1, 1);
        } else {
            GL11.glScissor(xx, yy, ww, hh);
        }
    }

    public static void unscissor(GuiContext context) {
        unscissor(context.getViewport().width, context.getViewport().height);
    }

    public static void unscissor(int sw, int sh) {
        scissors.pop();

        if (scissors.isEmpty()) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        } else {
            Area area = scissors.peek();

            scissorArea(area.x, area.y, area.width, area.height, sw, sh);
        }
    }

    public static void drawHorizontalGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        drawHorizontalGradientRect(left, top, right, bottom, startColor, endColor, 0);
    }

    /**
     * Draws a rectangle with a horizontal gradient between with specified
     * colors, the code is borrowed form drawGradient()
     */
    public static void drawHorizontalGradientRect(int left, int top, int right, int bottom, int startColor, int endColor, float zLevel) {
        float a1 = (startColor >> 24 & 255) / 255.0F;
        float r1 = (startColor >> 16 & 255) / 255.0F;
        float g1 = (startColor >> 8 & 255) / 255.0F;
        float b1 = (startColor & 255) / 255.0F;
        float a2 = (endColor >> 24 & 255) / 255.0F;
        float r2 = (endColor >> 16 & 255) / 255.0F;
        float g2 = (endColor >> 8 & 255) / 255.0F;
        float b2 = (endColor & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(right, top, zLevel).color(r2, g2, b2, a2).endVertex();
        buffer.pos(left, top, zLevel).color(r1, g1, b1, a1).endVertex();
        buffer.pos(left, bottom, zLevel).color(r1, g1, b1, a1).endVertex();
        buffer.pos(right, bottom, zLevel).color(r2, g2, b2, a2).endVertex();

        tessellator.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawVerticalGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        drawVerticalGradientRect(left, top, right, bottom, startColor, endColor, 0);
    }

    /**
     * Draws a rectangle with a vertical gradient between with specified
     * colors
     */
    public static void drawVerticalGradientRect(int left, int top, int right, int bottom, int startColor, int endColor, float zLevel) {
        float a1 = (startColor >> 24 & 255) / 255.0F;
        float r1 = (startColor >> 16 & 255) / 255.0F;
        float g1 = (startColor >> 8 & 255) / 255.0F;
        float b1 = (startColor & 255) / 255.0F;
        float a2 = (endColor >> 24 & 255) / 255.0F;
        float r2 = (endColor >> 16 & 255) / 255.0F;
        float g2 = (endColor >> 8 & 255) / 255.0F;
        float b2 = (endColor & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(right, top, zLevel).color(r1, g1, b1, a1).endVertex();
        buffer.pos(left, top, zLevel).color(r1, g1, b1, a1).endVertex();
        buffer.pos(left, bottom, zLevel).color(r2, g2, b2, a2).endVertex();
        buffer.pos(right, bottom, zLevel).color(r2, g2, b2, a2).endVertex();

        tessellator.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawBillboard(int x, int y, int u, int v, int w, int h, int textureW, int textureH) {
        drawBillboard(x, y, u, v, w, h, textureW, textureH, 0);
    }

    /**
     * Draw a textured quad with given UV, dimensions and custom texture size
     */
    public static void drawBillboard(int x, int y, int u, int v, int w, int h, int textureW, int textureH, float z) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        drawBillboard(buffer, x, y, u, v, w, h, textureW, textureH, z);
        tessellator.draw();
    }

    public static void drawBillboard(BufferBuilder buffer, int x, int y, int u, int v, int w, int h, int textureW, int textureH, float z) {
        float tw = 1F / textureW;
        float th = 1F / textureH;

        buffer.pos(x, y + h, z).tex(u * tw, (v + h) * th).endVertex();
        buffer.pos(x + w, y + h, z).tex((u + w) * tw, (v + h) * th).endVertex();
        buffer.pos(x + w, y, z).tex((u + w) * tw, v * th).endVertex();
        buffer.pos(x, y, z).tex(u * tw, v * th).endVertex();
    }

    public static void drawBillboard(int x, int y, int u, int v, int w, int h, int textureW, int textureH, int tu, int tv) {
        drawBillboard(x, y, u, v, w, h, textureW, textureH, tu, tv, 0);
    }

    /**
     * Draw a textured quad with given UV, dimensions and custom texture size
     */
    public static void drawBillboard(int x, int y, int u, int v, int w, int h, int textureW, int textureH, int tu, int tv, float z) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        drawBillboard(buffer, x, y, u, v, w, h, textureW, textureH, tu, tv, z);
        tessellator.draw();
    }

    public static void drawBillboard(BufferBuilder buffer, int x, int y, int u, int v, int w, int h, int textureW, int textureH, int tu, int tv, float z) {
        float tw = 1F / textureW;
        float th = 1F / textureH;

        buffer.pos(x, y + h, z).tex(u * tw, tv * th).endVertex();
        buffer.pos(x + w, y + h, z).tex(tu * tw, tv * th).endVertex();
        buffer.pos(x + w, y, z).tex(tu * tw, v * th).endVertex();
        buffer.pos(x, y, z).tex(u * tw, v * th).endVertex();
    }

    public static void drawBillboard(int x0, int y0, int x1, int y1, float u0, float v0, float u1, float v1) {
        drawBillboard(x0, y0, x1, y1, u0, v0, u1, v1, 0);
    }

    public static void drawBillboard(int x0, int y0, int x1, int y1, float u0, float v0, float u1, float v1, float z) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        drawBillboard(buffer, x0, y0, x1, y1, u0, v0, u1, v1, z);
        tessellator.draw();
    }

    public static void drawBillboard(BufferBuilder buffer, int x0, int y0, int x1, int y1, float u0, float v0, float u1, float v1, float z) {
        buffer.pos(x0, y1, z).tex(u0, v1).endVertex();
        buffer.pos(x1, y1, z).tex(u1, v1).endVertex();
        buffer.pos(x1, y0, z).tex(u1, v0).endVertex();
        buffer.pos(x0, y0, z).tex(u0, v0).endVertex();
    }

    public static int drawBorder(Area area, int color) {
        // TODO
        if (/*!ModularUI.enableBorders.get()*/false) {
            //area.draw(color);

            return 0;
        }

        //area.draw(0xff000000);
        //area.draw(color, 1);

        return 1;
    }

    public static void drawOutlineCenter(int x, int y, int offset, int color) {
        drawOutlineCenter(x, y, offset, color, 1);
    }

    public static void drawOutlineCenter(int x, int y, int offset, int color, int border) {
        drawOutline(x - offset, y - offset, x + offset, y + offset, color, border);
    }

    public static void drawOutline(int left, int top, int right, int bottom, int color) {
        drawOutline(left, top, right, bottom, color, 1);
    }

    /**
     * Draw rectangle outline with given border
     */
    public static void drawOutline(int left, int top, int right, int bottom, int color, int border) {
        Gui.drawRect(left, top, left + border, bottom, color);
        Gui.drawRect(right - border, top, right, bottom, color);
        Gui.drawRect(left + border, top, right - border, top + border, color);
        Gui.drawRect(left + border, bottom - border, right - border, bottom, color);
    }

    /*public static void drawOutlinedIcon(Drawable icon, int x, int y, int color) {
        drawOutlinedIcon(icon, x, y, color, 0F, 0F);
    }

    /**
     * Draw an icon with a black outline
     */
    /*public static void drawOutlinedIcon(Drawable icon, int x, int y, int color, float ax, float ay) {
        GlStateManager.color(0, 0, 0, 1);
        icon.render(x - 1, y, ax, ay);
        icon.render(x + 1, y, ax, ay);
        icon.render(x, y - 1, ax, ay);
        icon.render(x, y + 1, ax, ay);
        ColorUtils.bindColor(color);
        icon.render(x, y, ax, ay);
    }*/

    public static void drawLockedArea(IWidget element) {
        drawLockedArea(element, 0);
    }

    /**
     * Generic method for drawing locked (disabled) state of
     * an input field
     */
    public static void drawLockedArea(IWidget element, int padding) {
        if (!element.isEnabled()) {
            //element.getArea().draw(Color.HALF_BLACK, padding);

            //GuiDraw.drawOutlinedIcon(GuiTextures.LOCKED, element.area.mx(), element.area.my(), 0xffffffff, 0.5F, 0.5F);
        }
    }

    public static void drawDropShadow(int left, int top, int right, int bottom, int offset, int opaque, int shadow) {
        left -= offset;
        top -= offset;
        right += offset;
        bottom += offset;

        float a1 = (opaque >> 24 & 255) / 255.0F;
        float r1 = (opaque >> 16 & 255) / 255.0F;
        float g1 = (opaque >> 8 & 255) / 255.0F;
        float b1 = (opaque & 255) / 255.0F;
        float a2 = (shadow >> 24 & 255) / 255.0F;
        float r2 = (shadow >> 16 & 255) / 255.0F;
        float g2 = (shadow >> 8 & 255) / 255.0F;
        float b2 = (shadow & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        /* Draw opaque part */
        buffer.pos(right - offset, top + offset, 0).color(r1, g1, b1, a1).endVertex();
        buffer.pos(left + offset, top + offset, 0).color(r1, g1, b1, a1).endVertex();
        buffer.pos(left + offset, bottom - offset, 0).color(r1, g1, b1, a1).endVertex();
        buffer.pos(right - offset, bottom - offset, 0).color(r1, g1, b1, a1).endVertex();

        /* Draw top shadow */
        buffer.pos(right, top, 0).color(r2, g2, b2, a2).endVertex();
        buffer.pos(left, top, 0).color(r2, g2, b2, a2).endVertex();
        buffer.pos(left + offset, top + offset, 0).color(r1, g1, b1, a1).endVertex();
        buffer.pos(right - offset, top + offset, 0).color(r1, g1, b1, a1).endVertex();

        /* Draw bottom shadow */
        buffer.pos(right - offset, bottom - offset, 0).color(r1, g1, b1, a1).endVertex();
        buffer.pos(left + offset, bottom - offset, 0).color(r1, g1, b1, a1).endVertex();
        buffer.pos(left, bottom, 0).color(r2, g2, b2, a2).endVertex();
        buffer.pos(right, bottom, 0).color(r2, g2, b2, a2).endVertex();

        /* Draw left shadow */
        buffer.pos(left + offset, top + offset, 0).color(r1, g1, b1, a1).endVertex();
        buffer.pos(left, top, 0).color(r2, g2, b2, a2).endVertex();
        buffer.pos(left, bottom, 0).color(r2, g2, b2, a2).endVertex();
        buffer.pos(left + offset, bottom - offset, 0).color(r1, g1, b1, a1).endVertex();

        /* Draw right shadow */
        buffer.pos(right, top, 0).color(r2, g2, b2, a2).endVertex();
        buffer.pos(right - offset, top + offset, 0).color(r1, g1, b1, a1).endVertex();
        buffer.pos(right - offset, bottom - offset, 0).color(r1, g1, b1, a1).endVertex();
        buffer.pos(right, bottom, 0).color(r2, g2, b2, a2).endVertex();

        tessellator.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawDropCircleShadow(int x, int y, int radius, int segments, int opaque, int shadow) {
        float a1 = (opaque >> 24 & 255) / 255.0F;
        float r1 = (opaque >> 16 & 255) / 255.0F;
        float g1 = (opaque >> 8 & 255) / 255.0F;
        float b1 = (opaque & 255) / 255.0F;
        float a2 = (shadow >> 24 & 255) / 255.0F;
        float r2 = (shadow >> 16 & 255) / 255.0F;
        float g2 = (shadow >> 8 & 255) / 255.0F;
        float b2 = (shadow & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);

        buffer.pos(x, y, 0).color(r1, g1, b1, a1).endVertex();

        for (int i = 0; i <= segments; i++) {
            double a = i / (double) segments * Math.PI * 2 - Math.PI / 2;

            buffer.pos(x - Math.cos(a) * radius, y + Math.sin(a) * radius, 0).color(r2, g2, b2, a2).endVertex();
        }

        tessellator.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawDropCircleShadow(int x, int y, int radius, int offset, int segments, int opaque, int shadow) {
        if (offset >= radius) {
            drawDropCircleShadow(x, y, radius, segments, opaque, shadow);

            return;
        }

        float a1 = (opaque >> 24 & 255) / 255.0F;
        float r1 = (opaque >> 16 & 255) / 255.0F;
        float g1 = (opaque >> 8 & 255) / 255.0F;
        float b1 = (opaque & 255) / 255.0F;
        float a2 = (shadow >> 24 & 255) / 255.0F;
        float r2 = (shadow >> 16 & 255) / 255.0F;
        float g2 = (shadow >> 8 & 255) / 255.0F;
        float b2 = (shadow & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        /* Draw opaque base */
        buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x, y, 0).color(r1, g1, b1, a1).endVertex();

        for (int i = 0; i <= segments; i++) {
            double a = i / (double) segments * Math.PI * 2 - Math.PI / 2;

            buffer.pos(x - Math.cos(a) * offset, y + Math.sin(a) * offset, 0).color(r1, g1, b1, a1).endVertex();
        }

        tessellator.draw();

        /* Draw outer shadow */
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        for (int i = 0; i < segments; i++) {
            double alpha1 = i / (double) segments * Math.PI * 2 - Math.PI / 2;
            double alpha2 = (i + 1) / (double) segments * Math.PI * 2 - Math.PI / 2;

            buffer.pos(x - Math.cos(alpha2) * offset, y + Math.sin(alpha2) * offset, 0).color(r1, g1, b1, a1).endVertex();
            buffer.pos(x - Math.cos(alpha1) * offset, y + Math.sin(alpha1) * offset, 0).color(r1, g1, b1, a1).endVertex();
            buffer.pos(x - Math.cos(alpha1) * radius, y + Math.sin(alpha1) * radius, 0).color(r2, g2, b2, a2).endVertex();
            buffer.pos(x - Math.cos(alpha2) * radius, y + Math.sin(alpha2) * radius, 0).color(r2, g2, b2, a2).endVertex();
        }

        tessellator.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static int drawMultiText(FontRenderer font, String text, int x, int y, int color, int width) {
        return drawMultiText(font, text, x, y, color, width, 12);
    }

    public static int drawMultiText(FontRenderer font, String text, int x, int y, int color, int width, int lineHeight) {
        return drawMultiText(font, text, x, y, color, width, lineHeight, 0F, 0F);
    }

    public static int drawMultiText(FontRenderer font, String text, int x, int y, int color, int width, int lineHeight, float ax, float ay) {
        List<String> list = font.listFormattedStringToWidth(text, width);
        int h = (lineHeight * (list.size() - 1)) + font.FONT_HEIGHT;

        y -= h * ay;

        for (String string : list) {
            font.drawStringWithShadow(string, x + (width - font.getStringWidth(string)) * ax, y, color);

            y += lineHeight;
        }

        return h;
    }

    public static void drawTextBackground(FontRenderer font, String text, int x, int y, int color, int background) {
        drawTextBackground(font, text, x, y, color, background, 3);
    }

    public static void drawTextBackground(FontRenderer font, String text, int x, int y, int color, int background, int offset) {
        drawTextBackground(font, text, x, y, color, background, offset, true);
    }

    public static void drawTextBackground(FontRenderer font, String text, int x, int y, int color, int background, int offset, boolean shadow) {
        int a = background >> 24 & 0xff;

        if (a != 0) {
            Gui.drawRect(x - offset, y - offset, x + font.getStringWidth(text) + offset, y + font.FONT_HEIGHT + offset, background);
        }

        font.drawString(text, x, y, color, shadow);
    }

    // TODO
    /*public static void drawCustomBackground(int x, int y, int width, int height) {
        ResourceLocation background = ModularUI.backgroundImage.get();
        int color = ModularUI.backgroundColor.get();

        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (background == null) {
            Gui.drawRect(x, y, x + width, y + height, color);
        } else {
            Minecraft.getMinecraft().renderEngine.bindTexture(background);
            ColorUtils.bindColor(color);
            GlStateManager.enableAlpha();
            GuiDraw.drawBillboard(x, y, 0, 0, width, height, width, height);
        }
    }*/

    public static void drawRepeatBillboard(int x, int y, int w, int h, int u, int v, int tileW, int tileH, int tw, int th) {
        int countX = ((w - 1) / tileW) + 1;
        int countY = ((h - 1) / tileH) + 1;
        int fillerX = w - (countX - 1) * tileW;
        int fillerY = h - (countY - 1) * tileH;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        for (int i = 0, c = countX * countY; i < c; i++) {
            int ix = i % countX;
            int iy = i / countX;
            int xx = x + ix * tileW;
            int yy = y + iy * tileH;
            int xw = ix == countX - 1 ? fillerX : tileW;
            int yh = iy == countY - 1 ? fillerY : tileH;

            drawBillboard(buffer, xx, yy, u, v, xw, yh, tw, th, 0);
        }

        tessellator.draw();
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

    public static void drawFluidTexture(FluidStack content, float x0, float y0, float width, float height, float z) {
        if (content == null) {
            return;
        }
        Fluid fluid = content.getFluid();
        ResourceLocation fluidStill = fluid.getStill(content);
        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluidStill.toString());
        int fluidColor = fluid.getColor(content);
        GlStateManager.enableBlend();
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        float u0 = sprite.getMinU(), u1 = sprite.getMaxU(), v0 = sprite.getMinV(), v1 = sprite.getMaxV();
        float x1 = x0 + width, y1 = y0 + height;
        float r = Color.getRedF(fluidColor), g = Color.getGreenF(fluidColor), b = Color.getBlueF(fluidColor), a = Color.getAlphaF(fluidColor);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(x0, y1, z).tex(u0, v1).color(r, g, b, a).endVertex();
        buffer.pos(x1, y1, z).tex(u1, v1).color(r, g, b, a).endVertex();
        buffer.pos(x1, y0, z).tex(u1, v0).color(r, g, b, a).endVertex();
        buffer.pos(x0, y0, z).tex(u0, v0).color(r, g, b, a).endVertex();
        tessellator.draw();
        GlStateManager.disableBlend();
    }

    //==== Draw helpers ====

    public static void drawGradientRect(float zLevel, float left, float top, float right, float bottom, int startColor, int endColor) {
        float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;
        float startRed = (float) (startColor >> 16 & 255) / 255.0F;
        float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
        float startBlue = (float) (startColor & 255) / 255.0F;
        float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;
        float endRed = (float) (endColor >> 16 & 255) / 255.0F;
        float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
        float endBlue = (float) (endColor & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(right, top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        buffer.pos(left, top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        buffer.pos(left, bottom, zLevel).color(endRed, endGreen, endBlue, endAlpha).endVertex();
        buffer.pos(right, bottom, zLevel).color(endRed, endGreen, endBlue, endAlpha).endVertex();
        tessellator.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    //==== Tooltip helpers ====

    public static void drawHoveringText(GuiContext context, List<IKey> textLines, int maxWidth, float scale, boolean forceShadow, Alignment alignment) {
        if (textLines.isEmpty()) {
            return;
        }
        List<String> lines = textLines.stream().map(IKey::get).collect(Collectors.toList());
        drawHoveringTextFormatted(context, lines, maxWidth, scale, forceShadow, alignment);
    }

    public static void drawHoveringTextFormatted(GuiContext context, List<String> lines, int maxWidth) {
        drawHoveringTextFormatted(context, lines, maxWidth, 1f, false, Alignment.TopLeft);
    }

    public static void drawTooltip(GuiContext context, List<IDrawable> lines, int maxWidth, float scale, boolean forceShadow, Alignment alignment) {

    }

    public static void drawHoveringTextFormatted(GuiContext context, List<String> lines, int maxWidth, float scale, boolean forceShadow, Alignment alignment) {
        if (lines.isEmpty()) {
            return;
        }
        if (maxWidth < 0) {
            maxWidth = Integer.MAX_VALUE;
        }
        Area screen = context.screen.getViewport();
        int mouseX = context.getMouseX(), mouseY = context.getMouseY();
        RenderTooltipEvent.Pre event = new RenderTooltipEvent.Pre(ItemStack.EMPTY, lines, mouseX, mouseY, screen.width, screen.height, maxWidth, TextRenderer.getFontRenderer());
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return;
        }
        lines = event.getLines();
        mouseX = event.getX();
        mouseY = event.getY();
        int screenWidth = event.getScreenWidth(), screenHeight = event.getScreenHeight();
        maxWidth = event.getMaxWidth();

        int maxTextWidth = maxWidth;

        boolean mouseOnRightSide = false;
        int screenSpaceRight = screenWidth - mouseX - 16;
        if (mouseX > screenWidth / 2f) {
            mouseOnRightSide = true;
        }
        if (maxTextWidth > screenSpaceRight) {
            maxTextWidth = screenSpaceRight;
        }
        boolean putOnLeft = false;
        int tooltipY = mouseY - 12;
        int tooltipX = mouseX + 12;
        TextRenderer renderer = TextRenderer.SHARED;
        renderer.setPos(mouseX, mouseY);
        renderer.setAlignment(Alignment.TopLeft, maxTextWidth);
        renderer.setScale(scale);
        renderer.setShadow(forceShadow);
        renderer.setSimulate(true);
        List<Pair<String, Float>> measuredLines = renderer.measureLines(lines);
        if (mouseOnRightSide && measuredLines.size() > lines.size()) {
            putOnLeft = true;
            maxTextWidth = Math.min(maxWidth, mouseX - 16);
        }

        renderer.setAlignment(Alignment.TopLeft, maxTextWidth);
        measuredLines = renderer.measureLines(lines);
        renderer.drawMeasuredLines(measuredLines);
        int tooltipTextWidth = (int) renderer.lastWidth;
        int tooltipHeight = (int) renderer.lastHeight;

        if (mouseOnRightSide && putOnLeft) {
            tooltipX += -24 - tooltipTextWidth;
        }

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        int color = 0xFFFFFF;

        drawTooltipBackground(lines, tooltipX, tooltipY, tooltipTextWidth, tooltipHeight, 300);

        MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostBackground(ItemStack.EMPTY, lines, tooltipX, tooltipY, TextRenderer.getFontRenderer(), tooltipTextWidth, tooltipHeight));

        renderer.setSimulate(false);
        renderer.setPos(tooltipX, tooltipY);
        renderer.setAlignment(alignment, maxTextWidth);
        renderer.setColor(color);
        renderer.drawMeasuredLines(measuredLines);

        MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostText(ItemStack.EMPTY, lines, tooltipX, tooltipY, TextRenderer.getFontRenderer(), tooltipTextWidth, tooltipHeight));

        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableRescaleNormal();
    }

    public static void drawTooltipBackground(List<String> lines, int x, int y, int textWidth, int height, int z) {
        // TODO theme color
        int backgroundColor = 0xF0100010;
        int borderColorStart = 0x505000FF;
        int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
        RenderTooltipEvent.Color colorEvent = new RenderTooltipEvent.Color(ItemStack.EMPTY, lines, x, y, TextRenderer.getFontRenderer(), backgroundColor, borderColorStart, borderColorEnd);
        MinecraftForge.EVENT_BUS.post(colorEvent);
        backgroundColor = colorEvent.getBackground();
        borderColorStart = colorEvent.getBorderStart();
        borderColorEnd = colorEvent.getBorderEnd();
        drawGradientRect(z, x - 3, y - 4, x + textWidth + 3, y - 3, backgroundColor, backgroundColor);
        drawGradientRect(z, x - 3, y + height + 3, x + textWidth + 3, y + height + 4, backgroundColor, backgroundColor);
        drawGradientRect(z, x - 3, y - 3, x + textWidth + 3, y + height + 3, backgroundColor, backgroundColor);
        drawGradientRect(z, x - 4, y - 3, x - 3, y + height + 3, backgroundColor, backgroundColor);
        drawGradientRect(z, x + textWidth + 3, y - 3, x + textWidth + 4, y + height + 3, backgroundColor, backgroundColor);
        drawGradientRect(z, x - 3, y - 3 + 1, x - 3 + 1, y + height + 3 - 1, borderColorStart, borderColorEnd);
        drawGradientRect(z, x + textWidth + 2, y - 3 + 1, x + textWidth + 3, y + height + 3 - 1, borderColorStart, borderColorEnd);
        drawGradientRect(z, x - 3, y - 3, x + textWidth + 3, y - 3 + 1, borderColorStart, borderColorStart);
        drawGradientRect(z, x - 3, y + height + 2, x + textWidth + 3, y + height + 3, borderColorEnd, borderColorEnd);
    }
}