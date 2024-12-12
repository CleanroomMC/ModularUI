package com.cleanroommc.modularui.widgets.textfield;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.IStringValue;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTextFieldTheme;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;

import org.jetbrains.annotations.NotNull;
import org.mariuszgromada.math.mxparser.Constant;
import org.mariuszgromada.math.mxparser.Expression;

import java.awt.*;
import java.text.ParsePosition;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Text input widget with one line only. Can be synced between client and server. Can handle text validation.
 */
public class TextFieldWidget extends BaseTextFieldWidget<TextFieldWidget> {

    public static final Constant k = new Constant("k", 1e3);
    public static final Constant M = new Constant("M", 1e6);
    public static final Constant G = new Constant("G", 1e9);
    public static final Constant T = new Constant("T", 1e12);
    public static final Constant P = new Constant("P", 1e15);
    public static final Constant E = new Constant("E", 1e18);
    public static final Constant Z = new Constant("Z", 1e21);
    public static final Constant Y = new Constant("Y", 1e24);
    public static final Constant m = new Constant("m", 1e-3);
    public static final Constant u = new Constant("u", 1e-6);
    public static final Constant n = new Constant("n", 1e-9);
    public static final Constant p = new Constant("p", 1e-12);
    public static final Constant f = new Constant("f", 1e-15);
    public static final Constant a = new Constant("a", 1e-18);
    public static final Constant z = new Constant("z", 1e-21);
    public static final Constant y = new Constant("y", 1e-24);

    private IStringValue<?> stringValue;
    private Function<String, String> validator = val -> val;
    private boolean numbers = false;
    private String mathFailMessage = null;
    private double defaultNumber = 0;

    protected boolean changedMarkedColor = false;

    public double parse(String num) {
        if (num == null || num.isEmpty()) return 0;
        Expression e = new Expression(num);
        e.addConstants(k, M, G, T, P, E, Z, Y, m, u, n, p, f, a, z, y);
        double result = e.calculate();
        if (Double.isNaN(result)) {
            this.mathFailMessage = e.getErrorMessage();
            result = this.defaultNumber;
        }
        return result;
    }

    public IStringValue<?> createMathFailMessageValue() {
        return new StringValue.Dynamic(() -> this.mathFailMessage, val -> this.mathFailMessage = val);
    }

    @Override
    public void onInit() {
        super.onInit();
        if (this.stringValue == null) {
            this.stringValue = new StringValue("");
        }
        setText(this.stringValue.getStringValue());
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
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        if (syncHandler instanceof IStringValue<?> iStringValue && syncHandler instanceof ValueSyncHandler<?> valueSyncHandler) {
            this.stringValue = iStringValue;
            valueSyncHandler.setChangeListener(() -> {
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
        this.numbers = true;
        setValidator(val -> {
            long num;
            if (val.isEmpty()) {
                num = (long) this.defaultNumber;
            } else {
                num = (long) parse(val);
            }
            return format.format(validator.apply(num));
        });
        return this;
    }

    public TextFieldWidget setNumbers(Function<Integer, Integer> validator) {
        this.numbers = true;
        return setValidator(val -> {
            int num;
            if (val.isEmpty()) {
                num = (int) this.defaultNumber;
            } else {
                num = (int) parse(val);
            }
            return format.format(validator.apply(num));
        });
    }

    public TextFieldWidget setNumbersDouble(Function<Double, Double> validator) {
        this.numbers = true;
        return setValidator(val -> {
            double num;
            if (val.isEmpty()) {
                num = this.defaultNumber;
            } else {
                num = parse(val);
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

    public TextFieldWidget setDefaultNumber(double defaultNumber) {
        this.defaultNumber = defaultNumber;
        return this;
    }

    public TextFieldWidget value(IStringValue<?> stringValue) {
        this.stringValue = stringValue;
        setValue(stringValue);
        return this;
    }
}
