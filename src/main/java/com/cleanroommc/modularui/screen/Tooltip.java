package com.cleanroommc.modularui.screen;

import codechicken.lib.math.MathHelper;
import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.Icon;
import com.cleanroommc.modularui.drawable.IconRenderer;
import com.cleanroommc.modularui.drawable.TextIcon;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.sizer.Area;
import net.minecraft.client.renderer.RenderHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class Tooltip {

    private final List<IDrawable> lines = new ArrayList<>();
    private List<IDrawable> additionalLines = new ArrayList<>();
    private Area excludeArea;
    private Pos pos = ModularUIConfig.tooltipPos;
    private Consumer<Tooltip> tooltipBuilder;
    private int showUpTimer = 0;

    private int x = 0, y = 0;
    private int maxWidth = Integer.MAX_VALUE;
    private boolean textShadow = true;
    private int textColor = Color.WHITE.normal;
    private float scale = 1.0f;
    private Alignment alignment = Alignment.TopLeft;
    private boolean updateTooltipEveryTick = false;
    private boolean hasSpaceAfterFirstLine = false;

    private boolean dirty = true;

    public void buildTooltip() {
        this.dirty = false;
        this.lines.clear();
        List<IDrawable> additionalLines = this.additionalLines;
        this.additionalLines = lines;
        if (this.tooltipBuilder != null) {
            this.tooltipBuilder.accept(this);
        }
        this.lines.addAll(additionalLines);
        this.additionalLines = additionalLines;
        if (hasSpaceAfterFirstLine && lines.size() > 1) {
            lines.add(1, Icon.EMPTY_2PX);
        }
    }

    public void draw(GuiContext context) {
        if (updateTooltipEveryTick) {
            markDirty();
        }
        if (isEmpty()) return;

        if (maxWidth <= 0) {
            maxWidth = Integer.MAX_VALUE;
        }
        int mouseX = context.getAbsMouseX(), mouseY = context.getAbsMouseY();
        IconRenderer renderer = IconRenderer.SHARED;
        //List<IIcon> icons = renderer.measureLines(this.lines);
        List<String> textLines = Collections.emptyList();//icons.stream().filter(iIcon -> iIcon instanceof TextIcon).map(icon -> ((TextIcon) icon).getText()).collect(Collectors.toList());
        Dimension displaySize = codechicken.lib.gui.GuiDraw.displaySize();
        int screenWidth = displaySize.width, screenHeight = displaySize.height;

        renderer.setShadow(this.textShadow);
        renderer.setColor(this.textColor);
        renderer.setScale(this.scale);
        renderer.setAlignment(this.alignment, this.maxWidth);
        renderer.setSimulate(true);
        renderer.setPos(0, 0);

        //List<IIcon> icons = renderer.measureLines(this.lines);
        renderer.draw(context, this.lines);

        Rectangle area = determineTooltipArea(context, this.lines, renderer, screenWidth, screenHeight, mouseX, mouseY);

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);

        GuiDraw.drawTooltipBackground(textLines, area.x, area.y, area.width, area.height, 300);

        GL11.glColor4f(1f, 1f, 1f, 1f);

        renderer.setSimulate(false);
        //renderer.setAlignment(Alignment.TopLeft, area.width, area.height);
        renderer.setPos(area.x, area.y);
        renderer.draw(context, this.lines);
    }

    public Rectangle determineTooltipArea(GuiContext context, List<IDrawable> lines, IconRenderer renderer, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        int width = (int) renderer.getLastWidth();
        int height = (int) renderer.getLastHeight();

        if (this.pos == null) {
            return new Rectangle(this.x, this.y, width, height);
        }

        if (this.pos == Pos.NEXT_TO_MOUSE) {
            final int PADDING = 8;
            // magic number to place tooltip nicer. Look at GuiScreen#L178
            final int MOUSE_OFFSET = 12;
            int x = mouseX + MOUSE_OFFSET, y = mouseY - MOUSE_OFFSET;
            if (x < PADDING) {
                x = PADDING;
            } else if (x + width + PADDING > screenWidth) {
                x -= MOUSE_OFFSET * 2 + width; // flip side of cursor
                if (x < PADDING) {
                    x = PADDING;
                }
            }
            y = (int) MathHelper.clip(y, PADDING, screenHeight - PADDING - height);
            return new Rectangle(x, y, width, height);
        }

        if (this.excludeArea == null) {
            throw new IllegalStateException();
        }

        int minWidth = 0;
        for (IDrawable line : lines) {
            if (line instanceof IIcon && !(line instanceof TextIcon)) {
                minWidth = Math.max(minWidth, ((IIcon) line).getWidth());
            } else if (!(line instanceof IKey)) {
                minWidth = Math.max(minWidth, 18);
            }
        }

        int shiftAmount = 10;
        int borderSpace = 7;

        int x = 0, y = 0;
        if (this.pos.vertical) {
            int xArea = this.excludeArea.x;
            if (width < this.excludeArea.width) {
                x = xArea + shiftAmount;
            } else {
                x = xArea - shiftAmount;
                if (x < borderSpace) {
                    x = borderSpace;
                } else if (x + width > screenWidth - borderSpace) {
                    int maxWidth = Math.max(minWidth, screenWidth - x - borderSpace);
                    renderer.setAlignment(this.alignment, maxWidth);
                    renderer.draw(context, lines);
                    width = (int) renderer.getLastWidth();
                    height = (int) renderer.getLastHeight();
                }
            }

            Pos pos = this.pos;
            if (this.pos == Pos.VERTICAL) {
                int bottomSpace = screenHeight - this.excludeArea.y - this.excludeArea.height;
                pos = bottomSpace < height && bottomSpace < this.excludeArea.y ? Pos.ABOVE : Pos.BELOW;
            }

            if (pos == Pos.BELOW) {
                y = this.excludeArea.y + this.excludeArea.height + borderSpace;
            } else if (pos == Pos.ABOVE) {
                y = this.excludeArea.y - height - borderSpace;
            }
        } else if (this.pos.horizontal) {
            boolean usedMoreSpaceSide = false;
            Pos pos = this.pos;
            if (this.pos == Pos.HORIZONTAL) {
                if (this.excludeArea.x > screenWidth - this.excludeArea.x - this.excludeArea.width) {
                    pos = Pos.LEFT;
                    x = 0;
                } else {
                    pos = Pos.RIGHT;
                    x = screenWidth - this.excludeArea.x - this.excludeArea.width + borderSpace;
                }
            }

            int yArea = this.excludeArea.y;
            if (height < this.excludeArea.height) {
                y = yArea + shiftAmount;
            } else {
                y = yArea - shiftAmount;
                if (y < borderSpace) {
                    y = borderSpace;
                }
            }

            if (x + width > screenWidth - borderSpace) {
                int maxWidth;
                if (pos == Pos.LEFT) {
                    maxWidth = Math.max(minWidth, this.excludeArea.x - borderSpace * 2);
                } else {
                    maxWidth = Math.max(minWidth, screenWidth - this.excludeArea.x - this.excludeArea.width - borderSpace * 2);
                }
                usedMoreSpaceSide = true;
                renderer.setAlignment(this.alignment, maxWidth);
                renderer.draw(context, lines);
                width = (int) renderer.getLastWidth();
                height = (int) renderer.getLastHeight();
            }

            if (this.pos == Pos.HORIZONTAL && !usedMoreSpaceSide) {
                int rightSpace = screenWidth - this.excludeArea.x - this.excludeArea.width;
                pos = rightSpace < width && rightSpace < this.excludeArea.x ? Pos.LEFT : Pos.RIGHT;
            }

            if (pos == Pos.RIGHT) {
                x = this.excludeArea.x + this.excludeArea.width + borderSpace;
            } else if (pos == Pos.LEFT) {
                x = this.excludeArea.x - width - borderSpace;
            }
        }
        return new Rectangle(x, y, width, height);
    }

    public boolean isEmpty() {
        if (this.dirty) {
            buildTooltip();
        }
        return this.lines.isEmpty();
    }

    public void markDirty() {
        this.dirty = true;
    }

    public Area getExcludeArea() {
        return excludeArea;
    }

    public int getShowUpTimer() {
        return showUpTimer;
    }

    @Nullable
    public Consumer<Tooltip> getTooltipBuilder() {
        return tooltipBuilder;
    }

    public Tooltip excludeArea(Area area) {
        this.excludeArea = area;
        return this;
    }

    public Tooltip pos(Pos pos) {
        this.pos = pos;
        return this;
    }

    public Tooltip pos(int x, int y) {
        this.pos = null;
        this.x = x;
        this.y = y;
        return this;
    }

    public Tooltip alignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public Tooltip textShadow(boolean textShadow) {
        this.textShadow = textShadow;
        return this;
    }

    public Tooltip textColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    public Tooltip scale(float scale) {
        this.scale = scale;
        return this;
    }

    public Tooltip showUpTimer(int showUpTimer) {
        this.showUpTimer = showUpTimer;
        return this;
    }

    public Tooltip tooltipBuilder(Consumer<Tooltip> tooltipBuilder) {
        Consumer<Tooltip> existingBuilder = this.tooltipBuilder;
        if (existingBuilder != null) {
            this.tooltipBuilder = tooltip -> {
                existingBuilder.accept(this);
                tooltipBuilder.accept(this);
            };
        } else {
            this.tooltipBuilder = tooltipBuilder;
        }
        return this;
    }

    public Tooltip setUpdateTooltipEveryTick(boolean update) {
        this.updateTooltipEveryTick = update;
        return this;
    }

    public Tooltip setHasSpaceAfterFirstLine(boolean hasSpaceAfterFirstLine) {
        this.hasSpaceAfterFirstLine = hasSpaceAfterFirstLine;
        return this;
    }

    public Tooltip addLine(IDrawable drawable) {
        this.additionalLines.add(drawable);
        return this;
    }

    public Tooltip addLine(String line) {
        return addLine(IKey.str(line));
    }

    public Tooltip addDrawableLines(Iterable<IDrawable> lines) {
        for (IDrawable line : lines) {
            addLine(line);
        }
        return this;
    }

    public Tooltip addStringLines(Iterable<String> lines) {
        for (String line : lines) {
            addLine(IKey.str(line));
        }
        return this;
    }

    public enum Pos {

        ABOVE(false, true),
        BELOW(false, true),
        LEFT(true, false),
        RIGHT(true, false),
        VERTICAL(false, true),
        HORIZONTAL(true, false),
        NEXT_TO_MOUSE(false, false);

        public final boolean horizontal, vertical;

        Pos(boolean horizontal, boolean vertical) {
            this.horizontal = horizontal;
            this.vertical = vertical;
        }

        public static Pos fromString(String name) {
            try {
                return Pos.valueOf(name);
            } catch (IllegalArgumentException ignored) {
                return VERTICAL;
            }
        }
    }
}
