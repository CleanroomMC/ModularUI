package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.sync.SyncHandler;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.TextRenderer;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.Tooltip;
import com.cleanroommc.modularui.sync.FluidSlotSyncHandler;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.NumberFormat;
import com.cleanroommc.modularui.widget.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

public class FluidSlot extends Widget<FluidSlot> implements Interactable {

    private static final IFluidTank EMPTY = new FluidTank(0);

    private final TextRenderer textRenderer = new TextRenderer();
    private FluidSlotSyncHandler syncHandler;
    private int contentOffsetX = 1, contentOffsetY = 1;
    private boolean alwaysShowFull = true;
    @Nullable
    private IDrawable overlayTexture = null;

    public FluidSlot() {
        flex().startDefaultMode()
                .size(18, 18)
                .endDefaultMode();

        tooltipBuilder(tooltip -> {
            IFluidTank fluidTank = getFluidTank();
            FluidStack fluid = this.syncHandler.getCachedValue();
            if (this.syncHandler.isPhantom()) {
                if (fluid != null) {
                    tooltip.addLine(IKey.str(fluid.getLocalizedName()));
                    if (this.syncHandler.controlsAmount()) {
                        tooltip.addLine(IKey.format("modularui.fluid.phantom.amount", fluid.amount));
                    }
                } else {
                    tooltip.addLine(IKey.format("modularui.fluid.empty"));
                }
                if (this.syncHandler.controlsAmount()) {
                    tooltip.addLine(IKey.format("modularui.fluid.phantom.control"));
                }
            } else {
                if (fluid != null) {
                    tooltip.addLine(IKey.str(fluid.getLocalizedName()));
                    tooltip.addLine(IKey.format("modularui.fluid.amount", fluid.amount, fluidTank.getCapacity()));
                    addAdditionalFluidInfo(tooltip, fluid);
                } else {
                    tooltip.addLine(IKey.format("modularui.fluid.empty"));
                }
                if (this.syncHandler.canFillSlot() || this.syncHandler.canDrainSlot()) {
                    tooltip.addLine(IKey.EMPTY); // Add an empty line to separate from the bottom material tooltips
                    if (Interactable.hasShiftDown()) {
                        if (this.syncHandler.canFillSlot() && this.syncHandler.canDrainSlot()) {
                            tooltip.addLine(IKey.format("modularui.fluid.click_combined"));
                        } else if (this.syncHandler.canDrainSlot()) {
                            tooltip.addLine(IKey.format("modularui.fluid.click_to_fill"));
                        } else if (this.syncHandler.canFillSlot()) {
                            tooltip.addLine(IKey.format("modularui.fluid.click_to_empty"));
                        }
                    } else {
                        tooltip.addLine(IKey.format("modularui.tooltip.shift"));
                    }
                }
            }
        });
    }

    @ApiStatus.OverrideOnly
    public void addAdditionalFluidInfo(Tooltip tooltip, FluidStack fluidStack) {
    }

    @Override
    public void onInit() {
        textRenderer.setShadow(true);
        textRenderer.setScale(0.5f);
        textRenderer.setColor(Color.WHITE.normal);

        if (getBackground().length == 0) {
            background(GuiTextures.SLOT_DARK);
        }
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        if (syncHandler instanceof FluidSlotSyncHandler) {
            this.syncHandler = (FluidSlotSyncHandler) syncHandler;
            return true;
        }
        return false;
    }

    @Override
    public void draw(float partialTicks) {
        IFluidTank fluidTank = getFluidTank();
        FluidStack content = this.syncHandler.getCachedValue();
        if (content != null) {
            int y = contentOffsetY;
            float height = getArea().height - y * 2;
            if (!alwaysShowFull) {
                float newHeight = height * content.amount * 1f / fluidTank.getCapacity();
                y += height - newHeight;
                height = newHeight;
            }
            GuiDraw.drawFluidTexture(content, contentOffsetX, y, getArea().width - contentOffsetX * 2, height, 0);
        }
        if (overlayTexture != null) {
            overlayTexture.draw(getArea());
        }
        if (content != null && syncHandler.controlsAmount()) {
            String s = NumberFormat.format(content.amount);
            textRenderer.setAlignment(Alignment.CenterRight, getArea().width - contentOffsetX - 1f);
            textRenderer.setPos((int) (contentOffsetX + 0.5f), (int) (getArea().height - 5.5f));
            textRenderer.draw(s);
        }
        if (isHovering()) {
            if (isHovering()) {
                GlStateManager.colorMask(true, true, true, false);
                GuiDraw.drawSolidRect(1, 1, 16, 16, Color.withAlpha(Color.WHITE.normal, 0x80)/*TODO Theme.INSTANCE.getSlotHighlight()*/);
                GlStateManager.colorMask(true, true, true, true);
            }
        }
    }

    @NotNull
    @Override
    public Result onMouseTapped(int mouseButton) {
        if (!this.syncHandler.canFillSlot() && !this.syncHandler.canDrainSlot()) {
            return Result.IGNORE;
        }
        ItemStack cursorStack = Minecraft.getMinecraft().player.inventory.getItemStack();
        if (this.syncHandler.isPhantom() || (!cursorStack.isEmpty() && cursorStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null))) {
            this.syncHandler.syncToServer(1, buffer -> {
                buffer.writeVarInt(mouseButton);
                buffer.writeBoolean(Interactable.hasShiftDown());
            });
        }
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        if (this.syncHandler.isPhantom()) {
            amount = scrollDirection.modifier;
            if ((scrollDirection.isUp() && !this.syncHandler.canFillSlot()) || (scrollDirection.isDown() && !this.syncHandler.canDrainSlot())) {
                return false;
            }
            if (Interactable.hasShiftDown()) {
                amount *= 10;
            }
            if (Interactable.hasControlDown()) {
                amount *= 100;
            }
            final int finalAmount = amount;
            this.syncHandler.syncToServer(2, buffer -> buffer.writeVarInt(finalAmount));
            return true;
        }
        return false;
    }

    @Override
    public @NotNull Result onKeyPressed(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_LSHIFT || keyCode == Keyboard.KEY_RSHIFT) {
            markDirty();
        }
        return Interactable.super.onKeyPressed(typedChar, keyCode);
    }

    @Override
    public boolean onKeyRelease(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_LSHIFT || keyCode == Keyboard.KEY_RSHIFT) {
            markDirty();
        }
        return Interactable.super.onKeyRelease(typedChar, keyCode);
    }

    public IFluidTank getFluidTank() {
        if (this.syncHandler == null) {
            return EMPTY;
        }
        return this.syncHandler.getFluidTank();
    }

    /**
     * Set the offset in x and y (on both sides) at which the fluid should be rendered.
     * Default is 1 for both.
     *
     * @param x x offset
     * @param y y offset
     */
    public FluidSlot contentOffset(int x, int y) {
        this.contentOffsetX = x;
        this.contentOffsetY = y;
        return this;
    }

    /**
     * @param alwaysShowFull if the fluid should be rendered as full or as the partial amount.
     */
    public FluidSlot alwaysShowFull(boolean alwaysShowFull) {
        this.alwaysShowFull = alwaysShowFull;
        return this;
    }

    /**
     * @param overlayTexture texture that is rendered on top of the fluid
     */
    public FluidSlot overlayTexture(@Nullable IDrawable overlayTexture) {
        this.overlayTexture = overlayTexture;
        return this;
    }
}
