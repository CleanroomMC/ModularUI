package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.screen.ModularScreen;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class ModularScreenEntity extends Entity {

    private GuiScreenWrapper wrapper;
    private ModularScreen screen;

    public ModularScreenEntity(World worldIn) {
        super(worldIn);
    }

    public void setScreen(ModularScreen screen) {
        this.screen = screen;
        this.wrapper = new GuiScreenWrapper(new ModularContainer(), screen);
        ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
        int i = scaledresolution.getScaledWidth();
        int j = scaledresolution.getScaledHeight();
        this.wrapper.setWorldAndResolution(Minecraft.getMinecraft(), i, j);
    }

    public ModularScreen getScreen() {
        return screen;
    }

    public GuiScreenWrapper getWrapper() {
        return wrapper;
    }

    public void spawnInWorld() {
        getEntityWorld().spawnEntity(this);
    }

    @Override
    protected void entityInit() {

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
    public int getBrightnessForRender(){
        return 15728880;
    }
}
