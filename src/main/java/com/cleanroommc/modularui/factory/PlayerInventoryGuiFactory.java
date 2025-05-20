package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.inventory.InventoryType;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;
import com.cleanroommc.modularui.utils.Platform;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PlayerInventoryGuiFactory extends AbstractUIFactory<PlayerInventoryGuiData> {

    public static final PlayerInventoryGuiFactory INSTANCE = new PlayerInventoryGuiFactory();

    public void openFromPlayerInventory(EntityPlayer player, int index) {
        GuiManager.open(
                this, new PlayerInventoryGuiData(player, InventoryTypes.PLAYER, index), verifyServerSide(player));
    }

    public void openFromHand(EntityPlayer player, EnumHand hand) {
        openFromPlayerInventory(player, hand == EnumHand.OFF_HAND ? 40 : player.inventory.currentItem);
    }

    public void openFromBaubles(EntityPlayer player, int index) {
        if (!ModularUI.isBaubleLoaded()) {
            throw new IllegalArgumentException("Can't open UI for baubles item when bauble is not loaded!");
        }
        GuiManager.open(
                this, new PlayerInventoryGuiData(player, InventoryTypes.BAUBLES, index), verifyServerSide(player));
    }

    public void open(EntityPlayer player, InventoryType type, int index) {
        GuiManager.open(this, new PlayerInventoryGuiData(player, type, index), verifyServerSide(player));
    }

    @SideOnly(Side.CLIENT)
    public void openFromPlayerInventoryClient(int index) {
        GuiManager.openFromClient(this, new PlayerInventoryGuiData(Platform.getClientPlayer(), InventoryTypes.PLAYER, index));
    }

    @SideOnly(Side.CLIENT)
    public void openFromHandClient(EnumHand hand) {
        openFromPlayerInventoryClient(hand == EnumHand.OFF_HAND ? 40 : Platform.getClientPlayer().inventory.currentItem);
    }

    @SideOnly(Side.CLIENT)
    public void openFromBaublesClient(int index) {
        if (!ModularUI.isBaubleLoaded()) {
            throw new IllegalArgumentException("Can't open UI for baubles item when bauble is not loaded!");
        }
        GuiManager.openFromClient(
                this, new PlayerInventoryGuiData(Platform.getClientPlayer(), InventoryTypes.BAUBLES, index));
    }

    @SideOnly(Side.CLIENT)
    public void openClient(InventoryType type, int index) {
        GuiManager.openFromClient(this, new PlayerInventoryGuiData(Platform.getClientPlayer(), type, index));
    }

    private PlayerInventoryGuiFactory() {
        super("mui:player_inv");
    }

    @Override
    public @NotNull IGuiHolder<PlayerInventoryGuiData> getGuiHolder(PlayerInventoryGuiData data) {
        return Objects.requireNonNull(castGuiHolder(data.getUsedItemStack().getItem()), "Item was not a gui holder!");
    }

    @Override
    public void writeGuiData(PlayerInventoryGuiData guiData, PacketBuffer buffer) {
        guiData.getInventoryType().write(buffer);
        buffer.writeVarInt(guiData.getSlotIndex());
    }

    @Override
    public @NotNull PlayerInventoryGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        return new PlayerInventoryGuiData(player, InventoryType.read(buffer), buffer.readVarInt());
    }
}
