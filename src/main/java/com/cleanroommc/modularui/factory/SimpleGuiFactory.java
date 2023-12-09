package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.IGuiHolder;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;

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

    public void open(EntityPlayerMP player) {
        GuiManager.open(this, new GuiData(player), player);
    }


    @Override
    public void writeGuiData(GuiData guiData, PacketBuffer buffer) {
    }

    @Override
    public @NotNull GuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        return new GuiData(player);
    }

    @Override
    public @NotNull IGuiHolder<GuiData> getGuiHolder(GuiData data) {
        return this.guiHolderSupplier.get();
    }
}
