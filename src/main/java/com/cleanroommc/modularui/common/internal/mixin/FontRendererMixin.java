package com.cleanroommc.modularui.common.internal.mixin;

import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FontRenderer.class)
public interface FontRendererMixin {

    @Invoker("resetStyles")
    void invokeResetStyles();

    @Invoker("renderString")
    int invokeRenderString(String text, float v, float v1, int color, boolean b);

}
