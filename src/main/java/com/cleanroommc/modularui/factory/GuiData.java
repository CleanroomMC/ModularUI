package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.JeiSettings;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.JeiSettingsImpl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.Objects;

public class GuiData {

    private final EntityPlayer player;
    private JeiSettings jeiSettings;

    public GuiData(EntityPlayer player) {
        this.player = Objects.requireNonNull(player);
    }

    public EntityPlayer getPlayer() {
        return this.player;
    }

    public boolean isClient() {
        return NetworkUtils.isClient(this.player);
    }

    public ItemStack getMainHandItem() {
        return this.player.getHeldItemMainhand();
    }

    public ItemStack getOffHandItem() {
        return this.player.getHeldItemOffhand();
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
