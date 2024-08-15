package com.cleanroommc.modularui.factory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.EnumHand;

public class HandGuiData extends GuiData {

    private final InteractionHand hand;

    public HandGuiData(Player player, InteractionHand hand) {
        super(player);
        this.hand = hand;
    }

    public InteractionHand getHand() {
        return this.hand;
    }

    public ItemStack getUsedItemStack() {
        return getPlayer().getItemInHand(this.hand);
    }

    public void setItemInMainHand(ItemStack item) {
        getPlayer().setItemInHand(InteractionHand.MAIN_HAND, item);
    }

    public void setItemInOffHand(ItemStack item) {
        getPlayer().setItemInHand(InteractionHand.OFF_HAND, item);
    }

    public void setItemInUsedHand(ItemStack item) {
        getPlayer().setItemInHand(this.hand, item);
    }

}
