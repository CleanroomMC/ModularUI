package com.cleanroommc.modularui.widgets;

import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerTooltipHandler;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.sync.SyncHandler;
import com.cleanroommc.modularui.api.widget.IVanillaSlot;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.TextRenderer;
import com.cleanroommc.modularui.mixins.GuiContainerAccessor;
import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.sync.ItemSlotSH;
import com.cleanroommc.modularui.theme.WidgetSlotTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.ClickData;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.NumberFormat;
import com.cleanroommc.modularui.widget.Widget;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.List;

public class ItemSlot extends Widget<ItemSlot> implements IVanillaSlot, Interactable {

    private static final TextRenderer textRenderer = new TextRenderer();
    private ItemSlotSH syncHandler;

    public ItemSlot() {
        tooltipBuilder(tooltip -> {
            tooltip.setUpdateTooltipEveryTick(true).setHasSpaceAfterFirstLine(true);
            tooltip.excludeArea(getArea());
            if (!isSynced()) return;
            ItemStack stack = getSlot().getStack();
            if (stack == null) return;
            tooltip.addStringLines(getItemTooltip(stack));
        });
    }

    @Override
    public void onInit() {
        size(18, 18);
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        if (syncHandler instanceof ItemSlotSH) {
            this.syncHandler = (ItemSlotSH) syncHandler;
            return true;
        }
        return false;
    }

    @Override
    public void draw(GuiContext context) {
        if (this.syncHandler == null) return;
        RenderHelper.enableGUIStandardItemLighting();
        drawSlot(getSlot());
        RenderHelper.disableStandardItemLighting();
        if (isHovering()) {
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glColorMask(true, true, true, false);
            GuiDraw.drawSolidRect(1, 1, 16, 16, getWidgetTheme(context.getTheme()).getSlotHoverColor());
            GL11.glColorMask(true, true, true, true);
            GL11.glDisable(GL11.GL_BLEND);
        }
    }

    @Override
    public WidgetSlotTheme getWidgetTheme(ITheme theme) {
        return theme.getItemSlotTheme();
    }

    @Override
    public boolean canHover() {
        return true;
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (this.syncHandler.isPhantom()) {
            ClickData clickData = ClickData.create(mouseButton);
            this.syncHandler.syncToServer(2, clickData::writeToPacket);
        } else {
            getScreen().getScreenWrapper().clickSlot();
        }
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        getScreen().getScreenWrapper().releaseSlot();
        return true;
    }

