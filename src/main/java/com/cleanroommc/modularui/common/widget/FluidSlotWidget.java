package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.Interactable;
import com.cleanroommc.modularui.api.TooltipContainer;
import com.cleanroommc.modularui.api.drawable.GuiHelper;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.drawable.UITexture;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.common.internal.network.NetworkUtils;
import com.cleanroommc.modularui.common.internal.wrapper.FluidTankHandler;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class FluidSlotWidget extends SyncedWidget implements Interactable {

    public static final Size SIZE = new Size(18, 18);
    public static final UITexture TEXTURE = UITexture.fullImage("modularui", "gui/slot/fluid");

    @Nullable
    private IDrawable overlayTexture;
    private final IFluidTank fluidTank;
    private final IFluidHandler tankHandler;
    @Nullable
    private FluidStack cachedFluid;
    private Pos2d contentOffset = new Pos2d(1, 1);
    private boolean alwaysShowFull = true;
    private boolean allowManualFilling = true;
    private boolean allowManualEmptying = true;

    public FluidSlotWidget(IFluidTank fluidTank) {
        this.fluidTank = fluidTank;
        this.tankHandler = FluidTankHandler.getTankFluidHandler(fluidTank);
    }

    public FluidStack getContent() {
        return fluidTank.getFluid();
    }

    @Override
    public void onInit() {
        if (getBackground() == null) {
            setBackground(TEXTURE);
        }
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        return SIZE;
    }

    @Nullable
    @Override
    public TooltipContainer getHoverText() {
        TooltipContainer tooltip = new TooltipContainer();
        FluidStack fluid = cachedFluid;
        if (fluid != null) {
            tooltip.addLine(fluid.getLocalizedName());
            tooltip.addLine(Text.localised("modularui.fluid.amount", fluid.amount, fluidTank.getCapacity()));
            addAdditionalFluidInfo(tooltip, fluid);
        } else {
            tooltip.addLine(Text.localised("modularui.fluid.empty"));
        }
        if (allowManualEmptying || allowManualFilling) {
            tooltip.addLine(); // Add an empty line to separate from the bottom material tooltips
            if (Interactable.hasShiftDown()) {
                if (allowManualEmptying && allowManualFilling) {
                    tooltip.addLine(Text.localised("modularui.fluid.click_combined"));
                } else if (allowManualFilling) {
                    tooltip.addLine(Text.localised("modularui.fluid.click_to_fill"));
                } else if (allowManualEmptying) {
                    tooltip.addLine(Text.localised("modularui.fluid.click_to_empty"));
                }
            } else {
                tooltip.addLine(Text.localised("modularui.tooltip.shift"));
            }
        }
        return tooltip;
    }

    /**
     * Mods can override this to add custom tooltips for the fluid
     *
     * @param tooltipContainer add lines here
     * @param fluid            the nonnull fluid
     */
    public void addAdditionalFluidInfo(TooltipContainer tooltipContainer, @NotNull FluidStack fluid) {
    }

    @Override
    public void draw(float partialTicks) {
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

    @Override
    public boolean onClick(int buttonId, boolean doubleClick) {
        if (!allowManualEmptying && !allowManualFilling) {
            return false;
        }
        ItemStack cursorStack = getContext().getCursorStack();
        if (!cursorStack.isEmpty() && cursorStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            syncToServer(1, buffer -> {
                buffer.writeVarInt(buttonId);
                buffer.writeBoolean(Interactable.hasShiftDown());
            });
            Interactable.playButtonClickSound();
            return true;
        }
        return false;
    }

    @Override
    public void detectAndSendChanges() {
        FluidStack currentFluid = this.fluidTank.getFluid();
        if (currentFluid == null ^ this.cachedFluid == null || (currentFluid != null && (!currentFluid.isFluidEqual(cachedFluid) || currentFluid.amount != cachedFluid.amount))) {
            syncToClient(1, buffer -> NetworkUtils.writeFluidStack(buffer, currentFluid));
        }
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        if (id == 1) {
            this.cachedFluid = NetworkUtils.readFluidStack(buf);
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        if (id == 1) {
            tryClickContainer(buf.readVarInt(), buf.readBoolean());
        }
    }

    private void tryClickContainer(int mouseButton, boolean isShiftKeyDown) {
        EntityPlayer player = getContext().getPlayer();
        ItemStack currentStack = getContext().getCursorStack();
        if (!currentStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            return;
        }
        int maxAttempts = isShiftKeyDown ? currentStack.getCount() : 1;
        if (mouseButton == 0 && allowManualFilling) {
            boolean performedTransfer = false;
            for (int i = 0; i < maxAttempts; i++) {
                FluidActionResult result = FluidUtil.tryEmptyContainer(currentStack, tankHandler, Integer.MAX_VALUE, null, false);
                ItemStack remainingStack = result.getResult();
                if (!result.isSuccess() || (currentStack.getCount() > 1 && !remainingStack.isEmpty() && !player.inventory.addItemStackToInventory(remainingStack))) {
                    player.dropItem(remainingStack, true);
                    break; //do not continue if we can't add resulting container into inventory
                }

                remainingStack = FluidUtil.tryEmptyContainer(currentStack, tankHandler, Integer.MAX_VALUE, null, true).result;
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
                SoundEvent soundevent = fluid.getFluid().getFillSound(fluid);
                player.world.playSound(null, player.posX, player.posY + 0.5, player.posZ, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
                getContext().setCursorStack(currentStack, true);
            }
            return;
        }
        FluidStack currentFluid = fluidTank.getFluid();
        if (mouseButton == 1 && allowManualEmptying && currentFluid != null && currentFluid.amount > 0) {
            boolean performedTransfer = false;
            for (int i = 0; i < maxAttempts; i++) {
                FluidActionResult result = FluidUtil.tryFillContainer(currentStack, tankHandler, Integer.MAX_VALUE, null, false);
                ItemStack remainingStack = result.getResult();
                if (!result.isSuccess() || (currentStack.getCount() > 1 && !remainingStack.isEmpty() && !player.inventory.addItemStackToInventory(remainingStack))) {
                    break; //do not continue if we can't add resulting container into inventory
                }

                remainingStack = FluidUtil.tryFillContainer(currentStack, tankHandler, Integer.MAX_VALUE, null, true).result;
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
                SoundEvent soundevent = currentFluid.getFluid().getFillSound(currentFluid);
                player.world.playSound(null, player.posX, player.posY + 0.5, player.posZ, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
                getContext().setCursorStack(currentStack, true);
            }
        }
    }

    public boolean allowManualEmptying() {
        return allowManualEmptying;
    }

    public boolean allowManualFilling() {
        return allowManualFilling;
    }

    public boolean alwaysShowFull() {
        return alwaysShowFull;
    }

    public Pos2d getContentOffset() {
        return contentOffset;
    }

    @Nullable
    public IDrawable getOverlayTexture() {
        return overlayTexture;
    }

    public FluidSlotWidget setInteraction(boolean fillable, boolean drainable) {
        this.allowManualFilling = fillable;
        this.allowManualEmptying = drainable;
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
