package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.api.drawable.GuiHelper;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.drawable.TextSpan;
import com.cleanroommc.modularui.common.internal.ModularUIContext;

import java.util.ArrayList;
import java.util.List;

public class TooltipContainer implements IDrawable {


    private final List<TextSpan> lines = new ArrayList<>();
    private int maxBoxWidth = Integer.MAX_VALUE;
    private boolean forceShadow = true;
    private float scale = 1f;

    public TooltipContainer addLine(Text... line) {
        return addLine(new TextSpan(line));
    }

    public TooltipContainer addLine(String line) {
        return addLine(new Text(line));
    }

    public TooltipContainer addLine(TextSpan line) {
        this.lines.add(line);
        return this;
    }

    public TooltipContainer addLine(int index, Text... line) {
        return addLine(index, new TextSpan(line));
    }

    public TooltipContainer addLine(int index, String line) {
        return addLine(index, new Text(line));
    }

    public TooltipContainer addLine(int index, TextSpan line) {
        this.lines.add(index, line);
        return this;
    }

    public TooltipContainer setMaxBoxWidth(int maxBoxWidth) {
        this.maxBoxWidth = maxBoxWidth;
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

    public TooltipContainer with(List<TextSpan> lines) {
        TooltipContainer tooltipContainer = new TooltipContainer();
        tooltipContainer.lines.addAll(this.lines);
        tooltipContainer.maxBoxWidth = maxBoxWidth;
        tooltipContainer.forceShadow = forceShadow;
        tooltipContainer.scale = scale;
        tooltipContainer.lines.addAll(lines);
        return tooltipContainer;
    }

    public float getScale() {
        return scale;
    }

    public boolean isForceShadow() {
        return forceShadow;
    }

    @Override
    public void draw(float x, float y, float width, float height, float partialTicks) {
        if (!lines.isEmpty() && GuiHelper.hasScreen()) {
            GuiHelper.drawHoveringTextSpans(lines, GuiHelper.getCurrentMousePos(), GuiHelper.getScreenSize(), maxBoxWidth, scale, forceShadow);
        }
    }

    public void draw(ModularUIContext uiContext) {
        if (!lines.isEmpty()) {
            GuiHelper.drawHoveringTextSpans(lines, uiContext.getMousePos(), uiContext.getScaledScreenSize(), maxBoxWidth, scale, forceShadow);
        }
    }
}
