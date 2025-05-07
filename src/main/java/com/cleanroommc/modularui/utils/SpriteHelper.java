package com.cleanroommc.modularui.utils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpriteHelper {

    public static TextureAtlasSprite getSpriteOfBlockState(IBlockState blockState, EnumFacing facing) {
        IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(blockState);
        return getBestTexture(model, blockState, facing);
    }

    public static List<BakedQuad> getQuadsOfBlockState(IBlockState blockState, EnumFacing facing) {
        return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(blockState).getQuads(blockState, facing, 0);
    }

    public static TextureAtlasSprite getBestTexture(IBakedModel model, @Nullable IBlockState blockState, @Nullable EnumFacing facing) {
        List<BakedQuad> quads = model.getQuads(blockState, facing, 0);
        return quads.isEmpty() ? Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite() : quads.get(0).getSprite();
    }

    public static TextureAtlasSprite getSpriteOfItem(ItemStack item) {
        IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(item, null, null);
        return getBestTexture(model, null, null);
    }

    public static List<BakedQuad> getQuadsOfItem(ItemStack item) {
        return Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(item, null, null).getQuads(null, null, 0);
    }

    public static TextureAtlasSprite getDestroyBlockSprite(int stage) {
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/destroy_stage_" + stage);
    }
}
