package com.cleanroommc.modularui.sync;

import com.cleanroommc.modularui.api.ValueSyncHandler;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.utils.FluidTankHandler;
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
    public FluidStack getCachedValue() {
        return cache;
    }

    @Override
    public void setValue(@Nullable FluidStack value) {
        this.cache = value;
        onValueChanged();
    }

    @Override
    public boolean needsSync(boolean isFirstSync) {
        if (isFirstSync) return true;
        FluidStack current = this.fluidTank.getFluid();
        if (current == cache) return false;
        if (current == null || cache == null) return true;
        return current.amount != cache.amount || !current.isFluidEqual(cache);
    }

    @Override
    public void updateAndWrite(PacketBuffer buffer) {
        this.cache = this.fluidTank.getFluid() != null ? this.fluidTank.getFluid().copy() : null;
        NetworkUtils.writeFluidStack(buffer, cache);
    }

    @Override
    public void read(PacketBuffer buffer) {
        setValue(NetworkUtils.readFluidStack(buffer));
    }

    @Override
    public void updateFromClient(FluidStack value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id == 0) {
            read(buf);
        } else if (id == 3) {
            this.controlsAmount = buf.readBoolean();
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        if (id == 0) {
            read(buf);
        } else if (id == 1) {
            if (this.phantom) {
                tryClickPhantom(buf.readVarInt(), buf.readBoolean());
            } else {
                tryClickContainer(buf.readVarInt(), buf.readBoolean());
            }
        } else if (id == 2) {
            if (this.phantom) {
                tryScrollPhantom(buf.readVarInt());
            }
        } else if (id == 3) {
            this.controlsAmount = buf.readBoolean();
        } else if (id == 4) {
            this.fluidTank.drain(Integer.MAX_VALUE, true);
            this.fluidTank.fill(NetworkUtils.readFluidStack(buf), true);
        }
    }

    private void tryClickContainer(int mouseButton, boolean isShiftKeyDown) {
        EntityPlayer player = getSyncHandler().getPlayer();
        ItemStack currentStack = player.inventory.getItemStack();
        if (!currentStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            return;
        }
        int maxAttempts = isShiftKeyDown ? currentStack.getCount() : 1;
        if (mouseButton == 0 && canFillSlot) {
            boolean performedTransfer = false;
            for (int i = 0; i < maxAttempts; i++) {
                FluidActionResult result = FluidUtil.tryEmptyContainer(currentStack, fluidHandler, Integer.MAX_VALUE, null, false);
                ItemStack remainingStack = result.getResult();
                if (!result.isSuccess() || (currentStack.getCount() > 1 && !remainingStack.isEmpty() && !player.inventory.addItemStackToInventory(remainingStack))) {
                    player.dropItem(remainingStack, true);
                    break; //do not continue if we can't add resulting container into inventory
                }

                remainingStack = FluidUtil.tryEmptyContainer(currentStack, fluidHandler, Integer.MAX_VALUE, null, true).result;
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
            FluidStack fluid = fluidTank.getFluid();
            if (performedTransfer && fluid != null) {
                playSound(fluid, false);
                getSyncHandler().setCursorItem(currentStack);
            }
            return;
        }
        FluidStack currentFluid = fluidTank.getFluid();
        if (mouseButton == 1 && canDrainSlot && currentFluid != null && currentFluid.amount > 0) {
            boolean performedTransfer = false;
            for (int i = 0; i < maxAttempts; i++) {
                FluidActionResult result = FluidUtil.tryFillContainer(currentStack, fluidHandler, Integer.MAX_VALUE, null, false);
                ItemStack remainingStack = result.getResult();
                if (!result.isSuccess() || (currentStack.getCount() > 1 && !remainingStack.isEmpty() && !player.inventory.addItemStackToInventory(remainingStack))) {
                    break; //do not continue if we can't add resulting container into inventory
                }

                remainingStack = FluidUtil.tryFillContainer(currentStack, fluidHandler, Integer.MAX_VALUE, null, true).result;
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
                getSyncHandler().setCursorItem(currentStack);
            }
        }
    }

    public void tryClickPhantom(int mouseButton, boolean isShiftKeyDown) {
        EntityPlayer player = getSyncHandler().getPlayer();
        ItemStack currentStack = player.inventory.getItemStack();
        FluidStack currentFluid = this.fluidTank.getFluid();
        IFluidHandlerItem fluidHandlerItem = currentStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);

        if (mouseButton == 0) {
            if (currentStack.isEmpty() || fluidHandlerItem == null) {
                if (canDrainSlot) {
                    this.fluidTank.drain(isShiftKeyDown ? Integer.MAX_VALUE : 1000, true);
                }
            } else {
                FluidStack cellFluid = fluidHandlerItem.drain(Integer.MAX_VALUE, false);
                if ((this.controlsAmount || currentFluid == null) && cellFluid != null) {
                    if (canFillSlot) {
                        if (!this.controlsAmount) {
                            cellFluid.amount = 1;
                        }
                        if (this.fluidTank.fill(cellFluid, true) > 0) {
                            this.lastStoredPhantomFluid = cellFluid.copy();
                        }
                    }
                } else {
                    if (canDrainSlot) {
                        fluidTank.drain(isShiftKeyDown ? Integer.MAX_VALUE : 1000, true);
                    }
                }
            }
        } else if (mouseButton == 1) {
            if (canFillSlot) {
                if (currentFluid != null) {
                    if (this.controlsAmount) {
                        FluidStack toFill = currentFluid.copy();
                        toFill.amount = 1000;
                        this.fluidTank.fill(toFill, true);
                    }
                } else if (lastStoredPhantomFluid != null) {
                    FluidStack toFill = this.lastStoredPhantomFluid.copy();
                    toFill.amount = this.controlsAmount ? 1000 : 1;
                    this.fluidTank.fill(toFill, true);
                }
            }
        } else if (mouseButton == 2 && currentFluid != null && canDrainSlot) {
            this.fluidTank.drain(isShiftKeyDown ? Integer.MAX_VALUE : 1000, true);
        }
    }

    public void tryScrollPhantom(int direction) {
        FluidStack currentFluid = this.fluidTank.getFluid();
        if (currentFluid == null) {
            if (direction > 0 && this.lastStoredPhantomFluid != null) {
                FluidStack toFill = this.lastStoredPhantomFluid.copy();
                toFill.amount = this.controlsAmount ? direction : 1;
                this.fluidTank.fill(toFill, true);
            }
            return;
        }
        if (direction > 0 && this.controlsAmount) {
            FluidStack toFill = currentFluid.copy();
            toFill.amount = direction;
            this.fluidTank.fill(toFill, true);
        } else if (direction < 0) {
            this.fluidTank.drain(-direction, true);
        }
    }

    private void playSound(FluidStack fluid, boolean fill) {
        EntityPlayer player = getSyncHandler().getPlayer();
        SoundEvent soundevent = fill ? fluid.getFluid().getFillSound(fluid) : fluid.getFluid().getEmptySound(fluid);
        player.world.playSound(null, player.posX, player.posY + 0.5, player.posZ, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    public IFluidTank getFluidTank() {
        return fluidTank;
    }

    public boolean canDrainSlot() {
        return canDrainSlot;
    }

    public boolean canFillSlot() {
        return canFillSlot;
    }

    public boolean controlsAmount() {
        return controlsAmount;
    }

    public boolean isPhantom() {
        return phantom;
    }

    public FluidSlotSyncHandler phantom(boolean phantom) {
        this.phantom = phantom;
        return this;
    }

    public FluidSlotSyncHandler controlsAmount(boolean controlsAmount) {
        this.controlsAmount = controlsAmount;
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
