package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.drawable.IHoverable;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.api.widget.ITooltip;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widget.sizer.Box;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HoverableIcon extends DelegateIcon implements IHoverable, ITooltip<HoverableIcon> {

    private final Area area = new Area();
    private RichTooltip tooltip;

    public HoverableIcon(IIcon icon) {
        super(icon);
        setRenderedAt(0, 0);
    }

    @Override
    @Nullable
    public RichTooltip getTooltip() {
        return tooltip;
    }

    @Override
    public void setRenderedAt(int x, int y) {
        this.area.set(x, y, getWidth(), getHeight());
    }

    @Override
    public Area getRenderedArea() {
        this.area.setSize(getWidth(), getHeight());
        return this.area;
    }

    @Override
    public @NotNull RichTooltip tooltip() {
        if (this.tooltip == null) this.tooltip = new RichTooltip(area -> area.set(getRenderedArea()));
        return tooltip;
    }

    @Override
    public HoverableIcon tooltip(RichTooltip tooltip) {
        this.tooltip = tooltip;
        return this;
    }
}
