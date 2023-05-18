package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.sync.SyncHandler;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.TextRenderer;
import com.cleanroommc.modularui.integration.jei.JeiGhostIngredientSlot;
import com.cleanroommc.modularui.integration.jei.JeiIngredientProvider;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.Tooltip;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.sync.FluidSlotSyncHandler;
import com.cleanroommc.modularui.theme.WidgetSlotTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.MouseData;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

public class FluidSlot extends Widget<FluidSlot> implements Interactable, JeiGhostIngredientSlot<FluidStack>, JeiIngredientProvider {

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
            tooltip.setHasSpaceAfterFirstLine(true);
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

    public void addAdditionalFluidInfo(Tooltip tooltip, FluidStack fluidStack) {
    }

    @Override
    public void onInit() {
        textRenderer.setShadow(true);
        textRenderer.setScale(0.5f);
        textRenderer.setColor(Color.WHITE.normal);
        getContext().addJeiGhostIngredientSlot(this);
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
    public void draw(GuiContext context) {
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
            overlayTexture.draw(context, getArea());
        }
        if (content != null && syncHandler.controlsAmount()) {
            String s = NumberFormat.format(content.amount);
            textRenderer.setAlignment(Alignment.CenterRight, getArea().width - contentOffsetX - 1f);
            textRenderer.setPos((int) (contentOffsetX + 0.5f), (int) (getArea().height - 5.5f));
            textRenderer.draw(s);
        }
        if (isHovering()) {
            GlStateManager.colorMask(true, true, true, false);
            GuiDraw.drawRect(1, 1, 16, 16, getWidgetTheme(context.getTheme()).getSlotHoverColor());
            GlStateManager.colorMask(true, true, true, true);
        }
    }

    @Override
    public WidgetSlotTheme getWidgetTheme(ITheme theme) {
        return theme.getFluidSlotTheme();
    }

    @NotNull
    @Override
    public Result onMouseTapped(int mouseButton) {
        if (!this.syncHandler.canFillSlot() && !this.syncHandler.canDrainSlot()) {
            return Result.IGNORE;
        }
        ItemStack cursorStack = Minecraft.getMinecraft().player.inventory.getItemStack();
        if (this.syncHandler.isPhantom() || (!cursorStack.isEmpty() && cursorStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null))) {
            MouseData mouseData = MouseData.create(mouseButton);
            this.syncHandler.syncToServer(1, mouseData::writeToPacket);
        }
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        if (this.syncHandler.isPhantom()) {
            if ((scrollDirection.isUp() && !this.syncHandler.canFillSlot()) || (scrollDirection.isDown() && !this.syncHandler.canDrainSlot())) {
                return false;
            }
            MouseData mouseData = MouseData.create(scrollDirection.modifier);
            this.syncHandler.syncToServer(2, mouseData::writeToPacket);
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

    @Nullable
    public FluidStack getFluidStack() {
        return this.syncHandler == null ? null : this.syncHandler.getCachedValue();
    }

    public IFluidTank getFluidTank() {
        return this.syncHandler == null ? EMPTY : this.syncHandler.getFluidTank();
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

    /* === Jei ghost slot === */

    @Override
    public void setGhostIngredient(@NotNull FluidStack ingredient) {
        this.syncHandler.updateFromClient(ingredient);
    }

    @Override
    public @Nullable FluidStack castGhostIngredientIfValid(@NotNull Object ingredient) {
        return this.syncHandler.isPhantom() && ingredient instanceof FluidStack ? (FluidStack) ingredient : null;
    }

    @Override
    public @Nullable Object getIngredient() {
        return getFluidStack();
    }
}
