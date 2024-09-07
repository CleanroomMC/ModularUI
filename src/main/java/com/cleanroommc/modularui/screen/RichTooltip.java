package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.MCHelper;
import com.cleanroommc.modularui.api.drawable.IRichTextBuilder;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.text.RichText;
import com.cleanroommc.modularui.drawable.text.TextRenderer;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.sizer.Area;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;

import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RichTooltip implements IRichTextBuilder<RichTooltip> {

    private static final Area HOLDER = new Area();

    private final Consumer<Area> parent;
    private final RichText text = new RichText();
    private Pos pos = null;
    private Consumer<RichTooltip> tooltipBuilder;
    private int showUpTimer = 0;
    private boolean autoUpdate = false;

    private int x = 0, y = 0;
    private int maxWidth = Integer.MAX_VALUE;

    private boolean dirty;

    public RichTooltip(IWidget parent) {
        this(area -> {
            area.setSize(parent.getArea());
            area.setPos(0, 0);
        });
    }

    public RichTooltip(Area parent) {
        this(area -> area.set(parent));
    }

    public RichTooltip(Supplier<Area> parent) {
        this(area -> area.set(parent.get()));
    }

    public RichTooltip(Consumer<Area> parent) {
        this.parent = parent;
    }

    public void buildTooltip() {
        this.dirty = false;
        this.text.clearText();
        if (this.tooltipBuilder != null) {
            this.tooltipBuilder.accept(this);
        }
    }

    public void draw(GuiContext context) {
        draw(context, ItemStack.EMPTY);
    }

    public void draw(GuiContext context, @Nullable ItemStack stack) {
        if (this.autoUpdate) markDirty();
        if (isEmpty()) return;

        if (this.maxWidth <= 0) {
            this.maxWidth = Integer.MAX_VALUE;
        }
        if (stack == null) stack = ItemStack.EMPTY;
        Area screen = context.getScreenArea();
        this.maxWidth = Math.min(this.maxWidth, screen.width);
        int mouseX = context.getAbsMouseX(), mouseY = context.getAbsMouseY();
        TextRenderer renderer = TextRenderer.SHARED;
        // this only turns the text and not any drawables into strings
        List<String> textLines = this.text.getStringRepresentation();
        RenderTooltipEvent.Pre event = new RenderTooltipEvent.Pre(stack, textLines, mouseX, mouseY, screen.width, screen.height, this.maxWidth, TextRenderer.getFontRenderer());
        if (MinecraftForge.EVENT_BUS.post(event)) return; // canceled
        // we are supposed to now use the strings of the event, but we can't properly determine where to put them
        mouseX = event.getX();
        mouseY = event.getY();
        int screenWidth = event.getScreenWidth(), screenHeight = event.getScreenHeight();
        this.maxWidth = event.getMaxWidth();

        // simulate to figure how big this tooltip is without any restrictions
        this.text.setupRenderer(renderer, 0, 0, this.maxWidth, -1, Color.WHITE.main, false);
        this.text.compileAndDraw(renderer, context, true);

        Rectangle area = determineTooltipArea(context, renderer, screenWidth, screenHeight, mouseX, mouseY);

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableBlend();

        GuiDraw.drawTooltipBackground(stack, textLines, area.x, area.y, area.width, area.height);

        MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostBackground(stack, textLines, area.x, area.y, TextRenderer.getFontRenderer(), area.width, area.height));

        GlStateManager.color(1f, 1f, 1f, 1f);

        renderer.setPos(area.x, area.y);
        this.text.compileAndDraw(renderer, context, false);

        MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostText(stack, textLines, area.x, area.y, TextRenderer.getFontRenderer(), area.width, area.height));
    }

    public Rectangle determineTooltipArea(GuiContext context, TextRenderer renderer, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        int width = (int) renderer.getLastWidth();
        int height = (int) renderer.getLastHeight();

        Pos pos = this.pos;
        if (pos == null) {
            pos = context.isMuiContext() ? context.getMuiContext().getScreen().getCurrentTheme().getTooltipPosOverride() : null;
            if (pos == null) pos = ModularUIConfig.tooltipPos;
        }
        if (pos == Pos.FIXED) {
            return new Rectangle(this.x, this.y, width, height);
        }

        if (pos == Pos.NEXT_TO_MOUSE) {
            // vanilla style, tooltip floats next to mouse
            // note that this behaves slightly different from vanilla (better imo)
            final int padding = 8;
            // magic number to place tooltip nicer. Look at GuiScreen#L237
            final int mouseOffset = 12;
            int x = mouseX + mouseOffset, y = mouseY - mouseOffset;
            if (x < padding) {
                x = padding; // this cant happen mathematically since mouse is always positive
            } else if (x + width + padding > screenWidth) {
                // doesn't fit on the right side of the screen
                if (screenWidth - mouseX < mouseX) { // check if left side has more space
                    x -= mouseOffset * 2 + width; // flip side of cursor if other side has more space
                    if (x < padding) {
                        x = padding; // went of screen
                    }
                    width = mouseX - 12 - x; // max space on left side
                } else {
                    width = screenWidth - padding - x; // max space on right side
                }
                // recalculate with and height
                renderer.setPos(x, y);
                renderer.setAlignment(this.text.getAlignment(), width, -1);
                this.text.compileAndDraw(renderer, context, true);
                width = (int) renderer.getLastWidth();
                height = (int) renderer.getLastHeight();
            }
            y = MathHelper.clamp(y, padding, screenHeight - padding - height);
            return new Rectangle(x, y, width, height);
        }

        // the rest of the cases will put the tooltip next a given area
        if (this.parent == null) {
            throw new IllegalStateException("Tooltip pos is " + pos.name() + ", but no widget parent is set!");
        }

        int minWidth = this.text.getMinWidth();

        int shiftAmount = 10;
        int padding = 7;

        Area area = HOLDER;
        this.parent.accept(area);
        area.transformAndRectanglerize(context);
        int x = 0, y = 0;
        if (pos.axis.isVertical()) { // above or below
            if (width < area.width) {
                x = area.x + shiftAmount;
            } else {
                x = area.x - shiftAmount;
                if (x < padding) {
                    x = padding;
                } else if (x + width > screenWidth - padding) {
                    int maxWidth = Math.max(minWidth, screenWidth - x - padding);
                    renderer.setAlignment(this.text.getAlignment(), maxWidth);
                    this.text.compileAndDraw(renderer, context, true);
                    width = (int) renderer.getLastWidth();
                    height = (int) renderer.getLastHeight();
                }
            }

            if (pos == Pos.VERTICAL) {
                int bottomSpace = screenHeight - area.ey();
                pos = bottomSpace < height + padding && bottomSpace < area.y ? Pos.ABOVE : Pos.BELOW;
            }

            if (pos == Pos.BELOW) {
                y = area.ey() + padding;
            } else if (pos == Pos.ABOVE) {
                y = area.y - height - padding;
            }
        } else if (pos.axis.isHorizontal()) {
            boolean usedMoreSpaceSide = false;
            Pos oPos = pos;
            if (oPos == Pos.HORIZONTAL) {
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
                renderer.setAlignment(this.text.getAlignment(), maxWidth);
                this.text.compileAndDraw(renderer, context, true);
                width = (int) renderer.getLastWidth();
                height = (int) renderer.getLastHeight();
            }

            if (oPos == Pos.HORIZONTAL && !usedMoreSpaceSide) {
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
        if (this.dirty) buildTooltip();
        return this.text.isEmpty();
    }

    public void markDirty() {
        this.dirty = true;
    }

    public int getShowUpTimer() {
        return this.showUpTimer;
    }

    public boolean isAutoUpdate() {
        return this.autoUpdate;
    }

    public RichTooltip pos(Pos pos) {
        this.pos = pos;
        return this;
    }

    public RichTooltip pos(int x, int y) {
        this.pos = Pos.FIXED;
        this.x = x;
        this.y = y;
        return this;
    }

    @Override
    public RichTooltip getThis() {
        return this;
    }

    @Override
    public IRichTextBuilder<?> getRichText() {
        return text;
    }

    public RichTooltip showUpTimer(int showUpTimer) {
        this.showUpTimer = showUpTimer;
        return this;
    }

    public RichTooltip tooltipBuilder(Consumer<RichTooltip> tooltipBuilder) {
        Consumer<RichTooltip> existingBuilder = this.tooltipBuilder;
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

    public RichTooltip setAutoUpdate(boolean update) {
        this.autoUpdate = update;
        return this;
    }

    public RichTooltip addFromItem(ItemStack item) {
        List<String> lines = MCHelper.getItemToolTip(item);
        add(lines.get(0)).spaceLine(2);
        for (int i = 1, n = lines.size(); i < n; i++) {
            add(lines.get(i)).newLine();
        }
        return this;
    }

    public enum Pos {

        ABOVE(GuiAxis.Y),
        BELOW(GuiAxis.Y),
        LEFT(GuiAxis.X),
        RIGHT(GuiAxis.X),
        VERTICAL(GuiAxis.Y),
        HORIZONTAL(GuiAxis.X),
        NEXT_TO_MOUSE(null),
        FIXED(null);

        public final GuiAxis axis;

        Pos(GuiAxis axis) {
            this.axis = axis;
        }
    }
}
