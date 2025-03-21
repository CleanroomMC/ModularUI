package com.cleanroommc.modularui.integration.jei;

import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.screen.ModularScreen;

import net.minecraft.entity.player.EntityPlayer;

import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModularContainerJEIHandler<T extends ModularContainer> implements IRecipeTransferHandler<T> {

    public static <T extends ModularContainer> void register(Class<T> clz, IModRegistry registry) {
        new ModularContainerJEIHandler<>(clz).register(registry);
    }

    private final Class<T> clazz;

    private ModularContainerJEIHandler(Class<T> clazz) {
        this.clazz = clazz;
    }

    private void register(IModRegistry registry) {
        registry.getRecipeTransferRegistry().addUniversalRecipeTransferHandler(this);
    }

    @Override
    public @NotNull Class<T> getContainerClass() {
        return clazz;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@NotNull ModularContainer container, @NotNull IRecipeLayout recipeLayout, @NotNull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        ModularScreen screen = container.getScreen();
        if (screen instanceof JeiRecipeTransferHandler recipeTransferHandler) {
            return recipeTransferHandler.transferRecipe(recipeLayout, maxTransfer, !doTransfer);
        }
        return null;
    }
}
