package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.MCHelper;
import com.cleanroommc.modularui.api.drawable.IRichTextBuilder;
import com.cleanroommc.modularui.api.drawable.ITextLine;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.text.RichText;
import com.cleanroommc.modularui.drawable.text.TextRenderer;
import com.cleanroommc.modularui.integration.jei.ModularUIJeiPlugin;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.MathUtils;
import com.cleanroommc.modularui.widget.sizer.Area;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IShowsRecipeFocuses;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RichTooltip implements IRichTextBuilder<RichTooltip> {

    private static final Area HOLDER = new Area();

    private final RichText text = new RichText();
    private Consumer<Area> parent;
    private Pos pos = null;
    private Consumer<RichTooltip> tooltipBuilder;
    private int showUpTimer = 0;
    private boolean autoUpdate = false;
    private int titleMargin = 0;
    private boolean appliedMargin = true;

    private int x = 0, y = 0;
    private int maxWidth = Integer.MAX_VALUE;

    private boolean dirty;

    public RichTooltip() {
        parent(Area.ZERO);
    }

    @Override
    public RichTooltip reset() {
        this.text.reset();
        this.pos = null;
        this.tooltipBuilder = null;
        this.showUpTimer = 0;
        this.autoUpdate = false;
        this.titleMargin = 0;
        this.appliedMargin = true;
        this.x = 0;
        this.y = 0;
        this.maxWidth = Integer.MAX_VALUE;
        return this;
    }

    public RichTooltip parent(Consumer<Area> parent) {
        this.parent = parent;
        return this;
    }

    public RichTooltip parent(Supplier<Area> parent) {
        return parent(area -> area.set(parent.get()));
    }

    public RichTooltip parent(Area parent) {
        return parent(area -> area.set(parent));
    }

    public RichTooltip parent(IWidget parent) {
        return parent(area -> {
            area.setPos(0, 0);
            area.setSize(parent.getArea());
        });
    }

    public void buildTooltip() {
        this.dirty = false;
        if (this.tooltipBuilder != null) {
            this.text.clearText();
            this.tooltipBuilder.accept(this);
            this.appliedMargin = false;
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
        if (!this.appliedMargin) {
            if (this.titleMargin > 0) {
                this.text.insertTitleMargin(this.titleMargin);
            }
            this.appliedMargin = true;
        }
        Area screen = context.getScreenArea();
        this.maxWidth = Math.min(this.maxWidth, screen.width);
        int mouseX = context.getAbsMouseX(), mouseY = context.getAbsMouseY();
        TextRenderer renderer = TextRenderer.SHARED;
        RichText copy = this.text.copy();
        // this only turns the text and not any drawables into strings
        RichTooltipEvent.Pre event = new RichTooltipEvent.Pre(stack, copy.getAsStrings(), mouseX, mouseY, screen.width, screen.height, this.maxWidth, TextRenderer.getFontRenderer(), copy);
        if (MinecraftForge.EVENT_BUS.post(event)) return; // canceled
        // we are supposed to now use the strings of the event, but we can't properly determine where to put them
        mouseX = event.getX();
        mouseY = event.getY();
        int screenWidth = event.getScreenWidth(), screenHeight = event.getScreenHeight();
        this.maxWidth = event.getMaxWidth();

        // simulate to figure how big this tooltip is without any restrictions
        copy.setupRenderer(renderer, 0, 0, this.maxWidth, -1, Color.WHITE.main, false);
        copy.compileAndDraw(renderer, context, true);

        Rectangle area = determineTooltipArea(copy, context, renderer, screenWidth, screenHeight, mouseX, mouseY);
        renderer.setPos(area.x, area.y);
        renderer.setAlignment(copy.getAlignment(), (float) Math.ceil(area.width + copy.getScale()), -1);
        List<ITextLine> compiledLines = copy.compileAndDraw(renderer, context, true);
        area.width = (int) renderer.getLastTrimmedWidth();
        area.height = (int) renderer.getLastTrimmedHeight();

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableBlend();

        GuiDraw.drawTooltipBackground(stack, copy.getAsStrings(), area.x, area.y, area.width, area.height, this);

        MinecraftForge.EVENT_BUS.post(new RichTooltipEvent.PostBackground(stack, copy.getAsStrings(), area.x, area.y, TextRenderer.getFontRenderer(), area.width, area.height, copy));

        GlStateManager.color(1f, 1f, 1f, 1f);

        renderer.setPos(area.x, area.y);
        renderer.setSimulate(false);
        renderer.drawCompiled(context, compiledLines);

        MinecraftForge.EVENT_BUS.post(new RichTooltipEvent.PostText(stack, copy.getAsStrings(), area.x, area.y, TextRenderer.getFontRenderer(), area.width, area.height, copy));
    }

    public Rectangle determineTooltipArea(RichText text, GuiContext context, TextRenderer renderer, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        int width = (int) renderer.getLastTrimmedWidth();
        int height = (int) renderer.getLastTrimmedHeight();
        if (width > screenWidth - 14) {
            width = screenWidth - 14;
        }

        Pos pos = this.pos;
        if (pos == null) {
            pos = ModularUIConfig.tooltipPos;
        }
        if (pos == Pos.FIXED) {
            return new Rectangle(this.x, this.y, width, height);
        }

        Area area = HOLDER;
        this.parent.accept(area);
        if (area.x == 0 && area.y == 0 && area.width == 0 && area.height == 0) {
            pos = Pos.NEXT_TO_MOUSE;
        }

        if (pos == Pos.NEXT_TO_MOUSE) {
            // vanilla style, tooltip floats next to mouse
            // note that this behaves slightly different from vanilla (better imo)
            final int padding = 8;
            // magic number to place tooltip nicer. Look at GuiScreen#L237
            final int mouseOffset = 12;
            int x = mouseX + mouseOffset, y = mouseY - mouseOffset;
            if (x < padding) {
                x = padding; // this can't happen mathematically since mouse is always positive
            } else if (x + width + padding > screenWidth) {
                // doesn't fit on the right side of the screen
                if (screenWidth - mouseX < mouseX) { // check if left side has more space
                    x -= mouseOffset * 2 + width; // flip side of cursor if other side has more space
                    if (x < padding) {
                        x = padding; // went of screen
                    }
                    width = mouseX - mouseOffset - padding; // max space on left side
                } else {
                    width = screenWidth - padding - x; // max space on right side
                }
                // recalculate with and height
                renderer.setPos(x, y);
                renderer.setAlignment(text.getAlignment(), width, -1);
                text.compileAndDraw(renderer, context, true);
                width = (int) renderer.getLastTrimmedWidth();
                height = (int) renderer.getLastTrimmedHeight();
            }
            y = MathUtils.clamp(y, padding, screenHeight - padding - height);
            return new Rectangle(x, y, width, height);
        }

        // the rest of the cases will put the tooltip next a given area
        if (this.parent == null) {
            throw new IllegalStateException("Tooltip pos is " + pos.name() + ", but no widget parent is set!");
        }

        int minWidth = text.getMinWidth();

        int shiftAmount = 10;
        int padding = 7;

        area.transformAndRectanglerize(context);
        int x = 0, y = 0;
        if (pos.axis.isVertical()) { // above or below
            x = area.x + (width < area.width ? shiftAmount : -shiftAmount);
            x = MathUtils.clamp(x, padding, screenWidth - padding - width);

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
                renderer.setAlignment(text.getAlignment(), maxWidth);
                text.compileAndDraw(renderer, context, true);
                width = (int) renderer.getLastTrimmedWidth();
                height = (int) renderer.getLastTrimmedHeight();
            }

            if (oPos == Pos.HORIZONTAL && !usedMoreSpaceSide) {
                int rightSpace = screenWidth - area.ex();
                pos = rightSpace < width + padding && rightSpace < area.x ? Pos.LEFT : Pos.RIGHT;
            }

            if (pos == Pos.RIGHT) {
                x = area.ex() + padding;
            } else if (pos == Pos.LEFT) {
                x = area.x - width - padding;
                if (x < padding) {
                    x = padding;
                    width = area.x - x - padding;
                }
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
        if (existingBuilder != null && tooltipBuilder != null) {
            this.tooltipBuilder = tooltip -> {
                existingBuilder.accept(this);
                tooltipBuilder.accept(this);
            };
        } else {
            this.tooltipBuilder = tooltipBuilder;
        }
        markDirty();
        return this;
    }

    public RichTooltip setAutoUpdate(boolean update) {
        this.autoUpdate = update;
        return this;
    }

    public RichTooltip addFromItem(ItemStack item) {
        List<String> lines = MCHelper.getItemToolTip(item);
        add(lines.get(0)).newLine();
        if (lines.size() > 1) {
            spaceLine();
            for (int i = 1, n = lines.size(); i < n; i++) {
                add(lines.get(i)).newLine();
            }
        }
        return this;
    }

    public RichTooltip titleMargin() {
        return titleMargin(0);
    }

    public RichTooltip titleMargin(int margin) {
        this.titleMargin = margin;
        this.appliedMargin = false;
        return this;
    }

    private static void findIngredientArea(Area area, int x, int y) {
        GuiScreen screen = MCHelper.getCurrentScreen();
        if (screen instanceof GuiContainer guiContainer) {
            Slot slot = guiContainer.getSlotUnderMouse();
            if (slot != null) {
                int sx = slot.xPos + guiContainer.getGuiLeft();
                int sy = slot.yPos + guiContainer.getGuiTop();
                if (sx >= 0 && sy >= 0) {
                    area.set(sx - 1, sy - 1, 18, 18);
                    return;
                }
            }
        }
        if (ModularUI.Mods.JEI.isLoaded()) {
            IShowsRecipeFocuses overlay = (IShowsRecipeFocuses) ModularUIJeiPlugin.getRuntime().getIngredientListOverlay();
            IClickedIngredient<?> ingredient = overlay.getIngredientUnderMouse(x, y);
            if (ingredient == null || ingredient.getArea() == null) {
                overlay = (IShowsRecipeFocuses) ModularUIJeiPlugin.getRuntime().getBookmarkOverlay();
                ingredient = overlay.getIngredientUnderMouse(x, y);
            }
            if (ingredient != null && ingredient.getArea() != null) {
                Rectangle slot = ingredient.getArea();
                area.set(slot.x - 1, slot.y - 1, 18, 18);
                return;
            }
        }
        area.set(Area.ZERO);
    }

    @ApiStatus.Internal
    public static void injectRichTooltip(ItemStack stack, List<String> lines, int x, int y) {
        RichTooltip tooltip = new RichTooltip();
        tooltip.parent(area -> RichTooltip.findIngredientArea(area, x, y));
        // Other positions don't really work due to the lack of GuiContext in non-modular uis
        tooltip.add(lines.get(0)).newLine();
        if (lines.size() > 1) {
            if (!stack.isEmpty()) {
                tooltip.spaceLine();
            }
            for (int i = 1, n = lines.size(); i < n; i++) {
                tooltip.add(lines.get(i)).newLine();
            }
        }

        tooltip.draw(GuiContext.getDefault(), stack);
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
