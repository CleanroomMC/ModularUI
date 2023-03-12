package com.cleanroommc.modularui.widgets.textfield;

import com.cleanroommc.modularui.api.widget.IFocusedWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.GuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.ScrollData;
import com.cleanroommc.modularui.utils.ScrollDirection;
import com.cleanroommc.modularui.widget.ScrollWidget;
import net.minecraft.client.gui.GuiScreen;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The base of a text input widget. Handles mouse/keyboard input and rendering.
 */
public class BaseTextFieldWidget<W extends BaseTextFieldWidget<W>> extends ScrollWidget<W> implements IFocusedWidget {

    public static final DecimalFormat format = new DecimalFormat("###.###");

    // all positive whole numbers
    public static final Pattern NATURAL_NUMS = Pattern.compile("[0-9]*([+\\-*/%^][0-9]*)*");
    // all positive and negative numbers
    public static final Pattern WHOLE_NUMS = Pattern.compile("-?[0-9]*([+\\-*/%^][0-9]*)*");
    public static final Pattern DECIMALS = Pattern.compile("[0-9]*(" + getDecimalSeparator() + "[0-9]*)?([+\\-*/%^][0-9]*(" + getDecimalSeparator() + "[0-9]*)?)*");
    public static final Pattern LETTERS = Pattern.compile("[a-zA-Z]*");
    public static final Pattern ANY = Pattern.compile(".*");
    private static final Pattern BASE_PATTERN = Pattern.compile("[A-Za-z0-9\\s_+\\-.,!@#$%^&*();\\\\/|<>\"'\\[\\]?=]");

    protected TextFieldHandler handler = new TextFieldHandler(this);
    protected TextFieldRenderer renderer = new TextFieldRenderer(handler);
    protected Alignment textAlignment = Alignment.CenterLeft;
    protected int scrollOffset = 0;
    protected float scale = 1f;
    private int cursorTimer;

    public BaseTextFieldWidget() {
        super(new ScrollData(ScrollDirection.HORIZONTAL));
        this.handler.setRenderer(renderer);
        this.handler.setScrollArea(getScrollArea());
        getScrollArea().getScrollX().scrollItemSize = 4;
        padding(4, 0);
    }

    @Override
    public @NotNull List<IWidget> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public boolean addChild(IWidget child, int index) {
        return false;
    }

    @Override
    public void onInit() {
        super.onInit();
        this.handler.setGuiContext(getContext());
    }

    @Override
    public void onFrameUpdate() {
        super.onFrameUpdate();
        if (isFocused() && ++cursorTimer == 30) {
            renderer.toggleCursor();
            cursorTimer = 0;
        }
    }

    @Override
    public void preDraw(GuiContext context) {
        super.preDraw(context);
        drawText(context);
    }

    public void drawText(GuiContext context) {
        renderer.setSimulate(false);
        renderer.setScale(scale);
        renderer.setAlignment(textAlignment, -2, getArea().height);
        renderer.draw(handler.getText());
        getScrollArea().getScrollX().scrollSize = Math.max(0, (int) (renderer.getLastWidth() + 0.5f));
    }

    @Override
    public boolean isFocused() {
        return getContext().isFocused(this);
    }

    @Override
    public void onFocus(GuiContext context) {
        this.cursorTimer = 0;
        this.renderer.setCursor(true);
    }

    @Override
    public void onRemoveFocus(GuiContext context) {
        this.renderer.setCursor(false);
        this.cursorTimer = 0;
        this.scrollOffset = 0;
        this.handler.setCursor(0, 0, true, true);
    }

    @Override
    public void selectAll(GuiContext context) {

    }

    @Override
    public void unselect(GuiContext context) {
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        Result result = super.onMousePressed(mouseButton);
        if (result != Result.IGNORE) {
            return result;
        }
        if (!isHovering()) {
            return Result.IGNORE;
        }
        handler.setCursor(renderer.getCursorPos(handler.getText(), getContext().getMouseX() - getArea().x, getContext().getMouseY() - getArea().y), true);
        return Result.SUCCESS;
    }

