package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.Interactable;
import com.cleanroommc.modularui.api.TooltipContainer;
import com.cleanroommc.modularui.api.drawable.GuiHelper;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.drawable.UITexture;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.io.IOException;

public class FluidSlotWidget extends SyncedWidget implements Interactable {

    public static final Size SIZE = new Size(18, 18);
    public static final UITexture TEXTURE = UITexture.fullImage("modularui", "gui/slot/fluid");

    @Nullable
    private IDrawable overlayTexture;
    private final IFluidTank fluidTank;
    private FluidStack cachedFluid;
    private Pos2d contentOffset = new Pos2d(1, 1);
    private boolean alwaysShowFull = true;
    private boolean allowClickFilling = true;
    private boolean allowClickEmptying = true;

    public FluidSlotWidget(IFluidTank fluidTank) {
        this.fluidTank = fluidTank;
    }

    public FluidStack getContent() {
        return fluidTank.getFluid();
    }

    @Override
    public void onInit() {
        if (getDrawable() == null) {
            setBackground(TEXTURE);
        }
    }

    @Nullable
    @Override
    public TooltipContainer getHoverText() {
        TooltipContainer tooltip = new TooltipContainer();
        FluidStack fluid = cachedFluid;
        if (fluid != null) {
            tooltip.addLine(fluid.getLocalizedName());
            tooltip.addLine(Text.localised("modularui.fluid.amount", fluid.amount, fluidTank.getCapacity()));
        } else {
            tooltip.addLine(Text.localised("modularui.fluid.empty"));
        }
        if (allowClickEmptying || allowClickFilling) {
            tooltip.addLine(); // Add an empty line to separate from the bottom material tooltips
            if (Interactable.hasShiftDown()) {
                if (allowClickEmptying && allowClickFilling) {
                    tooltip.addLine(Text.localised("modularui.fluid.click_combined"));
                } else if (allowClickFilling) {
                    tooltip.addLine(Text.localised("modularui.fluid.click_to_fill"));
                } else if (allowClickEmptying) {
                    tooltip.addLine(Text.localised("modularui.fluid.click_to_empty"));
                }
            } else {
                tooltip.addLine(Text.localised("modularui.tooltip.shift"));
            }
        }
        return tooltip;
    }

    @Override
    public void drawInBackground(float partialTicks) {
        FluidStack content = cachedFluid;
        if (content != null) {
            float y = contentOffset.y;
            float height = size.height - contentOffset.y * 2;
            if (!alwaysShowFull) {
                float newHeight = height * content.amount * 1f / fluidTank.getCapacity();
                y += height - newHeight;
                height = newHeight;
            }
            GuiHelper.drawFluidTexture(content, contentOffset.x, y, size.width - contentOffset.x * 2, height, 0);
        }
        if (overlayTexture != null) {
            overlayTexture.draw(Pos2d.ZERO, size, partialTicks);
        }
    }

    @Nullable
    @Override
    protected Size determineSize() {
        return SIZE;
    }

    @Override
    public void onClick(int buttonId, boolean doubleClick) {
        if (!allowClickEmptying && !allowClickFilling) {
            return;
        }
        ItemStack cursorStack = getContext().getCursorStack();
        if (!cursorStack.isEmpty() && cursorStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            syncToServer(1, buffer -> {
                buffer.writeVarInt(buttonId);
                buffer.writeBoolean(Interactable.hasShiftDown());
            });
            Interactable.playButtonClickSound();
        }
    }

    @Override
    public void onServerTick() {
        FluidStack currentFluid = fluidTank.getFluid();
        if (currentFluid == null && cachedFluid != null) {
            this.cachedFluid = null;
            syncToClient(1, buffer -> {
            });
        } else if (currentFluid != null) {
            if (!currentFluid.isFluidEqual(cachedFluid) || currentFluid.amount != cachedFluid.amount) {
                this.cachedFluid = currentFluid.copy();
                NBTTagCompound fluidStackTag = currentFluid.writeToNBT(new NBTTagCompound());
                syncToClient(2, buffer -> buffer.writeCompoundTag(fluidStackTag));
            }
        }
    }

