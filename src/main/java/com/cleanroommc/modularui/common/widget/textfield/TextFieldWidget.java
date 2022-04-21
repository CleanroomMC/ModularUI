package com.cleanroommc.modularui.common.widget.textfield;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.widget.ISyncedWidget;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class TextFieldWidget extends BaseTextFieldWidget implements ISyncedWidget {

    private Supplier<String> getter;
    private Consumer<String> setter;
    private Function<String, String> validator = val -> val;
    private Pattern pattern = ANY;

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {

    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {

    }

    public TextFieldWidget setSetter(Consumer<String> setter) {
        this.setter = setter;
        return this;
    }

    public TextFieldWidget setSetterLong(Consumer<Long> setter) {
        this.setter = val -> {
            if (!val.isEmpty()) {
                try {
                    setter.accept(Long.parseLong(val));
                } catch (NumberFormatException e) {
                    ModularUI.LOGGER.warn("Error parsing text field value to long: {}", val);
                }
            }
        };
        return this;
    }

    public TextFieldWidget setSetterInt(Consumer<Integer> setter) {
        this.setter = val -> {
            if (!val.isEmpty()) {
                try {
                    setter.accept(Integer.parseInt(val));
                } catch (NumberFormatException e) {
                    ModularUI.LOGGER.warn("Error parsing text field value to int: {}", val);
                }
            }
        };
        return this;
    }

    public TextFieldWidget setGetter(Supplier<String> getter) {
        this.getter = getter;
        return this;
    }

    public TextFieldWidget setGetterLong(Supplier<Long> getter) {
        this.getter = () -> String.valueOf(getter.get());
        return this;
    }

    public TextFieldWidget setGetterInt(Supplier<Integer> getter) {
        this.getter = () -> String.valueOf(getter.get());
        return this;
    }

    public TextFieldWidget setPattern(Pattern pattern) {
        this.pattern = pattern;
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
                    num = Long.parseLong(val);
                } catch (NumberFormatException e) {
                    num = 0;
                }
            }
            return String.valueOf(validator.apply(num));
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
                    num = Integer.parseInt(val);
                } catch (NumberFormatException e) {
                    num = 0;
                }
            }
            return String.valueOf(validator.apply(num));
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
                    num = Double.parseDouble(val);
                } catch (NumberFormatException e) {
                    num = 0;
                }
            }
            return String.valueOf(validator.apply(num));
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
