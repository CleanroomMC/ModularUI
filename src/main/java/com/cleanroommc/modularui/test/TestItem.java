package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.IItemGuiHolder;
import com.cleanroommc.modularui.manager.GuiInfos;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import com.cleanroommc.modularui.sync.SyncHandlers;
import com.cleanroommc.modularui.utils.ItemStackItemHandler;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class TestItem extends Item implements IItemGuiHolder {

    public static final TestItem testItem = new TestItem();

    @Override
    public void buildSyncHandler(GuiSyncHandler guiSyncHandler, EntityPlayer entityPlayer, ItemStack itemStack) {
        IItemHandlerModifiable itemHandler = (IItemHandlerModifiable) itemStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        guiSyncHandler.registerSlotGroup("mixer_items", 2);
        for (int i = 0; i < 4; i++) {
            ModularUI.LOGGER.info("Create item sync handler {}", i);
            guiSyncHandler.syncValue("mixer_items", i, SyncHandlers.itemSlot(itemHandler, i).slotGroup("mixer_items"));
        }
    }

    @Override
    public ModularScreen createGuiScreen(EntityPlayer entityPlayer, ItemStack itemStack) {
        return ModularScreen.simple("knapping_gui", this::createPanel);
    }

    public ModularPanel createPanel(GuiContext context) {
        ModularPanel panel = ModularPanel.defaultPanel(context);

        panel.child(new Column()
                //.coverChildren()
                .padding(7)
                .child(SlotGroupWidget.playerInventory())
                .child(SlotGroupWidget.builder()
                        .row("II")
                        .row("II")
                        .key('I', index -> {
                            ModularUI.LOGGER.info("Create item slot {}", index);
                            return new ItemSlot().setSynced("mixer_items", index);
                        })
                        .build()));

        return panel;
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World world, @NotNull EntityPlayer player, @Nonnull EnumHand hand) {
        if (!world.isRemote) {
            GuiInfos.getForHand(hand).open(player);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(@NotNull ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new ItemStackItemHandler(stack, 4);
    }
}
