package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.core.mixin.GuiAccessor;
import com.cleanroommc.modularui.core.mixin.GuiScreenAccessor;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.neverenoughanimations.NEA;
import com.cleanroommc.neverenoughanimations.NEAConfig;
import com.cleanroommc.neverenoughanimations.animations.ItemHoverAnimation;
import com.cleanroommc.neverenoughanimations.animations.ItemMoveAnimation;
import com.cleanroommc.neverenoughanimations.animations.ItemMovePacket;
import com.cleanroommc.neverenoughanimations.animations.ItemPickupThrowAnimation;
import com.cleanroommc.neverenoughanimations.api.IItemLocation;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class NEAAnimationHandler {

    public static boolean shouldHandleNEA(ModularContainer container) {
        return ModularUI.Mods.NEA.isLoaded() && NetworkUtils.isClient(container.getPlayer());
    }

    public static ItemStack injectQuickMove(ModularContainer container, EntityPlayer player, int slotId, Slot slot) {
        if (slot == null || !slot.getHasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack oldStack = slot.getStack().copy();
        Pair<List<Slot>, List<ItemStack>> candidates = ItemMoveAnimation.getCandidates(slot, container.inventorySlots);
        ItemStack returnable = container.handleQuickMove(player, slotId, slot);
        if (candidates != null) ItemMoveAnimation.handleMove(slot, oldStack, candidates);
        return returnable;
    }

    public static ItemStack pickupAllPre(ModularContainer container) {
        if (shouldHandleNEA(container) && NEAConfig.moveAnimationTime > 0) {
            return container.getPlayer().inventory.getItemStack().copy();
        }
        return null;
    }

    public static void pickupAllMid(ModularContainer container, ItemStack instance, int quantity, Int2ObjectArrayMap<Object> packets, Slot slot) {
        if (shouldHandleNEA(container) && NEAConfig.moveAnimationTime > 0) {
            // handle animation
            IItemLocation source = IItemLocation.of(slot);
            ItemStack movingStack = instance.copy();
            movingStack.setCount(quantity);
            packets.put(source.nea$getSlotNumber(), new ItemMovePacket(NEA.time(), source, IItemLocation.CURSOR, movingStack));
        }
        // do the redirected action
        instance.grow(quantity);
    }

    public static void pickupAllPost(ModularContainer container, Int2ObjectArrayMap<Object> packets, ItemStack cursor) {
        if (shouldHandleNEA(container) && NEAConfig.moveAnimationTime > 0 && !packets.isEmpty()) {
            for (var iterator = packets.int2ObjectEntrySet().fastIterator(); iterator.hasNext(); ) {
                var e = iterator.next();
                ItemMoveAnimation.queueAnimation(e.getIntKey(), (ItemMovePacket) e.getValue());
                ItemMoveAnimation.updateVirtualStack(-1, cursor, 1);
            }
        }
    }

    public static ItemStack injectVirtualStack(GuiContainer guiContainer, ModularSlot slot) {
        if (!slot.isPhantom() && ModularUI.Mods.NEA.isLoaded() && NEAConfig.moveAnimationTime > 0) {
            return ItemMoveAnimation.getVirtualStack(guiContainer, slot);
        }
        return null;
    }

    public static float injectHoverScale(GuiContainer guiContainer, ModularSlot slot) {
        if (ModularUI.Mods.NEA.isLoaded() && NEAConfig.hoverAnimationTime > 0) {
            GlStateManager.pushMatrix();
            float scale = ItemHoverAnimation.getRenderScale(guiContainer, slot);
            if (scale > 1f) {
                int x = 8;
                int y = 8;
                GlStateManager.translate(x, y, 0);
                GlStateManager.scale(scale, scale, 1);
                GlStateManager.translate(-x, -y, 0);
                return scale;
            }
        }
        return 1f;
    }

    public static void endHoverScale() {
        if (ModularUI.Mods.NEA.isLoaded() && NEAConfig.hoverAnimationTime > 0) {
            GlStateManager.popMatrix();
        }
    }

    public static void drawItemAnimation(GuiContainer container) {
        if (ModularUI.Mods.NEA.isLoaded()) {
            RenderItem itemRender = ((GuiScreenAccessor) container).getItemRender();
            FontRenderer fontRenderer = ((GuiScreenAccessor) container).getFontRenderer();
            ((GuiAccessor) container).setZLevel(200f);
            itemRender.zLevel = 200f;
            ItemPickupThrowAnimation.drawIndependentAnimations(container, itemRender, fontRenderer);
            ItemMoveAnimation.drawAnimations(itemRender, fontRenderer);
            itemRender.zLevel = 0f;
            ((GuiAccessor) container).setZLevel(0f);
        }
    }

    public static ItemStack injectVirtualCursorStack(GuiContainer container, ItemStack stack) {
        if (ModularUI.Mods.NEA.isLoaded() && NEAConfig.moveAnimationTime > 0) {
            ItemStack virtual = ItemMoveAnimation.getVirtualStack(container, IItemLocation.CURSOR);
            return virtual == null ? stack : virtual;
        }
        return stack;
    }
}
