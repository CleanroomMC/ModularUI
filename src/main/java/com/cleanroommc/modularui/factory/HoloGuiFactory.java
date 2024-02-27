package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.IGuiHolder;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;

import net.minecraft.util.EnumHand;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class HoloGuiFactory extends AbstractUIFactory<HandGuiData>{

    public static final HoloGuiFactory INSTANCE = new HoloGuiFactory();

    protected HoloGuiFactory() {
        super("mui:holo");
    }

    public static void open(EntityPlayerMP player, EnumHand hand) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(hand);
        HandGuiData guiData = new HandGuiData(player, hand);
        HoloGuiManager.open(INSTANCE, guiData, player);
    }
    @Override
    public @NotNull IGuiHolder<HandGuiData> getGuiHolder(HandGuiData data) {
        return Objects.requireNonNull(castGuiHolder(data.getUsedItemStack().getItem()), "Item was not a gui holder!");
    }

    @Override
    public void writeGuiData(HandGuiData guiData, PacketBuffer buffer) {
        buffer.writeByte(guiData.getHand().ordinal());
    }

    @Override
    public @NotNull HandGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        return new HandGuiData(player, EnumHand.values()[buffer.readByte()]);
    }
}
