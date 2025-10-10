package com.cleanroommc.modularui.widgets.textinput;

import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.widget.IFocusedWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.widget.Widget;

import net.minecraft.client.gui.GuiScreen;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

public abstract class AbstractTextInput<W extends AbstractTextInput<W>> extends Widget<W> implements Interactable, IFocusedWidget {

    protected static final int CURSOR_BLINK_RATE = 10;

    protected final ITextInputHandler handler;
    protected int cursorTimer = 0;

    protected boolean focusOnGuiOpen;

    protected AbstractTextInput(ITextInputHandler handler) {
        this.handler = handler;
    }

    public String getText() {
        return this.handler.getText();
    }

    @Override
    public void onInit() {
        super.onInit();
        //this.handler.setGuiContext(getContext());
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
            //this.renderer.toggleCursor();
            this.cursorTimer = 0;
        }
    }

    protected abstract void onKeyReturn();

    @Override
    public @NotNull Result onKeyPressed(char character, int keyCode) {
        if (!isFocused()) {
            return Result.IGNORE;
        }
        switch (keyCode) {
            case Keyboard.KEY_NUMPADENTER:
            case Keyboard.KEY_RETURN:
                onKeyReturn();
                return Result.SUCCESS;
            case Keyboard.KEY_ESCAPE:
                if (ModularUIConfig.escRestoreLastText) {
                    this.handler.clearAll();
                    //this.handler.insert(this.lastText, canScrollHorizontally());
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

    public W focusOnGuiOpen() {
        this.focusOnGuiOpen = true;
        return getThis();
    }

}
