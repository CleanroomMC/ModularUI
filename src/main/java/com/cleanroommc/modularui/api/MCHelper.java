package com.cleanroommc.modularui.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class MCHelper {

    public static boolean hasMc() {
        return getMc() != null;
    }

    public static @Nullable Minecraft getMc() {
        return Minecraft.getMinecraft();
    }

    public static @Nullable EntityPlayerSP getPlayer() {
        if (hasMc()) {
            return getMc().player;
        }
        return null;
    }

    public static boolean closeScreen() {
        if (!hasMc()) return false;
        EntityPlayerSP player = getPlayer();
        if (player != null) {
            player.closeScreen();
            return true;
        }
        Minecraft.getMinecraft().displayGuiScreen(null);
        return false;
    }

    public static boolean displayScreen(GuiScreen screen) {
        Minecraft mc = getMc();
        if (mc != null) {
            mc.displayGuiScreen(screen);
            return true;
        }
        return false;
    }

    public static GuiScreen getCurrentScreen() {
        Minecraft mc = getMc();
        return mc != null ? mc.currentScreen : null;
    }

    public static FontRenderer getFontRenderer() {
        if (hasMc()) return getMc().fontRenderer;
        return null;
    }

    public static List<String> getItemToolTip(ItemStack item) {
        if (!hasMc()) return Collections.emptyList();
        if (getMc().currentScreen != null) return getMc().currentScreen.getItemToolTip(item);
        List<String> list = item.getTooltip(getPlayer(), getMc().gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
        for (int i = 0; i < list.size(); ++i) {
            if (i == 0) {
                list.set(i, item.getItem().getForgeRarity(item).getColor() + list.get(i));
            } else {
                list.set(i, TextFormatting.GRAY + list.get(i));
            }
        }
        return list;
    }
}
