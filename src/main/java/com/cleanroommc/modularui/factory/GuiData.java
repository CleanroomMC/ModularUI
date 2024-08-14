package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.JeiSettings;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.JeiSettingsImpl;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class GuiData {

    private final Player player;
    private JeiSettings jeiSettings;

    public GuiData(Player player) {
        this.player = Objects.requireNonNull(player);
    }

    public Player getPlayer() {
        return this.player;
    }

    public boolean isClient() {
        return NetworkUtils.isClient(this.player);
    }

    public ItemStack getMainHandItem() {
        return this.player.getMainHandItem();
    }

    public ItemStack getOffHandItem() {
        return this.player.getOffhandItem();
    }

    public JeiSettings getJeiSettings() {
        if (this.jeiSettings == null) {
            throw new IllegalStateException("Not yet initialised!");
        }
        return this.jeiSettings;
    }

    final JeiSettingsImpl getJeiSettingsImpl() {
        return (JeiSettingsImpl) this.jeiSettings;
    }

    final void setJeiSettings(JeiSettings jeiSettings) {
        this.jeiSettings = Objects.requireNonNull(jeiSettings);
    }
}
