package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
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
    protected int linePadding = 1;
    protected boolean simulate;
    protected float lastWidth = 0, lastHeight = 0;
    protected boolean useWholeWidth = false;

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

    public void setLinePadding(int linePadding) {
        this.linePadding = linePadding;
    }

    public void setSimulate(boolean simulate) {
        this.simulate = simulate;
    }

    public void setUseWholeWidth(boolean useWholeWidth) {
        this.useWholeWidth = useWholeWidth;
    }

    public void draw(GuiContext context, IDrawable text) {
        draw(context, Collections.singletonList(text));
    }

    public void draw(GuiContext context, List<IDrawable> lines) {
        drawMeasuredLines(context, measureLines(lines));
    }

    public void drawMeasuredLines(GuiContext context, List<IIcon> lines) {
        TextRenderer.SHARED.setColor(this.color);
        TextRenderer.SHARED.setShadow(this.shadow);
        TextRenderer.SHARED.setScale(this.scale);
        TextRenderer.SHARED.setAlignment(this.alignment, this.maxWidth);
        // Look at GuiScreen#L239; height starts with 8, which is equal to `FontRenderer.FONT_HEIGHT - 1`
        int totalHeight = -1, maxWidth = 0;
        if (this.useWholeWidth) {
            maxWidth = (int) this.maxWidth;
        }
        for (IIcon icon : lines) {
            totalHeight += icon.getHeight() + this.linePadding;
            if (!this.useWholeWidth && icon.getWidth() > 0) {
                maxWidth = Math.max(maxWidth, icon.getWidth());
            }
        }
        if (!lines.isEmpty()) {
            // don't add padding to last line
            totalHeight -= this.linePadding;
        }
        int y = getStartY(totalHeight);
        for (IIcon icon : lines) {
            int x = icon.getWidth() > 0 ? getStartX(icon.getWidth()) : this.x;
            if (!this.simulate) {
                icon.draw(context, x, y, maxWidth, icon.getHeight());
            }
            y += (int) ((icon.getHeight() + this.linePadding) * this.scale);
        }
        this.lastWidth = this.maxWidth > 0 ? Math.min(this.maxWidth, maxWidth) : maxWidth;
        this.lastHeight = totalHeight * this.scale;
    }

    public List<IIcon> measureLines(List<IDrawable> lines) {
        List<IIcon> icons = new ArrayList<>();
        for (IDrawable element : lines) {
            if (element instanceof IIcon) {
                icons.add((IIcon) element);
            } else if (element instanceof IKey) {
                float scale = this.scale;
                Alignment alignment1 = this.alignment;
                if (element instanceof StyledText) {
                    scale = ((StyledText) element).getScale();
                    alignment1 = ((StyledText) element).getAlignment();
                }
                String text = ((IKey) element).get();
                for (String subLine : text.split("\\\\n")) {
                    for (String subSubLine : wrapLine(subLine, scale)) {
                        int width = (int) (getFontRenderer().getStringWidth(subSubLine) * scale);
                        icons.add(new TextIcon(subSubLine, width, (int) (getFontRenderer().FONT_HEIGHT * scale), scale, alignment1));
                    }
                }
            } else {
                icons.add(element.asIcon().height(getFontRenderer().FONT_HEIGHT));
            }
        }
        return icons;
    }

    public List<String> wrapLine(String line, float scale) {
        return this.maxWidth > 0 ? getFontRenderer().listFormattedStringToWidth(line, (int) (this.maxWidth / scale)) : Collections.singletonList(line);
    }

    protected int getStartY(int totalHeight) {
        if (this.alignment.y > 0 && this.maxHeight > 0) {
            float height = totalHeight * this.scale;
            return (int) (this.y + (this.maxHeight * this.alignment.y) - height * this.alignment.y);
        }
        return this.y;
    }

    protected int getStartX(float lineWidth) {
        if (this.alignment.x > 0 && this.maxWidth > 0) {
            return (int) (this.x + (this.maxWidth * this.alignment.x) - lineWidth * this.alignment.x);
        }
        return this.x;
    }

    public float getFontHeight() {
        return getFontRenderer().FONT_HEIGHT * this.scale;
    }

    public float getLastHeight() {
        return this.lastHeight;
    }

    public float getLastWidth() {
        return this.lastWidth;
    }

    @SideOnly(Side.CLIENT)
    public static FontRenderer getFontRenderer() {
        return Minecraft.getMinecraft().fontRenderer;
    }
}
