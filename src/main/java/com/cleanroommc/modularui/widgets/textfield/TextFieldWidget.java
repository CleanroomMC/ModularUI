package com.cleanroommc.modularui.widgets.textfield;

import com.cleanroommc.modularui.api.IMathValue;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.IStringValue;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.math.Constant;
import com.cleanroommc.modularui.utils.math.MathBuilder;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.ParsePosition;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Text input widget with one line only. Can be synced between client and server. Can handle text validation.
 */
public class TextFieldWidget extends BaseTextFieldWidget<TextFieldWidget> {

    private IStringValue<?> stringValue;
    private Function<String, String> validator = val -> val;
    private boolean numbers = false;

    protected boolean changedMarkedColor = false;

    public static IMathValue parse(String num) {
        try {
            return MathBuilder.INSTANCE.parse(num);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Constant(0);
    }

    @Override
    public void onInit() {
        super.onInit();
        if (this.stringValue == null) {
            this.stringValue = new StringValue("");
        }
        setText(this.stringValue.getStringValue());
        if (!hasTooltip()) {
            tooltip().excludeArea(getArea());
            tooltipBuilder(tooltip -> tooltip.addLine(IKey.str(getText())));
        }
        if (!this.changedMarkedColor) {
            this.renderer.setMarkedColor(getWidgetTheme(getContext().getTheme()).getMarkedColor());
        }
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        if (syncHandler instanceof IStringValue && syncHandler instanceof ValueSyncHandler) {
            this.stringValue = (IStringValue<?>) syncHandler;
            ((ValueSyncHandler<?>) this.stringValue).setChangeListener(() -> {
                markTooltipDirty();
                setText(this.stringValue.getValue().toString());
            });
            return true;
        }
        return false;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!isFocused()) {
            String s = this.stringValue.getStringValue();
            if (!getText().equals(s)) {
                setText(s);
            }
        }
    }

    @Override
    public void drawText(GuiContext context) {
        this.renderer.setSimulate(false);
        this.renderer.setPos(getArea().getPadding().left, 0);
        this.renderer.setScale(this.scale);
        this.renderer.setAlignment(this.textAlignment, -1, getArea().height);
        this.renderer.draw(this.handler.getText());
        getScrollData().setScrollSize(Math.max(0, (int) this.renderer.getLastWidth()));
    }

    @Override
    public void drawForeground(GuiContext context) {
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
    public void onFocus(GuiContext context) {
        super.onFocus(context);
        Point main = this.handler.getMainCursor();
        if (main.x == 0) {
            this.handler.setCursor(main.y, getText().length(), true, true);
        }
    }

    @Override
    public void onRemoveFocus(GuiContext context) {
        super.onRemoveFocus(context);
        if (this.handler.getText().isEmpty()) {
            this.handler.getText().add(this.validator.apply(""));
        } else if (this.handler.getText().size() == 1) {
            this.handler.getText().set(0, this.validator.apply(this.handler.getText().get(0)));
            markTooltipDirty();
        } else {
            throw new IllegalStateException("TextFieldWidget can only have one line!");
        }
        this.stringValue.setStringValue(this.numbers ? format.parse(getText(), new ParsePosition(0)).toString() : getText());
    }

    @Override
    public boolean canHover() {
        return true;
    }

    public TextFieldWidget setMaxLength(int maxLength) {
        this.handler.setMaxCharacters(maxLength);
        return this;
    }

    public TextFieldWidget setPattern(Pattern pattern) {
        this.handler.setPattern(pattern);
        return this;
    }

    public TextFieldWidget setTextColor(int textColor) {
        this.renderer.setColor(textColor);
        this.changedTextColor = true;
        return this;
    }

    public TextFieldWidget setMarkedColor(int color) {
        this.renderer.setMarkedColor(color);
        this.changedMarkedColor = true;
        return this;
    }

    public TextFieldWidget setValidator(Function<String, String> validator) {
        this.validator = validator;
        return this;
    }

    public TextFieldWidget setNumbersLong(Function<Long, Long> validator) {
        //setPattern(WHOLE_NUMS);
        this.numbers = true;
        setValidator(val -> {
            long num;
            if (val.isEmpty()) {
                num = 0;
            } else {
                num = (long) parse(val).doubleValue();
            }
            return format.format(validator.apply(num));
        });
        return this;
    }

    public TextFieldWidget setNumbers(Function<Integer, Integer> validator) {
        //setPattern(WHOLE_NUMS);
        this.numbers = true;
        return setValidator(val -> {
            int num;
            if (val.isEmpty()) {
                num = 0;
            } else {
                num = (int) parse(val).doubleValue();
            }
            return format.format(validator.apply(num));
        });
    }

    public TextFieldWidget setNumbersDouble(Function<Double, Double> validator) {
        //setPattern(DECIMALS);
        this.numbers = true;
        return setValidator(val -> {
            double num;
            if (val.isEmpty()) {
                num = 0;
            } else {
                num = parse(val).doubleValue();
            }
            return format.format(validator.apply(num));
        });
    }

    public TextFieldWidget setNumbers(Supplier<Integer> min, Supplier<Integer> max) {
        return setNumbers(val -> Math.min(max.get(), Math.max(min.get(), val)));
    }

    public TextFieldWidget setNumbersLong(Supplier<Long> min, Supplier<Long> max) {
        return setNumbersLong(val -> Math.min(max.get(), Math.max(min.get(), val)));
    }

    public TextFieldWidget setNumbers(int min, int max) {
        return setNumbers(val -> Math.min(max, Math.max(min, val)));
    }

    public TextFieldWidget setNumbers() {
        return setNumbers(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public TextFieldWidget value(IStringValue<?> stringValue) {
        this.stringValue = stringValue;
        setValue(stringValue);
        return this;
    }
}
