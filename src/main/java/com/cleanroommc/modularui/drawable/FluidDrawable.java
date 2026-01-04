package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;

import com.cleanroommc.modularui.widget.Widget;

import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class FluidDrawable implements IDrawable {

    private FluidStack fluid = null;

    public FluidDrawable() {}

    /**
     * Takes a fluid stack, it can be null but will not draw anything
     *
     * @param fluid - fluid stack to draw
     */
    public FluidDrawable(@Nullable FluidStack fluid) {
        setFluid(fluid);
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        GuiDraw.drawFluidTexture(fluid, x, y, width, height, context.getCurrentDrawingZ());
    }

    @Override
    public int getDefaultWidth() {
        return 16;
    }

    @Override
    public int getDefaultHeight() {
        return 16;
    }

    @Override
    public Widget<?> asWidget() {
        return IDrawable.super.asWidget().size(16);
    }

    public FluidDrawable setFluid(FluidStack fluid) {
        this.fluid = fluid;
        return this;
    }

}
