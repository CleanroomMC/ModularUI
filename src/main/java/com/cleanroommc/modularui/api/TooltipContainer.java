package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.TextRenderer;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.common.internal.ModularUIContext;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.util.ArrayList;
import java.util.List;

public class TooltipContainer implements IDrawable {

    private int showUpDelay = 0;
    private int maxBoxWidth = -1;
    private final List<String> lines = new ArrayList<>();

    public TooltipContainer addLine(String line) {
        this.lines.add(line);
        return this;
    }

    public TooltipContainer addLine(int index, String line) {
        this.lines.add(index, line);
        return this;
    }

    public TooltipContainer setMaxBoxWidth(int maxBoxWidth) {
        this.maxBoxWidth = maxBoxWidth;
        return this;
    }

    public TooltipContainer setShowUpDelay(int showUpDelay) {
        this.showUpDelay = showUpDelay;
        return this;
    }

    public int getShowUpDelay() {
        return showUpDelay;
    }

    @Override
    public void draw(Pos2d pos, Size size, float partialTicks) {
        GuiUtils.drawHoveringText(ItemStack.EMPTY, lines, pos.x, pos.y, size.width, size.height, maxBoxWidth, TextRenderer.FR);
    }

    public void draw(ModularUIContext uiContext) {
        GuiUtils.drawHoveringText(ItemStack.EMPTY, lines,
                uiContext.getMousePos().x, uiContext.getMousePos().y,
                uiContext.getScaledScreenSize().width, uiContext.getScaledScreenSize().height,
                maxBoxWidth, TextRenderer.FR);
    }
}
