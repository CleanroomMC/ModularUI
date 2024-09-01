package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.*;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.sizer.Area;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;

import org.jetbrains.annotations.Nullable;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Tooltip {

    private final IWidget parent;
    private final List<IDrawable> lines = new ArrayList<>();
    private List<IDrawable> additionalLines = new ArrayList<>();
    private Pos pos = ModularUIConfig.tooltipPos;
    private boolean customPos = false;
    private Consumer<Tooltip> tooltipBuilder;
    private int showUpTimer = 0;

    private int x = 0, y = 0;
    private int maxWidth = Integer.MAX_VALUE;
    private boolean textShadow = true;
    private int textColor = Color.WHITE.main;
    private float scale = 1.0f;
    private Alignment alignment = Alignment.TopLeft;
    private boolean autoUpdate = false;
    private boolean hasTitleMargin = true;
    private int linePadding = 1;

    private boolean dirty = true;

    public Tooltip(IWidget parent) {
        this.parent = parent;
    }

    public void buildTooltip() {
        this.dirty = false;
        this.lines.clear();
        List<IDrawable> additionalLines = this.additionalLines;
        this.additionalLines = this.lines;
        if (this.tooltipBuilder != null) {
            this.tooltipBuilder.accept(this);
        }
        this.lines.addAll(additionalLines);
        this.additionalLines = additionalLines;
        if (this.hasTitleMargin && this.lines.size() > 1) {
            this.lines.add(1, Icon.EMPTY_2PX);
        }
    }

    public void draw(GuiContext context) {
        draw(context, ItemStack.EMPTY);
    }

    public void draw(GuiContext context, @Nullable ItemStack stack) {
        if (this.autoUpdate) {
            markDirty();
        }
        if (isEmpty()) return;

        if (this.maxWidth <= 0) {
            this.maxWidth = Integer.MAX_VALUE;
        }
        if (stack == null) stack = ItemStack.EMPTY;
        Area screen = context.getScreen().getScreenArea();
        int mouseX = context.getAbsMouseX(), mouseY = context.getAbsMouseY();
        IconRenderer renderer = IconRenderer.SHARED;
        List<String> textLines = lines.stream().filter(drawable -> drawable instanceof IKey).map(key -> ((IKey) key).get()).collect(Collectors.toList());
        RenderTooltipEvent.Pre event = new RenderTooltipEvent.Pre(stack, textLines, mouseX, mouseY, screen.width, screen.height, this.maxWidth, TextRenderer.getFontRenderer());
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return;
        }
        //lines = event.getLines();
        mouseX = event.getX();
        mouseY = event.getY();
        int screenWidth = event.getScreenWidth(), screenHeight = event.getScreenHeight();
        this.maxWidth = event.getMaxWidth();

        renderer.setShadow(this.textShadow);
        renderer.setColor(this.textColor);
        renderer.setScale(this.scale);
        renderer.setAlignment(this.alignment, this.maxWidth);
        renderer.setLinePadding(this.linePadding);
        renderer.setSimulate(true);
        renderer.setPos(0, 0);

        //List<IIcon> icons = renderer.measureLines(this.lines);
        renderer.draw(context, this.lines);

        Rectangle area = determineTooltipArea(context, this.lines, renderer, screenWidth, screenHeight, mouseX, mouseY);

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableBlend();

        GuiDraw.drawTooltipBackground(stack, textLines, area.x, area.y, area.width, area.height);

        MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostBackground(stack, textLines, area.x, area.y, TextRenderer.getFontRenderer(), area.width, area.height));

        GlStateManager.color(1f, 1f, 1f, 1f);

        renderer.setSimulate(false);
        //renderer.setAlignment(Alignment.TopLeft, area.width, area.height);
        renderer.setPos(area.x, area.y);
        renderer.draw(context, this.lines);

        MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostText(stack, textLines, area.x, area.y, TextRenderer.getFontRenderer(), area.width, area.height));
    }

    public Rectangle determineTooltipArea(GuiContext context, List<IDrawable> lines, IconRenderer renderer, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        int width = (int) renderer.getLastWidth();
        int height = (int) renderer.getLastHeight();

        if (!this.customPos) {
            this.pos = context.getScreen().getCurrentTheme().getTooltipPosOverride();
        }

        if (this.pos == null) {
            return new Rectangle(this.x, this.y, width, height);
        }

        if (this.pos == Pos.NEXT_TO_MOUSE) {
            final int padding = 8;
            // magic number to place tooltip nicer. Look at GuiScreen#L237
            final int mouseOffset = 12;
            int x = mouseX + mouseOffset, y = mouseY - mouseOffset;
            if (x < padding) {
                x = padding;
            } else if (x + width + padding > screenWidth) {
                x -= mouseOffset * 2 + width; // flip side of cursor
                if (x < padding) {
                    x = padding;
                }
            }
            y = MathHelper.clamp(y, padding, screenHeight - padding - height);
            return new Rectangle(x, y, width, height);
        }

        if (this.parent == null) {
            throw new IllegalStateException("Tooltip pos is " + this.pos.name() + ", but no widget parent is set!");
        }

        int minWidth = 0;
        for (IDrawable line : lines) {
            if (line instanceof IIcon icon && !(line instanceof TextIcon)) {
                minWidth = Math.max(minWidth, icon.getWidth());
            } else if (!(line instanceof IKey)) {
                minWidth = Math.max(minWidth, 18);
            }
        }

        int shiftAmount = 10;
        int padding = 7;

        Area area = Area.SHARED;
        area.set(this.parent.getArea());
        area.setPos(0, 0); // context is transformed to this widget
        area.transformAndRectanglerize(context);
        int x = 0, y = 0;
        if (this.pos.vertical) {
            if (width < area.width) {
                x = area.x + shiftAmount;
            } else {
                x = area.x - shiftAmount;
                if (x < padding) {
                    x = padding;
                } else if (x + width > screenWidth - padding) {
                    int maxWidth = Math.max(minWidth, screenWidth - x - padding);
                    renderer.setAlignment(this.alignment, maxWidth);
                    renderer.draw(context, lines);
                    width = (int) renderer.getLastWidth();
                    height = (int) renderer.getLastHeight();
                }
            }

            Pos pos = this.pos;
            if (this.pos == Pos.VERTICAL) {
                int bottomSpace = screenHeight - area.ey();
                pos = bottomSpace < height + padding && bottomSpace < area.y ? Pos.ABOVE : Pos.BELOW;
            }

            if (pos == Pos.BELOW) {
                y = area.ey() + padding;
            } else if (pos == Pos.ABOVE) {
                y = area.y - height - padding;
            }
        } else if (this.pos.horizontal) {
            boolean usedMoreSpaceSide = false;
            Pos pos = this.pos;
            if (this.pos == Pos.HORIZONTAL) {
                if (area.x > screenWidth - area.ex()) {
                    pos = Pos.LEFT;
                    // x = 0;
                } else {
                    pos = Pos.RIGHT;
                    x = screenWidth - area.ex() + padding;
                }
            }

            if (height < area.height) {
                y = area.y + shiftAmount;
            } else {
                y = area.y - shiftAmount;
                if (y < padding) {
                    y = padding;
                }
            }

            if (x + width > screenWidth - padding) {
                int maxWidth;
                if (pos == Pos.LEFT) {
                    maxWidth = Math.max(minWidth, area.x - padding * 2);
                } else {
                    maxWidth = Math.max(minWidth, screenWidth - area.ex() - padding * 2);
                }
                usedMoreSpaceSide = true;
                renderer.setAlignment(this.alignment, maxWidth);
                renderer.draw(context, lines);
                width = (int) renderer.getLastWidth();
                height = (int) renderer.getLastHeight();
            }

            if (this.pos == Pos.HORIZONTAL && !usedMoreSpaceSide) {
                int rightSpace = screenWidth - area.ex();
                pos = rightSpace < width + padding && rightSpace < area.x ? Pos.LEFT : Pos.RIGHT;
            }

            if (pos == Pos.RIGHT) {
                x = area.ex() + padding;
            } else if (pos == Pos.LEFT) {
                x = area.x - width - padding;
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

    public int getShowUpTimer() {
        return this.showUpTimer;
    }

    @Nullable
    public Consumer<Tooltip> getTooltipBuilder() {
        return this.tooltipBuilder;
    }

    public boolean isAutoUpdate() {
        return this.autoUpdate;
    }

    public boolean hasTitleMargin() {
        return this.hasTitleMargin;
    }

    public Tooltip pos(Pos pos) {
        this.customPos = true;
        this.pos = pos;
        return this;
    }

    public Tooltip pos(int x, int y) {
        this.customPos = true;
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

    public Tooltip setAutoUpdate(boolean update) {
        this.autoUpdate = update;
        return this;
    }

    public Tooltip setHasTitleMargin(boolean hasTitleMargin) {
        this.hasTitleMargin = hasTitleMargin;
        return this;
    }

    /**
     * By default, tooltips have 1px of space between lines. Set to 0 if you want to disable it.
     */
    public Tooltip setLinePadding(int linePadding) {
        this.linePadding = linePadding;
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
    }
}
