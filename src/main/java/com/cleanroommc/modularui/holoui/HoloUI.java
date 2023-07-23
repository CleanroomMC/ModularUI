package com.cleanroommc.modularui.holoui;

import com.cleanroommc.modularui.screen.ModularScreen;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Highly experimental
 */
@ApiStatus.Experimental
public class HoloUI {

    private static final Map<ResourceLocation, Supplier<ModularScreen>> syncedHolos = new Object2ObjectOpenHashMap<>();

    public static void registerSyncedHoloUI(ResourceLocation loc, Supplier<ModularScreen> screen) {
        syncedHolos.put(loc, screen);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private double x, y, z;
        private Plane3D plane3D = new Plane3D();
        private ScreenOrientation orientation = ScreenOrientation.FIXED;

        public Builder at(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        public Builder inFrontOf(EntityPlayer player, double distance, boolean fixed) {
            Vec3d look = player.getLookVec();
            this.orientation = fixed ? ScreenOrientation.FIXED : ScreenOrientation.TO_PLAYER;
            return at(player.posX + look.x * distance, player.posY + player.getEyeHeight() + look.y * distance, player.posZ + look.z * distance);
        }

        public Builder faceToPlayer() {
            this.orientation = ScreenOrientation.TO_PLAYER;
            return this;
        }

        public Builder faceTo(float x, float y, float z) {
            this.orientation = ScreenOrientation.FIXED;
            this.plane3D.setNormal(x, y, z);
            return this;
        }

        public Builder screenAnchor(float x, float y) {
            this.plane3D.setAnchor(x, y);
            return this;
        }

        public Builder virtualScreenSize(int width, int height) {
            this.plane3D.setSize(width, height);
            return this;
        }

        public Builder screenScale(float scale) {
            this.plane3D.setScale(scale);
            return this;
        }

        public Builder plane(Plane3D plane) {
            this.plane3D = plane;
            return this;
        }

        public void open(ModularScreen screen) {
            HoloScreenEntity holoScreenEntity = new HoloScreenEntity(Minecraft.getMinecraft().world, this.plane3D);
            holoScreenEntity.setPosition(this.x, this.y, this.z);
            holoScreenEntity.setScreen(screen);
            holoScreenEntity.spawnInWorld();
            holoScreenEntity.setOrientation(this.orientation);
        }
    }
}
