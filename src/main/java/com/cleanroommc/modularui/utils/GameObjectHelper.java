package com.cleanroommc.modularui.utils;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import com.google.common.base.Optional;

import net.minecraftforge.registries.ForgeRegistries;

import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;

public class GameObjectHelper {

    private static final String EQUALS = "=";

    public static BlockState getBlockState(String s) {
        String[] parts = s.split(":");
        String mod, path, state = null;
        if (parts.length == 1) throw new IllegalArgumentException();
        mod = parts[0];
        path = parts[1];
        if (parts.length > 2) state = parts[2];
        if (parts.length > 3) throw new IllegalArgumentException();
        return getBlockState(mod, path, state);
    }

    public static BlockState getBlockState(String mod, String path) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(mod, path));
        if (block == null) throw new NoSuchElementException();
        return block.defaultBlockState();
    }

    @SuppressWarnings("deprecation")
    /*public static BlockState getBlockState(String mod, String path, int meta) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(mod, path));
        if (block == null) throw new NoSuchElementException();
        return block.getStateFromMeta(meta);
    }*/

    public static BlockState getBlockState(String mod, String path, @Nullable String state) {
        if (state != null) {
            try {
                return getBlockState(mod, path, Integer.parseInt(state));
            } catch (NumberFormatException ignored) {
            }
        }
        Block block = getBlock(mod, path);
        IBlockState blockState = block.getDefaultState();
        return state == null ? blockState : parseBlockStates(blockState, state.split(","));
    }

    public static Block getBlock(String mod, String path) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(mod, path));
        if (block == null) throw new NoSuchElementException();
        return block;
    }

    @SuppressWarnings({"rawtypes", "Guava", "unchecked"})
    private static BlockState parseBlockStates(BlockState defaultState, String[] properties) {
        for (String propertyName : properties) {
            String[] prop = propertyName.split(EQUALS, 2);
            Property property = defaultState.getBlock().defaultBlockState().properties(prop[0]);
            if (property == null) {
                throw new IllegalArgumentException(String.format("Invalid property name '%s' for block '%s'", prop[0], defaultState.getBlock().getRegistryName()));
            }
            Optional<? extends Comparable> value = property.parseValue(prop[1]);
            if (value.isPresent()) {
                defaultState = defaultState.withProperty(property, value.get());
            } else {
                throw new IllegalArgumentException(String.format("Invalid property value '%s' for block '%s'", prop[1], defaultState.getBlock().getRegistryName()));
            }
        }

        return defaultState;
    }

    public static ItemStack getItemStack(String mod, String path) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(mod, path));
        if (item == null) throw new NoSuchElementException("Item '" + mod + ":" + path + "' was not found!");
        return new ItemStack(item, 1);
    }
}
