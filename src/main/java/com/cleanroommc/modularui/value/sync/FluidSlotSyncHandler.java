package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.utils.FluidTankHandler;
import com.cleanroommc.modularui.utils.MouseData;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import org.jetbrains.annotations.Nullable;

public class FluidSlotSyncHandler extends ValueSyncHandler<FluidStack> {

    public static boolean isFluidEmpty(@Nullable FluidStack fluidStack) {
        return fluidStack == null || fluidStack.getAmount() <= 0;
    }

    @Nullable
    public static FluidStack copyFluid(@Nullable FluidStack fluidStack) {
        return isFluidEmpty(fluidStack) ? null : fluidStack.copy();
    }

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
        if (setSource && !NetworkUtils.isClient()) {
            this.fluidTank.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
            if (!isFluidEmpty(value)) {
                this.fluidTank.fill(value.copy(), IFluidHandler.FluidAction.EXECUTE);
            }
        }
        if (sync) {
            if (NetworkUtils.isClient()) {
                syncToServer(0, this::write);
            } else {
                syncToClient(0, this::write);
            }
        }
        onValueChanged();
    }

    public boolean needsSync() {
        FluidStack current = this.fluidTank.getFluid();
        if (current == this.cache) return false;
        if (current == null || this.cache == null) return true;
        return current.getAmount() != this.cache.getAmount() || !current.isFluidEqual(this.cache);
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
    public void write(FriendlyByteBuf buffer) {
        NetworkUtils.writeFluidStack(buffer, this.cache);
    }

    @Override
    public void read(FriendlyByteBuf buffer) {
        setValue(NetworkUtils.readFluidStack(buffer), true, false);
    }

    @Override
    public void readOnClient(int id, FriendlyByteBuf buf) {
        if (id == 0) {
            read(buf);
        } else if (id == 3) {
            this.controlsAmount = buf.readBoolean();
        }
    }

    @Override
    public void readOnServer(int id, FriendlyByteBuf buf) {
        if (id == 0) {
            if (this.phantom) {
                read(buf);
            }
        } else if (id == 1) {
            if (this.phantom) {
                tryClickPhantom(MouseData.readPacket(buf));
            } else {
                tryClickContainer(MouseData.readPacket(buf));
            }
        } else if (id == 2) {
            if (this.phantom) {
                tryScrollPhantom(MouseData.readPacket(buf));
            }
        } else if (id == 3) {
            this.controlsAmount = buf.readBoolean();
        }
    }

    private void tryClickContainer(MouseData mouseData) {
        Player player = getSyncManager().getPlayer();
        ItemStack currentStack = player.getInventory().getSelected();
        if (!currentStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null).isPresent()) {
            return;
        }
        int maxAttempts = mouseData.shift ? currentStack.getCount() : 1;
        if (mouseData.mouseButton == 0 && this.canFillSlot) {
            boolean performedTransfer = false;
            for (int i = 0; i < maxAttempts; i++) {
                FluidActionResult result = FluidUtil.tryEmptyContainer(currentStack, this.fluidHandler, Integer.MAX_VALUE, null, false);
                ItemStack remainingStack = result.getResult();
                if (!result.isSuccess() || (currentStack.getCount() > 1 && !remainingStack.isEmpty() && !player.getInventory().add(remainingStack))) {
                    player.drop(remainingStack, false);
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
        if (mouseData.mouseButton == 1 && this.canDrainSlot && currentFluid != null && currentFluid.getAmount() > 0) {
            boolean performedTransfer = false;
            for (int i = 0; i < maxAttempts; i++) {
                FluidActionResult result = FluidUtil.tryFillContainer(currentStack, this.fluidHandler, Integer.MAX_VALUE, null, false);
                ItemStack remainingStack = result.getResult();
                if (!result.isSuccess() || (currentStack.getCount() > 1 && !remainingStack.isEmpty() && !player.getInventory().add(remainingStack))) {
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
        Player player = getSyncManager().getPlayer();
        ItemStack currentStack = player.getInventory().getSelected();
        FluidStack currentFluid = this.fluidTank.getFluid();
        IFluidHandlerItem fluidHandlerItem = currentStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null).orElse(null);

        if (mouseData.mouseButton == 0) {
            if (currentStack.isEmpty() || fluidHandlerItem == null) {
                if (this.canDrainSlot) {
                    this.fluidTank.drain(mouseData.shift ? Integer.MAX_VALUE : 1000, IFluidHandler.FluidAction.EXECUTE);
                }
            } else {
                FluidStack cellFluid = fluidHandlerItem.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
                if ((this.controlsAmount || currentFluid == null) && cellFluid != null) {
                    if (this.canFillSlot) {
                        if (!this.controlsAmount) {
                            cellFluid.setAmount(1);
                        }
                        if (this.fluidTank.fill(cellFluid, IFluidHandler.FluidAction.EXECUTE) > 0) {
                            this.lastStoredPhantomFluid = cellFluid.copy();
                        }
                    }
                } else {
                    if (this.canDrainSlot) {
                        this.fluidTank.drain(mouseData.shift ? Integer.MAX_VALUE : 1000, IFluidHandler.FluidAction.EXECUTE);
                    }
                }
            }
        } else if (mouseData.mouseButton == 1) {
            if (this.canFillSlot) {
                if (currentFluid != null) {
                    if (this.controlsAmount) {
                        FluidStack toFill = currentFluid.copy();
                        toFill.setAmount(1000);
                        this.fluidTank.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
                    }
                } else if (this.lastStoredPhantomFluid != null) {
                    FluidStack toFill = this.lastStoredPhantomFluid.copy();
                    toFill.setAmount(this.controlsAmount ? 1000 : 1);
                    this.fluidTank.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
                }
            }
        } else if (mouseData.mouseButton == 2 && currentFluid != null && this.canDrainSlot) {
            this.fluidTank.drain(mouseData.shift ? Integer.MAX_VALUE : 1000, IFluidHandler.FluidAction.EXECUTE);
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
                toFill.setAmount(this.controlsAmount ? amount : 1);
                this.fluidTank.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
            }
            return;
        }
        if (amount > 0 && this.controlsAmount) {
            FluidStack toFill = currentFluid.copy();
            toFill.setAmount(amount);
            this.fluidTank.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
        } else if (amount < 0) {
            this.fluidTank.drain(-amount, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    private void playSound(FluidStack fluid, boolean fill) {
        Player player = getSyncManager().getPlayer();
        SoundEvent soundevent = fill ? fluid.getFluid().getPickupSound().get() : fluid.getFluid().getPickupSound().get();
        player.level().playSound(null, player.position().x, player.position().y + 0.5, player.position().z, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
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
            sync(3, buffer -> buffer.writeBoolean(controlsAmount));
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
