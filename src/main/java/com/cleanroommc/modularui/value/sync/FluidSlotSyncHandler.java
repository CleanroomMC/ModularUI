package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.utils.FluidTankHandler;
import com.cleanroommc.modularui.utils.MouseData;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import org.jetbrains.annotations.Nullable;

public class FluidSlotSyncHandler extends ValueSyncHandler<FluidStack> {

    public static boolean isFluidEmpty(@Nullable FluidStack fluidStack) {
        return fluidStack == null || fluidStack.amount <= 0;
    }

    @Nullable
    public static FluidStack copyFluid(@Nullable FluidStack fluidStack) {
        return isFluidEmpty(fluidStack) ? null : fluidStack.copy();
    }

    public static final int SYNC_FLUID = 0;
    public static final int SYNC_CLICK = 1;
    public static final int SYNC_SCROLL = 2;
    public static final int SYNC_CONTROLS_AMOUNT = 3;

    @Nullable
    private FluidStack cache;
    private final IFluidTank fluidTank;
    private final IFluidHandler fluidHandler;
    private boolean canFillSlot = true, canDrainSlot = true, controlsAmount = true, phantom = false;
    @Nullable
    private FluidStack lastStoredPhantomFluid;

    public FluidSlotSyncHandler(IFluidTank fluidTank) {
        this.fluidTank = fluidTank;
        this.fluidHandler = FluidTankHandler.getTankFluidHandler(fluidTank);
    }

    @Nullable
    @Override
    public FluidStack getValue() {
        return this.cache;
    }

    @Override
    public void setValue(@Nullable FluidStack value, boolean setSource, boolean sync) {
        this.cache = copyFluid(value);
        if (setSource) {
            this.fluidTank.drain(Integer.MAX_VALUE, true);
            if (!isFluidEmpty(value)) {
                this.fluidTank.fill(value.copy(), true);
            }
        }
        if (sync) {
            if (NetworkUtils.isClient()) {
                syncToServer(SYNC_FLUID, this::write);
            } else {
                syncToClient(SYNC_FLUID, this::write);
            }
        }
        onValueChanged();
    }

