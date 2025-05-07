package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.drawable.IRichTextBuilder;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RichTooltipEvent {

    private RichTooltipEvent () {}

    @Cancelable
    public static class Pre extends RenderTooltipEvent.Pre {

        private final IRichTextBuilder<?> tooltip;

        public Pre(@NotNull ItemStack stack, @NotNull List<String> lines, int x, int y, int screenWidth, int screenHeight, int maxWidth,
                   @NotNull FontRenderer fr, IRichTextBuilder<?> tooltip) {
            super(stack, lines, x, y, screenWidth, screenHeight, maxWidth, fr);
            this.tooltip = tooltip;
        }

        public IRichTextBuilder<?> getTooltip() {
            return tooltip;
        }
    }

    public static class Color extends RenderTooltipEvent.Color {

        private final IRichTextBuilder<?> tooltip;

        public Color(@NotNull ItemStack stack, @NotNull List<String> lines, int x, int y,
                     @NotNull FontRenderer fr, int background, int borderStart,
                     int borderEnd, IRichTextBuilder<?> tooltip) {
            super(stack, lines, x, y, fr, background, borderStart, borderEnd);
            this.tooltip = tooltip;
        }

        public IRichTextBuilder<?> getTooltip() {
            return tooltip;
        }
    }

    public static class PostBackground extends RenderTooltipEvent.PostBackground {

        private final IRichTextBuilder<?> tooltip;

        public PostBackground(@NotNull ItemStack stack, @NotNull List<String> lines, int x, int y,
                              @NotNull FontRenderer fr, int width, int height, IRichTextBuilder<?> tooltip) {
            super(stack, lines, x, y, fr, width, height);
            this.tooltip = tooltip;
        }

        public IRichTextBuilder<?> getTooltip() {
            return tooltip;
        }
    }

    public static class PostText extends RenderTooltipEvent.PostText {

        private final IRichTextBuilder<?> tooltip;

        public PostText(@NotNull ItemStack stack, @NotNull List<String> lines, int x, int y,
                        @NotNull FontRenderer fr, int width, int height, IRichTextBuilder<?> tooltip) {
            super(stack, lines, x, y, fr, width, height);
            this.tooltip = tooltip;
        }

        public IRichTextBuilder<?> getTooltip() {
            return tooltip;
        }
    }
}
