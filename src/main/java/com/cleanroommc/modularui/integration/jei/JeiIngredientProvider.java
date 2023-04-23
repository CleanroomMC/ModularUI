package com.cleanroommc.modularui.integration.jei;

import org.jetbrains.annotations.Nullable;

/**
 * An interface for JEI to get the ingredient from a widget to show recipes for example.
 * Implement this on {@link com.cleanroommc.modularui.api.widget.IWidget}.
 * No further registration needed.
 */
public interface JeiIngredientProvider {

    @Nullable
    Object getIngredient();

}
