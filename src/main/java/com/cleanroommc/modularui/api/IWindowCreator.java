package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.common.internal.ModularWindow;
import net.minecraft.entity.player.EntityPlayer;

public interface IWindowCreator {

    ModularWindow create(EntityPlayer player);

}
