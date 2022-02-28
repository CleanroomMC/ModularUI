package com.cleanroommc.modularui.common.internal;

import com.cleanroommc.modularui.api.IWindowCreator;
import com.google.common.collect.ImmutableBiMap;
import net.minecraft.entity.player.EntityPlayer;

public class UIBuildContext {

    protected final ImmutableBiMap.Builder<Byte, IWindowCreator> windowMap = ImmutableBiMap.builder();
    protected final EntityPlayer player;

    public UIBuildContext(EntityPlayer player) {
        this.player = player;
    }

    public UIBuildContext registerSyncedWindow(byte id, IWindowCreator creator) {
        this.windowMap.put(id, creator);
        return this;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

}
