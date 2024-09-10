package com.cleanroommc.modularui.widgets.textfield;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTextFieldTheme;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.regex.Pattern;

/**
 * Text input widget with one line only. Can be synced between client and server. Can handle text validation.
 * <p>
 * Child classes are expected to have Value bound to it. It's capable of doing anything to displayed text
 * while keeping value consistent.
 */
public abstract class OneLineTextField< W extends OneLineTextField<W>> extends BaseTextFieldWidget<W> {

    protected boolean changedMarkedColor = false;

    /**
     * Called on widget init. Set default value if it's not set by user, so that it can be called anytime.
     */
    protected abstract void setupValueIfNull();

    /**
     * Called when initializing sync handlers. If supplied sync handler can satisfy the requirement, save it and return true.
     */
    protected abstract boolean checkAndSetSyncHandler(SyncHandler syncHandler);

    /**
     * Return text to display derived from value.
     */
    @NotNull
    protected abstract String getDisplayTextFromValue();

    /**
     * Parse supplied display text and store to the value.
     */
    protected abstract void parseDisplayText(String text);

    @Override
    public void onInit() {
        super.onInit();
        setupValueIfNull();
        setText(getDisplayTextFromValue());
        if (!hasTooltip()) {
            tooltipBuilder(tooltip -> tooltip.addLine(IKey.str(getText())));
        }
        if (!this.changedMarkedColor) {
            this.renderer.setMarkedColor(getMarkedColor());
        }
    }

    public int getMarkedColor() {
        WidgetTheme theme = getWidgetTheme(getContext().getTheme());
        if (theme instanceof WidgetTextFieldTheme textFieldTheme) {
            return textFieldTheme.getMarkedColor();
        }
        return ITheme.getDefault().getTextFieldTheme().getMarkedColor();
    }

    @Override
    public final boolean isValidSyncHandler(SyncHandler syncHandler) {
        if (syncHandler instanceof ValueSyncHandler<?> valueSyncHandler && checkAndSetSyncHandler(syncHandler)) {
            valueSyncHandler.setChangeListener(() -> {
                markTooltipDirty();
                setText(getDisplayTextFromValue());
            });
            return true;
        }
        return false;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!isFocused()) {
            String s = getDisplayTextFromValue();
            if (!getText().equals(s)) {
                setText(s);
            }
        }
    }

    @Override
    public void drawText(ModularGuiContext context) {
        this.renderer.setSimulate(false);
        this.renderer.setPos(getArea().getPadding().left, 0);
        this.renderer.setScale(this.scale);
        this.renderer.setAlignment(this.textAlignment, -1, getArea().height);
        this.renderer.draw(this.handler.getText());
        getScrollData().setScrollSize(Math.max(0, (int) this.renderer.getLastWidth()));
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        if (hasTooltip() && getScrollData().isScrollBarActive(getScrollArea()) && isHoveringFor(getTooltip().getShowUpTimer())) {
            getTooltip().draw(getContext());
        }
    }

    @NotNull
    public String getText() {
        if (this.handler.getText().isEmpty()) {
            return "";
        }
        if (this.handler.getText().size() > 1) {
            throw new IllegalStateException("TextFieldWidget can only have one line!");
        }
        return this.handler.getText().get(0);
    }

    public void setText(@NotNull String text) {
        if (this.handler.getText().isEmpty()) {
            this.handler.getText().add(text);
        } else {
            this.handler.getText().set(0, text);
        }
    }

    @Override
    public void onFocus(ModularGuiContext context) {
        super.onFocus(context);
        Point main = this.handler.getMainCursor();
        if (main.x == 0) {
            this.handler.setCursor(main.y, getText().length(), true, true);
        }
    }

    @Override
    public void onRemoveFocus(ModularGuiContext context) {
        super.onRemoveFocus(context);
        if (this.handler.getText().isEmpty()) {
            parseDisplayText("");
            this.handler.getText().add(getDisplayTextFromValue());
        } else if (this.handler.getText().size() == 1) {
            parseDisplayText(this.handler.getText().get(0));
            this.handler.getText().set(0, getDisplayTextFromValue());
            markTooltipDirty();
        } else {
            throw new IllegalStateException("TextFieldWidget can only have one line!");
        }
    }

    @Override
    public boolean canHover() {
        return true;
    }

    public W setMaxLength(int maxLength) {
        this.handler.setMaxCharacters(maxLength);
        return getThis();
    }

    public W setPattern(Pattern pattern) {
        this.handler.setPattern(pattern);
        return getThis();
    }

    public W setTextColor(int textColor) {
        this.renderer.setColor(textColor);
        this.changedTextColor = true;
        return getThis();
    }

    public W setMarkedColor(int color) {
        this.renderer.setMarkedColor(color);
        this.changedMarkedColor = true;
        return getThis();
    }
}
