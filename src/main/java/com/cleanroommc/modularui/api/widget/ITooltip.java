package com.cleanroommc.modularui.api.widget;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.Tooltip;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.sizer.Area;

import java.util.function.Consumer;

public interface ITooltip<W extends IWidget> {

    Tooltip getTooltip();

    Tooltip tooltip();

    @SuppressWarnings("unchecked")
    default W getThis() {
        return (W) this;
    }

    default W tooltip(Consumer<Tooltip> tooltipConsumer) {
        tooltipConsumer.accept(tooltip());
        return getThis();
    }

    default W tooltipBuilder(Consumer<Tooltip> tooltipBuilder) {
        tooltip().tooltipBuilder(tooltipBuilder);
        return getThis();
    }

    default W excludeTooltipArea(Area area) {
        tooltip().excludeArea(area);
        return getThis();
    }

    default W tooltipPos(Tooltip.Pos pos) {
        tooltip().pos(pos);
        return getThis();
    }

    default W tooltipPos(int x, int y) {
        tooltip().pos(x, y);
        return getThis();
    }

    default W tooltipAlignment(Alignment alignment) {
        tooltip().alignment(alignment);
        return getThis();
    }

    default W tooltipTextShadow(boolean textShadow) {
        tooltip().textShadow(textShadow);
        return getThis();
    }

    default W tooltipTextColor(int textColor) {
        tooltip().textColor(textColor);
        return getThis();
    }

    default W tooltipScale(float scale) {
        tooltip().scale(scale);
        return getThis();
    }

    default W tooltipShowUpTimer(int showUpTimer) {
        tooltip().showUpTimer(showUpTimer);
        return getThis();
    }

    default W addTooltipLine(IDrawable drawable) {
        tooltip().addLine(drawable);
        return getThis();
    }

    default W addTooltipLine(String line) {
        return addTooltipLine(IKey.str(line));
    }
}