    @Override
    public void onMouseDrag(int mouseButton, long timeSinceClick) {
        if (isFocused()) {
            handler.setMainCursor(renderer.getCursorPos(handler.getText(), getContext().getMouseX() - getArea().x, getContext().getMouseY() - getArea().y), true);
        }
    }

    @Override
    public @NotNull Result onKeyPressed(char character, int keyCode) {
        if (!isFocused()) {
            return Result.IGNORE;
        }
        switch (keyCode) {
            case Keyboard.KEY_RETURN:
                if (getMaxLines() > 1) {
                    handler.newLine();
                } else {
                    getContext().removeFocus();
                }
                return Result.SUCCESS;
            case Keyboard.KEY_ESCAPE:
                getContext().removeFocus();
                return Result.SUCCESS;
            case Keyboard.KEY_LEFT: {
                handler.moveCursorLeft(Interactable.hasControlDown(), Interactable.hasShiftDown());
                return Result.SUCCESS;
            }
            case Keyboard.KEY_RIGHT: {
                handler.moveCursorRight(Interactable.hasControlDown(), Interactable.hasShiftDown());
                return Result.SUCCESS;
            }
            case Keyboard.KEY_UP: {
                handler.moveCursorUp(Interactable.hasControlDown(), Interactable.hasShiftDown());
                return Result.SUCCESS;
            }
            case Keyboard.KEY_DOWN: {
                handler.moveCursorDown(Interactable.hasControlDown(), Interactable.hasShiftDown());
                return Result.SUCCESS;
            }
            case Keyboard.KEY_DELETE:
                handler.delete(true);
                return Result.SUCCESS;
            case Keyboard.KEY_BACK:
                handler.delete();
                return Result.SUCCESS;
        }

        if (character == Character.MIN_VALUE) {
            return Result.STOP;
        }

        if (GuiScreen.isKeyComboCtrlC(keyCode)) {
            // copy marked text
            GuiScreen.setClipboardString(handler.getSelectedText());
            return Result.SUCCESS;
        } else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
            // paste copied text in marked text
            handler.insert(GuiScreen.getClipboardString());
            return Result.SUCCESS;
        } else if (GuiScreen.isKeyComboCtrlX(keyCode) && handler.hasTextMarked()) {
            // copy and delete copied text
            GuiScreen.setClipboardString(handler.getSelectedText());
            handler.delete();
            return Result.SUCCESS;
        } else if (GuiScreen.isKeyComboCtrlA(keyCode)) {
            // mark whole text
            handler.markAll();
            return Result.SUCCESS;
        } else if (BASE_PATTERN.matcher(String.valueOf(character)).matches()) {
            // insert typed char
            handler.insert(String.valueOf(character));
            return Result.SUCCESS;
        }
        return Result.STOP;
    }

    public int getMaxLines() {
        return handler.getMaxLines();
    }

    public ScrollData getScrollData() {
        return getScrollArea().getScrollX();
    }

    public W setTextAlignment(Alignment textAlignment) {
        this.textAlignment = textAlignment;
        return getThis();
    }

    public W setScale(float scale) {
        this.scale = scale;
        return getThis();
    }

    /*public W setScrollBar() {
        return setScrollBar(0);
    }

    public W setScrollBar(int posOffset) {
        return setScrollBar(ScrollBar.defaultTextScrollBar().setPosOffset(posOffset));
    }

    public W setScrollBar(@Nullable ScrollBar scrollBar) {
        this.scrollBar = scrollBar;
        this.handler.setScrollBar(scrollBar);
        if (this.scrollBar != null) {
            this.scrollBar.setScrollType(ScrollType.HORIZONTAL, this, null);
        }
        return getThis();
    }*/

    public W setTextColor(int color) {
        this.renderer.setColor(color);
        return getThis();
    }

    public static char getDecimalSeparator() {
        return format.getDecimalFormatSymbols().getDecimalSeparator();
    }

    public static char getGroupSeparator() {
        return format.getDecimalFormatSymbols().getGroupingSeparator();
    }
}
