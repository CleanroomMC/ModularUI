package com.cleanroommc.modularui.core;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

@IFMLLoadingPlugin.Name("ModularUI-Core")
@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
@IFMLLoadingPlugin.SortingIndex(2000) // after ae2uel and stackup
public class ModularUICore implements IFMLLoadingPlugin, IEarlyMixinLoader {

    public static final Logger LOGGER = LogManager.getLogger("modularui");
    protected static boolean ae2Loaded = false, stackUpLoaded = false;

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{"com.cleanroommc.modularui.core.ClassTransformer"};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        // this checks needs to happen this early
        // randompatches would otherwise crash shortly after this
        if (classExists("com.therandomlabs.randompatches.core.RPCore")) {
            throw new IllegalStateException("ModularUI is incompatible with RandomPatches. Please remove RandomPatches or replace it with UniversalTweaks as most features are replaced by it.");
        }
        for (IClassTransformer transformer : Launch.classLoader.getTransformers()) {
            String name = transformer.getClass().getName();
            if (name.endsWith("appeng.core.transformer.AE2ELTransformer")) {
                ae2Loaded = true;
            } else if (name.endsWith("pl.asie.stackup.core.StackUpTransformer")) {
                stackUpLoaded = true;
            }
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList("mixin.modularui.json");
    }

    private static boolean classExists(String className) {
        try {
            Class.forName(className, false, Launch.classLoader);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
