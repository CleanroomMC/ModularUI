package com.cleanroommc.modularui.widgets.textinput;

import com.cleanroommc.modularui.utils.MathUtils;

import java.util.List;

public class Cursor {

    private int line;
    private int col;

    public int getCol() {
        return col;
    }

    public int getLine() {
        return line;
    }

    public void moveTo(int line, int col) {
        this.line = line;
        this.col = col;
    }

    public void moveLine(int line) {
        this.line = line;
    }

    public void moveCol(int col) {
        this.col = col;
    }

    public void clampToSingleLine(String text) {
        this.line = 0;
        this.col = MathUtils.clamp(this.col, 0, text.length());
    }

    public void clampToMultiline(List<String> text) {
        if (this.line < 0) {
            this.line = 0;
            this.col = 0;
            return;
        }
        if (this.line >= text.size()) {
            this.line = text.size() - 1;
            this.col = text.get(this.line).length();
            return;
        }
        this.col = MathUtils.clamp(this.col, 0, text.get(this.line).length());
    }

    public void setFrom(Cursor cursor) {
        moveTo(cursor.line, cursor.col);
    }
}
