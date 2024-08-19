package com.cleanroommc.modularui.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AssetHelper {

    public static Optional<Resource> findAsset(ResourceLocation resourceLocation) {
            return Minecraft.getInstance().getResourceManager().getResource(resourceLocation);
    }

    public static List<Resource> findAssets(String domain, String file) {
        return Minecraft.getInstance().getResourceManager().getResourceStack(new ResourceLocation(domain, file));
    }

    public static List<Resource> findAssets(String file) {
        ObjectList<Resource> assets = ObjectList.create();
        for (String domain : Minecraft.getInstance().getResourceManager().getNamespaces()) {
            assets.addAll(findAssets(domain, file));
        }
        return assets;
    }
}
