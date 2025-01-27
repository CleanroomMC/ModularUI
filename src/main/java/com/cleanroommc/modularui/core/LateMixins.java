package com.cleanroommc.modularui.core;

import com.google.common.collect.ImmutableList;

import net.minecraftforge.fml.common.Loader;

import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.List;
import java.util.stream.Collectors;

public class LateMixins implements ILateMixinLoader {

    public static final List<String> modMixins = ImmutableList.of("jei");

    @Override
    public List<String> getMixinConfigs() {
        return modMixins.stream().map(mod -> "mixin.modularui." + mod + ".json").collect(Collectors.toList());
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        String[] parts = mixinConfig.split("\\.");
        return parts.length != 4 || shouldEnableModMixin(parts[2]);
    }

    public boolean shouldEnableModMixin(String mod) {
        return Loader.isModLoaded(mod);
    }
}
