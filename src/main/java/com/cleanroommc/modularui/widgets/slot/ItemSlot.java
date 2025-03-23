package com.cleanroommc.modularui.widgets.slot;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.widget.IVanillaSlot;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.core.mixin.GuiAccessor;
import com.cleanroommc.modularui.core.mixin.GuiContainerAccessor;
import com.cleanroommc.modularui.core.mixin.GuiScreenAccessor;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.text.TextRenderer;
import com.cleanroommc.modularui.integration.jei.JeiIngredientProvider;
import com.cleanroommc.modularui.screen.ClientScreenHandler;
import com.cleanroommc.modularui.screen.NEAAnimationHandler;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetSlotTheme;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.NumberFormat;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.neverenoughanimations.NEAConfig;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemSlot extends Widget<ItemSlot> implements IVanillaSlot, Interactable, JeiIngredientProvider {

    public static final int SIZE = 18;

    public static ItemSlot create(boolean phantom) {
        return phantom ? new PhantomItemSlot() : new ItemSlot();
    }

    private static final TextRenderer textRenderer = new TextRenderer();
    private ItemSlotSH syncHandler;

    public ItemSlot() {
        tooltip().setAutoUpdate(true);//.setHasTitleMargin(true);
        tooltipBuilder(tooltip -> {
            if (!isSynced()) return;
            ItemStack stack = getSlot().getStack();
            buildTooltip(stack, tooltip);
        });
    }

    @Override
    public void onInit() {
        if (getScreen().isOverlay()) {
            throw new IllegalStateException("Overlays can't have slots!");
        }
        size(SIZE);
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        this.syncHandler = castIfTypeElseNull(syncHandler, ItemSlotSH.class);
        return this.syncHandler != null;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        boolean shouldBeEnabled = areAncestorsEnabled();
        if (shouldBeEnabled != getSlot().isEnabled()) {
            this.syncHandler.setEnabled(shouldBeEnabled, true);
        }
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        if (this.syncHandler == null) return;
        RenderHelper.enableGUIStandardItemLighting();
        drawSlot(getSlot());
        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableLighting();
        drawOverlay();
    }

    protected void drawOverlay() {
        if (isHovering() && (!ModularUI.Mods.NEA.isLoaded() || NEAConfig.itemHoverOverlay)) {
            GlStateManager.colorMask(true, true, true, false);
            GuiDraw.drawRect(1, 1, 16, 16, getSlotHoverColor());
            GlStateManager.colorMask(true, true, true, true);
        }
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        RichTooltip tooltip = getTooltip();
        if (tooltip != null && isHoveringFor(tooltip.getShowUpTimer())) {
            tooltip.draw(getContext(), getSlot().getStack());
        }
    }

    public void buildTooltip(ItemStack stack, RichTooltip tooltip) {
        if (stack.isEmpty()) return;
        tooltip.addFromItem(stack);
    }

    @Override
    public WidgetSlotTheme getWidgetThemeInternal(ITheme theme) {
        return theme.getItemSlotTheme();
    }

    public int getSlotHoverColor() {
        WidgetTheme theme = getWidgetTheme(getContext().getTheme());
        if (theme instanceof WidgetSlotTheme slotTheme) {
            return slotTheme.getSlotHoverColor();
        }
        return ITheme.getDefault().getItemSlotTheme().getSlotHoverColor();
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        ClientScreenHandler.clickSlot(getScreen(), getSlot());
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        ClientScreenHandler.releaseSlot();
        return true;
    }

    @Override
    public void onMouseDrag(int mouseButton, long timeSinceClick) {
        ClientScreenHandler.dragSlot(timeSinceClick);
    }

    public ModularSlot getSlot() {
        return this.syncHandler.getSlot();
    }

    @Override
    public Slot getVanillaSlot() {
        return this.syncHandler.getSlot();
    }

    @Override
    public boolean handleAsVanillaSlot() {
        return true;
    }

    @Override
    public @NotNull ItemSlotSH getSyncHandler() {
        if (this.syncHandler == null) {
            throw new IllegalStateException("Widget is not initialised!");
        }
        return this.syncHandler;
    }

    public ItemSlot slot(ModularSlot slot) {
        this.syncHandler = new ItemSlotSH(slot);
        setSyncHandler(this.syncHandler);
        return this;
    }

    public ItemSlot slot(IItemHandlerModifiable itemHandler, int index) {
        return slot(new ModularSlot(itemHandler, index));
    }

    @SideOnly(Side.CLIENT)
    private void drawSlot(ModularSlot slotIn) {
        GuiScreen guiScreen = getScreen().getScreenWrapper().getGuiScreen();
        if (!(guiScreen instanceof GuiContainer guiContainer))
            throw new IllegalStateException("The gui must be an instance of GuiContainer if it contains slots!");
        GuiContainerAccessor acc = (GuiContainerAccessor) guiScreen;
        RenderItem renderItem = ((GuiScreenAccessor) guiScreen).getItemRender();
        ItemStack itemstack = slotIn.getStack();
        boolean isDragPreview = false;
        boolean flag1 = slotIn == acc.getClickedSlot() && !acc.getDraggedStack().isEmpty() && !acc.getIsRightMouseClick();
        ItemStack itemstack1 = guiScreen.mc.player.inventory.getItemStack();
        int amount = -1;
        String format = null;

        if (!getSyncHandler().isPhantom()) {
            if (slotIn == acc.getClickedSlot() && !acc.getDraggedStack().isEmpty() && acc.getIsRightMouseClick() && !itemstack.isEmpty()) {
                itemstack = itemstack.copy();
                itemstack.setCount(itemstack.getCount() / 2);
            } else if (acc.getDragSplitting() && acc.getDragSplittingSlots().contains(slotIn) && !itemstack1.isEmpty()) {
                if (acc.getDragSplittingSlots().size() == 1) {
                    return;
                }

                if (Container.canAddItemToSlot(slotIn, itemstack1, true) && getScreen().getContainer().canDragIntoSlot(slotIn)) {
                    itemstack = itemstack1.copy();
                    isDragPreview = true;
                    Container.computeStackSize(acc.getDragSplittingSlots(), acc.getDragSplittingLimit(), itemstack, slotIn.getStack().isEmpty() ? 0 : slotIn.getStack().getCount());
                    int k = Math.min(itemstack.getMaxStackSize(), slotIn.getItemStackLimit(itemstack));

                    if (itemstack.getCount() > k) {
                        amount = k;
                        format = TextFormatting.YELLOW.toString();
                        itemstack.setCount(k);
                    }
                } else {
                    acc.getDragSplittingSlots().remove(slotIn);
                    acc.invokeUpdateDragSplitting();
                }
            }
        }

        ((GuiAccessor) guiScreen).setZLevel(100f);
        renderItem.zLevel = 100.0F;

        if (!flag1) {
            if (isDragPreview) {
                GuiDraw.drawRect(1, 1, 16, 16, -2130706433);
            }

            ItemStack virtualStack = NEAAnimationHandler.injectVirtualStack(guiContainer, slotIn);
            if (virtualStack != null) {
                itemstack = virtualStack;
            }

            if (!itemstack.isEmpty()) {
                GlStateManager.enableDepth();
                float itemScale = NEAAnimationHandler.injectHoverScale(guiContainer, slotIn);
                // render the item itself
                renderItem.renderItemAndEffectIntoGUI(guiScreen.mc.player, itemstack, 1, 1);
                // TODO render item borders from item borders mod here

                if (amount < 0) {
                    amount = itemstack.getCount();
                }
                // render the amount overlay
                if (amount > 1 || format != null) {
                    String amountText = NumberFormat.format(amount, NumberFormat.AMOUNT_TEXT);
                    if (format != null) {
                        amountText = format + amountText;
                    }
                    float scale = 1f;
                    if (amountText.length() == 3) {
                        scale = 0.8f;
                    } else if (amountText.length() == 4) {
                        scale = 0.6f;
                    } else if (amountText.length() > 4) {
                        scale = 0.5f;
                    }
                    textRenderer.setShadow(true);
                    textRenderer.setScale(scale);
                    textRenderer.setColor(Color.WHITE.main);
                    textRenderer.setAlignment(Alignment.BottomRight, getArea().width - 1, getArea().height - 1);
                    textRenderer.setPos(1, 1);
                    GlStateManager.disableLighting();
                    GlStateManager.disableDepth();
                    GlStateManager.disableBlend();
                    textRenderer.draw(amountText);
                    GlStateManager.enableLighting();
                    GlStateManager.enableDepth();
                    GlStateManager.enableBlend();
                }

                int cachedCount = itemstack.getCount();
                itemstack.setCount(1); // required to not render the amount overlay
                // render other overlays like durability bar
                renderItem.renderItemOverlayIntoGUI(((GuiScreenAccessor) guiScreen).getFontRenderer(), itemstack, 1, 1, null);
                NEAAnimationHandler.endHoverScale();
                itemstack.setCount(cachedCount);
                GlStateManager.disableDepth();
            }
        }

        ((GuiAccessor) guiScreen).setZLevel(0f);
        renderItem.zLevel = 0f;
    }

    @Override
    public @Nullable Object getIngredient() {
        return this.syncHandler.getSlot().getStack();
    }
}
