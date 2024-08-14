package com.cleanroommc.modularui.core.mixin;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyBindingMap;

import net.minecraftforge.client.settings.KeyMappingLookup;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyMapping.class)
public interface KeyBindAccess {

    @Accessor
    KeyMappingLookup getMap();

    @Accessor
    void setIsDown(boolean pressed);

    @Accessor
    int getClickCount();

    @Accessor
    void setClickCount(int time);
}
