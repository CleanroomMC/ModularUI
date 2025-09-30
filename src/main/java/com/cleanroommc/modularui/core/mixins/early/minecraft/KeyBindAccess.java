package com.cleanroommc.modularui.core.mixins.early.minecraft;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyBindingMap;

import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBinding.class)
public interface KeyBindAccess {

    @Accessor
    static KeyBindingMap getHASH() {
        throw new NotImplementedException("KeyBindingMap getHASH()");
    }

    @Accessor
    void setPressed(boolean pressed);

    @Accessor
    int getPressTime();

    @Accessor
    void setPressTime(int time);
}
