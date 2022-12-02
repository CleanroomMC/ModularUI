package com.cleanroommc.modularui.widget.resizer.constraint;

import com.cleanroommc.modularui.api.IWidget;
import com.cleanroommc.modularui.api.IResizer;
import com.cleanroommc.modularui.screen.GuiViewportStack;
import com.cleanroommc.modularui.utils.Area;
import com.cleanroommc.modularui.utils.MathUtils;
import com.cleanroommc.modularui.widget.resizer.DecoratedResizer;

/**
 * Bounds resizer
 * <p>
 * This resizer class allows to keep the element within the bounds of
 * current viewport
 */
public class BoundsResizer extends DecoratedResizer {

    public IWidget target;
    public int padding;

    private GuiViewportStack viewport = new GuiViewportStack();

    public static BoundsResizer apply(IWidget widget, IWidget target, int padding) {
        //BoundsResizer resizer = new BoundsResizer(widget.resizer(), target, padding);

        //widget.flex().post(resizer);

        return null;
    }

    protected BoundsResizer(IResizer resizer, IWidget target, int padding) {
        super(resizer);

        this.target = target;
        this.padding = padding;
    }

    @Override
    public void apply(Area area) {
        this.viewport.applyFromElement(this.target);

        //Area viewport = this.viewport.getViewport();

        //area.x = MathUtils.clamp(area.x, this.viewport.globalX(viewport.x) + this.padding, this.viewport.globalX(viewport.ex()) - area.w - this.padding);
        //area.y = MathUtils.clamp(area.y, this.viewport.globalY(viewport.y) + this.padding, this.viewport.globalY(viewport.ey()) - area.h - this.padding);

        this.viewport.reset();
    }

    @Override
    public int getX() {
        return 0;
    }

    @Override
    public int getY() {
        return 0;
    }

    @Override
    public int getW() {
        return 0;
    }

    @Override
    public int getH() {
        return 0;
    }
}