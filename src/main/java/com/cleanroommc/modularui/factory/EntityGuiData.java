package com.cleanroommc.modularui.factory;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class EntityGuiData extends GuiData {

    private final Entity guiHolder;

    public EntityGuiData(EntityPlayer player, Entity guiHolder) {
        super(player);
        this.guiHolder = guiHolder;
    }

    public Entity getGuiHolder() {
        return guiHolder;
    }
}
