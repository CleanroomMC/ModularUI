package com.cleanroommc.modularui.widgets.textinput;

import org.jetbrains.annotations.Nullable;

public class SingleLineTextInputHandler implements ITextInputHandler {

    private final Cursor main = new Cursor();
    private final Cursor secondary = new Cursor();
    private String text = "";

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Cursor getMainCursor() {
        return main;
    }

    @Override
    public Cursor getSecondaryCursor() {
        return secondary;
    }

    @Override
    public @Nullable String getMarkedText() {
        if (!hasMarkedText()) return null;
        int max = Math.max(main.getCol(), secondary.getCol());
        int min = Math.min(main.getCol(), secondary.getCol());
        return this.text.substring(min, max);
    }

    @Override
    public boolean insertAtCursor(String text) {
        if (text.contains("\n")) throw new IllegalArgumentException("Single line text input can't handle line feed.");
        this.text = this.text.substring(0, this.main.getCol()) + text + this.text.substring(this.main.getCol());
        clampCursor();
        return true;
    }

    @Override
    public boolean hasMarkedText() {
        return main.getCol() != secondary.getCol();
    }

    @Override
    public void clearAll() {
        this.text = "";
        moveCursorCol(0);
    }

    @Override
    public void markAll() {
        this.secondary.moveCol(0);
        this.main.moveCol(this.text.length());
    }

    @Override
    public void moveCursorColBy(int amount, boolean shiftDown, boolean ctrlDown) {
        moveSecondaryOntoMainCursor();
        if (ctrlDown) {
            // find next non abs_$&
        }
        this.main.moveCol(this.main.getCol() + amount);
        if (!shiftDown) moveSecondaryOntoMainCursor();
        clampCursor();
    }

    @Override
    public void deleteCharacter(int offset) {
        if (hasMarkedText()) {
            int max = Math.max(main.getCol(), secondary.getCol());
            int min = Math.min(main.getCol(), secondary.getCol());
            this.text = this.text.substring(0, min) + this.text.substring(max);
            moveCursorCol(min);
            return;
        }
        this.text = this.text.substring(0, this.main.getCol() - 1 + offset) + this.text.substring(this.main.getCol() + offset);
        moveCursorColBy(offset - 1, false, false);
    }

    public void clampCursor() {
        this.main.clampToSingleLine(this.text);
        this.secondary.clampToSingleLine(this.text);
    }
}
