package com.cleanroommc.modularui.widgets.textinput;

import org.jetbrains.annotations.Nullable;

public interface ITextInputHandler {

    String getText();

    Cursor getMainCursor();

    Cursor getSecondaryCursor();

    @Nullable String getMarkedText();

    boolean insertAtCursor(String text);

    boolean hasMarkedText();

    void clearAll();

    void markAll();

    void moveCursorColBy(int amount, boolean shiftDown, boolean ctrlDown);

    void deleteCharacter(int offset);

    default void moveSecondaryOntoMainCursor() {
        getSecondaryCursor().setFrom(getMainCursor());
    }

    default void moveMainCursorCol(int col) {
        getMainCursor().moveCol(col);
    }

    default void moveSecondaryCursorCol(int col) {
        getSecondaryCursor().moveCol(col);
    }

    default void moveCursorCol(int col) {
        moveMainCursorCol(col);
        moveSecondaryCursorCol(col);
    }
}
