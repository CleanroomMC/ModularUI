package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.factory.HoloGuiFactory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

public class TestHoloItem extends TestItem {

    public static final TestHoloItem testHoloItem = new TestHoloItem();
    @NotNull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, @NotNull EntityPlayer player, @NotNull EnumHand hand) {
        if (!world.isRemote) {
            HoloGuiFactory.open((EntityPlayerMP) player, hand);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }
}
