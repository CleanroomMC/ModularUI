package com.cleanroommc.modularui.utils.fakeworld;

import com.cleanroommc.modularui.ModularUI;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;

import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;

import it.unimi.dsi.fastutil.chars.CharArraySet;
import it.unimi.dsi.fastutil.chars.CharSet;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import com.google.common.collect.AbstractIterator;

import net.minecraftforge.fml.common.registry.ForgeRegistries;

import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

public class ArraySchema implements ISchema {

    public static Builder builder() {
        return new Builder();
    }

    private final World world;
    private final BlockInfo[][][] blocks;
    private BiPredicate<BlockPos, BlockInfo> renderFilter;
    private final Vec3d center;

    public ArraySchema(BlockInfo[][][] blocks) {
        this.blocks = blocks;
        this.world = new DummyWorld();
        BlockPos.MutableBlockPos current = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos max = new BlockPos.MutableBlockPos(BlockPosUtil.MIN);
        for (int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < blocks[x].length; y++) {
                for (int z = 0; z < blocks[x][y].length; z++) {
                    BlockInfo block = blocks[x][y][z];
                    if (block == null) continue;
                    current.setPos(x, y, z);
                    BlockPosUtil.setMax(max, current);
                    block.apply(this.world, current);
                }
            }
        }
        this.center = BlockPosUtil.getCenterD(BlockPos.ORIGIN, BlockPosUtil.add(max, 1, 1, 1));
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public Vec3d getFocus() {
        return center;
    }

    @Override
    public BlockPos getOrigin() {
        return BlockPos.ORIGIN;
    }

    @Override
    public void setRenderFilter(@Nullable BiPredicate<BlockPos, BlockInfo> renderFilter) {
        this.renderFilter = renderFilter;
    }

    @Override
    public @Nullable BiPredicate<BlockPos, BlockInfo> getRenderFilter() {
        return renderFilter;
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<BlockPos, BlockInfo>> iterator() {
        return new AbstractIterator<>() {

            private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            private final MutablePair<BlockPos, BlockInfo> pair = new MutablePair<>(pos, null);
            private int x = 0, y = 0, z = -1;

            @Override
            protected Map.Entry<BlockPos, BlockInfo> computeNext() {
                BlockInfo info;
                while (true) {
                    if (++z >= blocks[x][y].length) {
                        z = 0;
                        if (++y >= blocks[x].length) {
                            y = 0;
                            if (++x >= blocks.length) {
                                return endOfData();
                            }
                        }
                    }
                    pos.setPos(x, y, z);
                    info = blocks[x][y][z];
                    if (info != null && renderFilter.test(pos, info)) {
                        pair.setRight(info);
                        return pair;
                    }
                }
            }
        };
    }

    public static class Builder {

        private final List<String[]> tensor = new ArrayList<>();
        private final Char2ObjectMap<BlockInfo> blockMap = new Char2ObjectOpenHashMap<>();

        public Builder() {
            blockMap.put(' ', BlockInfo.EMPTY);
            blockMap.put('#', BlockInfo.EMPTY);
        }

        public Builder layer(String... layer) {
            this.tensor.add(layer);
            return this;
        }

        public Builder where(char c, BlockInfo info) {
            this.blockMap.put(c, info);
            return this;
        }

        public Builder whereAir(char c) {
            return where(c, BlockInfo.EMPTY);
        }

        public Builder where(char c, IBlockState blockState) {
            return where(c, new BlockInfo(blockState));
        }

        public Builder where(char c, IBlockState blockState, TileEntity tile) {
            return where(c, new BlockInfo(blockState, tile));
        }

        public Builder where(char c, Block block) {
            return where(c, new BlockInfo(block));
        }

        public Builder where(char c, ResourceLocation registryName, int stateMeta) {
            Block block = ForgeRegistries.BLOCKS.getValue(registryName);
            if (block == null) throw new IllegalArgumentException("Block with name " + registryName + " doesn't exist!");
            IBlockState state = block.getStateFromMeta(stateMeta);
            return where(c, new BlockInfo(state));
        }

        public Builder where(char c, ResourceLocation registryName) {
            return where(c, registryName, 0);
        }

        public Builder where(char c, String registryName, int stateMeta) {
            return where(c, new ResourceLocation(registryName), stateMeta);
        }

        public Builder where(char c, String registryName) {
            return where(c, new ResourceLocation(registryName), 0);
        }

        private void validate() {
            if (this.tensor.isEmpty()) {
                throw new IllegalArgumentException("no block matrix defined");
            }
            List<String> errors = new ArrayList<>();
            CharSet checkedChars = new CharArraySet();
            int layerSize = this.tensor.get(0).length;
            for (int x = 0; x < this.tensor.size(); x++) {
                String[] xLayer = this.tensor.get(x);
                if (xLayer.length == 0) {
                    errors.add(String.format("Layer %s is empty. This is not right", x + 1));
                } else if (xLayer.length != layerSize) {
                    errors.add(String.format("Invalid x-layer size. Expected %s, but got %s at layer %s", layerSize, xLayer.length, x + 1));
                }
                int rowSize = xLayer[0].length();
                for (int y = 0; y < xLayer.length; y++) {
                    String yRow = xLayer[y];
                    if (yRow.isEmpty()) {
                        errors.add(String.format("Row %s in layer %s is empty. This is not right", y + 1, x + 1));
                    } else if (yRow.length() != rowSize) {
                        errors.add(String.format("Invalid x-layer size. Expected %s, but got %s at row %s in layer %s", layerSize, xLayer.length, y + 1, x + 1));
                    }
                    for (int z = 0; z < yRow.length(); z++) {
                        char zChar = yRow.charAt(z);
                        if (!checkedChars.contains(zChar)) {
                            if (!this.blockMap.containsKey(zChar)) {
                                errors.add(String.format("Found char '%s' at char %s in row %s in layer %s, but character was not found in map!", zChar, z + 1, y + 1, x + 1));
                            }
                            checkedChars.add(zChar);
                        }
                    }
                }
            }
            if (!errors.isEmpty()) {
                ModularUI.LOGGER.error("Error validating ArrayScheme BlockArray:");
                for (String e : errors) ModularUI.LOGGER.error("  - {}", e);
                throw new IllegalArgumentException("The ArraySchema builder was misconfigured. See message above.");
            }
        }

        public ArraySchema build() {
            validate();
            BlockInfo[][][] blocks = new BlockInfo[this.tensor.size()][this.tensor.get(0).length][this.tensor.get(0)[0].length()];
            for (int x = 0; x < this.tensor.size(); x++) {
                String[] xLayer = this.tensor.get(x);
                for (int y = 0; y < xLayer.length; y++) {
                    String yRow = xLayer[y];
                    for (int z = 0; z < yRow.length(); z++) {
                        char zChar = yRow.charAt(z);
                        BlockInfo info = this.blockMap.get(zChar);
                        if (info == null || info == BlockInfo.EMPTY) continue; // null -> any allowed -> don't need to check
                        blocks[x][y][z] = info;
                    }
                }
            }
            return new ArraySchema(blocks);
        }
    }
}
