package com.cleanroommc.modularui.utils;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.integration.jei.ModularUIJeiPlugin;
import com.cleanroommc.modularui.screen.viewport.GuiContext;

import mezz.jei.gui.overlay.IngredientListOverlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;

/**
 * Version specific code is supposed to go here.
 * Ideally only the body of methods and value of fields should be changed and no signatures.
 */
public class Platform {

    public static final ItemStack EMPTY_STACK = ItemStack.EMPTY;

    @SideOnly(Side.CLIENT)
    public static @NotNull EntityPlayerSP getClientPlayer() {
        return Minecraft.getMinecraft().player;
    }

    @SideOnly(Side.CLIENT)
    public static String getKeyDisplay(KeyBinding keyBinding) {
        return keyBinding.getKeyDescription();
    }

    public static boolean isStackEmpty(ItemStack stack) {
        return stack == null || stack.isEmpty();
    }

    public static ItemStack copyStack(ItemStack stack) {
        return isStackEmpty(stack) ? EMPTY_STACK : stack.copy();
    }

    public static void unFocusRecipeViewer() {
        if (ModularUI.Mods.JEI.isLoaded()) {
            ((IngredientListOverlay) ModularUIJeiPlugin.getRuntime().getIngredientListOverlay()).setKeyboardFocus(false);
        }
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
     * Sets up the gl state for rendering an entity. The entity will be scaled so that it fits right in the given size when untransformed.
     *
     * @param entity entity to set up drawing for
     * @param x      x pos
     * @param y      y pos
     * @param w      the width of the area where the entity should be drawn
     * @param h      the height of the area where the entity should be drawn
     * @param z      the z layer ({@link GuiContext#getCurrentDrawingZ()} if drawn in a MUI)
     */
    public static void setupDrawEntity(Entity entity, float x, float y, float w, float h, float z) {
        float size;
        float scale;
        if (h / entity.height < w / entity.width) {
            size = entity.height;
            scale = h / size;
        } else {
            size = entity.width;
            scale = w / size;
        }
        GlStateManager.enableColorMaterial();
        GlStateManager.enableDepth();
        GlStateManager.translate(x + w / 2, y + h / 2, z + 50.0F);
        GlStateManager.scale(-scale, scale, scale);
        GlStateManager.translate(0, size / 2f, 0);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
    }

    public static void endDrawEntity() {
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.disableDepth();
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
