package com.cleanroommc.modularui.factory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.EnumHand;

public class HandGuiData extends GuiData {

    private final EnumHand hand;

    public HandGuiData(EntityPlayer player, EnumHand hand) {
        super(player);
        this.hand = hand;
    }

    public EnumHand getHand() {
        return this.hand;
    }

    public ItemStack getUsedItemStack() {
        return getPlayer().getHeldItem(this.hand);
    }

    public void setItemInMainHand(ItemStack item) {
        getPlayer().setHeldItem(EnumHand.MAIN_HAND, item);
    }

    public void setItemInOffHand(ItemStack item) {
        getPlayer().setHeldItem(EnumHand.OFF_HAND, item);
    }

    public void setItemInUsedHand(ItemStack item) {
        getPlayer().setHeldItem(this.hand, item);
    }

}
