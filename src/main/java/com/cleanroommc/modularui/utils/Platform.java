package com.cleanroommc.modularui.utils;

import net.minecraft.item.ItemStack;

/**
 * Version specific code is supposed to go here.
 * Ideally only the body of methods and value of fields should be changed and no signatures.
 */
public class Platform {

    public static final ItemStack EMPTY_STACK = ItemStack.EMPTY;

    public static boolean isStackEmpty(ItemStack stack) {
        return stack == null || stack.isEmpty();
    }

    public static ItemStack copyStack(ItemStack stack) {
        return isStackEmpty(stack) ? EMPTY_STACK : stack.copy();
    }
}
