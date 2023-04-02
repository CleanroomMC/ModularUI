package com.cleanroommc.modularui.terminal.guide;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.io.File;
import java.util.Map;

public class GuideManager {

    private static final Map<ResourceLocation, File> guides = new Object2ObjectOpenHashMap<>();

    public static void load() {
        guides.clear();
        for (ModContainer container : Loader.instance().getIndexedModList().values()) {
            String basePath = String.format("assets/%s/guides/%s", container.getModId(), Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode());
            CraftingHelper.findFiles(container, basePath, null, (root, path) -> {
                if (!path.toString().endsWith(".txt")) return true;
                String file = root.relativize(path).toString();
                guides.put(new ResourceLocation(container.getModId(), file), path.toFile());
                return true;
            }, true, true);
        }
    }
}
