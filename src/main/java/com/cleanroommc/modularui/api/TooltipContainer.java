package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.api.drawable.GuiHelper;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.drawable.TextSpan;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.common.internal.ModularUIContext;

import java.util.ArrayList;
import java.util.List;

public class TooltipContainer implements IDrawable {

    private final List<Text[]> lines = new ArrayList<>();
    private int showUpDelay = 0;
    private int maxBoxWidth = Integer.MAX_VALUE;
    private boolean forceShadow = true;
    private float scale = 1f;

    public TooltipContainer addLine(Text... line) {
        this.lines.add(line);
        return this;
    }

    public TooltipContainer addLine(String line) {
        return addLine(new Text(line));
    }

    public TooltipContainer addLine(TextSpan line) {
        return addLine(line.getTexts());
    }

    public TooltipContainer addLine(int index, Text... line) {
        this.lines.add(index, line);
        return this;
    }

    public TooltipContainer addLine(int index, String line) {
        return addLine(index, new Text(line));
    }

    public TooltipContainer addLine(int index, TextSpan line) {
        return addLine(index, line.getTexts());
    }

    public TooltipContainer setMaxBoxWidth(int maxBoxWidth) {
        this.maxBoxWidth = maxBoxWidth;
        return this;
    }

    public TooltipContainer setShowUpDelay(int showUpDelay) {
        this.showUpDelay = showUpDelay;
        return this;
    }

    public TooltipContainer forceShadow(boolean forceShadow) {
        this.forceShadow = forceShadow;
        return this;
    }

    public TooltipContainer setScale(float scale) {
        this.scale = scale;
        return this;
    }

    public int getShowUpDelay() {
        return showUpDelay;
    }

    public float getScale() {
        return scale;
    }

    public boolean isForceShadow() {
        return forceShadow;
    }

    @Override
    public void draw(Pos2d pos, Size size, float partialTicks) {
        if (!lines.isEmpty() && GuiHelper.hasScreen()) {
            GuiHelper.drawHoveringText(lines, GuiHelper.getCurrentMousePos(), GuiHelper.getScreenSize(), maxBoxWidth, scale, forceShadow);
        }
    }

    public void draw(ModularUIContext uiContext) {
        if (!lines.isEmpty()) {
            GuiHelper.drawHoveringText(lines, uiContext.getMousePos(), uiContext.getScaledScreenSize(), maxBoxWidth, scale, forceShadow);
        }
    }
}
