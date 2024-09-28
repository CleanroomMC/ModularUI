package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.screen.viewport.GuiContext;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link ModularScreen} which creates its panel via an overridable function for convenience.
 */
@OnlyIn(Dist.CLIENT)
public abstract class CustomModularScreen extends ModularScreen {

    /**
     * Creates a new screen with ModularUI as its owner.
     */
    public CustomModularScreen() {
        super(ModularUI.MOD_ID);
    }

    /**
     * Creates a new screen with a given owner.
     *
     * @param owner owner of this screen (usually a mod id)
     */
    public CustomModularScreen(@NotNull String owner) {
        super(owner);
    }

    /**
     * Creates the main panel of this screen. It's called in the super constructor and must return a new panel instance.
     *
     * @param context context used to build the panel
     * @return the created panel
     */
    @NotNull
    @ApiStatus.OverrideOnly
    public abstract ModularPanel buildUI(GuiContext context);
}
