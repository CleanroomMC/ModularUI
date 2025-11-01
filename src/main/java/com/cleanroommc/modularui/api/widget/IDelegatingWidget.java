package com.cleanroommc.modularui.api.widget;

import net.minecraft.inventory.Slot;

public interface IDelegatingWidget extends IWidget, IVanillaSlot {

    IWidget getDelegate();

    @Override
    default Slot getVanillaSlot() {
        return getDelegate() instanceof IVanillaSlot vanillaSlot ? vanillaSlot.getVanillaSlot() : null;
    }

    @Override
    default boolean handleAsVanillaSlot() {
        return getDelegate() instanceof IVanillaSlot vanillaSlot && vanillaSlot.handleAsVanillaSlot();
    }
}
