package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link ModularScreen} which creates its panel via an overridable function for convenience.
 */
@SideOnly(Side.CLIENT)
public abstract class CustomModularScreen extends ModularScreen {

    /**
     * Creates a new screen with ModularUI as its owner.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public CustomModularScreen() {
        super(ModularUI.ID);
        if (ModularUI.isDev) {
            ModularUI.LOGGER.error("The single arg ModularScreen constructor should not be used. Use the other one and pass in your mod id.");
        }
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
    public abstract ModularPanel buildUI(ModularGuiContext context);
}
