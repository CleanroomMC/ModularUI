package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.drawable.ITextLine;
import com.cleanroommc.modularui.core.mixins.early.minecraft.FontRendererAccessor;
import com.cleanroommc.modularui.drawable.DelegateIcon;
import com.cleanroommc.modularui.drawable.Icon;

import com.cleanroommc.modularui.screen.viewport.GuiContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * This class compiles a list of objects into renderable text. The objects can be strings or any drawable.
 * The compiler will try to inline the drawables into the text according to the given maximum width.
 * Recommended usage is via {@link TextRenderer#compileAndDraw(GuiContext, List)}.
 */
public class RichTextCompiler {

    public static final RichTextCompiler INSTANCE = new RichTextCompiler();

    private FontRenderer fr;
    private int maxWidth;

    private List<ITextLine> lines;
    private List<Object> currentLine;
    private int x, h;
    private final FormattingState formatting = new FormattingState();

    public List<ITextLine> compileLines(FontRenderer fr, List<Object> raw, int maxWidth, float scale) {
        reset(fr, (int) (maxWidth / scale));
        compile(raw);
        return lines;
    }

    public void reset(FontRenderer fr, int maxWidth) {
        this.fr = fr != null ? fr : Minecraft.getMinecraft().fontRenderer;
        this.maxWidth = maxWidth > 0 ? maxWidth : Integer.MAX_VALUE;
        this.lines = new ArrayList<>();
        this.currentLine = new ArrayList<>();
        this.x = 0;
        this.h = 0;
        this.formatting.reset();
    }

    private void compile(List<Object> raw) {
        for (Object o : raw) {
            if (o instanceof ITextLine line) {
                newLine();
                this.lines.add(line);
                continue;
            }
            String text = null;
            if (o instanceof IKey key) {
                if (key == IKey.EMPTY) continue;
                if (key == IKey.SPACE) {
                    String s = key.get();
                    addLineElement(s);
                    this.x += this.fr.getStringWidth(s);
                    continue;
                }
                if (key == IKey.LINE_FEED) {
                    newLine();
                    this.formatting.reset();
                    continue;
                }
                text = key.getFormatted();
            } else if (!(o instanceof IDrawable)) {
                text = String.valueOf(o);
            }
            if (text != null) {
                compileString(text);
                continue;
            }
            if (!(o instanceof IIcon)) {
                o = ((IDrawable) o).asIcon();//.size(fr.FONT_HEIGHT);
            }
            IIcon icon = (IIcon) o;
            IIcon delegate = icon;
            if (icon instanceof DelegateIcon di) {
                delegate = di.findRootDelegate();
            }
            if (delegate instanceof Icon icon1) {
                int defaultSize = this.fr.FONT_HEIGHT;
                //if (icon1.getWidth() <= 0) icon1.width(defaultSize);
                if (icon1.getHeight() <= 0) icon1.height(defaultSize);
            }
            if (icon.getWidth() > this.maxWidth) {
                ModularUI.LOGGER.warn("Icon is wider than max width");
            }
            checkNewLine(icon.getWidth());
            addLineElement(icon);
            h = Math.max(h, icon.getHeight());
            x += icon.getWidth();
        }
        newLine();
    }

    private void compileString(String text) {
        int l = text.indexOf('\n');
        int k = 0;
        do {
            // essentially splits text at \n and compiles it
            if (l < 0) l = text.length(); // no line feed, use rest of string
            String subText = text.substring(k, l);
            k = l + 1; // start next sub string here
            while (!subText.isEmpty()) {
                // how many chars fit
                int i = ((FontRendererAccessor) this.fr).invokeSizeStringToWidth(subText, this.maxWidth - this.x);
                if (i == 0) {
                    // doesn't fit at the end of the line, try new line
                    if (this.x > 0) i = ((FontRendererAccessor) fr).invokeSizeStringToWidth(subText, this.maxWidth);
                    if (i == 0) throw new IllegalStateException("No space for string '" + subText + "'");
                    newLine();
                } else if (i < subText.length()) {
                    // the whole string doesn't fit
                    char c = subText.charAt(i);
                    if (c != ' ' && this.x > 0) {
                        // line was split in the middle of a word, try new line
                        int j = ((FontRendererAccessor) fr).invokeSizeStringToWidth(subText, this.maxWidth);
                        if (j < subText.length()) {
                            c = subText.charAt(j);
                            if (j > i && c == ' ') {
                                // line was split properly on a new line
                                newLine();
                            }
                        } else {
                            // the end of the line is reached
                            newLine();
                        }
                    }
                }
                // get fitting string
                String current = subText.length() <= i ? subText : trimRight(subText.substring(0, i));
                int width = this.fr.getStringWidth(current);
                addLineElement(current); // add string
                this.h = Math.max(this.h, this.fr.FONT_HEIGHT);
                this.x += width;
                if (subText.length() <= i) break; // sub text reached the end
                newLine(); // string was split -> line is full
                char c = subText.charAt(i);
                if (c == ' ') i++; // if was split at space then don't include it in next sub text
                subText = subText.substring(i); // set sub text to part after split
            }
            if (l < text.length() && text.charAt(l) == '\n') {
                // was split at line feed -> new line
                newLine();
            }
        } while ((l = text.indexOf('\n', k)) >= 0 || k < text.length()); // if no line feed found, check if we are at the end of the text
    }

    private void newLine() {
        int i = this.currentLine.size() - 1;
        if (!this.currentLine.isEmpty() && this.currentLine.get(i) instanceof String s) {
            if (s.equals(" ")) {
                this.currentLine.remove(i);
            } else {
                this.currentLine.set(i, trimRight(s));
            }
        }
        if (!this.currentLine.isEmpty()) {
            if (this.currentLine.size() == 1 && this.currentLine.get(0) instanceof String) {
                this.lines.add(new TextLine((String) this.currentLine.get(0), this.x));
                this.currentLine.clear();
            } else {
                this.lines.add(new ComposedLine(this.currentLine, this.x, this.h));
                this.currentLine = new ArrayList<>();
            }
        }
        this.x = 0;
        this.h = 0;
    }

    private void addLineElement(Object o) {
        if (o instanceof String s2) {
            int s = this.currentLine.size();
            if (s > 0 && this.currentLine.get(s - 1) instanceof String s1) {
                // if the last element in the line is a string, merge them
                this.currentLine.set(s - 1, s1 + s2);
                return;
            }
            if (this.currentLine.isEmpty()) {
                // if there is currently no string, remove all whitespace from the start,
                // but don't remove any formatting before
                int l = FontRenderHelper.getFormatLength(s2, 0);
                if (l + 1 < s2.length()) {
                    o = trimAt(s2, l);
                }
            }
            o = this.formatting.getFormatting() + o;
            this.formatting.parseFrom(s2); // parse formatting from current string
        }
        this.currentLine.add(o);
    }

    private void checkNewLine(int width) {
        if (this.x > 0 && this.x + width > this.maxWidth) {
            newLine();
        }
    }

    public static String trimRight(String s) {
        int i = s.length() - 1;
        for (; i >= 0; i--) {
            if (!Character.isWhitespace(s.charAt(i))) break;
        }
        if (i < s.length() - 1) s = s.substring(0, i + 1);
        return s;
    }

    public static String trimAt(String s, int start) {
        int l = 0;
        for (int i = Math.max(0, start), n = s.length(); i < n; i++) {
            if (Character.isWhitespace(s.charAt(i))) {
                l++;
            } else {
                break;
            }
        }
        if (l == 0) return s;
        if (start <= 0) return s.substring(l);
        return s.substring(0, start) + s.substring(start + l);
    }
}
