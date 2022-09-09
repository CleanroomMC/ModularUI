package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.ModularUITextures;
import com.cleanroommc.modularui.api.NumberFormat;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.drawable.TextRenderer;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Color;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.api.widget.*;
import com.cleanroommc.modularui.common.internal.Theme;
import com.cleanroommc.modularui.common.internal.wrapper.BaseSlot;
import com.cleanroommc.modularui.common.internal.wrapper.GhostIngredientWrapper;
import com.cleanroommc.modularui.common.internal.wrapper.ModularGui;
import com.cleanroommc.modularui.core.mixin.GuiContainerMixin;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SlotWidget extends Widget implements IVanillaSlot, Interactable, ISyncedWidget, IIngredientProvider, IGhostIngredientTarget<ItemStack> {

    public static final Size SIZE = new Size(18, 18);

    private final TextRenderer textRenderer = new TextRenderer();
    private final BaseSlot slot;
    private ItemStack lastStoredPhantomItem = ItemStack.EMPTY;
    @Nullable
    private String sortAreaName = null;

    public SlotWidget(BaseSlot slot) {
        this.slot = slot;
    }

    public SlotWidget(IItemHandlerModifiable handler, int index) {
        this(new BaseSlot(handler, index, false));
    }

    public static SlotWidget phantom(IItemHandlerModifiable handler, int index) {
        return new SlotWidget(BaseSlot.phantom(handler, index));
    }

    @Override
    public void onInit() {
        getContext().getContainer().addSlotToContainer(this.slot);
        if (ModularUI.isSortModLoaded() && this.sortAreaName != null) {
            getContext().getContainer().setSlotSortable(this.sortAreaName, this.slot);
        }
        if (getBackground() == null) {
            setBackground(ModularUITextures.ITEM_SLOT);
        }
        if (!isClient() && !this.slot.getStack().isEmpty()) {
            this.lastStoredPhantomItem = this.slot.getStack().copy();
        }
    }

    @Override
    public void onDestroy() {
        getContext().getContainer().removeSlot(this.slot);
    }

    @Override
    public BaseSlot getMcSlot() {
        return this.slot;
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        return SIZE;
    }

    @Override
    public @Nullable String getBackgroundColorKey() {
        return Theme.KEY_ITEM_SLOT;
    }

    @Override
    public void draw(float partialTicks) {
        RenderHelper.enableGUIStandardItemLighting();
        drawSlot(this.slot);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableLighting();
        if (isHovering()) {
            GlStateManager.colorMask(true, true, true, false);
            ModularGui.drawSolidRect(1, 1, 16, 16, Theme.INSTANCE.getSlotHighlight());
            GlStateManager.colorMask(true, true, true, true);
        }
    }

    @Override
    public void onRebuild() {
        Pos2d pos = getAbsolutePos().subtract(getContext().getMainWindow().getPos()).add(1, 1);
        if (this.slot.xPos != pos.x || this.slot.yPos != pos.y) {
            this.slot.xPos = pos.x;
            this.slot.yPos = pos.y;
        }
    }

    @Override
    public void buildTooltip(List<Text> tooltip) {
        if (isPhantom()) {
            tooltip.add(Text.localised("modularui.item.phantom.control"));
        }
    }

    @Override
    public List<String> getExtraTooltip() {
        List<String> extraLines = new ArrayList<>();
        if (slot.getStack().getCount() >= 1000) {
            extraLines.add(I18n.format("modularui.amount", slot.getStack().getCount()));
        }
        if (isPhantom()) {
            String[] lines = I18n.format("modularui.item.phantom.control").split("\\\\n");
            extraLines.addAll(Arrays.asList(lines));
        }
        return extraLines.isEmpty() ? Collections.emptyList() : extraLines;
    }

    public boolean isPhantom() {
        return this.slot.isPhantom();
    }

    @Override
    public Object getIngredient() {
        return slot.getStack();
    }

    @Override
    public SlotWidget setPos(Pos2d relativePos) {
        return (SlotWidget) super.setPos(relativePos);
    }

    @Override
    public SlotWidget setSize(Size size) {
        return (SlotWidget) super.setSize(size);
    }

    public SlotWidget setShiftClickPrio(int prio) {
        this.slot.setShiftClickPriority(prio);
        return this;
    }

    public SlotWidget disableShiftInsert() {
        this.slot.disableShiftInsert();
        return this;
    }

    public SlotWidget setChangeListener(Runnable runnable) {
        this.slot.setChangeListener(runnable);
        return this;
    }

    public SlotWidget setChangeListener(Consumer<SlotWidget> changeListener) {
        return setChangeListener(() -> changeListener.accept(this));
    }

    public SlotWidget setFilter(Predicate<ItemStack> filter) {
        this.slot.setFilter(filter);
        return this;
    }

    public SlotWidget setAccess(boolean canTake, boolean canInsert) {
        this.slot.setAccess(canInsert, canTake);
        return this;
    }

    public SlotWidget setIgnoreStackSizeLimit(boolean ignoreStackSizeLimit) {
        this.slot.setIgnoreStackSizeLimit(ignoreStackSizeLimit);
        return this;
    }

    public SlotWidget setSortable(String areaName) {
        if (this.sortAreaName == null ^ areaName == null) {
            this.sortAreaName = areaName;
        }
        return this;
    }

    @Override
    public SlotWidget setEnabled(boolean enabled) {
        if (enabled != isEnabled()) {
            super.setEnabled(enabled);
            slot.setEnabled(enabled);
            if (isClient()) {
                syncToServer(4, buffer -> buffer.writeBoolean(enabled));
            }
        }
        return this;
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {

    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (id == 1) {
            this.slot.xPos = buf.readVarInt();
            this.slot.yPos = buf.readVarInt();
        } else if (id == 2) {
            phantomClick(ClickData.readPacket(buf));
        } else if (id == 3) {
            phantomScroll(buf.readVarInt());
        } else if (id == 4) {
            setEnabled(buf.readBoolean());
        } else if (id == 5) {
            this.slot.putStack(buf.readItemStack());
        }
    }

    @Override
    public ClickResult onClick(int buttonId, boolean doubleClick) {
        if (isPhantom()) {
            syncToServer(2, buffer -> ClickData.create(buttonId, doubleClick).writeToPacket(buffer));
            return ClickResult.ACCEPT;
        }
        return ClickResult.REJECT;
    }

    @Override
    public boolean onMouseScroll(int direction) {
        if (isPhantom()) {
            if (Interactable.hasShiftDown()) {
                direction *= 8;
            }
            final int finalDirection = direction;
            syncToServer(3, buffer -> buffer.writeVarInt(finalDirection));
            return true;
        }
        return false;
    }

    protected void phantomClick(ClickData clickData) {
        ItemStack cursorStack = getContext().getCursor().getItemStack();
        ItemStack slotStack = getMcSlot().getStack();
        ItemStack stackToPut;
        if (slotStack.isEmpty()) {
            if (cursorStack.isEmpty()) {
                if (clickData.mouseButton == 1 && !this.lastStoredPhantomItem.isEmpty()) {
                    stackToPut = this.lastStoredPhantomItem.copy();
                } else {
                    return;
                }
            } else {
                stackToPut = cursorStack.copy();
            }
            if (clickData.mouseButton == 1) {
                stackToPut.setCount(1);
            }
            slot.putStack(stackToPut);
            this.lastStoredPhantomItem = stackToPut.copy();
        } else {
            if (clickData.mouseButton == 0) {
                if (clickData.shift) {
                    this.slot.putStack(ItemStack.EMPTY);
                } else {
                    this.slot.incrementStackCount(-1);
                }
            } else if (clickData.mouseButton == 1) {
                this.slot.incrementStackCount(1);
            }
        }
    }

    protected void phantomScroll(int direction) {
        ItemStack currentItem = this.slot.getStack();
        if (direction > 0 && currentItem.isEmpty() && !lastStoredPhantomItem.isEmpty()) {
            ItemStack stackToPut = this.lastStoredPhantomItem.copy();
            stackToPut.setCount(direction);
            this.slot.putStack(stackToPut);
        } else {
            this.slot.incrementStackCount(direction);
        }
    }

    @Override
    public IGhostIngredientHandler.@Nullable Target<ItemStack> getTarget(@NotNull Object ingredient) {
        if (!isPhantom() || !(ingredient instanceof ItemStack) || ((ItemStack) ingredient).isEmpty()) {
            return null;
        }
        return new GhostIngredientWrapper<>(this);
    }

    @Override
    public void accept(@NotNull ItemStack ingredient) {
        syncToServer(5, buffer -> buffer.writeItemStack(ingredient));
    }

    private GuiContainerMixin getGuiAccessor() {
        return getContext().getScreen().getAccessor();
    }

    private ModularGui getScreen() {
        return getContext().getScreen();
    }

    /**
     * Copied from {@link net.minecraft.client.gui.inventory.GuiContainer} and removed the bad parts
     */
    @SideOnly(Side.CLIENT)
    private void drawSlot(Slot slotIn) {
        int x = slotIn.xPos;
        int y = slotIn.yPos;
        ItemStack itemstack = slotIn.getStack();
        boolean flag = false;
        boolean flag1 = slotIn == getGuiAccessor().getClickedSlot() && !getGuiAccessor().getDraggedStack().isEmpty() && !getGuiAccessor().getIsRightMouseClick();
        ItemStack itemstack1 = getScreen().mc.player.inventory.getItemStack();
        int amount = -1;
        String format = null;

        if (slotIn == this.getGuiAccessor().getClickedSlot() && !getGuiAccessor().getDraggedStack().isEmpty() && getGuiAccessor().getIsRightMouseClick() && !itemstack.isEmpty()) {
            itemstack = itemstack.copy();
            itemstack.setCount(itemstack.getCount() / 2);
        } else if (getScreen().isDragSplitting() && getScreen().getDragSlots().contains(slotIn) && !itemstack1.isEmpty()) {
            if (getScreen().getDragSlots().size() == 1) {
                return;
            }

            if (Container.canAddItemToSlot(slotIn, itemstack1, true) && getScreen().inventorySlots.canDragIntoSlot(slotIn)) {
                itemstack = itemstack1.copy();
                flag = true;
                Container.computeStackSize(getScreen().getDragSlots(), getGuiAccessor().getDragSplittingLimit(), itemstack, slotIn.getStack().isEmpty() ? 0 : slotIn.getStack().getCount());
                int k = Math.min(itemstack.getMaxStackSize(), slotIn.getItemStackLimit(itemstack));

                if (itemstack.getCount() > k) {
                    amount = k;
                    format = TextFormatting.YELLOW.toString();
                    itemstack.setCount(k);
                }
            } else {
                getScreen().getDragSlots().remove(slotIn);
                getGuiAccessor().invokeUpdateDragSplitting();
            }
        }

        getScreen().setZ(100f);
        getScreen().getItemRenderer().zLevel = 100.0F;

        if (!flag1) {
            if (flag) {
                ModularGui.drawSolidRect(1, 1, 16, 16, -2130706433);
            }

            if (!itemstack.isEmpty()) {
                GlStateManager.enableDepth();
                // render the item itself
                getScreen().getItemRenderer().renderItemAndEffectIntoGUI(getScreen().mc.player, itemstack, 1, 1);
                if (amount < 0) {
                    amount = itemstack.getCount();
                }
                // render the amount overlay
                if (amount > 1 || format != null) {
                    String amountText = NumberFormat.format(amount, 2);
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
                    textRenderer.setColor(Color.WHITE.normal);
                    textRenderer.setAlignment(Alignment.BottomRight, size.width - 1, size.height - 1);
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
                getScreen().getItemRenderer().renderItemOverlayIntoGUI(getScreen().getFontRenderer(), itemstack, 1, 1, null);
                itemstack.setCount(cachedCount);
                GlStateManager.disableDepth();
            }
        }

        getScreen().getItemRenderer().zLevel = 0.0F;
        getScreen().setZ(0f);
    }
}
