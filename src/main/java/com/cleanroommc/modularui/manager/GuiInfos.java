package com.cleanroommc.modularui.manager;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.IItemGuiHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;

public class GuiInfos {

    public static final GuiInfo PLAYER_ITEM_MAIN_HAND;
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
                .clientGui(context -> {
                    ItemStack itemStack = context.getMainHandItem();
                    if (itemStack.getItem() instanceof IItemGuiHolder) {
                        return ((IItemGuiHolder) itemStack.getItem()).createGuiScreen(context.getPlayer(), itemStack);
                    }
                    throw new UnsupportedOperationException();
                })
                .serverGui((context, guiSyncHandler) -> {
                    ItemStack itemStack = context.getMainHandItem();
                    if (itemStack.getItem() instanceof IItemGuiHolder) {
                        ((IItemGuiHolder) itemStack.getItem()).buildSyncHandler(guiSyncHandler, context.getPlayer(), itemStack);
                        return;
                    }
                    throw new UnsupportedOperationException();
                })
                .build();

        TILE_ENTITY = GuiInfo.builder()
                .clientGui(context -> {
                    TileEntity tile = context.getTileEntity();
                    if (tile instanceof IGuiHolder) {
                        return ((IGuiHolder) tile).createClientGui(context.getPlayer());
                    }
                    throw new UnsupportedOperationException();
                })
                .serverGui((context, guiSyncHandler) -> {
                    TileEntity tile = context.getTileEntity();
                    if (tile instanceof IGuiHolder) {
                        ((IGuiHolder) tile).buildSyncHandler(guiSyncHandler, context.getPlayer());
                        return;
                    }
                    throw new UnsupportedOperationException();
                })
                .build();
    }
}
