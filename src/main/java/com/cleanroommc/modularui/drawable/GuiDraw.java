package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.drawable.text.TextRenderer;
import com.cleanroommc.modularui.utils.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.*;
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

import org.lwjgl.opengl.GL11;

import java.util.List;

public class GuiDraw {

    public static final double PI2 = Math.PI * 2;
    public static final double PI_2 = Math.PI / 2;

    public static void drawRect(float x0, float y0, float w, float h, int color) {
        drawRect(x0, y0, w, h, color, color, color, color);
    }

    public static void drawHorizontalGradientRect(float x0, float y0, float w, float h, int colorLeft, int colorRight) {
        drawRect(x0, y0, w, h, colorLeft, colorRight, colorLeft, colorRight);
    }

    public static void drawVerticalGradientRect(float x0, float y0, float w, float h, int colorTop, int colorBottom) {
        drawRect(x0, y0, w, h, colorTop, colorTop, colorBottom, colorBottom);
    }

    public static void drawRect(float x0, float y0, float w, float h, int colorTL, int colorTR, int colorBL, int colorBR) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        float x1 = x0 + w, y1 = y0 + h;
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x0, y0, 0.0f).color(Color.getRed(colorTL), Color.getGreen(colorTL), Color.getBlue(colorTL), Color.getAlpha(colorTL)).endVertex();
        bufferbuilder.pos(x0, y1, 0.0f).color(Color.getRed(colorBL), Color.getGreen(colorBL), Color.getBlue(colorBL), Color.getAlpha(colorBL)).endVertex();
        bufferbuilder.pos(x1, y1, 0.0f).color(Color.getRed(colorBR), Color.getGreen(colorBR), Color.getBlue(colorBR), Color.getAlpha(colorBR)).endVertex();
        bufferbuilder.pos(x1, y0, 0.0f).color(Color.getRed(colorTR), Color.getGreen(colorTR), Color.getBlue(colorTR), Color.getAlpha(colorTR)).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawCircle(float x0, float y0, float diameter, int color, int segments) {
        drawEllipse(x0, y0, diameter, diameter, color, color, segments);
    }

    public static void drawCircle(float x0, float y0, float diameter, int centerColor, int outerColor, int segments) {
        drawEllipse(x0, y0, diameter, diameter, centerColor, outerColor, segments);
    }

    public static void drawEllipse(float x0, float y0, float w, float h, int color, int segments) {
        drawEllipse(x0, y0, w, h, color, color, segments);
    }

    public static void drawEllipse(float x0, float y0, float w, float h, int centerColor, int outerColor, int segments) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        float x_2 = x0 + w / 2f, y_2 = y0 + h / 2f;
        bufferbuilder.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        // start at center
        bufferbuilder.pos(x_2, y_2, 0.0f).color(Color.getRed(centerColor), Color.getGreen(centerColor), Color.getBlue(centerColor), Color.getAlpha(centerColor)).endVertex();
        int a = Color.getAlpha(outerColor), r = Color.getRed(outerColor), g = Color.getGreen(outerColor), b = Color.getBlue(outerColor);
        float incr = (float) (PI2 / segments);
        for (int i = 0; i <= segments; i++) {
            float angle = incr * i;
            float x = (float) (Math.sin(angle) * (w / 2) + x_2);
            float y = (float) (Math.cos(angle) * (h / 2) + y_2);
            bufferbuilder.pos(x, y, 0.0f).color(r, g, b, a).endVertex();
        }
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawRoundedRect(float x0, float y0, float w, float h, int color, int cornerRadius, int segments) {
        drawRoundedRect(x0, y0, w, h, color, color, color, color, cornerRadius, segments);
    }

    public static void drawVerticalGradientRoundedRect(float x0, float y0, float w, float h, int colorTop, int colorBottom, int cornerRadius, int segments) {
        drawRoundedRect(x0, y0, w, h, colorTop, colorTop, colorBottom, colorBottom, cornerRadius, segments);
    }

    public static void drawHorizontalGradientRoundedRect(float x0, float y0, float w, float h, int colorLeft, int colorRight, int cornerRadius, int segments) {
        drawRoundedRect(x0, y0, w, h, colorLeft, colorRight, colorLeft, colorRight, cornerRadius, segments);
    }

    public static void drawRoundedRect(float x0, float y0, float w, float h, int colorTL, int colorTR, int colorBL, int colorBR, int cornerRadius, int segments) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        float x1 = x0 + w, y1 = y0 + h;
        bufferbuilder.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        int color = Color.average(colorBL, colorBR, colorTR, colorTL);
        // start at center
        bufferbuilder.pos(x0 + w / 2f, y0 + h / 2f, 0.0f).color(Color.getRed(color), Color.getGreen(color), Color.getBlue(color), Color.getAlpha(color)).endVertex();
        // left side
        bufferbuilder.pos(x0, y0 + cornerRadius, 0.0f).color(Color.getRed(colorTL), Color.getGreen(colorTL), Color.getBlue(colorTL), Color.getAlpha(colorTL)).endVertex();
        bufferbuilder.pos(x0, y1 - cornerRadius, 0.0f).color(Color.getRed(colorBL), Color.getGreen(colorBL), Color.getBlue(colorBL), Color.getAlpha(colorBL)).endVertex();
        // bottom left corner
        for (int i = 1; i <= segments; i++) {
            float x = (float) (x0 + cornerRadius - Math.cos(PI_2 / segments * i) * cornerRadius);
            float y = (float) (y1 - cornerRadius + Math.sin(PI_2 / segments * i) * cornerRadius);
            bufferbuilder.pos(x, y, 0.0f).color(Color.getRed(colorBL), Color.getGreen(colorBL), Color.getBlue(colorBL), Color.getAlpha(colorBL)).endVertex();
        }
        // bottom side
        bufferbuilder.pos(x1 - cornerRadius, y1, 0.0f).color(Color.getRed(colorBR), Color.getGreen(colorBR), Color.getBlue(colorBR), Color.getAlpha(colorBR)).endVertex();
        // bottom right corner
        for (int i = 1; i <= segments; i++) {
            float x = (float) (x1 - cornerRadius + Math.sin(PI_2 / segments * i) * cornerRadius);
            float y = (float) (y1 - cornerRadius + Math.cos(PI_2 / segments * i) * cornerRadius);
            bufferbuilder.pos(x, y, 0.0f).color(Color.getRed(colorBR), Color.getGreen(colorBR), Color.getBlue(colorBR), Color.getAlpha(colorBR)).endVertex();
        }
        // right side
        bufferbuilder.pos(x1, y0 + cornerRadius, 0.0f).color(Color.getRed(colorTR), Color.getGreen(colorTR), Color.getBlue(colorTR), Color.getAlpha(colorTR)).endVertex();
        // top right corner
        for (int i = 1; i <= segments; i++) {
            float x = (float) (x1 - cornerRadius + Math.cos(PI_2 / segments * i) * cornerRadius);
            float y = (float) (y0 + cornerRadius - Math.sin(PI_2 / segments * i) * cornerRadius);
            bufferbuilder.pos(x, y, 0.0f).color(Color.getRed(colorTR), Color.getGreen(colorTR), Color.getBlue(colorTR), Color.getAlpha(colorTR)).endVertex();
        }
        // top side
        bufferbuilder.pos(x0 + cornerRadius, y0, 0.0f).color(Color.getRed(colorTL), Color.getGreen(colorTL), Color.getBlue(colorTL), Color.getAlpha(colorTL)).endVertex();
        // top left corner
        for (int i = 1; i <= segments; i++) {
            float x = (float) (x0 + cornerRadius - Math.sin(PI_2 / segments * i) * cornerRadius);
            float y = (float) (y0 + cornerRadius - Math.cos(PI_2 / segments * i) * cornerRadius);
            bufferbuilder.pos(x, y, 0.0f).color(Color.getRed(colorTL), Color.getGreen(colorTL), Color.getBlue(colorTL), Color.getAlpha(colorTL)).endVertex();
        }
        bufferbuilder.pos(x0, y0 + cornerRadius, 0.0f).color(Color.getRed(colorTL), Color.getGreen(colorTL), Color.getBlue(colorTL), Color.getAlpha(colorTL)).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawTexture(ResourceLocation location, float x, float y, float w, float h, int u, int v, int textureWidth, int textureHeight) {
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
        Minecraft.getMinecraft().renderEngine.bindTexture(location);
        drawTexture(x, y, u, v, w, h, textureWidth, textureHeight);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
    }

    public static void drawTexture(float x, float y, int u, int v, float w, float h, int textureW, int textureH) {
        drawTexture(x, y, u, v, w, h, textureW, textureH, 0);
    }

    /**
     * Draw a textured quad with given UV, dimensions and custom texture size
     */
    public static void drawTexture(float x, float y, int u, int v, float w, float h, int textureW, int textureH, float z) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        drawTexture(buffer, x, y, u, v, w, h, textureW, textureH, z);
        tessellator.draw();
    }

    public static void drawTexture(BufferBuilder buffer, float x, float y, int u, int v, float w, float h, int textureW, int textureH, float z) {
        float tw = 1F / textureW;
        float th = 1F / textureH;

        buffer.pos(x, y + h, z).tex(u * tw, (v + h) * th).endVertex();
        buffer.pos(x + w, y + h, z).tex((u + w) * tw, (v + h) * th).endVertex();
        buffer.pos(x + w, y, z).tex((u + w) * tw, v * th).endVertex();
        buffer.pos(x, y, z).tex(u * tw, v * th).endVertex();
    }

    public static void drawTexture(float x, float y, int u, int v, float w, float h, int textureW, int textureH, int tu, int tv) {
        drawTexture(x, y, u, v, w, h, textureW, textureH, tu, tv, 0);
    }

    /**
     * Draw a textured quad with given UV, dimensions and custom texture size
     */
    public static void drawTexture(float x, float y, int u, int v, float w, float h, int textureW, int textureH, int tu, int tv, float z) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        drawTexture(buffer, x, y, u, v, w, h, textureW, textureH, tu, tv, z);
        tessellator.draw();
    }

    public static void drawTexture(BufferBuilder buffer, float x, float y, int u, int v, float w, float h, int textureW, int textureH, int tu, int tv, float z) {
        float tw = 1F / textureW;
        float th = 1F / textureH;

        buffer.pos(x, y + h, z).tex(u * tw, tv * th).endVertex();
        buffer.pos(x + w, y + h, z).tex(tu * tw, tv * th).endVertex();
        buffer.pos(x + w, y, z).tex(tu * tw, v * th).endVertex();
        buffer.pos(x, y, z).tex(u * tw, v * th).endVertex();
    }

    public static void drawTexture(ResourceLocation location, float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1) {
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
        Minecraft.getMinecraft().renderEngine.bindTexture(location);
        drawTexture(x0, y0, x1, y1, u0, v0, u1, v1, 0);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
    }

    public static void drawTexture(float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1) {
        drawTexture(x0, y0, x1, y1, u0, v0, u1, v1, 0);
    }

    public static void drawTexture(float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1, float z) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        drawTexture(buffer, x0, y0, x1, y1, u0, v0, u1, v1, z);
        tessellator.draw();
    }

    public static void drawTexture(BufferBuilder buffer, float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1, float z) {
        buffer.pos(x0, y1, z).tex(u0, v1).endVertex();
        buffer.pos(x1, y1, z).tex(u1, v1).endVertex();
        buffer.pos(x1, y0, z).tex(u1, v0).endVertex();
        buffer.pos(x0, y0, z).tex(u0, v0).endVertex();
    }

    public static void drawTiledTexture(ResourceLocation location, float x, float y, float w, float h, int u, int v, int tileW, int tileH, int tw, int th, float z) {
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
        Minecraft.getMinecraft().renderEngine.bindTexture(location);
        drawTiledTexture(x, y, w, h, u, v, tileW, tileH, tw, th, z);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
    }

    public static void drawTiledTexture(float x, float y, float w, float h, int u, int v, int tileW, int tileH, int tw, int th, float z) {
        int countX = (((int) w - 1) / tileW) + 1;
        int countY = (((int) h - 1) / tileH) + 1;
        float fillerX = w - (countX - 1) * tileW;
        float fillerY = h - (countY - 1) * tileH;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        for (int i = 0, c = countX * countY; i < c; i++) {
            int ix = i % countX;
            int iy = i / countX;
            float xx = x + ix * tileW;
            float yy = y + iy * tileH;
            float xw = ix == countX - 1 ? fillerX : tileW;
            float yh = iy == countY - 1 ? fillerY : tileH;

            drawTexture(buffer, xx, yy, u, v, xw, yh, tw, th, z);
        }

        tessellator.draw();
    }

    public static void drawTiledTexture(ResourceLocation location, float x, float y, float w, float h, float u0, float v0, float u1, float v1, int textureWidth, int textureHeight, float z) {
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
        Minecraft.getMinecraft().renderEngine.bindTexture(location);
        drawTiledTexture(x, y, w, h, u0, v0, u1, v1, textureWidth, textureHeight, z);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
    }

    public static void drawTiledTexture(float x, float y, float w, float h, float u0, float v0, float u1, float v1, int tileWidth, int tileHeight, float z) {
        int countX = (((int) w - 1) / tileWidth) + 1;
        int countY = (((int) h - 1) / tileHeight) + 1;
        float fillerX = w - (countX - 1) * tileWidth;
        float fillerY = h - (countY - 1) * tileHeight;
        float fillerU = u0 + (u1 - u0) * fillerX / tileWidth;
        float fillerV = v0 + (v1 - v0) * fillerY / tileHeight;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        for (int i = 0, c = countX * countY; i < c; i++) {
            int ix = i % countX;
            int iy = i / countX;
            float xx = x + ix * tileWidth;
            float yy = y + iy * tileHeight;
            float xw = tileWidth, yh = tileHeight, uEnd = u1, vEnd = v1;
            if (ix == countX - 1) {
                xw = fillerX;
                uEnd = fillerU;
            }
            if (iy == countY - 1) {
                yh = fillerY;
                vEnd = fillerV;
            }

            drawTexture(buffer, xx, yy, xx + xw, yy + yh, u0, v0, uEnd, vEnd, z);
        }

        tessellator.draw();
    }

    public static void drawItem(ItemStack item, int x, int y, float width, float height) {
        if (item.isEmpty()) return;
        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(width / 16f, height / 16f, 1);
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        renderItem.zLevel = 200;
        renderItem.renderItemAndEffectIntoGUI(Minecraft.getMinecraft().player, item, 0, 0);
        renderItem.zLevel = 0;
        GlStateManager.disableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();
    }

    public static void drawFluidTexture(FluidStack content, float x0, float y0, float width, float height, float z) {
        if (content == null) {
            return;
        }
        Fluid fluid = content.getFluid();
        ResourceLocation fluidStill = fluid.getStill(content);
        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluidStill.toString());
        int fluidColor = fluid.getColor(content);
        GlStateManager.color(Color.getRedF(fluidColor), Color.getGreenF(fluidColor), Color.getBlueF(fluidColor), Color.getAlphaF(fluidColor));
        drawTiledTexture(TextureMap.LOCATION_BLOCKS_TEXTURE, x0, y0, width, height, sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV(), sprite.getIconWidth(), sprite.getIconHeight(), z);
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    public static void drawSprite(TextureAtlasSprite sprite, float x0, float y0, float w, float h) {
        drawSprite(Minecraft.getMinecraft().getTextureMapBlocks(), sprite, x0, y0, w, h);
    }

    public static void drawSprite(TextureMap textureMap, TextureAtlasSprite sprite, float x0, float y0, float w, float h) {
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(textureMap.getGlTextureId());
        drawTexture(x0, y0, x0 + w, y0 + h, sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV());
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
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

    /**
     * Draws a rectangular shadow
     *
     * @param x      left of solid shadow part
     * @param y      top of solid shadow part
     * @param w      width of solid shadow part
     * @param h      height of solid shadow part
     * @param oX     shadow gradient size in x
     * @param oY     shadow gradient size in y
     * @param opaque solid shadow color
     * @param shadow gradient end color
     */
    public static void drawDropShadow(int x, int y, int w, int h, int oX, int oY, int opaque, int shadow) {

        float a1 = Color.getAlphaF(opaque);
        float r1 = Color.getRedF(opaque);
        float g1 = Color.getGreenF(opaque);
        float b1 = Color.getBlueF(opaque);
        float a2 = Color.getAlphaF(shadow);
        float r2 = Color.getRedF(shadow);
        float g2 = Color.getGreenF(shadow);
        float b2 = Color.getBlueF(shadow);

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        float x1 = x + w, y1 = y + h;

        /* Draw opaque part */
        buffer.pos(x1, y, 0).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x, y, 0).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x, y1, 0).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x1, y1, 0).color(r1, g1, b1, a1).endVertex();

        /* Draw top shadow */
        buffer.pos(x1 + oX, y - oY, 0).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x - oX, y - oY, 0).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x, y, 0).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x1, y, 0).color(r1, g1, b1, a1).endVertex();

        /* Draw bottom shadow */
        buffer.pos(x1, y1, 0).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x, y1, 0).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x - oX, y1 + oY, 0).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x1 + oX, y1 + oY, 0).color(r2, g2, b2, a2).endVertex();

        /* Draw left shadow */
        buffer.pos(x, y, 0).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x - oX, y - oY, 0).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x - oX, y1 + oY, 0).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x, y1, 0).color(r1, g1, b1, a1).endVertex();

        /* Draw right shadow */
        buffer.pos(x1 + oX, y - oY, 0).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x1, y, 0).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x1, y1, 0).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x1 + oX, y1 + oY, 0).color(r2, g2, b2, a2).endVertex();

        tessellator.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawDropCircleShadow(int x, int y, int radius, int segments, int opaque, int shadow) {
        float a1 = Color.getAlphaF(opaque);
        float r1 = Color.getRedF(opaque);
        float g1 = Color.getGreenF(opaque);
        float b1 = Color.getBlueF(opaque);
        float a2 = Color.getAlphaF(shadow);
        float r2 = Color.getRedF(shadow);
        float g2 = Color.getGreenF(shadow);
        float b2 = Color.getBlueF(shadow);

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

        float a1 = Color.getAlphaF(opaque);
        float r1 = Color.getRedF(opaque);
        float g1 = Color.getGreenF(opaque);
        float b1 = Color.getBlueF(opaque);
        float a2 = Color.getAlphaF(shadow);
        float r2 = Color.getRedF(shadow);
        float g2 = Color.getGreenF(shadow);
        float b2 = Color.getBlueF(shadow);

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

    @SideOnly(Side.CLIENT)
    public static void drawBorder(float x, float y, float width, float height, int color, float border) {
        drawRect(x - border, y - border, width + 2 * border, border, color);
        drawRect(x - border, y + height, width + 2 * border, border, color);
        drawRect(x - border, y, border, height, color);
        drawRect(x + width, y, border, height, color);
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

    public static void drawTooltipBackground(ItemStack stack, List<String> lines, int x, int y, int textWidth, int height) {
        // TODO theme color
        int backgroundColor = 0xF0100010;
        int borderColorStart = 0x505000FF;
        int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
        RenderTooltipEvent.Color colorEvent = new RenderTooltipEvent.Color(stack, lines, x, y, TextRenderer.getFontRenderer(), backgroundColor, borderColorStart, borderColorEnd);
        MinecraftForge.EVENT_BUS.post(colorEvent);
        backgroundColor = colorEvent.getBackground();
        borderColorStart = colorEvent.getBorderStart();
        borderColorEnd = colorEvent.getBorderEnd();

        // top background border
        drawVerticalGradientRect(x - 3, y - 4, textWidth + 6, 1, backgroundColor, backgroundColor);
        // bottom background border
        drawVerticalGradientRect(x - 3, y + height + 3, textWidth + 6, 1, backgroundColor, backgroundColor);
        // center background
        drawVerticalGradientRect(x - 3, y - 3, textWidth + 6, height + 6, backgroundColor, backgroundColor);
        // left background border
        drawVerticalGradientRect(x - 4, y - 3, 1, height + 6, backgroundColor, backgroundColor);
        // right background border
        drawVerticalGradientRect(x + textWidth + 3, y - 3, 1, height + 6, backgroundColor, backgroundColor);

        // left accent border
        drawVerticalGradientRect(x - 3, y - 2, 1, height + 4, borderColorStart, borderColorEnd);
        // right accent border
        drawVerticalGradientRect(x + textWidth + 2, y - 2, 1, height + 4, borderColorStart, borderColorEnd);
        // top accent border
        drawVerticalGradientRect(x - 3, y - 3, textWidth + 6, 1, borderColorStart, borderColorStart);
        // bottom accent border
        drawVerticalGradientRect(x - 3, y + height + 2, textWidth + 6, 1, borderColorEnd, borderColorEnd);
    }
}