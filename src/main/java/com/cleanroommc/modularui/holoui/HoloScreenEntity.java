package com.cleanroommc.modularui.holoui;

import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import com.cleanroommc.modularui.screen.ModularScreen;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Highly experimental
 */
@ApiStatus.Experimental
public class HoloScreenEntity extends Entity {

    private GuiScreenWrapper wrapper;
    private ModularScreen screen;
    private final Plane3D plane3D;
    private static final DataParameter<Byte> ORIENTATION = EntityDataManager.createKey(HoloScreenEntity.class, DataSerializers.BYTE);

    public HoloScreenEntity(World worldIn, Plane3D plane3D) {
        super(worldIn);
        this.plane3D = plane3D;
    }

    public HoloScreenEntity(World world) {
        this(world, new Plane3D());
    }

    public void setScreen(ModularScreen screen) {
        this.screen = screen;
    }

    public void setWrapper(GuiScreenWrapper wrapper) {
        this.setScreen(wrapper.getScreen());
        this.wrapper = wrapper;
        this.wrapper.setWorldAndResolution(Minecraft.getMinecraft(), (int) this.plane3D.getWidth(), (int) this.plane3D.getHeight());
    }

    public ModularScreen getScreen() {
        return this.screen;
    }

    public GuiScreenWrapper getWrapper() {
        return this.wrapper;
    }

    public void spawnInWorld() {
        getEntityWorld().spawnEntity(this);
    }

    public void setOrientation(ScreenOrientation orientation) {
        this.dataManager.set(ORIENTATION, (byte) orientation.ordinal());
    }

    public ScreenOrientation getOrientation() {
        return ScreenOrientation.values()[this.dataManager.get(ORIENTATION)];
    }

    public Plane3D getPlane3D() {
        return this.plane3D;
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(ORIENTATION, (byte) 1);
    }

    @Override
    public void onEntityUpdate() {
        this.world.profiler.startSection("entityBaseTick");
        this.prevDistanceWalkedModified = this.distanceWalkedModified;
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.prevRotationPitch = this.rotationPitch;
        this.prevRotationYaw = this.rotationYaw;

        if (this.posY < -64.0D) {
            this.outOfWorld();
        }

        if (this.world.isRemote) {
            this.extinguish();
            int w = (int) this.plane3D.getWidth(), h = (int) this.plane3D.getHeight();
            if (w != this.wrapper.width || h != this.wrapper.height) {
                this.wrapper.onResize(Minecraft.getMinecraft(), w, h);
            }
        }

        this.firstUpdate = false;
        this.world.profiler.endSection();
    }

    @Override
    public boolean isInRangeToRender3d(double x, double y, double z) {
        return true;
    }

    @Override
    public boolean isInRangeToRenderDist(double distance) {
        return true;
    }

    @Override
    protected void readEntityFromNBT(@NotNull NBTTagCompound compound) {

    }

    @Override
    protected void writeEntityToNBT(@NotNull NBTTagCompound compound) {

    }

    @Override
    public boolean doesEntityNotTriggerPressurePlate() {
        return true;
    }

    @Override
    public boolean isImmuneToExplosions() {
        return true;
    }

    @Override
    public boolean isCreatureType(@NotNull EnumCreatureType type, boolean forSpawnCount) {
        return false;
    }

    @Override
    public boolean canTrample(@NotNull World world, @NotNull Block block, @NotNull BlockPos pos, float fallDistance) {
        return false;
    }

    @Override
    protected boolean canBeRidden(@NotNull Entity entityIn) {
        return false;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getBrightnessForRender() {
        return 15728880;
    }
}
