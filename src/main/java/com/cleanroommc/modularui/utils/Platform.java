package com.cleanroommc.modularui.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;

/**
 * Version specific code is supposed to go here.
 * Ideally only the body of methods and value of fields should be changed and no signatures.
 */
public class Platform {

    public static final ItemStack EMPTY_STACK = ItemStack.EMPTY;

    public static boolean isStackEmpty(ItemStack stack) {
        return stack == null || stack.isEmpty();
    }

    public static ItemStack copyStack(ItemStack stack) {
        return isStackEmpty(stack) ? EMPTY_STACK : stack.copy();
    }

    public static void startDrawing(DrawMode drawMode, VertexFormat format, Consumer<BufferBuilder> bufferBuilder) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(drawMode.mode, format.format);
        bufferBuilder.accept(buffer);
        tessellator.draw();
    }

    public static void setupDrawColor() {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
    }

    public static void setupDrawTex(ResourceLocation texture) {
        setupDrawTex();
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
    }

    public static void setupDrawTex(int textureId) {
        setupDrawTex();
        GlStateManager.bindTexture(textureId);
    }

    public static void setupDrawTex() {
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
    }

    public static void setupDrawGradient() {
        setupDrawGradient(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    }

    public static void setupDrawGradient(GlStateManager.SourceFactor srcFactor, GlStateManager.DestFactor destFactor, GlStateManager.SourceFactor srcFactorAlpha, GlStateManager.DestFactor destFactorAlpha) {
        GlStateManager.tryBlendFuncSeparate(srcFactor, destFactor, srcFactorAlpha, destFactorAlpha);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
    }

    public static void endDrawGradient() {
        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    public static void setupDrawItem() {
        setupDrawTex();
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();
    }

    public static void endDrawItem() {
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableDepth();
    }

    public static void setupDrawFont() {
        setupDrawTex();
    }

    /**
     * <a href="https://i.sstatic.net/sfQdh.jpg">Reference</a>
     */
    public enum DrawMode {
        QUADS(GL11.GL_QUADS),
        POINTS(GL11.GL_POINTS),
        LINES(GL11.GL_LINES),
        LINE_STRIP(GL11.GL_LINE_STRIP),
        LINE_LOOP(GL11.GL_LINE_LOOP),
        TRIANGLES(GL11.GL_TRIANGLES),
        TRIANGLE_STRIP(GL11.GL_TRIANGLE_STRIP),
        TRIANGLE_FAN(GL11.GL_TRIANGLE_FAN);

        public final int mode;

        DrawMode(int mode) {
            this.mode = mode;
        }
    }

    public enum VertexFormat {

        POS(DefaultVertexFormats.POSITION),
        POS_TEX(DefaultVertexFormats.POSITION_TEX),
        POS_COLOR(DefaultVertexFormats.POSITION_COLOR),
        POS_TEX_COLOR(DefaultVertexFormats.POSITION_TEX_COLOR),
        POS_NORMAL(DefaultVertexFormats.POSITION_NORMAL),
        POS_TEX_NORMAL(DefaultVertexFormats.POSITION_TEX_NORMAL),
        POS_TEX_COLOR_NORMAL(DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL),
        POS_TEX_LMAP_COLOR(DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

        public final net.minecraft.client.renderer.vertex.VertexFormat format;

        VertexFormat(net.minecraft.client.renderer.vertex.VertexFormat format) {
            this.format = format;
        }
    }
}