    public boolean needsSync() {
        FluidStack current = this.fluidTank.getFluid();
        if (current == this.cache) return false;
        if (current == null || this.cache == null) return true;
        return current.amount != this.cache.amount || !current.isFluidEqual(this.cache);
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || needsSync()) {
            setValue(this.fluidTank.getFluid(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void write(PacketBuffer buffer) {
        NetworkUtils.writeFluidStack(buffer, this.cache);
    }

    @Override
    public void read(PacketBuffer buffer) {
        setValue(NetworkUtils.readFluidStack(buffer), true, false);
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id == SYNC_FLUID) {
            read(buf);
        } else if (id == SYNC_CONTROLS_AMOUNT) {
            this.controlsAmount = buf.readBoolean();
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        if (id == SYNC_FLUID) {
            if (this.phantom) {
                read(buf);
            }
        } else if (id == SYNC_CLICK) {
            if (this.phantom) {
                tryClickPhantom(MouseData.readPacket(buf));
            } else {
                tryClickContainer(MouseData.readPacket(buf));
            }
        } else if (id == SYNC_SCROLL) {
            if (this.phantom) {
                tryScrollPhantom(MouseData.readPacket(buf));
            }
        } else if (id == SYNC_CONTROLS_AMOUNT) {
            this.controlsAmount = buf.readBoolean();
        }
    }

    private void tryClickContainer(MouseData mouseData) {
        EntityPlayer player = getSyncManager().getPlayer();
        ItemStack currentStack = player.inventory.getItemStack();
        if (!currentStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            return;
        }
        int maxAttempts = mouseData.shift ? currentStack.getCount() : 1;
        if (mouseData.mouseButton == 0 && this.canFillSlot) {
            boolean performedTransfer = false;
            for (int i = 0; i < maxAttempts; i++) {
                FluidActionResult result = FluidUtil.tryEmptyContainer(currentStack, this.fluidHandler, Integer.MAX_VALUE, null, false);
                ItemStack remainingStack = result.getResult();
                if (!result.isSuccess() || (currentStack.getCount() > 1 && !remainingStack.isEmpty() && !player.inventory.addItemStackToInventory(remainingStack))) {
                    player.dropItem(remainingStack, true);
                    break; //do not continue if we can't add resulting container into inventory
                }

                remainingStack = FluidUtil.tryEmptyContainer(currentStack, this.fluidHandler, Integer.MAX_VALUE, null, true).result;
                if (currentStack.getCount() == 1) {
                    currentStack = remainingStack;
                } else {
                    currentStack.shrink(1);
                }
                performedTransfer = true;
                if (currentStack.isEmpty()) {
                    break;
                }
            }
            FluidStack fluid = this.fluidTank.getFluid();
            if (performedTransfer && fluid != null) {
                playSound(fluid, false);
                getSyncManager().setCursorItem(currentStack);
            }
            return;
        }
        FluidStack currentFluid = this.fluidTank.getFluid();
        if (mouseData.mouseButton == 1 && this.canDrainSlot && currentFluid != null && currentFluid.amount > 0) {
            boolean performedTransfer = false;
            for (int i = 0; i < maxAttempts; i++) {
                FluidActionResult result = FluidUtil.tryFillContainer(currentStack, this.fluidHandler, Integer.MAX_VALUE, null, false);
                ItemStack remainingStack = result.getResult();
                if (!result.isSuccess() || (currentStack.getCount() > 1 && !remainingStack.isEmpty() && !player.inventory.addItemStackToInventory(remainingStack))) {
                    break; //do not continue if we can't add resulting container into inventory
                }

                remainingStack = FluidUtil.tryFillContainer(currentStack, this.fluidHandler, Integer.MAX_VALUE, null, true).result;
                if (currentStack.getCount() == 1) {
                    currentStack = remainingStack;
                } else {
                    currentStack.shrink(1);
                }
                performedTransfer = true;
                if (currentStack.isEmpty()) {
                    break;
                }
            }
            if (performedTransfer) {
                playSound(currentFluid, true);
                getSyncManager().setCursorItem(currentStack);
            }
        }
    }

    public void tryClickPhantom(MouseData mouseData) {
        EntityPlayer player = getSyncManager().getPlayer();
        ItemStack currentStack = player.inventory.getItemStack();
        FluidStack currentFluid = this.fluidTank.getFluid();
        IFluidHandlerItem fluidHandlerItem = currentStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);

        if (mouseData.mouseButton == 0) {
            if (currentStack.isEmpty() || fluidHandlerItem == null) {
                if (this.canDrainSlot) {
                    this.fluidTank.drain(mouseData.shift ? Integer.MAX_VALUE : 1000, true);
                }
            } else {
                FluidStack cellFluid = fluidHandlerItem.drain(Integer.MAX_VALUE, false);
                if ((this.controlsAmount || currentFluid == null) && cellFluid != null) {
                    if (this.canFillSlot) {
                        if (!this.controlsAmount) {
                            cellFluid.amount = 1;
                        }
                        if (this.fluidTank.fill(cellFluid, true) > 0) {
                            this.lastStoredPhantomFluid = cellFluid.copy();
                        }
                    }
                } else {
                    if (this.canDrainSlot) {
                        this.fluidTank.drain(mouseData.shift ? Integer.MAX_VALUE : 1000, true);
                    }
                }
            }
        } else if (mouseData.mouseButton == 1) {
            if (this.canFillSlot) {
                if (currentFluid != null) {
                    if (this.controlsAmount) {
                        FluidStack toFill = currentFluid.copy();
                        toFill.amount = 1000;
                        this.fluidTank.fill(toFill, true);
                    }
                } else if (this.lastStoredPhantomFluid != null) {
                    FluidStack toFill = this.lastStoredPhantomFluid.copy();
                    toFill.amount = this.controlsAmount ? 1000 : 1;
                    this.fluidTank.fill(toFill, true);
                }
            }
        } else if (mouseData.mouseButton == 2 && currentFluid != null && this.canDrainSlot) {
            this.fluidTank.drain(mouseData.shift ? Integer.MAX_VALUE : 1000, true);
        }
    }

    public void tryScrollPhantom(MouseData mouseData) {
        FluidStack currentFluid = this.fluidTank.getFluid();
        int amount = mouseData.mouseButton;
        if (mouseData.shift) {
            amount *= 10;
        }
        if (mouseData.ctrl) {
            amount *= 100;
        }
        if (mouseData.alt) {
            amount *= 1000;
        }
        if (currentFluid == null) {
            if (amount > 0 && this.lastStoredPhantomFluid != null) {
                FluidStack toFill = this.lastStoredPhantomFluid.copy();
                toFill.amount = this.controlsAmount ? amount : 1;
                this.fluidTank.fill(toFill, true);
            }
            return;
        }
        if (amount > 0 && this.controlsAmount) {
            FluidStack toFill = currentFluid.copy();
            toFill.amount = amount;
            this.fluidTank.fill(toFill, true);
        } else if (amount < 0) {
            this.fluidTank.drain(-amount, true);
        }
    }

    private void playSound(FluidStack fluid, boolean fill) {
        EntityPlayer player = getSyncManager().getPlayer();
        SoundEvent soundevent = fill ? fluid.getFluid().getFillSound(fluid) : fluid.getFluid().getEmptySound(fluid);
        player.world.playSound(null, player.posX, player.posY + 0.5, player.posZ, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    public IFluidTank getFluidTank() {
        return this.fluidTank;
    }

    public boolean canDrainSlot() {
        return this.canDrainSlot;
    }

    public boolean canFillSlot() {
        return this.canFillSlot;
    }

    public boolean controlsAmount() {
        return this.controlsAmount;
    }

    public boolean isPhantom() {
        return this.phantom;
    }

    public FluidSlotSyncHandler phantom(boolean phantom) {
        this.phantom = phantom;
        return this;
    }

    public FluidSlotSyncHandler controlsAmount(boolean controlsAmount) {
        this.controlsAmount = controlsAmount;
        if (isValid()) {
            sync(SYNC_CONTROLS_AMOUNT, buffer -> buffer.writeBoolean(controlsAmount));
        }
        return this;
    }

    public FluidSlotSyncHandler canDrainSlot(boolean canDrainSlot) {
        this.canDrainSlot = canDrainSlot;
        return this;
    }

    public FluidSlotSyncHandler canFillSlot(boolean canFillSlot) {
        this.canFillSlot = canFillSlot;
        return this;
    }
}
