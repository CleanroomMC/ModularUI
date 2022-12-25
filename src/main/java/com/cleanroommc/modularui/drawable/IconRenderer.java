package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.IDrawable;
import com.cleanroommc.modularui.api.IIcon;
import com.cleanroommc.modularui.api.IKey;
import com.cleanroommc.modularui.utils.Alignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IconRenderer {

    public static final IconRenderer SHARED = new IconRenderer();

    protected float maxWidth = -1, maxHeight = -1;
    protected int x = 0, y = 0;
    protected Alignment alignment = Alignment.TopLeft;
    protected float scale = 1f;
    protected boolean shadow = false;
    protected int color = 0;//Theme.INSTANCE.getText();
    protected boolean simulate;
    protected float lastWidth = 0, lastHeight = 0;

    public void setAlignment(Alignment alignment, float maxWidth) {
        setAlignment(alignment, maxWidth, -1);
    }

    public void setAlignment(Alignment alignment, float maxWidth, float maxHeight) {
        this.alignment = alignment;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    public void setShadow(boolean shadow) {
        this.shadow = shadow;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setSimulate(boolean simulate) {
        this.simulate = simulate;
    }

    public void draw(IDrawable text) {
        draw(Collections.singletonList(text));
    }

    public void draw(List<IDrawable> lines) {
        drawMeasuredLines(measureLines(lines));
    }

    public void drawMeasuredLines(List<IIcon> lines) {
        int totalHeight = 0, maxWidth = 0;
        for (IIcon icon : lines) {
            totalHeight += icon.getHeight();
            maxWidth = Math.max(maxWidth, icon.getWidth());
        }
        int y = getStartY(totalHeight);
        for (IIcon icon : lines) {
            int x = getStartX(icon.getWidth());
            if (!simulate) {
                icon.draw(x, y, icon.getWidth(), icon.getHeight());
            }
            y += icon.getHeight() * scale;
        }
        this.lastWidth = this.maxWidth > 0 ? Math.min(this.maxWidth, maxWidth) : maxWidth;
        this.lastHeight = totalHeight * scale;
        this.lastWidth = Math.max(0, this.lastWidth - scale);
        this.lastHeight = Math.max(0, this.lastHeight - scale);
    }

    public List<IIcon> measureLines(List<IDrawable> lines) {
        List<IIcon> icons = new ArrayList<>();
        for (IDrawable element : lines) {
            if (element instanceof IIcon) {
                icons.add((IIcon) element);
            } else if (element instanceof IKey) {
                String text = ((IKey) element).get();
                for (String subLine : wrapLine(text)) {
                    int width = (int) (getFontRenderer().getStringWidth(subLine) * scale);
                    icons.add(new TextIcon(subLine, width, getFontRenderer().FONT_HEIGHT));
                }
            } else {
                icons.add(element.asIcon().height(getFontRenderer().FONT_HEIGHT));
            }
        }
        return icons;
    }

    public List<String> wrapLine(String line) {
        return maxWidth > 0 ? getFontRenderer().listFormattedStringToWidth(line, (int) (maxWidth / scale)) : Collections.singletonList(line);
    }

    protected int getStartY(int totalHeight) {
        if (alignment.y > 0 && maxHeight > 0) {
            float height = totalHeight * scale;
            return (int) (y + (maxHeight * alignment.y) - height * alignment.y);
        }
        return y;
    }

    protected int getStartX(float lineWidth) {
        if (alignment.x > 0 && maxWidth > 0) {
            return (int) (x + (maxWidth * alignment.x) - lineWidth * alignment.x);
        }
        return x;
    }

    public float getFontHeight() {
        return getFontRenderer().FONT_HEIGHT * scale;
    }

    public float getLastHeight() {
        return lastHeight;
    }

    public float getLastWidth() {
        return lastWidth;
    }

    @SideOnly(Side.CLIENT)
    public static FontRenderer getFontRenderer() {
        return Minecraft.getMinecraft().fontRenderer;
    }
}
