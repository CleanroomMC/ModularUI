package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.ModularUITextures;
import com.cleanroommc.modularui.api.NumberFormat;
import com.cleanroommc.modularui.api.drawable.GuiHelper;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.drawable.TextRenderer;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Color;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.api.widget.IGhostIngredientTarget;
import com.cleanroommc.modularui.api.widget.IIngredientProvider;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.common.internal.Theme;
import com.cleanroommc.modularui.common.internal.network.NetworkUtils;
import com.cleanroommc.modularui.common.internal.wrapper.FluidTankHandler;
import com.cleanroommc.modularui.common.internal.wrapper.GhostIngredientWrapper;
import com.cleanroommc.modularui.common.internal.wrapper.ModularGui;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

public class FluidSlotWidget extends SyncedWidget implements Interactable, IIngredientProvider, IGhostIngredientTarget<Object> {

    public static final Size SIZE = new Size(18, 18);

    @Nullable
    private IDrawable overlayTexture;
    private final TextRenderer textRenderer = new TextRenderer();
    private final IFluidTank fluidTank;
    private final IFluidHandler tankHandler;
    @Nullable
    private FluidStack cachedFluid;
    private FluidStack lastStoredPhantomFluid;
    private Pos2d contentOffset = new Pos2d(1, 1);
    private boolean alwaysShowFull = true;
    private boolean canDrainSlot = true;
    private boolean canFillSlot = true;
    private boolean phantom = false;
    private boolean controlsAmount = true;
    private boolean lastShift = false;

    public FluidSlotWidget(IFluidTank fluidTank) {
        this.fluidTank = fluidTank;
        this.tankHandler = FluidTankHandler.getTankFluidHandler(fluidTank);
        this.textRenderer.setColor(Color.WHITE.normal);
        this.textRenderer.setShadow(true);
    }

    public static FluidSlotWidget phantom(IFluidTank fluidTank, boolean controlsAmount) {
        FluidSlotWidget slot = new FluidSlotWidget(fluidTank);
        slot.phantom = true;
        slot.controlsAmount = controlsAmount;
        return slot;
    }

    public static FluidSlotWidget phantom(int capacity) {
        return phantom(new FluidTank(capacity > 0 ? capacity : 1), capacity > 1);
    }

    public FluidStack getContent() {
        return this.fluidTank.getFluid();
    }

    @Override
    public void onInit() {
        if (isClient()) {
            this.textRenderer.setShadow(true);
            this.textRenderer.setScale(0.5f);
        }
        if (getBackground() == null) {
            setBackground(ModularUITextures.FLUID_SLOT);
        }
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        return SIZE;
    }

    public void setControlsAmount(boolean controlsAmount, boolean sync) {
        if (this.controlsAmount != controlsAmount) {
            this.controlsAmount = controlsAmount;
            if (sync) {
                if (isClient()) {
                    syncToServer(3, buffer -> buffer.writeBoolean(controlsAmount));
                } else {
                    syncToClient(3, buffer -> buffer.writeBoolean(controlsAmount));
                }
            }
        }
    }

    @Override
    public void buildTooltip(List<Text> tooltip) {
        FluidStack fluid = cachedFluid;
        if (phantom) {
            if (fluid != null) {
                tooltip.add(new Text(fluid.getLocalizedName()));
                if (controlsAmount) {
                    tooltip.add(Text.localised("modularui.fluid.phantom.amount", fluid.amount));
                }
            } else {
                tooltip.add(Text.localised("modularui.fluid.empty"));
            }
            if (controlsAmount) {
                tooltip.add(Text.localised("modularui.fluid.phantom.control"));
            }
        } else {
            if (fluid != null) {
                tooltip.add(new Text(fluid.getLocalizedName()));
                tooltip.add(Text.localised("modularui.fluid.amount", fluid.amount, fluidTank.getCapacity()));
                addAdditionalFluidInfo(tooltip, fluid);
            } else {
                tooltip.add(Text.localised("modularui.fluid.empty"));
            }
            if (canFillSlot || canDrainSlot) {
                tooltip.add(Text.EMPTY); // Add an empty line to separate from the bottom material tooltips
                if (Interactable.hasShiftDown()) {
                    if (canFillSlot && canDrainSlot) {
                        tooltip.add(Text.localised("modularui.fluid.click_combined"));
                    } else if (canDrainSlot) {
                        tooltip.add(Text.localised("modularui.fluid.click_to_fill"));
                    } else if (canFillSlot) {
                        tooltip.add(Text.localised("modularui.fluid.click_to_empty"));
                    }
                } else {
                    tooltip.add(Text.localised("modularui.tooltip.shift"));
                }
            }
        }
    }

    /**
     * Mods can override this to add custom tooltips for the fluid
     *
     * @param tooltipContainer add lines here
     * @param fluid            the nonnull fluid
     */
    public void addAdditionalFluidInfo(List<Text> tooltipContainer, @NotNull FluidStack fluid) {
    }

    @Override
    public @Nullable String getBackgroundColorKey() {
        return Theme.KEY_FLUID_SLOT;
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
        if (content != null && this.controlsAmount) {
            String s = NumberFormat.format(content.amount);
            textRenderer.setAlignment(Alignment.CenterRight, size.width - contentOffset.x - 1f);
            textRenderer.setPos((int) (contentOffset.x + 0.5f), (int) (size.height - 5.5f));
            textRenderer.draw(s);
        }
        if (isHovering()) {
            if (isHovering()) {
                GlStateManager.colorMask(true, true, true, false);
                ModularGui.drawSolidRect(1, 1, 16, 16, Theme.INSTANCE.getSlotHighlight());
                GlStateManager.colorMask(true, true, true, true);
            }
        }
    }

