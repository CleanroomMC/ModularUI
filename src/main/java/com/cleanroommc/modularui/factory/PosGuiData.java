package com.cleanroommc.modularui.factory;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

/**
 * See {@link GuiData} for an explanation for what this is for.
 */
public class PosGuiData extends GuiData {

    private static final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    private final int x, y, z;

    public PosGuiData(EntityPlayer player, int x, int y, int z) {
        super(player);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public PosGuiData(@NotNull EntityPlayer player, @NotNull BlockPos pos) {
        super(player);
        Objects.requireNonNull(pos);
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public double getSquaredDistance(double x, double y, double z) {
        double dx = this.x + 0.5 - x;
        double dy = this.y + 0.5 - y;
        double dz = this.z + 0.5 - z;
        return dx * dx + dy * dy + dz * dz;
    }

    public double getDistance(double x, double y, double z) {
        return Math.sqrt(getSquaredDistance(x, y, z));
    }

    public double getSquaredDistance(Entity entity) {
        return getSquaredDistance(entity.posX, entity.posY, entity.posZ);
    }

    public double getDistance(Entity entity) {
        return Math.sqrt(getSquaredDistance(entity));
    }

    public BlockPos getBlockPos() {
        return new BlockPos(this.x, this.y, this.z);
    }

    public TileEntity getTileEntity() {
        pos.setPos(this.x, this.y, this.z);
        return getWorld().getTileEntity(pos);
    }
}