    @Override
    public void readServerData(int id, PacketBuffer buf) {
        if (id == 1) {
            cachedFluid = null;
        } else if (id == 2) {
            NBTTagCompound fluidStackTag;
            try {
                fluidStackTag = buf.readCompoundTag();
            } catch (IOException ignored) {
                return;
            }
            this.cachedFluid = FluidStack.loadFluidStackFromNBT(fluidStackTag);
        } else if (id == 4) {
            ItemStack currentStack = getContext().getCursorStack();
            int newStackSize = buf.readVarInt();
            currentStack.setCount(newStackSize);
            getContext().setCursorStack(currentStack);
        }
    }

    @Override
    public void readClientData(int id, PacketBuffer buf) {
        if (id == 1) {
            int clickResult = tryClickContainer(buf.readVarInt(), buf.readBoolean());
            if (clickResult >= 0) {
                syncToClient(4, buffer -> buffer.writeVarInt(clickResult));
            }
        }
    }

    private int tryClickContainer(int mouseButton, boolean isShiftKeyDown) {
        EntityPlayer player = getContext().getPlayer();
        ItemStack currentStack = getContext().getCursorStack();
        if (!currentStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null))
            return -1;
        int maxAttempts = isShiftKeyDown ? currentStack.getCount() : 1;

        if (allowClickFilling && fluidTank.getFluidAmount() > 0) {
            boolean performedFill = false;
            FluidStack initialFluid = fluidTank.getFluid();
            for (int i = 0; i < maxAttempts; i++) {
                FluidActionResult result = FluidUtil.tryFillContainer(currentStack,
                        (IFluidHandler) fluidTank, Integer.MAX_VALUE, null, false);
                if (!result.isSuccess()) break;
                ItemStack remainingStack = result.getResult();
                if (!remainingStack.isEmpty() && !player.inventory.addItemStackToInventory(remainingStack))
                    break; //do not continue if we can't add resulting container into inventory
                FluidUtil.tryFillContainer(currentStack, (IFluidHandler) fluidTank, Integer.MAX_VALUE, null, true);
                currentStack.shrink(1);
                performedFill = true;
            }
            if (performedFill) {
                SoundEvent soundevent = initialFluid.getFluid().getFillSound(initialFluid);
                player.world.playSound(null, player.posX, player.posY + 0.5, player.posZ,
                        soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
                player.inventory.setItemStack(currentStack);
                return currentStack.getCount();
            }
        }

        if (allowClickEmptying) {
            boolean performedEmptying = false;
            for (int i = 0; i < maxAttempts; i++) {
                FluidActionResult result = FluidUtil.tryEmptyContainer(currentStack,
                        (IFluidHandler) fluidTank, Integer.MAX_VALUE, null, false);
                if (!result.isSuccess()) break;
                ItemStack remainingStack = result.getResult();
                if (!remainingStack.isEmpty() && !player.inventory.addItemStackToInventory(remainingStack))
                    break; //do not continue if we can't add resulting container into inventory
                FluidUtil.tryEmptyContainer(currentStack, (IFluidHandler) fluidTank, Integer.MAX_VALUE, null, true);
                currentStack.shrink(1);
                performedEmptying = true;
            }
            FluidStack filledFluid = fluidTank.getFluid();
            if (performedEmptying && filledFluid != null) {
                SoundEvent soundevent = filledFluid.getFluid().getEmptySound(filledFluid);
                player.world.playSound(null, player.posX, player.posY + 0.5, player.posZ,
                        soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
                player.inventory.setItemStack(currentStack);
                return currentStack.getCount();
            }
        }

        return -1;
    }

    public FluidSlotWidget setInteraction(boolean fillable, boolean drainable) {
        this.allowClickFilling = fillable;
        this.allowClickEmptying = drainable;
        return this;
    }

    public FluidSlotWidget setAlwaysShowFull(boolean alwaysShowFull) {
        this.alwaysShowFull = alwaysShowFull;
        return this;
    }

    public FluidSlotWidget setContentOffset(Pos2d contentOffset) {
        this.contentOffset = contentOffset;
        return this;
    }

    public FluidSlotWidget setOverlayTexture(@Nullable IDrawable overlayTexture) {
        this.overlayTexture = overlayTexture;
        return this;
    }
}
