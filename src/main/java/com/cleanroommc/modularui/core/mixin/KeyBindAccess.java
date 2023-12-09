package com.cleanroommc.modularui.core.mixin;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyBindingMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBinding.class)
public interface KeyBindAccess {

    @Accessor
    KeyBindingMap getHASH();

    @Accessor
    void setPressed(boolean pressed);

    @Accessor
    int getPressTime();

    @Accessor
    void setPressTime(int time);
}
