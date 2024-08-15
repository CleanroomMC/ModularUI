package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.IGuiHolder;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ItemGuiFactory extends AbstractUIFactory<HandGuiData> {

    public static final ItemGuiFactory INSTANCE = new ItemGuiFactory();

    private ItemGuiFactory() {
        super("mui:item");
    }

    public static void open(ServerPlayer player, InteractionHand hand) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(hand);
        HandGuiData guiData = new HandGuiData(player, hand);
        GuiManager.open(INSTANCE, guiData, player);
    }

    @Override
    public @NotNull IGuiHolder<HandGuiData> getGuiHolder(HandGuiData data) {
        return Objects.requireNonNull(castGuiHolder(data.getUsedItemStack().getItem()), "Item was not a gui holder!");
    }

    @Override
    public void writeGuiData(HandGuiData guiData, FriendlyByteBuf buffer) {
        buffer.writeByte(guiData.getHand().ordinal());
    }

    @Override
    public @NotNull HandGuiData readGuiData(Player player, FriendlyByteBuf buffer) {
        return new HandGuiData(player, InteractionHand.values()[buffer.readByte()]);
    }
}
