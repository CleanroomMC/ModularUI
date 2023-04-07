package com.cleanroommc.modularui.api.drawable;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.resources.I18n;

public interface IInterpolation {

    float interpolate(float a, float b, float x);

    double interpolate(double a, double b, double x);

    @SideOnly(Side.CLIENT)
    default String getName() {
        return I18n.format(this.getKey());
    }

    @SideOnly(Side.CLIENT)
    String getKey();

    @SideOnly(Side.CLIENT)
    default String getTooltip() {
        return I18n.format(this.getTooltipKey());
    }

    @SideOnly(Side.CLIENT)
    String getTooltipKey();
}
