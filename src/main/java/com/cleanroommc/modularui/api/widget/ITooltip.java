package com.cleanroommc.modularui.api.widget;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.Tooltip;
import com.cleanroommc.modularui.utils.Alignment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Helper interface with tooltip builder methods for widgets.
 *
 * @param <W> widget type
 */
public interface ITooltip<W extends IWidget> {

    /**
     * @return the current tooltip of this widget. Null if there is none
     */
    @Nullable
    Tooltip getTooltip();

    /**
     * @return the current tooltip of this widget. Creates a new one if there is none
     */
    @NotNull
    Tooltip tooltip();

    /**
     * @return true if this widget has a tooltip
     */
    default boolean hasTooltip() {
        return getTooltip() != null && !getTooltip().isEmpty();
    }

    /**
     * @return this cast to the true widget type
     */
    @SuppressWarnings("unchecked")
    default W getThis() {
        return (W) this;
    }

    /**
     * Helper method to call tooltip setters within a widget tree initialisation.
     * Only called once.
     *
     * @param tooltipConsumer tooltip function
     * @return this
     */
    default W tooltip(Consumer<Tooltip> tooltipConsumer) {
        return tooltipStatic(tooltipConsumer);
    }

    /**
     * Helper method to call tooltip setters within a widget tree initialisation.
     * Only called once.
     *
     * @param tooltipConsumer tooltip function
     * @return this
     */
    default W tooltipStatic(Consumer<Tooltip> tooltipConsumer) {
        tooltipConsumer.accept(tooltip());
        return getThis();
    }

    /**
     * Sets a tooltip builder. The builder will be called every time the tooltip is marked dirty.
     * Should be used for dynamic tooltips.
     *
     * @param tooltipBuilder tooltip function
     * @return this
     */
    default W tooltipBuilder(Consumer<Tooltip> tooltipBuilder) {
        return tooltipDynamic(tooltipBuilder);
    }

    /**
     * Sets a tooltip builder. The builder will be called every time the tooltip is marked dirty.
     * Should be used for dynamic tooltips.
     *
     * @param tooltipBuilder tooltip function
     * @return this
     */
    default W tooltipDynamic(Consumer<Tooltip> tooltipBuilder) {
        tooltip().tooltipBuilder(tooltipBuilder);
        return getThis();
    }

    /**
     * Sets a general tooltip position. The true position is calculated every frame.
     *
     * @param pos tooltip pos
     * @return this
     */
    default W tooltipPos(Tooltip.Pos pos) {
        tooltip().pos(pos);
        return getThis();
    }

    /**
     * Sets a fixed tooltip position.
     *
     * @param x x pos
     * @param y y pos
     * @return this
     */
    default W tooltipPos(int x, int y) {
        tooltip().pos(x, y);
        return getThis();
    }

    /**
     * Sets an alignment. The alignment determines how the content is aligned in the tooltip.
     *
     * @param alignment alignment
     * @return this
     */
    default W tooltipAlignment(Alignment alignment) {
        tooltip().alignment(alignment);
        return getThis();
    }

    /**
     * Sets if the tooltip text should have shadow enabled by default.
     * Can be overridden with {@link com.cleanroommc.modularui.drawable.StyledText} lines.
     *
     * @param textShadow true if text should have a shadow
     * @return this
     */
    default W tooltipTextShadow(boolean textShadow) {
        tooltip().textShadow(textShadow);
        return getThis();
    }

    /**
     * Sets a default tooltip text color. Can be overridden with text formatting.
     *
     * @param textColor text color
     * @return this
     */
    default W tooltipTextColor(int textColor) {
        tooltip().textColor(textColor);
        return getThis();
    }

    /**
     * Sets a tooltip scale. The whole tooltip with content will be drawn with this scale.
     *
     * @param scale scale
     * @return this
     */
    default W tooltipScale(float scale) {
        tooltip().scale(scale);
        return getThis();
    }

    /**
     * Sets a show up timer. This is the time in ticks needed for the cursor to hover this widget for the tooltip to appear.
     *
     * @param showUpTimer show up timer in ticks
     * @return this
     */
    default W tooltipShowUpTimer(int showUpTimer) {
        tooltip().showUpTimer(showUpTimer);
        return getThis();
    }

    /**
     * Adds any drawable as a new line. Inlining elements is currently not possible.
     *
     * @param drawable drawable element.
     * @return this
     */
    default W addTooltipLine(IDrawable drawable) {
        tooltip().addLine(drawable);
        return getThis();
    }

    /**
     * Helper method to add a simple string as a line. Adds multiple lines if string contains <code>\n</code>.
     *
     * @param line text line
     * @return this
     */
    default W addTooltipLine(String line) {
        return addTooltipLine(IKey.str(line));
    }
}
