package com.cleanroommc.modularui.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import org.jetbrains.annotations.NotNull;

@JEIPlugin
public class ModularUIJeiPlugin implements IModPlugin {

    @Override
    public void register(@NotNull IModRegistry registry) {
        registry.addAdvancedGuiHandlers(new ModularUIHandler());
    }
}
