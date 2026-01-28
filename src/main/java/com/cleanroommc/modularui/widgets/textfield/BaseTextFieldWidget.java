package com.cleanroommc.modularui.widgets.textfield;

import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.widget.IFocusedWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.TextFieldTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.AbstractScrollWidget;
import com.cleanroommc.modularui.widget.scroll.HorizontalScrollData;
import com.cleanroommc.modularui.widget.scroll.ScrollData;
import com.cleanroommc.modularui.widgets.VoidWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The base of a text input widget. Handles mouse/keyboard input and rendering.
 */
public class BaseTextFieldWidget<W extends BaseTextFieldWidget<W>> extends AbstractScrollWidget<VoidWidget, W> implements IFocusedWidget {

    public static final DecimalFormat format = new DecimalFormat("###.###");

    // all positive whole numbers
    public static final Pattern NATURAL_NUMS = Pattern.compile("[0-9]*([+\\-*/%^][0-9]*)*");
    // all positive and negative numbers
    public static final Pattern WHOLE_NUMS = Pattern.compile("-?[0-9]*([+\\-*/%^][0-9]*)*");
    public static final Pattern DECIMALS = Pattern.compile("[0-9]*(" + getDecimalSeparator() + "[0-9]*)?([+\\-*/%^][0-9]*(" + getDecimalSeparator() + "[0-9]*)?)*");
    public static final Pattern LETTERS = Pattern.compile("[a-zA-Z]*");
    public static final Pattern ANY = Pattern.compile(".*");
    private static final Pattern BASE_PATTERN = Pattern.compile("[^§]");

    private static final int CURSOR_BLINK_RATE = 10;
    private static final int DOUBLE_CLICK_THRESHOLD = 300; // max time between clicks to count as double-click in ms

    protected TextFieldHandler handler = new TextFieldHandler(this);
    protected TextFieldRenderer renderer = new TextFieldRenderer(this.handler);
    protected Alignment textAlignment = Alignment.CenterLeft;
    protected List<String> lastText;
    protected int scrollOffset = 0;
    protected float scale = 1f;
    protected boolean focusOnGuiOpen;
    private int cursorTimer;
    protected long lastClickTime = 0;

    protected Integer textColor;
    protected Integer markedColor;
    protected String hintText = null;
    protected Integer hintTextColor;

    public BaseTextFieldWidget() {
        super(new HorizontalScrollData(false, 4), null);
        this.handler.setRenderer(this.renderer);
        this.handler.setScrollArea(getScrollArea());
        padding(4, 0);
    }

    @Override
    public @NotNull List<IWidget> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public boolean isChildValid(VoidWidget child) {
        return false;
    }

    @Override
    public void onInit() {
        super.onInit();
        this.handler.setGuiContext(getContext());
    }

    @Override
    public void afterInit() {
        super.afterInit();
        if (this.focusOnGuiOpen) {
            getContext().focus(this);
            this.handler.markAll();
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (isFocused() && ++this.cursorTimer == CURSOR_BLINK_RATE) {
            this.renderer.toggleCursor();
            this.cursorTimer = 0;
        }
    }

    @Override
    public void preDraw(ModularGuiContext context, boolean transformed) {
        if (transformed) {
            WidgetThemeEntry<TextFieldTheme> entry = getWidgetTheme(getPanel().getTheme(), TextFieldTheme.class);
            TextFieldTheme widgetTheme = entry.getTheme();
            this.renderer.setColor(this.textColor != null ? this.textColor : widgetTheme.getTextColor());
            this.renderer.setCursorColor(this.textColor != null ? this.textColor : widgetTheme.getTextColor());
            this.renderer.setMarkedColor(this.markedColor != null ? this.markedColor : widgetTheme.getMarkedColor());
            setupDrawText(context, widgetTheme);
            drawText(context, widgetTheme);
        } else {
            Stencil.apply(1, 1, getArea().w() - 2, getArea().h() - 2, context);
        }
    }

    protected void setupDrawText(ModularGuiContext context, TextFieldTheme widgetTheme) {
        this.renderer.setSimulate(false);
        this.renderer.setPos(getArea().getPadding().getLeft(), getArea().getPadding().getTop());
        this.renderer.setScale(this.scale);
        this.renderer.setAlignment(this.textAlignment, getArea().paddedWidth(), getArea().paddedHeight());
    }

    protected void drawText(ModularGuiContext context, TextFieldTheme widgetTheme) {
        if (this.handler.isTextEmpty() && this.hintText != null) {
            int c = this.renderer.getColor();
            int hintColor = this.hintTextColor != null ? this.hintTextColor : widgetTheme.getHintColor();
            this.renderer.setColor(hintColor);
            this.renderer.draw(Collections.singletonList(this.hintText));
            this.renderer.setColor(c);
        } else {
            this.renderer.draw(this.handler.getText());
        }
        getScrollArea().getScrollX().setScrollSize(Math.max(0, (int) (this.renderer.getLastActualWidth() + 0.5f)));
    }

    @Override
    public WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getTextFieldTheme();
    }

