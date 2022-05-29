package com.cleanroommc.modularui.common.widget.textfield;

import com.cleanroommc.modularui.api.drawable.GuiHelper;
import com.cleanroommc.modularui.api.math.MathExpression;
import com.cleanroommc.modularui.api.widget.ISyncedWidget;
import com.cleanroommc.modularui.common.internal.network.NetworkUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.ParseException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Text input widget with one line only. Can be synced between client and server. Can handle text validation.
 */
public class TextFieldWidget extends BaseTextFieldWidget implements ISyncedWidget {

    private Supplier<String> getter;
    private Consumer<String> setter;
    private Function<String, String> validator = val -> val;
    private boolean syncsToServer = true;
    private boolean syncsToClient = true;
    private boolean init = false;

    public static Number parse(String num) {
        try {
            return format.parse(num);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void onInit() {
        setText(getter.get());
    }

    @Override
    public void draw(float partialTicks) {
        GuiHelper.useScissor(pos.x, pos.y, size.width, size.height, () -> {
            GlStateManager.pushMatrix();
            GlStateManager.translate(1 - scrollOffset, 1, 0);
            renderer.setSimulate(false);
            renderer.setScale(scale);
            renderer.setAlignment(textAlignment, scrollBar == null ? size.width - 2 : -1, size.height);
            renderer.draw(handler.getText());
            GlStateManager.popMatrix();
        });
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
    public void onRemoveFocus() {
        super.onRemoveFocus();
        if (handler.getText().isEmpty()) {
            handler.getText().add(validator.apply(""));
        } else if (handler.getText().size() == 1) {
            handler.getText().set(0, validator.apply(handler.getText().get(0)));
        } else {
            throw new IllegalStateException("TextFieldWidget can only have one line!");
        }
        this.setter.accept(getText());
        if (syncsToServer()) {
            syncToServer(1, buffer -> NetworkUtils.writeStringSafe(buffer, getText()));
        }
    }

    @Override
    public void detectAndSendChanges() {
        if (syncsToClient() && getter != null) {
            String val = getter.get();
            if (!getText().equals(val)) {
                setText(val);
                syncToClient(1, buffer -> NetworkUtils.writeStringSafe(buffer, getText()));
            }
        }
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id == 1) {
            if (!isFocused()) {
                setText(buf.readString(Short.MAX_VALUE));
                if (this.setter != null && (this.getter == null || !getText().equals(this.getter.get()))) {
                    this.setter.accept(getText());
                }
            }
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        if (id == 1) {
            setText(buf.readString(Short.MAX_VALUE));
            if (this.setter != null) {
                this.setter.accept(getText());
            }
        }
    }

    /**
     * @return if this widget should operate on the sever side.
     * For example detecting and sending changes to client.
     */
    public boolean syncsToClient() {
        return syncsToClient;
    }

    /**
     * @return if this widget should operate on the client side.
     * For example, sending a changed value to the server.
     */
    public boolean syncsToServer() {
        return syncsToServer;
    }

    /**
     * Determines how this widget should sync values
     *
     * @param syncsToClient if this widget should sync changes to the server
     * @param syncsToServer if this widget should detect changes on server and sync them to client
     */
    public TextFieldWidget setSynced(boolean syncsToClient, boolean syncsToServer) {
        this.syncsToClient = syncsToClient;
        this.syncsToServer = syncsToServer;
        return this;
    }

    public TextFieldWidget setMaxLength(int maxLength) {
        this.handler.setMaxCharacters(maxLength);
        return this;
    }

    public TextFieldWidget setSetter(Consumer<String> setter) {
        this.setter = setter;
        return this;
    }

    public TextFieldWidget setSetterLong(Consumer<Long> setter) {
        this.setter = val -> {
            if (!val.isEmpty()) {
                setter.accept(parse(val).longValue());
            }
        };
        return this;
    }

    public TextFieldWidget setSetterInt(Consumer<Integer> setter) {
        this.setter = val -> {
            if (!val.isEmpty()) {
                setter.accept(parse(val).intValue());
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
                num = (long) MathExpression.parseMathExpression(val);
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
                num = (int) MathExpression.parseMathExpression(val);
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
                num = MathExpression.parseMathExpression(val);
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
