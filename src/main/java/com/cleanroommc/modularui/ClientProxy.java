package com.cleanroommc.modularui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = ModularUIMod.ID, value = Side.CLIENT)
public class ClientProxy extends CommonProxy {

    public static final Map<String, JsonObject> GUIS = new HashMap<>();

    @Override
    public void postInit() {
        ((SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(this::onReload);
    }

    public void onReload(IResourceManager manager) {
        ModularUIMod.LOGGER.info("Reloading GUIs");
        GUIS.clear();
        JsonParser parser = new JsonParser();
        try {
            for (IResource resource : manager.getAllResources(new ResourceLocation(ModularUIMod.ID, "guis/test.json"))) {
                JsonElement json = parser.parse(new InputStreamReader(resource.getInputStream()));
                if(!json.isJsonObject()) {
                    ModularUIMod.LOGGER.error("Invalid JSON syntax");
                    continue;
                }
                String location = resource.getResourceLocation().getPath();
                location = location.substring(location.indexOf("guis/") + 5, location.length() - 5);
                ModularUIMod.LOGGER.info("Found GUI {}", location);
                GUIS.put(location, json.getAsJsonObject());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
