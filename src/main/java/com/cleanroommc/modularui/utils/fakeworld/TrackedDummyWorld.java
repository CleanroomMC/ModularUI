package com.cleanroommc.modularui.utils.fakeworld;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.util.vector.Vector3f;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

@SideOnly(Side.CLIENT)
public class TrackedDummyWorld extends DummyWorld {

    public final Set<BlockPos> renderedBlocks = new ObjectOpenHashSet<>();
    private Predicate<BlockPos> renderFilter;
    private final World proxyWorld;

    private final Vector3f minPos = new Vector3f(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    private final Vector3f maxPos = new Vector3f(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

    public void setRenderFilter(Predicate<BlockPos> renderFilter) {
        this.renderFilter = renderFilter;
    }

    public TrackedDummyWorld() {
        proxyWorld = null;
    }

    public TrackedDummyWorld(World world) {
        proxyWorld = world;
    }

    public void addBlocks(Map<BlockPos, BlockInfo> renderedBlocks) {
        renderedBlocks.forEach(this::addBlock);
    }

    public void addBlock(BlockPos pos, BlockInfo blockInfo) {
        if (blockInfo.getBlockState().getBlock() == Blocks.AIR)
            return;
        this.renderedBlocks.add(pos);
        blockInfo.apply(this, pos);
    }

    @Override
    public TileEntity getTileEntity(@NotNull BlockPos pos) {
        if (renderFilter != null && !renderFilter.test(pos))
            return null;
        return proxyWorld != null ? proxyWorld.getTileEntity(pos) : super.getTileEntity(pos);
    }

    @NotNull
    @Override
    public IBlockState getBlockState(@NotNull BlockPos pos) {
        if (renderFilter != null && !renderFilter.test(pos))
            return Blocks.AIR.getDefaultState(); // return air if not rendering this block
        return proxyWorld != null ? proxyWorld.getBlockState(pos) : super.getBlockState(pos);
    }

    @Override
    public boolean setBlockState(@NotNull BlockPos pos, @NotNull IBlockState newState, int flags) {
        minPos.setX(Math.min(minPos.getX(), pos.getX()));
        minPos.setY(Math.min(minPos.getY(), pos.getY()));
        minPos.setZ(Math.min(minPos.getZ(), pos.getZ()));
        maxPos.setX(Math.max(maxPos.getX(), pos.getX()));
        maxPos.setY(Math.max(maxPos.getY(), pos.getY()));
        maxPos.setZ(Math.max(maxPos.getZ(), pos.getZ()));
        return super.setBlockState(pos, newState, flags);
    }

    public Vector3f getCenter() {
        Vector3f center = (Vector3f) Vector3f.sub(maxPos, minPos, null).scale(0.5f);
        return Vector3f.add(center, minPos, center);
    }

    public float getMaxSize() {
        return Math.max(maxPos.x - minPos.x, Math.max(maxPos.y - minPos.y, maxPos.z - minPos.z));
    }

    public Vector3f getSize() {
        Vector3f result = new Vector3f();
        result.setX(maxPos.getX() - minPos.getX() + 1);
        result.setY(maxPos.getY() - minPos.getY() + 1);
        result.setZ(maxPos.getZ() - minPos.getZ() + 1);
        return result;
    }

    public Vector3f getMinPos() {
        return minPos;
    }

    public Vector3f getMaxPos() {
        return maxPos;
    }
}
