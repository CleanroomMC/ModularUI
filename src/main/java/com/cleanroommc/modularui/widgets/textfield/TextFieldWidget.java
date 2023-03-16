package com.cleanroommc.modularui.widgets.textfield;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.IValue;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.sync.IStringSyncHandler;
import com.cleanroommc.modularui.api.sync.SyncHandler;
import com.cleanroommc.modularui.api.sync.ValueSyncHandler;
import com.cleanroommc.modularui.screen.GuiContext;
import com.cleanroommc.modularui.utils.math.Constant;
import com.cleanroommc.modularui.utils.math.MathBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.text.ParsePosition;
import java.util.function.*;
import java.util.regex.Pattern;

/**
 * Text input widget with one line only. Can be synced between client and server. Can handle text validation.
 */
public class TextFieldWidget extends BaseTextFieldWidget<TextFieldWidget> {

    private IStringSyncHandler<?> syncHandler;
    private Function<String, String> validator = val -> val;

    @Nullable
    private Supplier<String> getter;
    @Nullable
    private Consumer<String> setter;

    public static IValue parse(String num) {
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
        if (this.getter != null) {
            setText(getter.get());
        }
        if (!hasTooltip()) {
            tooltip().excludeArea(getArea());
            tooltipBuilder(tooltip -> {
                tooltip.addLine(IKey.str(getText()));
            });
        }
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        if (syncHandler instanceof IStringSyncHandler && syncHandler instanceof ValueSyncHandler) {
            this.syncHandler = (IStringSyncHandler<?>) syncHandler;
            ((ValueSyncHandler<?>) this.syncHandler).setChangeListener(() -> {
                markDirty();
                setText(this.syncHandler.getCachedValue().toString());
            });
            return true;
        }
        return false;
    }

    @Override
    public void onFrameUpdate() {
        super.onFrameUpdate();
        if (!isFocused() && this.getter != null) {
            String s = this.getter.get();
            if (!getText().equals(s)) {
                setText(s);
            }
        }
    }

    @Override
    public void drawText(GuiContext context) {
        renderer.setSimulate(false);
        renderer.setPos(getArea().getPadding().left, 0);
        renderer.setScale(scale);
        renderer.setAlignment(textAlignment, -1, getArea().height);
        renderer.draw(handler.getText());
        getScrollData().scrollSize = Math.max(0, (int) renderer.getLastWidth());
    }

    @Override
    public void drawForeground(GuiContext context) {
        if (hasTooltip() && getScrollData().isScrollBarActive(getScrollArea()) && isHoveringFor(getTooltip().getShowUpTimer())) {
            getTooltip().draw(getContext());
        }
    }

    @NotNull
    public String getText() {
        if (handler.getText().isEmpty()) {
            return "";
        }
        if (handler.getText().size() > 1) {
            throw new IllegalStateException("TextFieldWidget can only have one line!");
        }
        return handler.getText().get(0);
    }

    public void setText(@NotNull String text) {
        if (handler.getText().isEmpty()) {
            handler.getText().add(text);
        } else {
            handler.getText().set(0, text);
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
        if (handler.getText().isEmpty()) {
            handler.getText().add(validator.apply(""));
        } else if (handler.getText().size() == 1) {
            handler.getText().set(0, validator.apply(handler.getText().get(0)));
        } else {
            throw new IllegalStateException("TextFieldWidget can only have one line!");
        }
        if (this.syncHandler != null) {
            if (this.syncHandler.getCachedValue() instanceof Number) {
                this.syncHandler.updateFromClient(format.parse(getText(), new ParsePosition(0)).toString());
            } else {
                this.syncHandler.updateFromClient(getText());
            }
        }
        if (this.setter != null) {
            this.setter.accept(getText());
        }
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
        handler.setPattern(pattern);
        return this;
    }

    public TextFieldWidget setTextColor(int textColor) {
        this.renderer.setColor(textColor);
        return this;
    }

    public TextFieldWidget setMarkedColor(int color) {
        this.renderer.setMarkedColor(color);
        return this;
    }

    public TextFieldWidget setValidator(Function<String, String> validator) {
        this.validator = validator;
        return this;
    }

    public TextFieldWidget setNumbersLong(Function<Long, Long> validator) {
        //setPattern(WHOLE_NUMS);
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

    public TextFieldWidget getter(Supplier<String> getter) {
        this.getter = getter;
        return this;
    }

    public TextFieldWidget setter(Consumer<String> setter) {
        this.setter = setter;
        return this;
    }

    public TextFieldWidget getterLong(LongSupplier getter) {
        this.getter = () -> String.valueOf(getter.getAsLong());
        return this;
    }

    public TextFieldWidget setterLong(LongConsumer setter) {
        this.setter = val -> {
            try {
                setter.accept(Long.parseLong(val));
            } catch (NumberFormatException e) {
                ModularUI.LOGGER.catching(e);
                setter.accept(0);
            }
        };
        return this;
    }

    public TextFieldWidget getterDouble(DoubleSupplier getter) {
        this.getter = () -> String.valueOf(getter.getAsDouble());
        return this;
    }

    public TextFieldWidget setterDouble(DoubleConsumer setter) {
        this.setter = val -> {
            try {
                setter.accept(Double.parseDouble(val));
            } catch (NumberFormatException e) {
                ModularUI.LOGGER.catching(e);
                setter.accept(0);
            }
        };
        return this;
    }
}
