package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.core.mixin.ContainerAccessor;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.value.sync.ModularSyncManager;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import com.cleanroommc.bogosorter.api.IPosSetter;
import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Interface(modid = ModularUI.BOGO_SORT, iface = "com.cleanroommc.bogosorter.api.ISortableContainer")
public class ModularContainer extends Container implements ISortableContainer {

    public static ModularContainer getCurrent(EntityPlayer player) {
        if (player.openContainer instanceof ModularContainer container) {
            return container;
        }
        return null;
    }

    private final EntityPlayer player;
    private final ModularSyncManager syncManager;
    private final GuiData guiData;
    private boolean init = true;
    private final List<ModularSlot> slots = new ArrayList<>();
    private final List<ModularSlot> shiftClickSlots = new ArrayList<>();
    private ContainerCustomizer containerCustomizer;

    @SideOnly(Side.CLIENT)
    private ModularScreen optionalScreen;

    public ModularContainer(EntityPlayer player, PanelSyncManager panelSyncManager, String mainPanelName, GuiData guiData) {
        this.player = player;
        this.syncManager = new ModularSyncManager(this);
        this.syncManager.construct(mainPanelName, panelSyncManager);
        this.containerCustomizer = panelSyncManager.getContainerCustomizer();
        if (this.containerCustomizer == null) {
            this.containerCustomizer = new ContainerCustomizer();
        }
        this.containerCustomizer.initialize(this);
        sortShiftClickSlots();
        this.guiData = guiData;
    }

    @SideOnly(Side.CLIENT)
    public ModularContainer(ContainerCustomizer containerCustomizer) {
        this.player = Minecraft.getMinecraft().player;
        this.syncManager = null;
        this.containerCustomizer = containerCustomizer != null ? containerCustomizer : new ContainerCustomizer();
        this.containerCustomizer.initialize(this);
        this.guiData = null;
    }

    @SideOnly(Side.CLIENT)
    void construct(ModularScreen screen) {
        this.optionalScreen = screen;
    }

    @SideOnly(Side.CLIENT)
    public ModularScreen getScreen() {
        if (this.optionalScreen == null) throw new NullPointerException("ModularScreen is not yet initialised!");
        return optionalScreen;
    }

    public ContainerAccessor acc() {
        return (ContainerAccessor) this;
    }

    @Override
    public void onContainerClosed(@NotNull EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        if (this.syncManager != null) {
            this.syncManager.onClose();
        }
        this.containerCustomizer.onContainerClosed();
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (this.syncManager != null) {
            this.syncManager.detectAndSendChanges(this.init);
        }
        this.init = false;
    }

    private void sortShiftClickSlots() {
        this.shiftClickSlots.sort(Comparator.comparingInt(slot -> Objects.requireNonNull(slot.getSlotGroup()).getShiftClickPriority()));
    }

    @Override
    public void setAll(@NotNull List<ItemStack> items) {
        if (this.inventorySlots.size() != items.size()) {
            ModularUI.LOGGER.error("Here are {} slots, but expected {}", this.inventorySlots.size(), items.size());
        }
        for (int i = 0; i < Math.min(this.inventorySlots.size(), items.size()); ++i) {
            this.getSlot(i).putStack(items.get(i));
        }
    }

    @ApiStatus.Internal
    public void registerSlot(String panelName, ModularSlot slot) {
        if (this.inventorySlots.contains(slot)) {
            throw new IllegalArgumentException("Tried to register slot which already exists!");
        }
        addSlotToContainer(slot);
        this.slots.add(slot);
        if (slot.getSlotGroupName() != null) {
            SlotGroup slotGroup = getSyncManager().getSlotGroup(panelName, slot.getSlotGroupName());
            if (slotGroup == null) {
                ModularUI.LOGGER.throwing(new IllegalArgumentException("SlotGroup '" + slot.getSlotGroupName() + "' is not registered!"));
                return;
            }
            slot.slotGroup(slotGroup);
        }
        if (slot.getSlotGroup() != null) {
            SlotGroup slotGroup = slot.getSlotGroup();
            if (slotGroup.allowShiftTransfer()) {
                this.shiftClickSlots.add(slot);
                if (!this.init) {
                    sortShiftClickSlots();
                }
            }
        }
    }

