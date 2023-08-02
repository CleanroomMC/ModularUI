package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widget.sizer.Flex;
import com.cleanroommc.modularui.widget.sizer.IResizeable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EmptyWidget implements IWidget {

    private final Area area = new Area();
    private Flex flex;
    private IWidget parent;

    @Override
    public ModularScreen getScreen() {
        return null;
    }

    @Override
    public void initialise(@NotNull IWidget parent) {
        this.parent = parent;
    }

    @Override
    public void dispose() {
        this.parent = null;
    }

    @Override
    public boolean isValid() {
        return this.parent != null;
    }

    @Override
    public void drawBackground(GuiContext context) {
    }

    @Override
    public void draw(GuiContext context) {
    }

    @Override
    public void drawForeground(GuiContext context) {
    }

    @Override
    public void onFrameUpdate() {
    }

    @Override
    public Area getArea() {
        return this.area;
    }

    @Override
    public @NotNull ModularPanel getPanel() {
        return this.parent.getPanel();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
    }

    @Override
    public boolean canBeSeen(IViewportStack stack) {
        return false;
    }

    @Override
    public void markTooltipDirty() {
    }

    @Override
    public @NotNull IWidget getParent() {
        return this.parent;
    }

    @Override
    public GuiContext getContext() {
        return this.parent.getContext();
    }

    @Override
    public Flex flex() {
        if (this.flex == null) {
            this.flex = new Flex(this);
        }
        return this.flex;
    }

    @Override
    public @Nullable IResizeable resizer() {
        return this.flex;
    }

    @Override
    public void resizer(IResizeable resizer) {
    }

    @Override
    public Flex getFlex() {
        return this.flex;
    }
}