    @Override
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        if (this.syncHandler.isPhantom()) {
            ClickData clickData = ClickData.create(scrollDirection.modifier);
            this.syncHandler.syncToServer(3, clickData::writeToPacket);
            return true;
        }
        return false;
    }

    @Override
    public void onMouseDrag(int mouseButton, long timeSinceClick) {
        getScreen().getScreenWrapper().dragSlot(timeSinceClick);
    }

    public Slot getSlot() {
        return syncHandler.getSlot();
    }

    @Override
    public Slot getVanillaSlot() {
        return syncHandler.getSlot();
    }

    @Override
    public boolean isSynced() {
        return syncHandler != null;
    }

    @Override
    public ItemSlotSH getSyncHandler() {
        if (this.syncHandler == null) {
            throw new IllegalStateException("Widget is not initialised!");
        }
        return syncHandler;
    }

    protected List<String> getItemTooltip(ItemStack stack) {
        List<String> tooltips = GuiContainerManager.itemDisplayNameMultiline(stack, getScreen().getScreenWrapper(), true);

        GuiContainerManager.applyItemCountDetails(tooltips, stack);

        if (GuiContainerManager.getManager() != null && GuiContainerManager.shouldShowTooltip(getScreen().getScreenWrapper())) {
            for (IContainerTooltipHandler handler : GuiContainerManager.getManager().instanceTooltipHandlers)
                tooltips = handler.handleItemTooltip(getScreen().getScreenWrapper(), stack, getContext().getMouseX(), getContext().getMouseY(), tooltips);
        }

        return tooltips;
    }

    @SideOnly(Side.CLIENT)
    private void drawSlot(Slot slotIn) {
        GuiScreenWrapper guiScreen = getScreen().getScreenWrapper();
        GuiContainerAccessor accessor = guiScreen.getAccessor();
        int x = slotIn.xDisplayPosition;
        int y = slotIn.yDisplayPosition;
        ItemStack itemstack = slotIn.getStack();
        boolean flag = false;
        boolean flag1 = slotIn == accessor.getClickedSlot() && accessor.getDraggedStack() != null && !accessor.getIsRightMouseClick();
        ItemStack itemstack1 = guiScreen.mc.thePlayer.inventory.getItemStack();
        int amount = -1;
        String format = null;

        if (slotIn == accessor.getClickedSlot() && accessor.getDraggedStack() != null && accessor.getIsRightMouseClick() && itemstack != null) {
            itemstack = itemstack.copy();
            itemstack.stackSize = itemstack.stackSize / 2;
        } else if (guiScreen.isDragSplitting() && guiScreen.getDragSlots().contains(slotIn) && itemstack1 != null) {
            if (guiScreen.getDragSlots().size() == 1) {
                return;
            }

            // canAddItemToSlot
            if (Container.func_94527_a(slotIn, itemstack1, true) && guiScreen.inventorySlots.canDragIntoSlot(slotIn)) {
                itemstack = itemstack1.copy();
                flag = true;
                // computeStackSize
                Container.func_94525_a(guiScreen.getDragSlots(), accessor.getDragSplittingLimit(), itemstack, slotIn.getStack() == null ? 0 : slotIn.getStack().stackSize);
                int k = Math.min(itemstack.getMaxStackSize(), slotIn.getSlotStackLimit());

                if (itemstack.stackSize > k) {
                    amount = k;
                    format = EnumChatFormatting.YELLOW.toString();
                    itemstack.stackSize = k;
                }
            } else {
                guiScreen.getDragSlots().remove(slotIn);
                accessor.invokeUpdateDragSplitting();
            }
        }

        guiScreen.setZ(100f);
        GuiScreenWrapper.getItemRenderer().zLevel = 100.0F;

        if (!flag1) {
            if (flag) {
                GuiDraw.drawSolidRect(1, 1, 16, 16, -2130706433);
            }

            if (itemstack != null) {
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                GL11.glEnable(GL11.GL_LIGHTING);
                RenderHelper.enableGUIStandardItemLighting();
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                // render the item itself
                GuiScreenWrapper.getItemRenderer().renderItemAndEffectIntoGUI(Minecraft.getMinecraft().fontRenderer, Minecraft.getMinecraft().getTextureManager(), itemstack, 1, 1);
                GuiDraw.afterRenderItemAndEffectIntoGUI(itemstack);
                GL11.glDisable(GL12.GL_RESCALE_NORMAL);
                if (amount < 0) {
                    amount = itemstack.stackSize;
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
                    textRenderer.setAlignment(Alignment.BottomRight, getArea().width - 1, getArea().height - 1);
                    textRenderer.setPos(1, 1);
                    GL11.glDisable(GL11.GL_LIGHTING);
                    GL11.glDisable(GL11.GL_DEPTH_TEST);
                    GL11.glDisable(GL11.GL_BLEND);
                    textRenderer.draw(amountText);
                    GL11.glEnable(GL11.GL_LIGHTING);
                    GL11.glEnable(GL11.GL_DEPTH_TEST);
                    GL11.glEnable(GL11.GL_BLEND);
                }

                int cachedCount = itemstack.stackSize;
                itemstack.stackSize = 1; // required to not render the amount overlay
                // render other overlays like durability bar
                GuiScreenWrapper.getItemRenderer().renderItemOverlayIntoGUI(guiScreen.getFontRenderer(), Minecraft.getMinecraft().getTextureManager(), itemstack, 1, 1, null);
                itemstack.stackSize = cachedCount;
                GL11.glDisable(GL11.GL_DEPTH_TEST);
            }
        }

        GuiScreenWrapper.getItemRenderer().zLevel = 0.0F;
        guiScreen.setZ(0f);
    }
}
