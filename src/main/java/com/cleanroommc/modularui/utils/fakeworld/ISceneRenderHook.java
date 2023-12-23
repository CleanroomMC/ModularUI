package com.cleanroommc.modularui.utils.fakeworld;

import net.minecraft.util.BlockRenderLayer;

public interface ISceneRenderHook {

    void apply(boolean isTESR, int pass, BlockRenderLayer layer);
}
