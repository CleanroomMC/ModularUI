package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.utils.Area;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IResizer {

    void preApply(Area area);

    void apply(Area area);

    void postApply(Area area);

    @SideOnly(Side.CLIENT)
    void add(IWidget parent, IWidget child);

    @SideOnly(Side.CLIENT)
    void remove(IWidget parent, IWidget child);

    int getX();

    int getY();

    int getW();

    int getH();
}