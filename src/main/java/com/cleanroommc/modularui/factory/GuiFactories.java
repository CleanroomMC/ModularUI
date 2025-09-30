package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.IGuiHolder;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

public class GuiFactories {

    public static TileEntityGuiFactory tileEntity() {
        return TileEntityGuiFactory.INSTANCE;
    }

    public static SidedTileEntityGuiFactory sidedTileEntity() {
        return SidedTileEntityGuiFactory.INSTANCE;
    }

    public static EntityGuiFactory entity() {
        return EntityGuiFactory.INSTANCE;
    }

    @Deprecated
    public static ItemGuiFactory item() {
        return ItemGuiFactory.INSTANCE;
    }

    public static PlayerInventoryGuiFactory playerInventory() {
        return PlayerInventoryGuiFactory.INSTANCE;
    }

    public static SimpleGuiFactory createSimple(String name, IGuiHolder<GuiData> holder) {
        return new SimpleGuiFactory(name, holder);
    }

    public static SimpleGuiFactory createSimple(String name, Supplier<IGuiHolder<GuiData>> holder) {
        return new SimpleGuiFactory(name, holder);
    }

    @ApiStatus.Internal
    public static void init() {
        GuiManager.registerFactory(tileEntity());
        GuiManager.registerFactory(sidedTileEntity());
        GuiManager.registerFactory(entity());
        GuiManager.registerFactory(item());
        GuiManager.registerFactory(playerInventory());
    }

    private GuiFactories() {}
}
