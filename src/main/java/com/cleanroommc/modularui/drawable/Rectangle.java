package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.util.function.IntConsumer;

public class Rectangle implements IDrawable {

    public static final double PI_2 = Math.PI / 2;

    private int cornerRadius, colorTL, colorTR, colorBL, colorBR, cornerSegments;
    private boolean canApplyTheme = false;

    public Rectangle() {
        this.cornerRadius = 0;
        this.colorTL = 0;
        this.colorTR = 0;
        this.colorBL = 0;
        this.colorBR = 0;
        this.cornerSegments = 6;
    }

    public int getColor() {
        return colorTL;
    }

    public Rectangle setCornerRadius(int cornerRadius) {
        this.cornerRadius = Math.max(0, cornerRadius);
        return this;
    }

    public Rectangle setColor(int colorTL, int colorTR, int colorBL, int colorBR) {
        this.colorTL = colorTL;
        this.colorTR = colorTR;
        this.colorBL = colorBL;
        this.colorBR = colorBR;
        return this;
    }

    public Rectangle setVerticalGradient(int colorTop, int colorBottom) {
        return setColor(colorTop, colorTop, colorBottom, colorBottom);
    }

    public Rectangle setHorizontalGradient(int colorLeft, int colorRight) {
        return setColor(colorLeft, colorRight, colorLeft, colorRight);
    }

    public Rectangle setColor(int color) {
        return setColor(color, color, color, color);
    }

    public Rectangle setCornerSegments(int cornerSegments) {
        this.cornerSegments = cornerSegments;
        return this;
    }

    public void setCanApplyTheme(boolean canApplyTheme) {
        this.canApplyTheme = canApplyTheme;
    }

    @Override
    public void applyThemeColor(ITheme theme, WidgetTheme widgetTheme) {
        if (canApplyTheme()) {
            Color.setGlColor(widgetTheme.getColor());
        } else {
            Color.setGlColorOpaque(Color.WHITE.normal);
        }
    }