    @Override
    public boolean isFocused() {
        return getContext().isFocused(this);
    }

    @Override
    public void onFocus(ModularGuiContext context) {
        this.cursorTimer = 0;
        this.renderer.setCursor(true);
        this.lastText = new ArrayList<>(this.handler.getText());
    }

    @Override
    public void onRemoveFocus(ModularGuiContext context) {
        this.renderer.setCursor(false);
        this.cursorTimer = 0;
        this.scrollOffset = 0;
        this.handler.setCursor(0, 0, true, true);
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        Result result = super.onMousePressed(mouseButton);
        if (result != Result.IGNORE) {
            return Result.SUCCESS; // keep focused
        }
        if (!isHovering()) {
            return Result.IGNORE;
        }
        if (mouseButton == 1) {
            this.handler.clear();
        } else {
            // the current transformation does not include the transformation of the children (the scroll) so we need to manually transform here
            int x = getContext().getMouseX() + getScrollX();
            int y = getContext().getMouseY() + getScrollY();
            long now = Minecraft.getSystemTime();
            if (this.lastClickTime < 0) {
                // triple click
                if (now + this.lastClickTime < DOUBLE_CLICK_THRESHOLD) {
                    this.handler.markAll();
                    this.lastClickTime = 0;
                    return Result.SUCCESS;
                }
                this.lastClickTime = 0;
            } else if (this.lastClickTime > 0) {
                // double click
                if (now - this.lastClickTime < DOUBLE_CLICK_THRESHOLD) {
                    this.handler.markCurrentLine();
                    this.lastClickTime = -Minecraft.getSystemTime();
                    return Result.SUCCESS;
                }
                this.lastClickTime = 0;
            }
            // single click
            this.handler.setCursor(this.renderer.getCursorPos(this.handler.getText(), x, y), true);
            this.lastClickTime = Minecraft.getSystemTime();
        }
        return Result.SUCCESS;
    }

    @Override
    public void onMouseDrag(int mouseButton, long timeSinceClick) {
        super.onMouseDrag(mouseButton, timeSinceClick);
        if (isFocused() && !getScrollArea().isDragging()) {
            int x = getContext().getMouseX() + getScrollX();
            int y = getContext().getMouseY() + getScrollY();
            this.handler.setMainCursor(this.renderer.getCursorPos(this.handler.getText(), x, y), true);
        }
    }

