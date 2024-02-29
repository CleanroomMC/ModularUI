package com.cleanroommc.modularui.holoui;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.screen.GuiContainerWrapper;
import com.cleanroommc.modularui.utils.Animator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
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
import java.util.UUID;

/**
 * Highly experimental
 */
@ApiStatus.Experimental
public class ScreenEntityRender extends Render<HoloScreenEntity> {
    private static final Map<UUID, GuiContainerWrapper> lookingPlayers = new Object2ObjectOpenHashMap<>();

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
        var screen = screenWrapper.getScreen();

        Plane3D plane3D = entity.getPlane3D();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (entity.getOrientation() == ScreenOrientation.TO_PLAYER) {
            plane3D.transform(player.getPositionVector(), entity.getPositionVector());
        } else {
            plane3D.transform();
        }
        var mouse = calculateMousePos(player.getPositionVector().add(0, player.getEyeHeight(), 0), entity, player.getLookVec());
        screenWrapper.drawScreen(mouse.getX(), mouse.getY(), partialTicks);
//        screen.drawScreen(mouse.getX(), mouse.getY(), partialTicks);
        screen.onFrameUpdate();

        UUID id = player.getUniqueID();
        Animator.advance();
        if (withinScreen(mouse, entity.getPlane3D()) && !lookingPlayers.containsKey(id)) {
            lookingPlayers.put(id, screenWrapper);
        } else if (!withinScreen(mouse, entity.getPlane3D())) {
            lookingPlayers.remove(id);
        }
        GlStateManager.popMatrix();
    }

    public static void clickScreen(EntityPlayer player) {
        if (lookingPlayers.containsKey(player.getUniqueID())) {
            try {
                lookingPlayers.get(player.getUniqueID()).handleMouseInput();
            } catch (Throwable throwable1) {
                CrashReport c = CrashReport.makeCrashReport(throwable1, "Updating screen events");
                c.makeCategory("Affected screen")
                    .addDetail("Screen name", () -> lookingPlayers.get(player.getUniqueID()).getClass().getCanonicalName());
                throw new ReportedException(c);
            }
        }
    }

    @Override
    public boolean shouldRender(HoloScreenEntity screen, ICamera camera, double camX, double camY, double camZ) {
        boolean render = super.shouldRender(screen, camera, camX, camY, camZ);
        if (!render) {
            lookingPlayers.remove(Minecraft.getMinecraft().player);
        }
        return render;
    }

    private static Vec3i calculateMousePos(Vec3d player, HoloScreenEntity screen, Vec3d looking) {
        var holoPos = screen.getPositionVector();
        var plane = screen.getPlane3D();
        var planeRot = plane.getRotation();

        // get the difference of the player's eye position and holo position
        // rotate diff based on plane rotation
        double worldAngle = calculateAngle(holoPos.x, holoPos.z);
        double verticalAngle = calculateAngle(holoPos.y,  holoPos.z);
        var posR = player.rotateYaw((float) (planeRot.y - worldAngle))
                .rotatePitch((float) (planeRot.x + verticalAngle));
        var holoR = holoPos.rotateYaw((float) (planeRot.y - worldAngle))
                .rotatePitch((float) (planeRot.x + verticalAngle));

        // rotate looking so that 0, 0, 0 is facing exactly at the origin of the screen
        var lookRot = looking
                .rotateYaw((float) (planeRot.y - worldAngle))
                .rotatePitch((float) (planeRot.x + verticalAngle));

        // x should be the left-right offset from the player to the holo screen
        // y should be the up-down offset from the player to the holo screen
        // z should be the distance from the player to the holo screen's plane
        var diff = holoR.subtract(posR);

        // the x, y of look rot should be the mouse pos if scaled by looRot z
        // the scale factor should be the distance from the player to the plane by the z component of lookRot
        double sf = diff.z / lookRot.z;
        double mX = ((lookRot.x * sf) - diff.x) * 16;
        double mY = ((lookRot.y * sf) - diff.y) * 16;
        mY += plane.getHeight() / 2;
        mX += plane.getWidth() / 2;

        return new Vec3i(mX, mY, 0);
    }

    private static boolean withinScreen(Vec3i mousePos, Plane3D plane) {
        return mousePos.getX() > 0 && mousePos.getX() < plane.getWidth() &&
                mousePos.getY() > 0 && mousePos.getY() < plane.getHeight();
    }

    public static double calculateAngle(double opposite, double adjacent) {
        // x is opposite, z is adjacent, theta = atan(x/z)
        double a3 = Math.atan(opposite / adjacent);
        if (adjacent < 0) {
            // if z is negative, the angle returned by atan has to be offset by PI
            a3 += opposite < 0 ? -Math.PI : Math.PI;
        }
        return a3;
    }
}
