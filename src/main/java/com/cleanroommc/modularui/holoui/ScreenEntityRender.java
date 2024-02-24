package com.cleanroommc.modularui.holoui;

import com.cleanroommc.modularui.screen.GuiScreenWrapper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Highly experimental
 */
@ApiStatus.Experimental
public class ScreenEntityRender extends Render<HoloScreenEntity> {

    public ScreenEntityRender(RenderManager renderManager) {
        super(renderManager);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(@NotNull HoloScreenEntity entity) {
        return null;
    }

    @Override
    public void doRender(@NotNull HoloScreenEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GuiScreenWrapper screenWrapper = entity.getWrapper();
        if (screenWrapper == null) return;

        Plane3D plane3D = entity.getPlane3D();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        if (entity.getOrientation() == ScreenOrientation.TO_PLAYER) {
            EntityPlayer player = Minecraft.getMinecraft().player;
            plane3D.transform(player.getPositionVector(), entity.getPositionVector(), player.getLookVec());
        } else {
            plane3D.transform();
        }
        screenWrapper.drawScreen(0, 0, partialTicks);
        GlStateManager.popMatrix();
    }
}
