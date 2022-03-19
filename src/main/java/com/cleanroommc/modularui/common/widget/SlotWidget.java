package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.ISyncedWidget;
import com.cleanroommc.modularui.api.IVanillaSlot;
import com.cleanroommc.modularui.api.Interactable;
import com.cleanroommc.modularui.api.drawable.UITexture;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.common.internal.mixin.GuiContainerMixin;
import com.cleanroommc.modularui.common.internal.wrapper.ModularGui;
import com.cleanroommc.modularui.integration.vanilla.slot.BaseSlot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.items.IItemHandler;

public class SlotWidget extends Widget implements IVanillaSlot, Interactable, ISyncedWidget {

    public static final Size SIZE = new Size(18, 18);
    public static final UITexture TEXTURE = UITexture.fullImage("modularui", "gui/slot/item");

    private final BaseSlot slot;

    public SlotWidget(BaseSlot slot) {
        this.slot = slot;
    }

    public SlotWidget(IItemHandler handler, int index) {
        this(handler, index, false);
    }

    public SlotWidget(IItemHandler handler, int index, boolean output) {
        this(new BaseSlot(handler, index, output, false));
    }

    public static SlotWidget phantom(IItemHandler handler, int index) {
        return phantom(handler, index, false);
    }

    public static SlotWidget phantom(IItemHandler handler, int index, boolean output) {
        return new SlotWidget(BaseSlot.phantom(handler, index, output));
    }

    @Override
    public void onInit() {
        getContext().getContainer().addSlotToContainer(slot);
        if (getDrawable() == null) {
            setBackground(TEXTURE);
        }
    }

    @Override
    public Slot getMcSlot() {
        return slot;
    }

    @Override
    protected Size getDefaultSize() {
        return SIZE;
    }

    @Override
    public void drawInBackground(float partialTicks) {
        RenderHelper.enableGUIStandardItemLighting();
        drawSlot(slot);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableLighting();
        if (isHovering()) {
            GlStateManager.colorMask(true, true, true, false);
            ModularGui.drawSolidRect(1, 1, 16, 16, 0x80ffffff);
            GlStateManager.colorMask(true, true, true, true);
        }
    }

    @Override
    public void onRebuild() {
        Pos2d pos = getAbsolutePos().subtract(getWindow().getPos()).add(1, 1);
        if (slot.xPos != pos.x || slot.yPos != pos.y) {
            slot.xPos = pos.x;
            slot.yPos = pos.y;
            // widgets are only rebuild on client and mc requires the slot pos on server
            syncToServer(1, buffer -> {
                buffer.writeVarInt(pos.x);
                buffer.writeVarInt(pos.y);
            });
        }
    }

    public boolean isPhantom() {
        return slot.isPhantom();
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
        slot.setShiftClickPriority(prio);
        return this;
    }

    public SlotWidget setChangeListener(Runnable runnable) {
        slot.setChangeListener(runnable);
        return this;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        slot.setEnabled(enabled);
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {

    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        if (id == 1) {
            slot.xPos = buf.readVarInt();
            slot.yPos = buf.readVarInt();
        } else if (id == 2) {
            phantomClick(ClickData.readPacket(buf));
        } else if (id == 3) {
            phantomScroll(buf.readVarInt());
        }
    }

    @Override
    public boolean onClick(int buttonId, boolean doubleClick) {
        if (isPhantom()) {
            syncToServer(2, buffer -> ClickData.create(buttonId, doubleClick).writeToPacket(buffer));
            return true;
        }
        return false;
    }

    @Override
    public void onMouseScroll(int direction) {
        if (isPhantom()) {
            syncToServer(3, buffer -> buffer.writeVarInt(direction));
        }
    }

    protected void phantomClick(ClickData clickData) {
        ItemStack cursorStack = getContext().getCursorStack();
        ItemStack slotStack = getMcSlot().getStack();
        if (slotStack.isEmpty()) {
            if (cursorStack.isEmpty()) {
                return;
            }
            ItemStack stackToPut = cursorStack.copy();
            if (clickData.mouseButton == 1) {
                stackToPut.setCount(1);
            }
            slot.putStack(stackToPut);
        } else {
            if (clickData.mouseButton == 0) {
                if (clickData.shift) {
                    slot.putStack(ItemStack.EMPTY);
                } else {
                    slot.incrementStackCount(-1);
                }
            } else if (clickData.mouseButton == 1) {
                slot.incrementStackCount(1);
            }
        }
    }

    protected void phantomScroll(int direction) {
        slot.incrementStackCount(direction);
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
    private void drawSlot(Slot slotIn) {
        int x = slotIn.xPos;
        int y = slotIn.yPos;
        ItemStack itemstack = slotIn.getStack();
        boolean flag = false;
        boolean flag1 = slotIn == getGuiAccessor().getClickedSlot() && !getGuiAccessor().getDraggedStack().isEmpty() && !getGuiAccessor().getIsRightMouseClick();
        ItemStack itemstack1 = getScreen().mc.player.inventory.getItemStack();
        String s = null;

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
                    s = TextFormatting.YELLOW.toString() + k;
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
                getScreen().getItemRenderer().renderItemAndEffectIntoGUI(getScreen().mc.player, itemstack, 1, 1);
                getScreen().getItemRenderer().renderItemOverlayIntoGUI(getScreen().getFontRenderer(), itemstack, 1, 1, s);
                GlStateManager.disableDepth();
            }
        }

        getScreen().getItemRenderer().zLevel = 0.0F;
        getScreen().setZ(0f);
    }
}
