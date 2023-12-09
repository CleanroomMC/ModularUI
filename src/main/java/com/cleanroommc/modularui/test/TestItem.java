package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.factory.ItemGuiFactory;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.ItemStackItemHandler;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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

public class TestItem extends Item implements IGuiHolder<HandGuiData> {

    public static final TestItem testItem = new TestItem();

    @Override
    public ModularPanel buildUI(HandGuiData guiData, GuiSyncManager guiSyncManager) {
        IItemHandlerModifiable itemHandler = (IItemHandlerModifiable) guiData.getUsedItemStack().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        guiSyncManager.registerSlotGroup("mixer_items", 2);

        ModularPanel panel = ModularPanel.defaultPanel("knapping_gui");
        panel.child(new Column()
                //.coverChildren()
                .padding(7)
                .child(SlotGroupWidget.playerInventory())
                .child(SlotGroupWidget.builder()
                        .row("II")
                        .row("II")
                        .key('I', index -> new ItemSlot().slot(SyncHandlers.phantomItemSlot(itemHandler, index)
                                .ignoreMaxStackSize(true)
                                .slotGroup("mixer_items")))
                        .build()));

        return panel;
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World world, @NotNull EntityPlayer player, @Nonnull EnumHand hand) {
        if (!world.isRemote) {
            ItemGuiFactory.open((EntityPlayerMP) player, hand);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(@NotNull ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new ItemStackItemHandler(stack, 4);
    }
}
