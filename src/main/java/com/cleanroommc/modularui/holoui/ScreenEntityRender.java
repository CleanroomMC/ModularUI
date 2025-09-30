package com.cleanroommc.modularui.holoui;

import com.cleanroommc.modularui.screen.GuiContainerWrapper;
import com.cleanroommc.modularui.utils.Platform;

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
        GuiContainerWrapper screenWrapper = entity.getWrapper();
        if (screenWrapper == null) return;

        Plane3D plane3D = entity.getPlane3D();
        if (entity.getOrientation() == ScreenOrientation.TO_PLAYER) {
            EntityPlayer player = Platform.getClientPlayer();
            float xN = (float) (player.posX - entity.posX);
            float yN = (float) (player.posY - entity.posY);
            float zN = (float) (player.posZ - entity.posZ);
            plane3D.setNormal(xN, yN, zN);
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        plane3D.transformRectangle();
        screenWrapper.drawScreen(0, 0, partialTicks);
        GlStateManager.popMatrix();
    }
}
