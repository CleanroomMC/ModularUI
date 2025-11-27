package com.cleanroommc.modularui.widgets.textfield;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.drawable.ITextLine;
import com.cleanroommc.modularui.api.value.IStringValue;
import com.cleanroommc.modularui.api.value.IValue;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.utils.MathUtils;
import com.cleanroommc.modularui.utils.ParseResult;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParsePosition;
import java.util.function.Consumer;
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
    private String mathFailMessage = null;
    private double defaultNumber = 0;
    private boolean tooltipOverride = false;

    public double parse(String num) {
        ParseResult result = MathUtils.parseExpression(num, this.defaultNumber, true);
        double value = result.getResult();
        if (result.isFailure()) {
            this.mathFailMessage = result.getError();
            ModularUI.LOGGER.error("Math expression error in {}: {}", this, this.mathFailMessage);
        }
        return value;
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
        if (!hasTooltip() && !tooltipOverride) {
            tooltipBuilder(tooltip -> tooltip.addLine(IKey.str(getText())));
            // set back to false so this won't get triggered
            tooltipOverride = false;
        }
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        return syncHandler instanceof IStringValue<?>;
    }

    @Override
    protected void setSyncHandler(@Nullable SyncHandler syncHandler) {
        super.setSyncHandler(syncHandler);
        if (syncHandler instanceof ValueSyncHandler<?> valueSyncHandler) {
            valueSyncHandler.setChangeListener(() -> {
                markTooltipDirty();
                setText(this.stringValue.getValue().toString());
            });
        }
    }

    @Override
    protected void setValue(IValue<?> value) {
        super.setValue(value);
        if (value instanceof IStringValue<?> stringValue1) {
            this.stringValue = stringValue1;
        }
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
    public void drawForeground(ModularGuiContext context) {
        if (hasTooltip() && (tooltipOverride || getScrollData().isScrollBarActive(getScrollArea())) && isHoveringFor(getTooltip().getShowUpTimer())) {
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

    public String getMathFailMessage() {
        return mathFailMessage;
    }

    public TextFieldWidget setMaxLength(int maxLength) {
        this.handler.setMaxCharacters(maxLength);
        return this;
    }

    public TextFieldWidget setPattern(Pattern pattern) {
        this.handler.setPattern(pattern);
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

    /**
     * Normally, Tooltips on text field widgets are used to display the contents of the widget when the scrollbar is active
     * This value is an override, that allows the methods provided by {@link com.cleanroommc.modularui.api.widget.ITooltip} to be used
     * Every method that adds a tooltip from ITooltip is overridden to enable the tooltipOverride
     *
     * @param value - sets the tooltip override on or off
     */
    public TextFieldWidget setTooltipOverride(boolean value) {
        this.tooltipOverride = value;
        return this;
    }

    @Override
    public TextFieldWidget tooltipBuilder(Consumer<RichTooltip> tooltipBuilder) {
        tooltipOverride = true;
        return super.tooltipBuilder(tooltipBuilder);
    }

    @Override
    public TextFieldWidget tooltip(RichTooltip tooltip) {
        tooltipOverride = true;
        return super.tooltip(tooltip);
    }

    @Override
    public TextFieldWidget tooltip(Consumer<RichTooltip> tooltipConsumer) {
        tooltipOverride = true;
        return super.tooltip(tooltipConsumer);
    }

    @Override
    public @NotNull RichTooltip tooltip() {
        tooltipOverride = true;
        return super.tooltip();
    }

    @Override
    public TextFieldWidget tooltipDynamic(Consumer<RichTooltip> tooltipBuilder) {
        tooltipOverride = true;
        return super.tooltipDynamic(tooltipBuilder);
    }

    @Override
    public TextFieldWidget addTooltipDrawableLines(Iterable<IDrawable> lines) {
        tooltipOverride = true;
        return super.addTooltipDrawableLines(lines);
    }

    @Override
    public TextFieldWidget addTooltipElement(String s) {
        tooltipOverride = true;
        return super.addTooltipElement(s);
    }

    @Override
    public TextFieldWidget addTooltipElement(IDrawable drawable) {
        tooltipOverride = true;
        return super.addTooltipElement(drawable);
    }

    @Override
    public TextFieldWidget addTooltipLine(String line) {
        tooltipOverride = true;
        return super.addTooltipLine(line);
    }

    @Override
    public TextFieldWidget addTooltipLine(ITextLine line) {
        tooltipOverride = true;

        return super.addTooltipLine(line);
    }

    @Override
    public TextFieldWidget addTooltipLine(IDrawable drawable) {
        tooltipOverride = true;
        return super.addTooltipLine(drawable);
    }

    @Override
    public TextFieldWidget addTooltipStringLines(Iterable<String> lines) {
        tooltipOverride = true;
        return super.addTooltipStringLines(lines);
    }
}
