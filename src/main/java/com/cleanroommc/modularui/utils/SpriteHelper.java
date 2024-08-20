package com.cleanroommc.modularui.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpriteHelper {

    public static TextureAtlasSprite getSpriteOfBlockState(BlockState blockState, Direction facing) {
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(blockState);
        return getBestTexture(model, blockState, facing);
    }

    public static List<BakedQuad> getQuadsOfBlockState(BlockState blockState, Direction facing) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(blockState).getQuads(blockState, facing, RandomSource.create());
    }

    public static TextureAtlasSprite getBestTexture(BakedModel model, @Nullable BlockState blockState, @Nullable Direction facing) {
        List<BakedQuad> quads = model.getQuads(blockState, facing, RandomSource.create());
        return quads.isEmpty() ? Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(MissingTextureAtlasSprite.getLocation()) :
                quads.get(0).getSprite();
    }

    public static TextureAtlasSprite getSpriteOfItem(ItemStack item) {
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(item, null, null, 0);
        return getBestTexture(model, null, null);
    }

    public static List<BakedQuad> getQuadsOfItem(ItemStack item) {
        return Minecraft.getInstance().getItemRenderer().getModel(item, null, null, 0).getQuads(null, null, RandomSource.create());
    }
}
