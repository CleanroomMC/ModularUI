package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.ModularUIConfig;

import com.cleanroommc.modularui.api.drawable.IKey;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import org.jetbrains.annotations.NotNull;
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

    public static @NotNull List<String> getItemToolTip(@NotNull ItemStack item) {
        if (!hasMc()) return Collections.emptyList();
        if (getMc().currentScreen != null) {
            List<String> tooltips = getMc().currentScreen.getItemToolTip(item);
            if (!ModularUI.Mods.MODNAMETOOLTIP.isLoaded()) {
                tooltips.add(ModularUIConfig.modNameFormat + getItemModName(item) + "§r");
            }

            return tooltips;
        }

        List<String> tooltips = item.getTooltip(getPlayer(), getMc().gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
        for (int i = 0; i < tooltips.size(); ++i) {
            if (i == 0) {
                tooltips.set(i, item.getItem().getForgeRarity(item).getColor() + tooltips.get(i));
            } else {
                tooltips.set(i, TextFormatting.GRAY + tooltips.get(i));
            }
        }

        if (!ModularUI.Mods.MODNAMETOOLTIP.isLoaded()) {
            tooltips.add(ModularUIConfig.modNameFormat + getItemModName(item) + "§r");
        }

        return tooltips;
    }

    public static @Nullable String getItemModName(@NotNull ItemStack item) {
        ModContainer modContainer = Loader.instance().getIndexedModList().get(item.getItem().getCreatorModId(item));
        return modContainer == null ? null : modContainer.getName();
    }

    public static @NotNull IKey getFluidModNameKey(@NotNull FluidStack fluidStack) {
        return IKey.str(getFluidModName(fluidStack));
    }

    public static @NotNull String getFluidModName(@NotNull FluidStack fluid) {
        ModContainer modContainer = Loader.instance().getIndexedModList().get(getFluidModID(fluid.getFluid()));
        if (modContainer == null) throw new IllegalStateException(
                "Tried to get the mod name of a fluid that isn't registered to the Forge FluidRegistry");
        return "§9§o" + modContainer.getName() + "§r";
    }

    public static @NotNull String getFluidModID(@NotNull Fluid fluid) {
        String fluidModName = FluidRegistry.getDefaultFluidName(fluid);
        return fluidModName.substring(0, fluidModName.indexOf(":"));
    }
}
