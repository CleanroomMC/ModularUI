package com.cleanroommc.modularui.common.widget.textfield;

import com.cleanroommc.modularui.api.drawable.GuiHelper;
import com.cleanroommc.modularui.api.drawable.TextFieldRenderer;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.api.widget.IWidgetParent;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.api.widget.scroll.IHorizontalScrollable;
import com.cleanroommc.modularui.api.widget.scroll.ScrollType;
import com.cleanroommc.modularui.common.widget.ScrollBar;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The base of a text input widget. Handles mouse/keyboard input and rendering.
 */
public class BaseTextFieldWidget extends Widget implements IWidgetParent, Interactable, IHorizontalScrollable {

    // all positive whole numbers
    public static final Pattern NATURAL_NUMS = Pattern.compile("[0-9]*([+\\-*/%^][0-9]*)*");
    // all positive and negative numbers
    public static final Pattern WHOLE_NUMS = Pattern.compile("-?[0-9]*([+\\-*/%^][0-9]*)*");
    public static final Pattern DECIMALS = Pattern.compile("[0-9]*(\\.[0-9]*)?");
    public static final Pattern LETTERS = Pattern.compile("[a-zA-Z]*");
    public static final Pattern ANY = Pattern.compile(".*");
    private static final Pattern BASE_PATTERN = Pattern.compile("[A-Za-z0-9\\s_+\\-.,!@#$%^&*();\\\\/|<>\"'\\[\\]?=]");

    protected TextFieldHandler handler = new TextFieldHandler();
    protected TextFieldRenderer renderer = new TextFieldRenderer(handler);
    protected Alignment textAlignment = Alignment.TopLeft;
    protected int scrollOffset = 0;
    protected float scale = 1f;
    private int cursorTimer;

    protected ScrollBar scrollBar;

    public BaseTextFieldWidget() {
        this.handler.setRenderer(renderer);
    }

    @Override
    public List<Widget> getChildren() {
        return scrollBar == null ? Collections.emptyList() : Collections.singletonList(scrollBar);
    }

    @Override
    public void onScreenUpdate() {
        if (isFocused() && ++cursorTimer == 10) {
            renderer.toggleCursor();
            cursorTimer = 0;
        }
    }

    @Override
    public void draw(float partialTicks) {
        GuiHelper.useScissor(pos.x, pos.y, size.width, size.height, () -> {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.5f - scrollOffset, 0.5f, 0);
            renderer.setSimulate(false);
            renderer.setScale(scale);
            renderer.setAlignment(textAlignment, -1, size.height);
            renderer.draw(handler.getText());
            GlStateManager.popMatrix();
        });
    }

    @Override
    public boolean shouldGetFocus() {
        this.cursorTimer = 0;
        this.renderer.setCursor(true);
        return true;
    }

    @Override
    public boolean canHover() {
        return true;
    }

    @Override
    public boolean onClick(int buttonId, boolean doubleClick) {
        handler.setCursor(renderer.getCursorPos(handler.getText(), getContext().getCursor().getX() - pos.x + scrollOffset, getContext().getCursor().getY() - pos.y));
        return true;
    }

    @Override
    public void onMouseDragged(int buttonId, long deltaTime) {
        handler.setMainCursor(renderer.getCursorPos(handler.getText(), getContext().getCursor().getX() - pos.x + scrollOffset, getContext().getCursor().getY() - pos.y));
    }

    @Override
    public void onHoverMouseScroll(int direction) {
        if (this.scrollBar != null) {
            this.scrollBar.onHoverMouseScroll(direction);
        }
    }

    @Override
    public boolean onKeyPressed(char character, int keyCode) {
        switch (keyCode) {
            case Keyboard.KEY_RETURN:
                if (getMaxLines() > 1) {
                    handler.newLine();
                } else {
                    removeFocus();
                }
                return true;
            case Keyboard.KEY_ESCAPE:
                removeFocus();
                return true;
            case Keyboard.KEY_LEFT: {
                handler.moveCursorLeft(Interactable.hasControlDown(), Interactable.hasShiftDown());
                return true;
            }
            case Keyboard.KEY_RIGHT: {
                handler.moveCursorRight(Interactable.hasControlDown(), Interactable.hasShiftDown());
                return true;
            }
            case Keyboard.KEY_UP: {
                handler.moveCursorUp(Interactable.hasControlDown(), Interactable.hasShiftDown());
                return true;
            }
            case Keyboard.KEY_DOWN: {
                handler.moveCursorDown(Interactable.hasControlDown(), Interactable.hasShiftDown());
                return true;
            }
            case Keyboard.KEY_DELETE:
                handler.delete(true);
                return true;
            case Keyboard.KEY_BACK:
                handler.delete();
                return true;
        }

        if (character == Character.MIN_VALUE) {
            return false;
        }

        if (GuiScreen.isKeyComboCtrlC(keyCode)) {
            // copy marked text
            GuiScreen.setClipboardString(handler.getSelectedText());
            return true;
        } else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
            // paste copied text in marked text
            handler.insert(GuiScreen.getClipboardString());
            return true;
        } else if (GuiScreen.isKeyComboCtrlX(keyCode) && handler.hasTextMarked()) {
            // copy and delete copied text
            GuiScreen.setClipboardString(handler.getSelectedText());
            handler.delete();
            return true;
        } else if (GuiScreen.isKeyComboCtrlA(keyCode)) {
            // mark whole text
            handler.markAll();
            return true;
        } else if (BASE_PATTERN.matcher(String.valueOf(character)).matches()) {
            // insert typed char
            handler.insert(String.valueOf(character));
            return true;
        }
        return false;
    }

    @Override
    public void onRemoveFocus() {
        super.onRemoveFocus();
        renderer.setCursor(false);
        cursorTimer = 0;
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        return new Size(maxWidth, (int) (renderer.getFontHeight() * getMaxLines() + 0.5));
    }

    @Override
    public void setHorizontalScrollOffset(int offset) {
        if (this.scrollBar != null && this.scrollBar.isActive()) {
            this.scrollOffset = offset;
        } else {
            this.scrollOffset = 0;
        }
    }

    @Override
    public int getHorizontalScrollOffset() {
        return this.scrollOffset;
    }

    @Override
    public int getVisibleWidth() {
        return size.width;
    }

    @Override
    public int getActualWidth() {
        return (int) Math.ceil(renderer.getLastWidth());
    }

    public int getMaxLines() {
        return handler.getMaxLines();
    }

    public BaseTextFieldWidget setTextAlignment(Alignment textAlignment) {
        this.textAlignment = textAlignment;
        return this;
    }

    public BaseTextFieldWidget setScale(float scale) {
        this.scale = scale;
        return this;
    }

    public BaseTextFieldWidget setScrollBar(@Nullable ScrollBar scrollBar) {
        this.scrollBar = scrollBar;
        this.handler.setScrollBar(scrollBar);
        if (this.scrollBar != null) {
            this.scrollBar.setScrollType(ScrollType.HORIZONTAL, this, null);
        }
        return this;
    }

    public BaseTextFieldWidget setTextColor(int color) {
        this.renderer.setColor(color);
        return this;
    }
}
