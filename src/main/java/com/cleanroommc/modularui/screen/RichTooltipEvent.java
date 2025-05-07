package com.cleanroommc.modularui.screen;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.function.Consumer;

@Cancelable
public class RichTooltipEvent extends Event {

    protected ItemStack stack;
    protected RichTooltip tooltip;

    public RichTooltipEvent(ItemStack stack, RichTooltip tooltip) {
        this.stack = stack;
        this.tooltip = tooltip;
    }

    public void addCustom(Consumer<RichTooltip> consumer) {
        consumer.accept(tooltip);
    }
}
