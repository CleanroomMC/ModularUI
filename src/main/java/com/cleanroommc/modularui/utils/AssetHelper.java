package com.cleanroommc.modularui.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class AssetHelper {

    public static @Nullable IResource findAsset(ResourceLocation resourceLocation) {
        try {
            return Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation);
        } catch (IOException e) {
            return null;
        }
    }

    public static List<IResource> findAssets(String domain, String file) {
        try {
            return Minecraft.getMinecraft().getResourceManager().getAllResources(new ResourceLocation(domain, file));
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public static List<IResource> findAssets(String file) {
        ObjectList<IResource> assets = ObjectList.create();
        for (String domain : Minecraft.getMinecraft().getResourceManager().getResourceDomains()) {
            assets.addAll(findAssets(domain, file));
        }
        return assets;
    }
}
