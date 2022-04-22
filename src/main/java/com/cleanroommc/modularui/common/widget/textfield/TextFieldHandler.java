package com.cleanroommc.modularui.common.widget.textfield;

import com.cleanroommc.modularui.api.drawable.TextFieldRenderer;
import com.cleanroommc.modularui.common.widget.ScrollBar;
import com.google.common.base.Joiner;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TextFieldHandler {

    private static final Joiner JOINER = Joiner.on('\n');

    private final List<String> text = new ArrayList<>();
    private final Point cursor = new Point(), cursorEnd = new Point();
    private TextFieldRenderer renderer;
    @Nullable
    private ScrollBar scrollBar;
    private boolean mainCursorStart = true;
    private int maxLines = 1;

    public void setScrollBar(@Nullable ScrollBar scrollBar) {
        this.scrollBar = scrollBar;
    }

    public void setRenderer(TextFieldRenderer renderer) {
        this.renderer = renderer;
    }

    public void switchCursors() {
        this.mainCursorStart = !this.mainCursorStart;
    }

    public Point getMainCursor() {
        return mainCursorStart ? cursor : cursorEnd;
    }

    public Point getOffsetCursor() {
        return mainCursorStart ? cursorEnd : cursor;
    }

    public Point getStartCursor() {
        if (!hasTextMarked()) {
            return cursor;
        }
        return cursor.y > cursorEnd.y || (cursor.y == cursorEnd.y && cursor.x > cursorEnd.x) ? cursorEnd : cursor;
    }

    public Point getEndCursor() {
        if (!hasTextMarked()) {
            return cursor;
        }
        return cursor.y > cursorEnd.y || (cursor.y == cursorEnd.y && cursor.x > cursorEnd.x) ? cursor : cursorEnd;
    }

    public boolean hasTextMarked() {
        return cursor.y != cursorEnd.y || cursor.x != cursorEnd.x;
    }

    public void setOffsetCursor(int linePos, int charPos) {
        getOffsetCursor().setLocation(charPos, linePos);
    }

    public void setMainCursor(int linePos, int charPos) {
        Point main = getMainCursor();
        if (main.x != charPos || main.y != linePos) {
            main.setLocation(charPos, linePos);
            if (!this.text.isEmpty() && this.renderer != null && this.scrollBar != null && this.scrollBar.isActive()) {
                // update actual width
                this.renderer.setSimulate(true);
                this.renderer.draw(this.text);
                this.renderer.setSimulate(false);
                String line = this.text.get(main.y);
                this.scrollBar.setScrollOffsetOfCursor(this.renderer.getPosOf(this.renderer.measureLines(Collections.singletonList(line)), main).x);
            }
        }
    }

    public void setCursor(int linePos, int charPos) {
        setMainCursor(linePos, charPos);
        setOffsetCursor(linePos, charPos);
    }

    public void setOffsetCursor(Point cursor) {
        setOffsetCursor(cursor.y, cursor.x);
    }

    public void setMainCursor(Point cursor) {
        setMainCursor(cursor.y, cursor.x);
    }

    public void setCursor(Point cursor) {
        setMainCursor(cursor);
        setOffsetCursor(cursor);
    }

    public void putMainCursorAtStart() {
        if (hasTextMarked() && getMainCursor() != getStartCursor()) {
            switchCursors();
        }
    }

    public void putMainCursorAtEnd() {
        if (hasTextMarked() && getMainCursor() != getEndCursor()) {
            switchCursors();
        }
    }

    public void moveCursorLeft(boolean ctrl, boolean shift) {
        if (this.text.isEmpty()) return;
        Point main = getMainCursor();
        if (main.x == 0) {
            if (main.y == 0) return;
            setCursor(main.y - 1, this.text.get(main.y - 1).length() - 1);
        } else {
            setCursor(main.y, main.x - 1);
        }
    }

    public void moveCursorRight(boolean ctrl, boolean shift) {
        if (this.text.isEmpty()) return;
        Point main = getMainCursor();
        String line = this.text.get(main.y);
        if (main.x == line.length()) {
            if (main.y == this.text.size() - 1) return;
            setCursor(main.y + 1, 0);
        } else {
            setCursor(main.y, main.x + 1);
        }
    }

    public void moveCursorUp(boolean ctrl, boolean shift) {
        if (this.text.isEmpty()) return;
        Point main = getMainCursor();
        if (main.y > 0) {
            setCursor(main.y - 1, main.x);
        } else {
            setCursor(main.y, 0);
        }
    }

    public void moveCursorDown(boolean ctrl, boolean shift) {
        if (this.text.isEmpty()) return;
        Point main = getMainCursor();
        if (main.y < this.text.size() - 1) {
            setCursor(main.y + 1, main.x);
        } else {
            setCursor(main.y, this.text.get(main.y).length());
        }
    }

    public void markAll() {
        setOffsetCursor(0, 0);
        setMainCursor(this.text.size() - 1, this.text.get(this.text.size() - 1).length());
    }

    public String getTextAsString() {
        return JOINER.join(this.text);
    }

    public List<String> getText() {
        return this.text;
    }

    public String getSelectedText() {
        if (!hasTextMarked()) return "";
        Point min = getStartCursor();
        Point max = getEndCursor();
        if (min.y == max.y) {
            return this.text.get(min.y).substring(min.x, max.x);
        }
        StringBuilder builder = new StringBuilder();
        builder.append(this.text.get(min.y).substring(min.x));
        if (max.y > min.y + 2) {
            for (int i = min.y + 1; i < max.y - 1; i++) {
                builder.append(this.text.get(i));
            }
        }
        builder.append(this.text.get(max.y), 0, max.x);
        return builder.toString();
    }

    public void insert(String text) {
        insert(Arrays.asList(text.split("\n")));
    }

    public void insert(List<String> text) {
        if (text.isEmpty() || (text.size() > 1 && this.text.size() + text.size() - 1 > this.maxLines)) {
            return;
        }
        if (hasTextMarked()) {
            delete(false);
        }
        if (text.isEmpty()) return;
        if (this.text.isEmpty()) {
            this.text.addAll(text);
            setCursor(this.text.size() - 1, this.text.get(this.text.size() - 1).length());
            return;
        }
        String lineStart = this.text.get(cursor.y).substring(0, cursor.x);
        String lineEnd = this.text.get(cursor.y).substring(cursor.x);
        this.text.set(cursor.y, lineStart + text.get(0));
        if (text.size() == 1) {
            this.text.set(cursor.y, this.text.get(cursor.y) + lineEnd);
            setCursor(cursor.y, cursor.x + text.get(0).length());
        } else {
            if (text.size() > 1) {
                this.text.add(cursor.y + 1, text.get(text.size() - 1) + lineEnd);
                setCursor(cursor.y + 1, text.get(text.size() - 1).length());
            }
            if (text.size() > 2) {
                this.text.addAll(cursor.y + 1, this.text.subList(1, text.size() - 1));
                setCursor(cursor.y + text.size() - 1, text.get(text.size() - 1).length());
            }
        }
    }

    public void newLine() {
        if (hasTextMarked()) {
            delete(false);
        }
        String line = this.text.get(cursor.y);
        this.text.set(cursor.y, line.substring(0, cursor.x));
        this.text.add(cursor.y + 1, line.substring(cursor.x));
        setCursor(cursor.y + 1, 0);
    }

    public void delete() {
        delete(false);
    }

    public void delete(boolean inFront) {
        if (hasTextMarked()) {
            Point min = getStartCursor();
            Point max = getEndCursor();
            String minLine = this.text.get(min.y);
            if (min.y == max.y) {
                this.text.set(min.y, minLine.substring(0, min.x) + minLine.substring(max.x));
            } else {
                this.text.set(min.y, minLine.substring(0, min.x));
                if (max.y > min.y + 2) {
                    this.text.subList(min.y, max.y - 1).clear();
                }
                this.text.set(min.y, minLine.substring(max.x));
            }
            setCursor(min.y, min.x);
        } else {
            String line = this.text.get(cursor.y);
            if (inFront) {
                if (cursor.x == line.length()) {
                    if (this.text.size() > cursor.y + 1) {
                        this.text.set(cursor.y, line + this.text.get(cursor.y + 1));
                        this.text.remove(cursor.y + 1);
                    }
                } else {
                    line = line.substring(0, cursor.x) + line.substring(cursor.x + 1);
                    this.text.set(cursor.y, line);
                }
            } else {
                if (cursor.x == 0) {
                    if (cursor.y > 0) {
                        this.text.set(cursor.y - 1, this.text.get(cursor.y - 1) + line);
                        this.text.remove(cursor.y);
                        setCursor(cursor.y - 1, cursor.x);
                    }
                } else {
                    line = line.substring(0, cursor.x - 1) + line.substring(cursor.x);
                    this.text.set(cursor.y, line);
                    setCursor(cursor.y, cursor.x - 1);
                }
            }
        }
        if (this.scrollBar != null) {
            scrollBar.clampScrollOffset();
        }
    }

    public void setMaxLines(int maxLines) {
        this.maxLines = Math.max(1, maxLines);
    }

    public int getMaxLines() {
        return maxLines;
    }
}