    @Contract("_, null, null -> fail")
    @NotNull
    @ApiStatus.Internal
    public SlotGroup validateSlotGroup(String panelName, @Nullable String slotGroupName, @Nullable SlotGroup slotGroup) {
        if (slotGroup != null) {
            if (getSyncManager().getSlotGroup(panelName, slotGroup.getName()) == null) {
                throw new IllegalArgumentException("Slot group is not registered in the GUI.");
            }
            return slotGroup;
        }
        if (slotGroupName != null) {
            slotGroup = getSyncManager().getSlotGroup(panelName, slotGroupName);
            if (slotGroup == null) {
                throw new IllegalArgumentException("Can't find slot group for name " + slotGroupName);
            }
            return slotGroup;
        }
        throw new IllegalArgumentException("Either the slot group or the name must not be null!");
    }

    public ModularSyncManager getSyncManager() {
        if (this.syncManager == null) {
            throw new IllegalStateException("GuiSyncManager is not available for client only GUI's.");
        }
        return this.syncManager;
    }

    public GuiData getGuiData() {
        if (this.guiData == null) {
            throw new IllegalStateException("GuiData is not available for client only GUI's.");
        }
        return this.guiData;
    }

    public boolean isClient() {
        return this.syncManager == null || NetworkUtils.isClient(this.player);
    }

    public boolean isClientOnly() {
        return this.syncManager == null;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public ModularSlot getModularSlot(int index) {
        return this.slots.get(index);
    }

    public List<ModularSlot> getShiftClickSlots() {
        return Collections.unmodifiableList(this.shiftClickSlots);
    }

    @Override
    public boolean canInteractWith(@NotNull EntityPlayer playerIn) {
        return guiData.canInteractWith(playerIn);
    }

    @Override
    public @NotNull ItemStack slotClick(int slotId, int mouseButton, @NotNull ClickType clickTypeIn, @NotNull EntityPlayer player) {
        return this.containerCustomizer.slotClick(slotId, mouseButton, clickTypeIn, player);
    }

    public @NotNull ItemStack superSlotClick(int slotId, int mouseButton, @NotNull ClickType clickTypeIn, @NotNull EntityPlayer player) {
        return super.slotClick(slotId, mouseButton, clickTypeIn, player);
    }

    @Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer playerIn, int index) {
        return this.containerCustomizer.transferStackInSlot(playerIn, index);
    }

    public @NotNull ItemStack superTransferStackInSlot(@NotNull EntityPlayer playerIn, int index) {
        return this.containerCustomizer.transferStackInSlot(playerIn, index);
    }

    @Override
    public boolean canMergeSlot(@NotNull ItemStack stack, @NotNull Slot slotIn) {
        return this.containerCustomizer.canMergeSlot(stack, slotIn);
    }

    public boolean superCanMergeSlot(@NotNull ItemStack stack, @NotNull Slot slotIn) {
        return super.canMergeSlot(stack, slotIn);
    }

    @Override
    protected boolean mergeItemStack(@NotNull ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        return this.containerCustomizer.mergeItemStack(stack, startIndex, endIndex, reverseDirection);
    }

    public boolean superMergeItemStack(@NotNull ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        return super.mergeItemStack(stack, startIndex, endIndex, reverseDirection);
    }

    @Override
    protected void clearContainer(@NotNull EntityPlayer playerIn, @NotNull World worldIn, @NotNull IInventory inventoryIn) {
        this.containerCustomizer.clearContainer(playerIn, worldIn, inventoryIn);
    }

    public void superClearContainer(@NotNull EntityPlayer playerIn, @NotNull World worldIn, @NotNull IInventory inventoryIn) {
        super.clearContainer(playerIn, worldIn, inventoryIn);
    }

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        if (this.syncManager != null) {
            this.syncManager.buildSortingContext(builder);
        }
    }

    @Override
    public IPosSetter getPlayerButtonPosSetter() {
        return null;
    }
}
