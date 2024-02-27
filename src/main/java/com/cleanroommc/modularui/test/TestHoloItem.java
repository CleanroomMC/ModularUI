package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.factory.HoloGuiFactory;
import com.cleanroommc.modularui.holoui.HoloUI;

import com.cleanroommc.modularui.screen.ModularScreen;

import com.cleanroommc.modularui.value.sync.GuiSyncManager;

import com.cleanroommc.modularui.widget.WidgetTree;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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
