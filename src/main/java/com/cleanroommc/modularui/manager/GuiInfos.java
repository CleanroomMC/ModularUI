package com.cleanroommc.modularui.manager;

import com.cleanroommc.modularui.api.IGuiHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;

public class GuiInfos {

    public static final GuiInfo PLAYER_ITEM_MAIN_HAND;
    public static final GuiInfo PLAYER_ITEM_OFF_HAND;
    public static final GuiInfo TILE_ENTITY;

    public static GuiInfo getForHand(EnumHand hand) {
        if (hand == EnumHand.OFF_HAND) {
            return PLAYER_ITEM_OFF_HAND;
        }
        return PLAYER_ITEM_MAIN_HAND;
    }

    public static void init() {
    }

    static {
        PLAYER_ITEM_MAIN_HAND = GuiInfo.builder()
                .clientGui((context, panel) -> {
                    ItemStack itemStack = context.getMainHandItem();
                    if (itemStack.getItem() instanceof IGuiHolder) {
                        return ((IGuiHolder) itemStack.getItem()).createScreen(context.with(EnumHand.MAIN_HAND), panel);
                    }
                    throw new UnsupportedOperationException();
                })
                .commonGui((context, guiSyncHandler) -> {
                    ItemStack itemStack = context.getMainHandItem();
                    if (itemStack.getItem() instanceof IGuiHolder) {
                        return ((IGuiHolder) itemStack.getItem()).buildUI(context.with(EnumHand.MAIN_HAND), guiSyncHandler, context.getWorld().isRemote);
                    }
                    throw new UnsupportedOperationException();
                })
                .build();

        PLAYER_ITEM_OFF_HAND = GuiInfo.builder()
                .clientGui((context, panel) -> {
                    ItemStack itemStack = context.getOffHandItem();
                    if (itemStack.getItem() instanceof IGuiHolder) {
                        return ((IGuiHolder) itemStack.getItem()).createScreen(context.with(EnumHand.OFF_HAND), panel);
                    }
                    throw new UnsupportedOperationException();
                })
                .commonGui((context, guiSyncHandler) -> {
                    ItemStack itemStack = context.getOffHandItem();
                    if (itemStack.getItem() instanceof IGuiHolder) {
                        return ((IGuiHolder) itemStack.getItem()).buildUI(context.with(EnumHand.OFF_HAND), guiSyncHandler, context.getWorld().isRemote);
                    }
                    throw new UnsupportedOperationException();
                })
                .build();

        TILE_ENTITY = GuiInfo.builder()
                .clientGui((context, panel) -> {
                    TileEntity tile = context.getTileEntity();
                    if (tile instanceof IGuiHolder) {
                        return ((IGuiHolder) tile).createScreen(context, panel);
                    }
                    throw new UnsupportedOperationException();
                })
                .commonGui((context, guiSyncHandler) -> {
                    TileEntity tile = context.getTileEntity();
                    if (tile instanceof IGuiHolder) {
                        return ((IGuiHolder) tile).buildUI(context, guiSyncHandler, context.getWorld().isRemote);
                    }
                    throw new UnsupportedOperationException();
                })
                .build();
    }
}
