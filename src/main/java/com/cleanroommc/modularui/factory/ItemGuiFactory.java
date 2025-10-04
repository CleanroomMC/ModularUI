package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.IGuiHolder;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @deprecated use {@link PlayerInventoryGuiFactory}
 */
@Deprecated
public class ItemGuiFactory extends AbstractUIFactory<HandGuiData> {

    public static final ItemGuiFactory INSTANCE = new ItemGuiFactory();

    private ItemGuiFactory() {
        super("mui:item");
    }

    public void open(EntityPlayer player, EnumHand hand) {
        if (player instanceof EntityPlayerMP entityPlayerMP) {
            open(entityPlayerMP, hand);
            return;
        }
        throw new IllegalStateException("Synced GUIs must be opened from server side");
    }

    public void open(EntityPlayerMP player, EnumHand hand) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(hand);
        HandGuiData guiData = new HandGuiData(player, hand);
        GuiManager.open(this, guiData, player);
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
