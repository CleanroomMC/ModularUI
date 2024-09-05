package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.drawable.ITextLine;
import com.cleanroommc.modularui.core.mixin.FontRendererAccessor;
import com.cleanroommc.modularui.drawable.DelegateIcon;
import com.cleanroommc.modularui.drawable.Icon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RichTextCompiler {

    public static final RichTextCompiler INSTANCE = new RichTextCompiler();

    private FontRenderer fr;
    private int maxWidth;

    private List<ITextLine> lines;
    private List<Object> currentLine;
    private int x, h;
    private TextFormatting[] formatting = FontRenderHelper.createFormattingState();

    public void newLine() {
        int i = currentLine.size() - 1;
        if (!currentLine.isEmpty() && currentLine.get(i) instanceof String s) {
            if (s.equals(" ")) {currentLine.remove(i);} else currentLine.set(i, trimRight(s));
        }
        if (currentLine.isEmpty()) {
            //lines.add(null);
        } else if (currentLine.size() == 1 && currentLine.get(0) instanceof String) {
            lines.add(new TextLine((String) currentLine.get(0), x));
            currentLine.clear();
        } else {
            lines.add(new ComposedLine(currentLine, x, h));
            currentLine = new ArrayList<>();
        }
        x = 0;
        h = 0;
    }

    public void addLineElement(Object o) {
        if (o instanceof String s2) {
            if (this.currentLine.size() == 1 && this.currentLine.get(0) instanceof String s1) {
                // if there is already one string in the line, merge them
                this.currentLine.set(0, s1 + s2);
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
            o = FontRenderHelper.getFormatting(this.formatting) + o; // add formatting from previous string
            FontRenderHelper.parseFormattingState(this.formatting, s2); // parse formatting from current string
        }
        this.currentLine.add(o);
    }

    public void checkNewLine(int width) {
        if (x > 0 && x + width > maxWidth) {
            newLine();
        }
    }

    public void reset(FontRenderer fr, int maxWidth) {
        this.fr = fr != null ? fr : Minecraft.getMinecraft().fontRenderer;
        this.maxWidth = maxWidth > 0 ? maxWidth : Integer.MAX_VALUE;
        this.lines = new ArrayList<>();
        this.currentLine = new ArrayList<>();
        this.x = 0;
        this.h = 0;
        Arrays.fill(this.formatting, null);
    }

    public List<ITextLine> compileLines(FontRenderer fr, List<Object> raw, int maxWidth, float scale) {
        reset(fr, (int) (maxWidth / scale));
        compile(raw);
        return lines;
    }

    private void compile(List<Object> raw) {
        for (Object o : raw) {
            if (o instanceof ITextLine line) {
                newLine();
                lines.add(line);
                continue;
            }
            String text = null;
            if (o instanceof IKey key) {
                if (key == IKey.EMPTY) continue;
                if (key == IKey.SPACE) {
                    addLineElement(key.get());
                    continue;
                }
                if (key == IKey.LINE_FEED) {
                    newLine();
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
                if (icon1.getWidth() <= 0) icon1.width(fr.FONT_HEIGHT);
                if (icon1.getHeight() <= 0) icon1.height(fr.FONT_HEIGHT);
            }
            if (icon.getWidth() > maxWidth) {
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
                int i = ((FontRendererAccessor) fr).invokeSizeStringToWidth(subText, maxWidth - this.x);
                if (i == 0) {
                    // doesn't fit at the end of the line, try new line
                    if (this.x > 0) i = ((FontRendererAccessor) fr).invokeSizeStringToWidth(subText, maxWidth);
                    if (i == 0) throw new IllegalStateException("No space for string '" + subText + "'");
                    newLine();
                } else if (i < subText.length()) {
                    // the whole string doesn't fit
                    char c = subText.charAt(i);
                    if (c != ' ' && this.x > 0) {
                        // line was split in the middle of a word, try new line
                        int j = ((FontRendererAccessor) fr).invokeSizeStringToWidth(subText, maxWidth);
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
                int width = fr.getStringWidth(current);
                addLineElement(current); // add string
                h = Math.max(h, fr.FONT_HEIGHT);
                x += width;
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

    public static String trimRight(String s) {
        int i = s.length() - 1;
        for (; i >= 0; i--) {
            if (!Character.isWhitespace(s.charAt(i))) break;
        }
        if (i < s.length() - 1) s = s.substring(0, i);
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
