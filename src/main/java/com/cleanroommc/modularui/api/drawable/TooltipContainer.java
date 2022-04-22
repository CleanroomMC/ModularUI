package com.cleanroommc.modularui.api.drawable;

import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.screen.ModularUIContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TooltipContainer implements IDrawable {

    private final List<Text> lines = new ArrayList<>();
    private int maxBoxWidth = Integer.MAX_VALUE;
    private boolean forceShadow = true;
    private float scale = 1f;

    public TooltipContainer addLine(Text... line) {
        lines.addAll(Arrays.asList(line));
        return this;
    }

    public TooltipContainer addLine(String line) {
        return addLine(new Text(line));
    }

    public TooltipContainer addLine(int index, Text... line) {
        lines.addAll(index, Arrays.asList(line));
        return this;
    }

    public TooltipContainer addLine(int index, String line) {
        return addLine(index, new Text(line));
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

    public TooltipContainer with(List<Text> lines) {
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
            GuiHelper.drawHoveringText(lines, GuiHelper.getCurrentMousePos(), GuiHelper.getScreenSize(), maxBoxWidth, scale, forceShadow, Alignment.TopLeft);
        }
    }

    public void draw(ModularUIContext uiContext) {
        if (!lines.isEmpty()) {
            GuiHelper.drawHoveringText(lines, uiContext.getMousePos(), uiContext.getScaledScreenSize(), maxBoxWidth, scale, forceShadow, Alignment.TopLeft);
        }
    }
}
