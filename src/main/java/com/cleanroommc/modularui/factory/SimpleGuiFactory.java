package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.IGuiHolder;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class SimpleGuiFactory extends AbstractUIFactory<GuiData> {

    private final Supplier<IGuiHolder<GuiData>> guiHolderSupplier;

    public SimpleGuiFactory(String name, Supplier<IGuiHolder<GuiData>> guiHolderSupplier) {
        super(name);
        this.guiHolderSupplier = guiHolderSupplier;
        GuiManager.registerFactory(this);
    }

    public void init() {
    }

    public void open(ServerPlayer player) {
        GuiManager.open(this, new GuiData(player), player);
    }


    @Override
    public void writeGuiData(GuiData guiData, FriendlyByteBuf buffer) {
    }

    @Override
    public @NotNull GuiData readGuiData(Player player, FriendlyByteBuf buffer) {
        return new GuiData(player);
    }

    @Override
    public @NotNull IGuiHolder<GuiData> getGuiHolder(GuiData data) {
        return this.guiHolderSupplier.get();
    }
}
