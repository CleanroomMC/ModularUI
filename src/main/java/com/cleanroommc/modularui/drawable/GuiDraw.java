package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.drawable.text.TextRenderer;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.RichTooltipEvent;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.NumberFormat;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.widget.sizer.Area;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class GuiDraw {

    private static final TextRenderer textRenderer = new TextRenderer();

    public static final double PI2 = Math.PI * 2;
    public static final double PI_2 = Math.PI / 2;

    public static void drawRect(float x0, float y0, float w, float h, int color) {
        Platform.setupDrawColor();
        Platform.startDrawing(Platform.DrawMode.QUADS, Platform.VertexFormat.POS_COLOR, bufferBuilder -> {
            drawRectRaw(bufferBuilder, x0, y0, x0 + w, y0 + h, color);
        });
    }

    public static void drawHorizontalGradientRect(float x0, float y0, float w, float h, int colorLeft, int colorRight) {
        drawRect(x0, y0, w, h, colorLeft, colorRight, colorLeft, colorRight);
    }

    public static void drawVerticalGradientRect(float x0, float y0, float w, float h, int colorTop, int colorBottom) {
        drawRect(x0, y0, w, h, colorTop, colorTop, colorBottom, colorBottom);
    }

    public static void drawRect(float x0, float y0, float w, float h, int colorTL, int colorTR, int colorBL, int colorBR) {
        Platform.setupDrawColor();
        Platform.setupDrawGradient();
        float x1 = x0 + w, y1 = y0 + h;
        Platform.startDrawing(Platform.DrawMode.QUADS, Platform.VertexFormat.POS_COLOR, bufferBuilder -> {
            bufferBuilder.pos(x0, y0, 0.0f).color(Color.getRed(colorTL), Color.getGreen(colorTL), Color.getBlue(colorTL), Color.getAlpha(colorTL)).endVertex();
            bufferBuilder.pos(x0, y1, 0.0f).color(Color.getRed(colorBL), Color.getGreen(colorBL), Color.getBlue(colorBL), Color.getAlpha(colorBL)).endVertex();
            bufferBuilder.pos(x1, y1, 0.0f).color(Color.getRed(colorBR), Color.getGreen(colorBR), Color.getBlue(colorBR), Color.getAlpha(colorBR)).endVertex();
            bufferBuilder.pos(x1, y0, 0.0f).color(Color.getRed(colorTR), Color.getGreen(colorTR), Color.getBlue(colorTR), Color.getAlpha(colorTR)).endVertex();
        });
        Platform.endDrawGradient();
    }

    public static void drawRectRaw(BufferBuilder buffer, float x0, float y0, float x1, float y1, int color) {
        int r = Color.getRed(color);
        int g = Color.getGreen(color);
        int b = Color.getBlue(color);
        int a = Color.getAlpha(color);
        drawRectRaw(buffer, x0, y0, x1, y1, r, g, b, a);
    }

    public static void drawRectRaw(BufferBuilder buffer, float x0, float y0, float x1, float y1, int r, int g, int b, int a) {
        buffer.pos(x0, y0, 0.0f).color(r, g, b, a).endVertex();
        buffer.pos(x0, y1, 0.0f).color(r, g, b, a).endVertex();
        buffer.pos(x1, y1, 0.0f).color(r, g, b, a).endVertex();
        buffer.pos(x1, y0, 0.0f).color(r, g, b, a).endVertex();
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
        Platform.setupDrawColor();
        Platform.setupDrawGradient();
        float x_2 = x0 + w / 2f, y_2 = y0 + h / 2f;
        Platform.startDrawing(Platform.DrawMode.TRIANGLE_FAN, Platform.VertexFormat.POS_COLOR, bufferBuilder -> {
            // start at center
            bufferBuilder.pos(x_2, y_2, 0.0f).color(Color.getRed(centerColor), Color.getGreen(centerColor), Color.getBlue(centerColor), Color.getAlpha(centerColor)).endVertex();
            int a = Color.getAlpha(outerColor), r = Color.getRed(outerColor), g = Color.getGreen(outerColor), b = Color.getBlue(outerColor);
            float incr = (float) (PI2 / segments);
            for (int i = 0; i <= segments; i++) {
                float angle = incr * i;
                float x = (float) (Math.sin(angle) * (w / 2) + x_2);
                float y = (float) (Math.cos(angle) * (h / 2) + y_2);
                bufferBuilder.pos(x, y, 0.0f).color(r, g, b, a).endVertex();
            }
        });
        Platform.endDrawGradient();
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
        Platform.setupDrawColor();
        Platform.setupDrawGradient();
        float x1 = x0 + w, y1 = y0 + h;
        Platform.startDrawing(Platform.DrawMode.TRIANGLE_FAN, Platform.VertexFormat.POS_COLOR, bufferBuilder -> {
            int color = Color.average(colorBL, colorBR, colorTR, colorTL);
            // start at center
            bufferBuilder.pos(x0 + w / 2f, y0 + h / 2f, 0.0f).color(Color.getRed(color), Color.getGreen(color), Color.getBlue(color), Color.getAlpha(color)).endVertex();
            // left side
            bufferBuilder.pos(x0, y0 + cornerRadius, 0.0f).color(Color.getRed(colorTL), Color.getGreen(colorTL), Color.getBlue(colorTL), Color.getAlpha(colorTL)).endVertex();
            bufferBuilder.pos(x0, y1 - cornerRadius, 0.0f).color(Color.getRed(colorBL), Color.getGreen(colorBL), Color.getBlue(colorBL), Color.getAlpha(colorBL)).endVertex();
            // bottom left corner
            for (int i = 1; i <= segments; i++) {
                float x = (float) (x0 + cornerRadius - Math.cos(PI_2 / segments * i) * cornerRadius);
                float y = (float) (y1 - cornerRadius + Math.sin(PI_2 / segments * i) * cornerRadius);
                bufferBuilder.pos(x, y, 0.0f).color(Color.getRed(colorBL), Color.getGreen(colorBL), Color.getBlue(colorBL), Color.getAlpha(colorBL)).endVertex();
            }
            // bottom side
            bufferBuilder.pos(x1 - cornerRadius, y1, 0.0f).color(Color.getRed(colorBR), Color.getGreen(colorBR), Color.getBlue(colorBR), Color.getAlpha(colorBR)).endVertex();
            // bottom right corner
            for (int i = 1; i <= segments; i++) {
                float x = (float) (x1 - cornerRadius + Math.sin(PI_2 / segments * i) * cornerRadius);
                float y = (float) (y1 - cornerRadius + Math.cos(PI_2 / segments * i) * cornerRadius);
                bufferBuilder.pos(x, y, 0.0f).color(Color.getRed(colorBR), Color.getGreen(colorBR), Color.getBlue(colorBR), Color.getAlpha(colorBR)).endVertex();
            }
            // right side
            bufferBuilder.pos(x1, y0 + cornerRadius, 0.0f).color(Color.getRed(colorTR), Color.getGreen(colorTR), Color.getBlue(colorTR), Color.getAlpha(colorTR)).endVertex();
            // top right corner
            for (int i = 1; i <= segments; i++) {
                float x = (float) (x1 - cornerRadius + Math.cos(PI_2 / segments * i) * cornerRadius);
                float y = (float) (y0 + cornerRadius - Math.sin(PI_2 / segments * i) * cornerRadius);
                bufferBuilder.pos(x, y, 0.0f).color(Color.getRed(colorTR), Color.getGreen(colorTR), Color.getBlue(colorTR), Color.getAlpha(colorTR)).endVertex();
            }
            // top side
            bufferBuilder.pos(x0 + cornerRadius, y0, 0.0f).color(Color.getRed(colorTL), Color.getGreen(colorTL), Color.getBlue(colorTL), Color.getAlpha(colorTL)).endVertex();
            // top left corner
            for (int i = 1; i <= segments; i++) {
                float x = (float) (x0 + cornerRadius - Math.sin(PI_2 / segments * i) * cornerRadius);
                float y = (float) (y0 + cornerRadius - Math.cos(PI_2 / segments * i) * cornerRadius);
                bufferBuilder.pos(x, y, 0.0f).color(Color.getRed(colorTL), Color.getGreen(colorTL), Color.getBlue(colorTL), Color.getAlpha(colorTL)).endVertex();
            }
            bufferBuilder.pos(x0, y0 + cornerRadius, 0.0f).color(Color.getRed(colorTL), Color.getGreen(colorTL), Color.getBlue(colorTL), Color.getAlpha(colorTL)).endVertex();
        });
        Platform.endDrawGradient();
    }

    public static void drawTexture(ResourceLocation location, float x, float y, float w, float h, int u, int v, int textureWidth, int textureHeight) {
        Platform.setupDrawTex(location);
        drawTexture(x, y, u, v, w, h, textureWidth, textureHeight);
    }

    public static void drawTexture(float x, float y, int u, int v, float w, float h, int textureW, int textureH) {
        drawTexture(x, y, u, v, w, h, textureW, textureH, 0);
    }

    /**
     * Draw a textured quad with given UV, dimensions and custom texture size
     */
    public static void drawTexture(float x, float y, int u, int v, float w, float h, int textureW, int textureH, float z) {
        Platform.startDrawing(Platform.DrawMode.QUADS, Platform.VertexFormat.POS_TEX, bufferBuilder -> {
            drawTexture(bufferBuilder, x, y, u, v, w, h, textureW, textureH, z);
        });
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
        Platform.startDrawing(Platform.DrawMode.QUADS, Platform.VertexFormat.POS_TEX, bufferBuilder -> {
            drawTexture(bufferBuilder, x, y, u, v, w, h, textureW, textureH, tu, tv, z);
        });
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
        drawTexture(location, x0, y0, x1, y1, u0, v0, u1, v1, false);
    }

    public static void drawTexture(ResourceLocation location, float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1, boolean withBlend) {
        Platform.setupDrawTex(location, withBlend);
        drawTexture(x0, y0, x1, y1, u0, v0, u1, v1, 0);
    }

    public static void drawTexture(float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1) {
        drawTexture(x0, y0, x1, y1, u0, v0, u1, v1, 0);
    }

    public static void drawTexture(float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1, float z) {
        Platform.startDrawing(Platform.DrawMode.QUADS, Platform.VertexFormat.POS_TEX, bufferBuilder -> {
            drawTexture(bufferBuilder, x0, y0, x1, y1, u0, v0, u1, v1, z);
        });
    }

    public static void drawTexture(BufferBuilder buffer, float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1, float z) {
        buffer.pos(x0, y1, z).tex(u0, v1).endVertex();
        buffer.pos(x1, y1, z).tex(u1, v1).endVertex();
        buffer.pos(x1, y0, z).tex(u1, v0).endVertex();
        buffer.pos(x0, y0, z).tex(u0, v0).endVertex();
    }

    public static void drawTiledTexture(ResourceLocation location, float x, float y, float w, float h, int u, int v, int tileW, int tileH, int tw, int th, float z) {
        Platform.setupDrawTex(location);
        drawTiledTexture(x, y, w, h, u, v, tileW, tileH, tw, th, z);
    }

    public static void drawTiledTexture(float x, float y, float w, float h, int u, int v, int tileW, int tileH, int textureW, int textureH, float z) {
        float tw = 1f / textureW;
        float th = 1f / textureH;
        drawTiledTexture(x, y, w, h, u * tw, v * th, (u + w) * tw, (v + h) * th, textureW, textureH, z);
    }

    public static void drawTiledTexture(ResourceLocation location, float x, float y, float w, float h, float u0, float v0, float u1, float v1, int textureWidth, int textureHeight, float z) {
        Platform.setupDrawTex(location);
        drawTiledTexture(x, y, w, h, u0, v0, u1, v1, textureWidth, textureHeight, z);
    }

    public static void drawTiledTexture(float x, float y, float w, float h, float u0, float v0, float u1, float v1, int tileWidth, int tileHeight, float z) {
        Platform.startDrawing(Platform.DrawMode.QUADS, Platform.VertexFormat.POS_TEX, bufferBuilder -> {
            drawTiledTexture(bufferBuilder, x, y, w, h, u0, v0, u1, v1, tileWidth, tileHeight, z);
        });
    }

    public static void drawTiledTexture(BufferBuilder bufferBuilder, float x, float y, float w, float h, float u0, float v0, float u1, float v1, int tileWidth, int tileHeight, float z) {
        int countX = (((int) w - 1) / tileWidth) + 1;
        int countY = (((int) h - 1) / tileHeight) + 1;
        float fillerX = w - (countX - 1) * tileWidth;
        float fillerY = h - (countY - 1) * tileHeight;
        float fillerU = u0 + (u1 - u0) * fillerX / tileWidth;
        float fillerV = v0 + (v1 - v0) * fillerY / tileHeight;

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

            drawTexture(bufferBuilder, xx, yy, xx + xw, yy + yh, u0, v0, uEnd, vEnd, z);
        }
    }

    public static void drawItem(ItemStack item, int x, int y, float width, float height, int z) {
        if (item.isEmpty()) return;
        GlStateManager.pushMatrix();
        Platform.setupDrawItem();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(width / 16f, height / 16f, 1);
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        renderItem.zLevel = z + 100;
        renderItem.renderItemAndEffectIntoGUI(Platform.getClientPlayer(), item, 0, 0);
        renderItem.zLevel = 0;
        Platform.endDrawItem();
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

    public static void drawStandardSlotAmountText(int amount, String format, Area area) {
        if (amount != 1 || format != null) {
            drawAmountText(amount, format, 0, 0, area.width, area.height, Alignment.BottomRight);
        }
    }

    public static void drawAmountText(int amount, String format, int x, int y, int width, int height, Alignment alignment) {
        String s = NumberFormat.AMOUNT_TEXT.format(amount);
        if (format != null) s = format + s;
        drawScaledAlignedTextInBox(s, x, y, width, height, alignment);
    }

    public static void drawScaledAlignedTextInBox(String amountText, int x, int y, int width, int height, Alignment alignment) {
        drawScaledAlignedTextInBox(amountText, x, y, width, height, alignment, 1f);
    }

    public static void drawScaledAlignedTextInBox(String amountText, int x, int y, int width, int height, Alignment alignment, float maxScale) {
        if (amountText == null || amountText.isEmpty()) return;
        // setup text renderer
        textRenderer.setShadow(true);
        textRenderer.setScale(1f);
        textRenderer.setColor(Color.WHITE.main);
        textRenderer.setAlignment(alignment, width, height);
        textRenderer.setPos(x, y);
        textRenderer.setHardWrapOnBorder(false);
        if (amountText.length() > 2 && width > 16) { // we know that numbers below 100 will always fit in standard slots
            // simulate and calculate scale with width
            textRenderer.setSimulate(true);
            textRenderer.draw(amountText);
            textRenderer.setSimulate(false);
            textRenderer.setScale(Math.min(maxScale, width / textRenderer.getLastActualWidth()));
        }
        textRenderer.draw(amountText);
        textRenderer.setHardWrapOnBorder(true);
    }

    public static void drawSprite(TextureAtlasSprite sprite, float x0, float y0, float w, float h) {
        drawSprite(Minecraft.getMinecraft().getTextureMapBlocks(), sprite, x0, y0, w, h);
    }

    public static void drawSprite(TextureMap textureMap, TextureAtlasSprite sprite, float x0, float y0, float w, float h) {
        Platform.setupDrawTex(textureMap.getGlTextureId());
        drawTexture(x0, y0, x0 + w, y0 + h, sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV());
    }

    public static void drawTiledSprite(TextureAtlasSprite sprite, float x0, float y0, float w, float h) {
        drawTiledSprite(Minecraft.getMinecraft().getTextureMapBlocks(), sprite, x0, y0, w, h);
    }

    public static void drawTiledSprite(TextureAtlasSprite sprite, float x0, float y0, float w, float h, int tileWidth, int tileHeight) {
        drawTiledSprite(Minecraft.getMinecraft().getTextureMapBlocks(), sprite, x0, y0, w, h, tileWidth, tileHeight);
    }

    public static void drawTiledSprite(TextureMap textureMap, TextureAtlasSprite sprite, float x0, float y0, float w, float h) {
        drawTiledSprite(textureMap, sprite, x0, y0, w, h, sprite.getIconWidth(), sprite.getIconHeight());
    }

    public static void drawTiledSprite(TextureMap textureMap, TextureAtlasSprite sprite, float x0, float y0, float w, float h, int tileWidth, int tileHeight) {
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(textureMap.getGlTextureId());
        drawTiledTexture(x0, y0, x0 + w, y0 + h, sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV(), tileWidth, tileHeight, 0);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
    }

    /**
     * @deprecated no replacement
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public static void drawOutlineCenter(int x, int y, int offset, int color) {
        drawOutlineCenter(x, y, offset, color, 1);
    }

    /**
     * @deprecated no replacement
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public static void drawOutlineCenter(int x, int y, int offset, int color, int border) {
        drawOutline(x - offset, y - offset, x + offset, y + offset, color, border);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public static void drawOutline(int left, int top, int right, int bottom, int color) {
        drawOutline(left, top, right, bottom, color, 1);
    }

    /**
     * Draw rectangle outline with given border
     *
     * @deprecated use {@link #drawBorderInsideLTRB(float, float, float, float, float, int)} or {@link #drawBorderInsideLTRB(float, float, float, float, float, int)}
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public static void drawOutline(int left, int top, int right, int bottom, int color, int border) {
        drawBorderInsideLTRB(left, top, right, bottom, border, color);
    }

    private static void drawBorderLTRB(float left, float top, float right, float bottom, float border, int color, boolean outside) {
        if (outside) {
            left -= border;
            top -= border;
            right += border;
            bottom += border;
        }
        float x0 = left, y0 = top, x1 = right, y1 = bottom, d = border;
        Platform.setupDrawColor();
        Platform.startDrawing(Platform.DrawMode.TRIANGLE_STRIP, Platform.VertexFormat.POS_COLOR, buffer -> {
            pc(buffer, x0, y0, color);
            pc(buffer, x1 - d, y0 + d, color);
            pc(buffer, x1, y0, color);
            pc(buffer, x1 - d, y1 - d, color);
            pc(buffer, x1, y1, color);
            pc(buffer, x0 + d, y1 - d, color);
            pc(buffer, x0, y1, color);
            pc(buffer, x0 + d, y0 + d, color);
            pc(buffer, x0, y0, color);
            pc(buffer, x1 - d, y0 + d, color);
        });
    }

    public static void drawBorderOutsideLTRB(float left, float top, float right, float bottom, int color) {
        drawBorderLTRB(left, top, right, bottom, 1, color, true);
    }

    public static void drawBorderOutsideLTRB(float left, float top, float right, float bottom, float border, int color) {
        drawBorderLTRB(left, top, right, bottom, border, color, true);
    }

    public static void drawBorderInsideLTRB(float left, float top, float right, float bottom, int color) {
        drawBorderLTRB(left, top, right, bottom, 1, color, false);
    }

    public static void drawBorderInsideLTRB(float left, float top, float right, float bottom, float border, int color) {
        drawBorderLTRB(left, top, right, bottom, border, color, false);
    }

    private static void drawBorderXYWH(float x, float y, float w, float h, float border, int color, boolean outside) {
        drawBorderLTRB(x, y, x + w, y + h, border, color, outside);
    }

    public static void drawBorderOutsideXYWH(float x, float y, float w, float h, float border, int color) {
        drawBorderXYWH(x, y, w, h, border, color, true);
    }

    public static void drawBorderOutsideXYWH(float x, float y, float w, float h, int color) {
        drawBorderXYWH(x, y, w, h, 1, color, true);
    }

    public static void drawBorderInsideXYWH(float x, float y, float w, float h, float border, int color) {
        drawBorderXYWH(x, y, w, h, border, color, false);
    }

    public static void drawBorderInsideXYWH(float x, float y, float w, float h, int color) {
        drawBorderXYWH(x, y, w, h, 1, color, false);
    }

    private static void pc(BufferBuilder buffer, float x, float y, int c) {
        buffer.pos(x, y, 0).color(Color.getRed(c), Color.getGreen(c), Color.getBlue(c), Color.getAlpha(c)).endVertex();
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

        Platform.setupDrawColor();
        Platform.setupDrawGradient();
        Platform.startDrawing(Platform.DrawMode.QUADS, Platform.VertexFormat.POS_COLOR, bufferBuilder -> {
            float x1 = x + w, y1 = y + h;

            /* Draw opaque part */
            bufferBuilder.pos(x1, y, 0).color(r1, g1, b1, a1).endVertex();
            bufferBuilder.pos(x, y, 0).color(r1, g1, b1, a1).endVertex();
            bufferBuilder.pos(x, y1, 0).color(r1, g1, b1, a1).endVertex();
            bufferBuilder.pos(x1, y1, 0).color(r1, g1, b1, a1).endVertex();

            /* Draw top shadow */
            bufferBuilder.pos(x1 + oX, y - oY, 0).color(r2, g2, b2, a2).endVertex();
            bufferBuilder.pos(x - oX, y - oY, 0).color(r2, g2, b2, a2).endVertex();
            bufferBuilder.pos(x, y, 0).color(r1, g1, b1, a1).endVertex();
            bufferBuilder.pos(x1, y, 0).color(r1, g1, b1, a1).endVertex();

            /* Draw bottom shadow */
            bufferBuilder.pos(x1, y1, 0).color(r1, g1, b1, a1).endVertex();
            bufferBuilder.pos(x, y1, 0).color(r1, g1, b1, a1).endVertex();
            bufferBuilder.pos(x - oX, y1 + oY, 0).color(r2, g2, b2, a2).endVertex();
            bufferBuilder.pos(x1 + oX, y1 + oY, 0).color(r2, g2, b2, a2).endVertex();

            /* Draw left shadow */
            bufferBuilder.pos(x, y, 0).color(r1, g1, b1, a1).endVertex();
            bufferBuilder.pos(x - oX, y - oY, 0).color(r2, g2, b2, a2).endVertex();
            bufferBuilder.pos(x - oX, y1 + oY, 0).color(r2, g2, b2, a2).endVertex();
            bufferBuilder.pos(x, y1, 0).color(r1, g1, b1, a1).endVertex();

            /* Draw right shadow */
            bufferBuilder.pos(x1 + oX, y - oY, 0).color(r2, g2, b2, a2).endVertex();
            bufferBuilder.pos(x1, y, 0).color(r1, g1, b1, a1).endVertex();
            bufferBuilder.pos(x1, y1, 0).color(r1, g1, b1, a1).endVertex();
            bufferBuilder.pos(x1 + oX, y1 + oY, 0).color(r2, g2, b2, a2).endVertex();
        });
        Platform.endDrawGradient();
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

        Platform.setupDrawColor();
        Platform.setupDrawGradient();
        Platform.startDrawing(Platform.DrawMode.TRIANGLE_FAN, Platform.VertexFormat.POS_COLOR, bufferBuilder -> {
            bufferBuilder.pos(x, y, 0).color(r1, g1, b1, a1).endVertex();

            for (int i = 0; i <= segments; i++) {
                double a = i / (double) segments * Math.PI * 2 - Math.PI / 2;

                bufferBuilder.pos(x - Math.cos(a) * radius, y + Math.sin(a) * radius, 0).color(r2, g2, b2, a2).endVertex();
            }
        });
        Platform.endDrawGradient();
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

        Platform.setupDrawColor();
        Platform.setupDrawGradient();
        Platform.startDrawing(Platform.DrawMode.TRIANGLE_FAN, Platform.VertexFormat.POS_COLOR, bufferBuilder -> {
            bufferBuilder.pos(x, y, 0).color(r1, g1, b1, a1).endVertex();
            for (int i = 0; i <= segments; i++) {
                double a = i / (double) segments * Math.PI * 2 - Math.PI / 2;
                bufferBuilder.pos(x - Math.cos(a) * offset, y + Math.sin(a) * offset, 0).color(r1, g1, b1, a1).endVertex();
            }
        });
        Platform.startDrawing(Platform.DrawMode.QUADS, Platform.VertexFormat.POS_COLOR, bufferBuilder -> {
            for (int i = 0; i < segments; i++) {
                double alpha1 = i / (double) segments * Math.PI * 2 - Math.PI / 2;
                double alpha2 = (i + 1) / (double) segments * Math.PI * 2 - Math.PI / 2;
                bufferBuilder.pos(x - Math.cos(alpha2) * offset, y + Math.sin(alpha2) * offset, 0).color(r1, g1, b1, a1).endVertex();
                bufferBuilder.pos(x - Math.cos(alpha1) * offset, y + Math.sin(alpha1) * offset, 0).color(r1, g1, b1, a1).endVertex();
                bufferBuilder.pos(x - Math.cos(alpha1) * radius, y + Math.sin(alpha1) * radius, 0).color(r2, g2, b2, a2).endVertex();
                bufferBuilder.pos(x - Math.cos(alpha2) * radius, y + Math.sin(alpha2) * radius, 0).color(r2, g2, b2, a2).endVertex();
            }
        });
        Platform.endDrawGradient();
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public static void drawBorder(float x, float y, float width, float height, int color, float border) {
        drawBorderLTRB(x, y, x + width, y + height, border, color, false);
    }

    public static void drawText(String text, float x, float y, float scale, int color, boolean shadow) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        Platform.setupDrawFont();
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 0f);
        float sf = 1 / scale;
        fontRenderer.drawString(text, x * sf, y * sf, color, shadow);
        GlStateManager.popMatrix();
    }

    public static void drawTooltipBackground(ItemStack stack, List<String> lines, int x, int y, int textWidth, int height, @Nullable RichTooltip tooltip) {
        // TODO theme color
        int backgroundColor = 0xF0100010;
        int borderColorStart = 0x505000FF;
        int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
        RenderTooltipEvent.Color colorEvent;
        if (tooltip != null) {
            colorEvent = new RichTooltipEvent.Color(stack, lines, x, y, TextRenderer.getFontRenderer(), backgroundColor, borderColorStart, borderColorEnd, tooltip);
        } else {
            colorEvent = new RenderTooltipEvent.Color(stack, lines, x, y, TextRenderer.getFontRenderer(), backgroundColor, borderColorStart, borderColorEnd);
        }
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

    /**
     * Draws an entity. Note that this does NOT do any necessary setup for rendering the entity. Please see
     * {@link #drawEntity(Entity, float, float, float, float, float, Consumer, Consumer)} for a full draw method.
     *
     * @param entity entity to draw.
     * @see #drawEntity(Entity, float, float, float, float, float, Consumer, Consumer)
     */
    public static void drawEntityRaw(Entity entity) {
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
        rendermanager.renderEntity(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
        rendermanager.setRenderShadow(true);
    }

    /**
     * A simple method to a draw an entity in a GUI. Using the consumers is not always ideal to modify and restore entity state. In those
     * cases just copy and paste this method and put your code where the consumers would be called. The entity will be scaled so that it
     * fits right in the given size when untransformed (default). When transforming during pre draw, you may need to manually correct the
     * scale and offset.
     *
     * @param entity   entity to draw
     * @param x        x pos
     * @param y        y pos
     * @param w        the width of the area where the entity should be drawn
     * @param h        the height of the area where the entity should be drawn
     * @param z        the z layer ({@link GuiContext#getCurrentDrawingZ()} if drawn in a MUI)
     * @param preDraw  a function to call before rendering. Transform or modify the entity here.
     * @param postDraw a function to call after rendering. Restore old entity state here if needed.
     * @param <T>      type of the entity to render
     */
    public static <T extends Entity> void drawEntity(T entity, float x, float y, float w, float h, float z, @Nullable Consumer<T> preDraw, @Nullable Consumer<T> postDraw) {
        GlStateManager.pushMatrix();
        Platform.setupDrawEntity(entity, x, y, w, h, z);
        if (preDraw != null) preDraw.accept(entity);
        drawEntityRaw(entity);
        if (postDraw != null) postDraw.accept(entity);
        Platform.endDrawEntity();
        GlStateManager.popMatrix();
    }

    /**
     * Draws an entity which looks in the direction of the mouse like the player render in the player inventory does.
     * The code was copied from
     * {@link net.minecraft.client.gui.inventory.GuiInventory#drawEntityOnScreen(int, int, int, float, float, EntityLivingBase) GuiInventory.drawEntityOnScreen}.
     *
     * @param entity entity to draw
     * @param x      x pos
     * @param y      y pos
     * @param w      the width of the area where the entity should be drawn
     * @param h      the height of the area where the entity should be drawn
     * @param z      the z layer ({@link GuiContext#getCurrentDrawingZ()} if drawn in a MUI)
     * @param mouseX current x pos of the mouse
     * @param mouseY current y pos of the mouse
     */
    public static void drawEntityLookingAtMouse(EntityLivingBase entity, float x, float y, float w, float h, float z, int mouseX, int mouseY) {
        GlStateManager.pushMatrix();
        Platform.setupDrawEntity(entity, x, y, w, h, z);

        // pre draw
        float f = entity.renderYawOffset;
        float f1 = entity.rotationYaw;
        float f2 = entity.rotationPitch;
        float f3 = entity.prevRotationYawHead;
        float f4 = entity.rotationYawHead;
        GlStateManager.rotate(-((float) Math.atan(mouseY / 40.0F)) * 20.0F, 1.0F, 0.0F, 0.0F);
        entity.renderYawOffset = (float) Math.atan(mouseX / 40.0F) * 20.0F;
        entity.rotationYaw = (float) Math.atan(mouseX / 40.0F) * 40.0F;
        entity.rotationPitch = -((float) Math.atan(mouseY / 40.0F)) * 20.0F;
        entity.rotationYawHead = entity.rotationYaw;
        entity.prevRotationYawHead = entity.rotationYaw;

        drawEntityRaw(entity);

        // post draw
        entity.renderYawOffset = f;
        entity.rotationYaw = f1;
        entity.rotationPitch = f2;
        entity.prevRotationYawHead = f3;
        entity.rotationYawHead = f4;

        Platform.endDrawEntity();
        GlStateManager.popMatrix();
    }
}
