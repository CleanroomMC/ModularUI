package com.cleanroommc.modularui.widgets.textfield;

import com.cleanroommc.modularui.api.SyncHandler;
import com.cleanroommc.modularui.screen.GuiContext;
import com.cleanroommc.modularui.sync.StringSyncHandler;
import com.cleanroommc.modularui.utils.math.MathBuilder;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Text input widget with one line only. Can be synced between client and server. Can handle text validation.
 */
public class TextFieldWidget extends BaseTextFieldWidget<TextFieldWidget> {

    private StringSyncHandler syncHandler;
    private Function<String, String> validator = val -> val;

    public static Number parse(String num) {
        try {
            return format.parse(num);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        if (syncHandler instanceof StringSyncHandler) {
            this.syncHandler = (StringSyncHandler) syncHandler;
            return true;
        }
        return false;
    }

    @Override
    public void onFrameUpdate() {
        super.onFrameUpdate();
        if (this.syncHandler != null) {
            String current = getText();
            if (!current.equals(this.syncHandler.getCachedValue())) {
                setText(current);
            }
        }
    }

    @Override
    protected void preDraw(GuiContext context) {
        //GlStateManager.pushMatrix();
        //GlStateManager.translate(1 - scrollOffset, 1, 0);
        renderer.setSimulate(false);
        renderer.setPos(getArea().getPadding().left, 0);
        renderer.setScale(scale);
        //renderer.setAlignment(textAlignment, getScrollArea() == null ? getArea().width - 2 : -1, getArea().height);
        renderer.setAlignment(textAlignment, -1, getArea().height);
        renderer.draw(handler.getText());
        getScrollArea().scrollSize = Math.max(0, (int) renderer.getLastWidth());
        //GlStateManager.popMatrix();
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
    public void onRemoveFocus(GuiContext context) {
        super.onRemoveFocus(context);
        if (handler.getText().isEmpty()) {
            handler.getText().add(validator.apply(""));
        } else if (handler.getText().size() == 1) {
            handler.getText().set(0, validator.apply(handler.getText().get(0)));
        } else {
            throw new IllegalStateException("TextFieldWidget can only have one line!");
        }
        this.syncHandler.updateFromClient(getText());
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
        setPattern(WHOLE_NUMS);
        setValidator(val -> {
            long num;
            if (val.isEmpty()) {
                num = 0;
            } else {
                try {
                    num = (long) MathBuilder.INSTANCE.parse(val).doubleValue();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return format.format(validator.apply(num));
        });
        return this;
    }

    public TextFieldWidget setNumbers(Function<Integer, Integer> validator) {
        setPattern(WHOLE_NUMS);
        return setValidator(val -> {
            int num;
            if (val.isEmpty()) {
                num = 0;
            } else {
                try {
                    num = (int) MathBuilder.INSTANCE.parse(val).doubleValue();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return format.format(validator.apply(num));
        });
    }

    public TextFieldWidget setNumbersDouble(Function<Double, Double> validator) {
        setPattern(DECIMALS);
        return setValidator(val -> {
            double num;
            if (val.isEmpty()) {
                num = 0;
            } else {
                try {
                    num = MathBuilder.INSTANCE.parse(val).doubleValue();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
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

}
