package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.future.GlStateManager;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Color;
import net.minecraft.client.renderer.Tessellator;
import org.jetbrains.annotations.Contract;

import static com.cleanroommc.modularui.drawable.BufferBuilder.bufferbuilder;

public class Circle implements IDrawable {

    public static final double PI2 = Math.PI * 2;

    private int colorInner, colorOuter, segments;

    public Circle() {
        this.colorInner = 0;
        this.colorOuter = 0;
        this.segments = 40;
    }

    @Contract("_ -> this")
    public Circle setColorInner(int colorInner) {
        this.colorInner = colorInner;
        return this;
    }

    public Circle setColorOuter(int colorOuter) {
        this.colorOuter = colorOuter;
        return this;
    }

    public Circle setColor(int inner, int outer) {
        this.colorInner = inner;
        this.colorOuter = outer;
        return this;
    }

    public Circle setSegments(int segments) {
        this.segments = segments;
        return this;
    }

    /*@Override
    public void applyThemeColor(int color) {
        if (colorInner == 0 && colorOuter == 0) {
            IDrawable.super.applyThemeColor(color == 0 ? 0xFFFFFFFF : color);
        }
    }*/

    @Override
    public void draw(GuiContext context, int x0, int y0, int width, int height) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        float x_2 = x0 + width / 2f, y_2 = y0 + height / 2f;
        bufferbuilder.pos(x_2, y_2, 0.0f).color(Color.getRed(colorInner), Color.getGreen(colorInner), Color.getBlue(colorInner), Color.getAlpha(colorInner)).endVertex();
        float incr = (float) (PI2 / segments);
        for (int i = 0; i <= segments; i++) {
            float angle = incr * i;
            float x = (float) (Math.sin(angle) * (width / 2) + x_2);
            float y = (float) (Math.cos(angle) * (height / 2) + y_2);
            bufferbuilder.pos(x, y, 0.0f).color(Color.getRed(colorOuter), Color.getGreen(colorOuter), Color.getBlue(colorOuter), Color.getAlpha(colorOuter)).endVertex();
        }
        Tessellator.instance.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
}