    @Override
    public void onScreenUpdate() {
        if (lastShift != Interactable.hasShiftDown()) {
            lastShift = Interactable.hasShiftDown();
            notifyTooltipChange();
        }
    }

    @Override
    public Object getIngredient() {
        return cachedFluid;
    }

    @Override
    public ClickResult onClick(int buttonId, boolean doubleClick) {
        if (!this.canFillSlot && !this.canDrainSlot) {
            return ClickResult.ACKNOWLEDGED;
        }
        ItemStack cursorStack = getContext().getCursor().getItemStack();
        if (this.phantom || (!cursorStack.isEmpty() && cursorStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null))) {
            syncToServer(1, buffer -> {
                buffer.writeVarInt(buttonId);
                buffer.writeBoolean(Interactable.hasShiftDown());
            });
            Interactable.playButtonClickSound();
            return ClickResult.ACCEPT;
        }
        return ClickResult.ACKNOWLEDGED;
    }

    @Override
    public boolean onMouseScroll(int direction) {
        if (this.phantom) {
            if ((direction > 0 && !this.canFillSlot) || (direction < 0 && !this.canDrainSlot)) {
                return false;
            }
            if (Interactable.hasShiftDown()) {
                direction *= 10;
            }
            if (Interactable.hasControlDown()) {
                direction *= 100;
            }
            final int finalDirection = direction;
            syncToServer(2, buffer -> buffer.writeVarInt(finalDirection));
            return true;
        }
        return false;
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        FluidStack currentFluid = this.fluidTank.getFluid();
        if (init || fluidChanged(currentFluid, this.cachedFluid)) {
            this.cachedFluid = currentFluid == null ? null : currentFluid.copy();
            syncToClient(1, buffer -> NetworkUtils.writeFluidStack(buffer, currentFluid));
        }
    }

    public static boolean fluidChanged(@Nullable FluidStack current, @Nullable FluidStack cached) {
        return current == null ^ cached == null || (current != null && (current.amount != cached.amount || !current.isFluidEqual(cached)));
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        if (id == 1) {
            this.cachedFluid = NetworkUtils.readFluidStack(buf);
            notifyTooltipChange();
        } else if (id == 3) {
            this.controlsAmount = buf.readBoolean();
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (id == 1) {
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
        EntityPlayer player = getContext().getPlayer();
        ItemStack currentStack = getContext().getCursor().getItemStack();
        if (!currentStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            return;
        }
        int maxAttempts = isShiftKeyDown ? currentStack.getCount() : 1;
        if (mouseButton == 0 && canFillSlot) {
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
                playSound(fluid, false);
                getContext().getCursor().setItemStack(currentStack, true);
            }
            return;
        }
        FluidStack currentFluid = fluidTank.getFluid();
        if (mouseButton == 1 && canDrainSlot && currentFluid != null && currentFluid.amount > 0) {
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
                playSound(currentFluid, true);
                getContext().getCursor().setItemStack(currentStack, true);
            }
        }
    }

    public void tryClickPhantom(int mouseButton, boolean isShiftKeyDown) {
        EntityPlayer player = getContext().getPlayer();
        ItemStack currentStack = getContext().getCursor().getItemStack();
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
        EntityPlayer player = getContext().getPlayer();
        SoundEvent soundevent = fill ? fluid.getFluid().getFillSound(fluid) : fluid.getFluid().getEmptySound(fluid);
        player.world.playSound(null, player.posX, player.posY + 0.5, player.posZ, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    public IGhostIngredientHandler.@Nullable Target<Object> getTarget(@NotNull Object ingredient) {
        if (!isPhantom()) {
            return null;
        }
        if (ingredient instanceof FluidStack) {
            return ((FluidStack) ingredient).amount > 0 ? new GhostIngredientWrapper<>(this) : null;
        }
        if (ingredient instanceof ItemStack) {
            if (((ItemStack) ingredient).isEmpty()) return null;
            IFluidHandlerItem fluidHandlerItem = ((ItemStack) ingredient).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fluidHandlerItem != null) {
                return new GhostIngredientWrapper<>(this);
            }
        }
        return null;
    }

    @Override
    public void accept(@NotNull Object ingredient) {
        FluidStack fluid = null;
        if (ingredient instanceof FluidStack) {
            fluid = (FluidStack) ingredient;
        } else if (ingredient instanceof ItemStack) {
            IFluidHandlerItem fluidHandlerItem = ((ItemStack) ingredient).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fluidHandlerItem == null) return;
            fluid = fluidHandlerItem.drain(Integer.MAX_VALUE, false);
        }
        if (fluid == null) return;
        final FluidStack finalFluid = fluid;
        syncToServer(4, buffer -> NetworkUtils.writeFluidStack(buffer, finalFluid));
    }

    public boolean canFillSlot() {
        return canFillSlot;
    }

    public boolean canDrainSlot() {
        return canDrainSlot;
    }

    public boolean alwaysShowFull() {
        return alwaysShowFull;
    }

    public Pos2d getContentOffset() {
        return contentOffset;
    }

    public boolean controlsAmount() {
        return controlsAmount;
    }

    public boolean isPhantom() {
        return phantom;
    }

    @Nullable
    public IDrawable getOverlayTexture() {
        return overlayTexture;
    }

    public FluidSlotWidget setInteraction(boolean canDrainSlot, boolean canFillSlot) {
        this.canDrainSlot = canDrainSlot;
        this.canFillSlot = canFillSlot;
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
