package com.cleanroommc.modularui.core.mixins.early.minecraft;

import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FontRenderer.class)
public interface FontRendererAccessor {

    @Invoker
    int invokeSizeStringToWidth(String str, int wrapWidth);
}
