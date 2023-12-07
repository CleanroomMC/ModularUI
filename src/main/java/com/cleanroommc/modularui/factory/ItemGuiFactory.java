package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ItemGuiFactory extends AbstractUIFactory<HandGuiData> {

    public static final ItemGuiFactory INSTANCE = new ItemGuiFactory();

    private ItemGuiFactory() {
        super("mui:item");
    }

    public static void open(EntityPlayerMP player, EnumHand hand) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(hand);
        HandGuiData guiData = new HandGuiData(player, hand);
        GuiManager.open(INSTANCE, guiData, player);
    }

    @Override
    public @NotNull IGuiHolder<HandGuiData> getGuiHolder(HandGuiData data) {
        ItemStack item = data.getUsedItemStack();
        if (isGuiHolder(item.getItem())) {
            return (IGuiHolder<HandGuiData>) item.getItem();
        }
        throw new IllegalStateException("Item was not a gui holder!");
    }

    @Override
    public ModularPanel createPanel(HandGuiData guiData, GuiSyncManager syncManager) {
        ItemStack item = guiData.getUsedItemStack();
        if (item.getItem() instanceof IGuiHolder) {
            return ((IGuiHolder<HandGuiData>) item.getItem()).buildUI(guiData, syncManager, guiData.isClient());
        }
        return null;
    }

    @Override
    public ModularScreen createScreen(HandGuiData guiData, ModularPanel mainPanel) {
        ItemStack item = guiData.getUsedItemStack();
        if (item.getItem() instanceof IGuiHolder) {
            return ((IGuiHolder<HandGuiData>) item.getItem()).createScreen(guiData, mainPanel);
        }
        return null;
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