    @Override
    public @NotNull Result onKeyPressed(char character, int keyCode) {
        if (!isFocused()) {
            return Result.IGNORE;
        }
        switch (keyCode) {
            case Keyboard.KEY_NUMPADENTER:
            case Keyboard.KEY_RETURN:
                if (getMaxLines() > 1) {
                    this.handler.newLine();
                } else {
                    getContext().removeFocus();
                }
                return Result.SUCCESS;
            case Keyboard.KEY_ESCAPE:
                if (ModularUIConfig.escRestoreLastText) {
                    this.handler.clear();
                    this.handler.insert(this.lastText, canScrollHorizontally());
                }
                getContext().removeFocus();
                return Result.SUCCESS;
            case Keyboard.KEY_LEFT: {
                this.handler.moveCursorLeft(Interactable.hasControlDown(), Interactable.hasShiftDown());
                return Result.SUCCESS;
            }
            case Keyboard.KEY_RIGHT: {
                this.handler.moveCursorRight(Interactable.hasControlDown(), Interactable.hasShiftDown());
                return Result.SUCCESS;
            }
            case Keyboard.KEY_UP: {
                this.handler.moveCursorUp(Interactable.hasControlDown(), Interactable.hasShiftDown());
                return Result.SUCCESS;
            }
            case Keyboard.KEY_DOWN: {
                this.handler.moveCursorDown(Interactable.hasControlDown(), Interactable.hasShiftDown());
                return Result.SUCCESS;
            }
            case Keyboard.KEY_DELETE:
                this.handler.delete(true);
                return Result.SUCCESS;
            case Keyboard.KEY_BACK:
                this.handler.delete();
                return Result.SUCCESS;
        }

        if (character == Character.MIN_VALUE) {
            return Result.STOP;
        }

        if (GuiScreen.isKeyComboCtrlC(keyCode)) {
            // copy marked text
            GuiScreen.setClipboardString(this.handler.getSelectedText());
            return Result.SUCCESS;
        } else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
            if (this.handler.hasTextMarked()) {
                this.handler.delete();
            }
            // paste copied text in marked text
            this.handler.insert(GuiScreen.getClipboardString().replace("§", ""), canScrollHorizontally());
            return Result.SUCCESS;
        } else if (GuiScreen.isKeyComboCtrlX(keyCode) && this.handler.hasTextMarked()) {
            // copy and delete copied text
            GuiScreen.setClipboardString(this.handler.getSelectedText());
            this.handler.delete();
            return Result.SUCCESS;
        } else if (GuiScreen.isKeyComboCtrlA(keyCode)) {
            // mark whole text
            this.handler.markAll();
            return Result.SUCCESS;
        } else if (BASE_PATTERN.matcher(String.valueOf(character)).matches() && handler.test(String.valueOf(character))) {
            if (this.handler.hasTextMarked()) {
                this.handler.delete();
            }
            // insert typed char
            this.handler.insert(String.valueOf(character), canScrollHorizontally());
            return Result.SUCCESS;
        }
        return Result.STOP;
    }

    public boolean canScrollHorizontally() {
        return getScrollArea().getScrollX() != null;
    }

    public int getMaxLines() {
        return this.handler.getMaxLines();
    }

    public ScrollData getScrollData() {
        return getScrollArea().getScrollX();
    }

    public List<String> getLastText() {
        return lastText;
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
        this.textColor = color;
        return getThis();
    }

    public W setMarkedColor(int color) {
        this.markedColor = color;
        return getThis();
    }

    public W setFocusOnGuiOpen(boolean focusOnGuiOpen) {
        this.focusOnGuiOpen = focusOnGuiOpen;
        return getThis();
    }

    /**
     * Sets a constant hint text. The hint is displayed in a less noticeable color when the field is empty.
     * The color is by default obtained from the current them, but can be overriden with {@link #hintColor(int)}.
     *
     * @param hint hint text to display
     * @return this
     */
    public W hintText(String hint) {
        this.hintText = hint;
        return getThis();
    }

    public W hintColor(int color) {
        this.hintTextColor = color;
        return getThis();
    }

    public static char getDecimalSeparator() {
        return format.getDecimalFormatSymbols().getDecimalSeparator();
    }

    public static char getGroupSeparator() {
        return format.getDecimalFormatSymbols().getGroupingSeparator();
    }
}
