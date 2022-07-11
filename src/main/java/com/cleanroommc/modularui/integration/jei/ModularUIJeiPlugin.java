package com.cleanroommc.modularui.integration.jei;

import com.cleanroommc.modularui.common.internal.wrapper.ModularGui;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import org.jetbrains.annotations.NotNull;

@JEIPlugin
public class ModularUIJeiPlugin implements IModPlugin {

    @Override
    public void register(@NotNull IModRegistry registry) {
        ModularUIHandler uiHandler = new ModularUIHandler();
        registry.addAdvancedGuiHandlers(uiHandler);
        registry.addGhostIngredientHandler(ModularGui.class, uiHandler);
        registry.addGuiScreenHandler(ModularGui.class, uiHandler);
    }
}
