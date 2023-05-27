package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScreenEntityRender extends Render<ModularScreenEntity> {

    public ScreenEntityRender(RenderManager renderManager) {
        super(renderManager);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(@NotNull ModularScreenEntity entity) {
        return null;
    }

    @Override
    public void doRender(@NotNull ModularScreenEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GuiScreenWrapper screenWrapper = entity.getWrapper();
        if (screenWrapper == null) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.scale(0.02, 0.02, 0.02);
        GlStateManager.rotate(180, 0, 0, 1);
        screenWrapper.drawScreen(Integer.MIN_VALUE, Integer.MIN_VALUE, partialTicks);
        GlStateManager.popMatrix();
    }
}
