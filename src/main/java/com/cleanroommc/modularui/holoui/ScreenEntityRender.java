package com.cleanroommc.modularui.holoui;

import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import com.cleanroommc.modularui.utils.Animator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Highly experimental
 */
@ApiStatus.Experimental
public class ScreenEntityRender extends Render<HoloScreenEntity> {
    private static final Map<EntityPlayer, GuiScreenWrapper> lookingPlayers = new Object2ObjectOpenHashMap<>();

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
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (entity.getOrientation() == ScreenOrientation.TO_PLAYER) {
            plane3D.transform(player.getPositionVector(), entity.getPositionVector(), player.getLookVec());
        } else {
            plane3D.transform();
        }
        var mouse = calculateMousePos(player.getPositionVector().add(0, player.getEyeHeight(), 0), entity, player.getLookVec());
        screenWrapper.drawScreen(mouse.getX(), mouse.getY(), partialTicks);
        screenWrapper.getScreen().onFrameUpdate();
        Animator.advance();
        if (withinScreen(mouse, entity.getPlane3D()) && !lookingPlayers.containsKey(player)) {
            lookingPlayers.put(player, screenWrapper);
        } else if (!withinScreen(mouse, entity.getPlane3D())) {
            lookingPlayers.remove(player);
        }
        GlStateManager.popMatrix();
    }

    public static void clickScreen(EntityPlayer player) {
        if (lookingPlayers.containsKey(player)) {
            try {
                lookingPlayers.get(player).handleMouseInput();
            } catch (Throwable throwable1) {
                CrashReport c = CrashReport.makeCrashReport(throwable1, "Updating screen events");
                c.makeCategory("Affected screen")
                    .addDetail("Screen name", () -> lookingPlayers.get(player).getClass().getCanonicalName());
                throw new ReportedException(c);
            }
        }
    }

    private static Vec3i calculateMousePos(Vec3d player, HoloScreenEntity entity, Vec3d looking) {
        var holoPos = entity.getPositionVector();

        var plane = entity.getPlane3D();
        var planeRot = plane.getRotation();

        // get the difference of the player's eye position and holo position
        // rotate diff based on plane rotation
        double worldAngle = calculateHorizontalAngle(holoPos);
        var posR = player.rotateYaw((float) (planeRot.y - worldAngle));
        var holoR = holoPos.rotateYaw((float) (planeRot.y - worldAngle));

        // x should be the left-right offset from the player to the holo screen
        // y should be the up-down offset from the player to the holo screen
        // z should be the distance from the player to the holo screen's plane
        var diff = holoR.subtract(posR);

        // rotate to make x zero
        var lookRot = looking
                .rotateYaw((float) (planeRot.y - worldAngle))
                .rotatePitch((float) planeRot.x);

        // the x, y of look rot should be the mouse pos if scaled by looRot z
        // the scale factor should be the distance from the player to the plane by the z component of lookRot
        double sf = diff.z / lookRot.z;
        double mX = ((lookRot.x * sf) - diff.x) * 16;
        double mY = ((lookRot.y * -sf) + diff.y) * 16;
        mY += plane.getHeight() / 2;
        mX += plane.getWidth() / 2;

        return new Vec3i(mX, mY, 0);
    }

    private static boolean withinScreen(Vec3i mousePos, Plane3D plane) {
        return mousePos.getX() > 0 && mousePos.getX() < plane.getWidth() &&
                mousePos.getY() > 0 && mousePos.getY() < plane.getHeight();
    }

    private static double calculateHorizontalAngle(Vec3d vec) {
        // x is opposite, z is adjacent, theta = atan(x/z)
        double a3 = Math.atan(vec.x / vec.z);
        if (vec.z < 0) {
            // if z is negative, the angle returned by atan has to be offset by PI
            a3 += vec.x < 0 ? -Math.PI : Math.PI;
        }
        return a3;
    }
}
