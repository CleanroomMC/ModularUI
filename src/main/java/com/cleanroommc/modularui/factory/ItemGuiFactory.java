package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.IGuiHolder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ItemGuiFactory extends AbstractUIFactory<GuiData> {

    public static final ItemGuiFactory INSTANCE = new ItemGuiFactory();

    private ItemGuiFactory() {
        super("mui:item");
    }

    public static void open(EntityPlayerMP player) {
        Objects.requireNonNull(player);
        GuiData guiData = new GuiData(player);
        GuiManager.open(INSTANCE, guiData, player);
    }

    @Override
    public @NotNull IGuiHolder<GuiData> getGuiHolder(GuiData data) {
        return Objects.requireNonNull(castGuiHolder(data.getMainHandItem().getItem()), "Item was not a gui holder!");
    }

    @Override
    public void writeGuiData(GuiData guiData, PacketBuffer buffer) {
    }

    @Override
    public @NotNull GuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        return new GuiData(player);
    }
}