    @Override
    public void draw(GuiContext context, int x0, int y0, int width, int height) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        float x1 = x0 + width, y1 = y0 + height;
        if (this.cornerRadius == 0) {
            bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            bufferbuilder.pos(x0, y0, 0.0f).color(Color.getRed(colorTL), Color.getGreen(colorTL), Color.getBlue(colorTL), Color.getAlpha(colorTL)).endVertex();
            bufferbuilder.pos(x0, y1, 0.0f).color(Color.getRed(colorBL), Color.getGreen(colorBL), Color.getBlue(colorBL), Color.getAlpha(colorBL)).endVertex();
            bufferbuilder.pos(x1, y1, 0.0f).color(Color.getRed(colorBR), Color.getGreen(colorBR), Color.getBlue(colorBR), Color.getAlpha(colorBR)).endVertex();
            bufferbuilder.pos(x1, y0, 0.0f).color(Color.getRed(colorTR), Color.getGreen(colorTR), Color.getBlue(colorTR), Color.getAlpha(colorTR)).endVertex();
        } else {
            bufferbuilder.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
            int color = Color.average(colorBL, colorBR, colorTR, colorTL);
            bufferbuilder.pos(x0 + width / 2f, y0 + height / 2f, 0.0f).color(Color.getRed(color), Color.getGreen(color), Color.getBlue(color), Color.getAlpha(color)).endVertex();
            bufferbuilder.pos(x0, y0 + cornerRadius, 0.0f).color(Color.getRed(colorTL), Color.getGreen(colorTL), Color.getBlue(colorTL), Color.getAlpha(colorTL)).endVertex();
            bufferbuilder.pos(x0, y1 - cornerRadius, 0.0f).color(Color.getRed(colorBL), Color.getGreen(colorBL), Color.getBlue(colorBL), Color.getAlpha(colorBL)).endVertex();
            int n = cornerSegments;
            for (int i = 1; i <= n; i++) {
                float x = (float) (x0 + cornerRadius - Math.cos(PI_2 / n * i) * cornerRadius);
                float y = (float) (y1 - cornerRadius + Math.sin(PI_2 / n * i) * cornerRadius);
                bufferbuilder.pos(x, y, 0.0f).color(Color.getRed(colorBL), Color.getGreen(colorBL), Color.getBlue(colorBL), Color.getAlpha(colorBL)).endVertex();
            }
            bufferbuilder.pos(x1 - cornerRadius, y1, 0.0f).color(Color.getRed(colorBR), Color.getGreen(colorBR), Color.getBlue(colorBR), Color.getAlpha(colorBR)).endVertex();
            for (int i = 1; i <= n; i++) {
                float x = (float) (x1 - cornerRadius + Math.sin(PI_2 / n * i) * cornerRadius);
                float y = (float) (y1 - cornerRadius + Math.cos(PI_2 / n * i) * cornerRadius);
                bufferbuilder.pos(x, y, 0.0f).color(Color.getRed(colorBR), Color.getGreen(colorBR), Color.getBlue(colorBR), Color.getAlpha(colorBR)).endVertex();
            }
            bufferbuilder.pos(x1, y0 + cornerRadius, 0.0f).color(Color.getRed(colorTR), Color.getGreen(colorTR), Color.getBlue(colorTR), Color.getAlpha(colorTR)).endVertex();
            for (int i = 1; i <= n; i++) {
                float x = (float) (x1 - cornerRadius + Math.cos(PI_2 / n * i) * cornerRadius);
                float y = (float) (y0 + cornerRadius - Math.sin(PI_2 / n * i) * cornerRadius);
                bufferbuilder.pos(x, y, 0.0f).color(Color.getRed(colorTR), Color.getGreen(colorTR), Color.getBlue(colorTR), Color.getAlpha(colorTR)).endVertex();
            }
            bufferbuilder.pos(x0 + cornerRadius, y0, 0.0f).color(Color.getRed(colorTL), Color.getGreen(colorTL), Color.getBlue(colorTL), Color.getAlpha(colorTL)).endVertex();
            for (int i = 1; i <= n; i++) {
                float x = (float) (x0 + cornerRadius - Math.sin(PI_2 / n * i) * cornerRadius);
                float y = (float) (y0 + cornerRadius - Math.cos(PI_2 / n * i) * cornerRadius);
                bufferbuilder.pos(x, y, 0.0f).color(Color.getRed(colorTL), Color.getGreen(colorTL), Color.getBlue(colorTL), Color.getAlpha(colorTL)).endVertex();
            }
            bufferbuilder.pos(x0, y0 + cornerRadius, 0.0f).color(Color.getRed(colorTL), Color.getGreen(colorTL), Color.getBlue(colorTL), Color.getAlpha(colorTL)).endVertex();
        }
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    @Override
    public boolean canApplyTheme() {
        return canApplyTheme;
    }

    @Override
    public void loadFromJson(JsonObject json) {
        if (json.has("color")) {
            Integer c = Color.ofJson(json.get("color"));
            if (c != null) {
                setColor(c);
            }
        }
        if (json.has("colorTop")) {
            Integer c = Color.ofJson(json.get("colorTop"));
            if (c != null) {
                this.colorTL = c;
                this.colorTR = c;
            }
        }
        if (json.has("colorBottom")) {
            Integer c = Color.ofJson(json.get("colorBottom"));
            if (c != null) {
                this.colorBL = c;
                this.colorBR = c;
            }
        }
        if (json.has("colorLeft")) {
            Integer c = Color.ofJson(json.get("colorLeft"));
            if (c != null) {
                this.colorTL = c;
                this.colorBL = c;
            }
        }
        if (json.has("colorRight")) {
            Integer c = Color.ofJson(json.get("colorRight"));
            if (c != null) {
                this.colorTR = c;
                this.colorBR = c;
            }
        }
        setColor(json, val -> colorTL = val, "colorTopLeft", "colorTL");
        setColor(json, val -> colorTR = val, "colorTopRight", "colorTR");
        setColor(json, val -> colorBL = val, "colorBottomLeft", "colorBL");
        setColor(json, val -> colorBR = val, "colorBottomRight", "colorBR");
        this.cornerRadius = JsonHelper.getInt(json, 0, "cornerRadius");
        this.cornerSegments = JsonHelper.getInt(json, 10, "cornerSegments");
    }

    private void setColor(JsonObject json, IntConsumer color, String... keys) {
        JsonElement element = JsonHelper.getJsonElement(json, keys);
        if (element != null) {
            Integer c = Color.ofJson(element);
            if (c != null) {
                color.accept(c);
            }
        }
    }
}
