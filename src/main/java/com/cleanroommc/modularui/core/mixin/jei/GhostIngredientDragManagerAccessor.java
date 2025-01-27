package com.cleanroommc.modularui.core.mixin.jei;

import mezz.jei.gui.ghost.GhostIngredientDrag;
import mezz.jei.gui.ghost.GhostIngredientDragManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GhostIngredientDragManager.class, remap = false)
public interface GhostIngredientDragManagerAccessor {

    @Accessor
    GhostIngredientDrag<?> getGhostIngredientDrag();
}
