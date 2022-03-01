package com.cleanroommc.modularui.common.internal;

import net.minecraft.entity.player.EntityPlayer;

public class UIBuildContext {

    protected final EntityPlayer player;

    public UIBuildContext(EntityPlayer player) {
        this.player = player;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

}
