package com.cleanroommc.modularui;

import com.cleanroommc.modularui.common.internal.JsonLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = ModularUI.ID, value = Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void postInit() {
        ((SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(this::onReload);
    }

    public void onReload(IResourceManager manager) {
        ModularUI.LOGGER.info("Reloading GUIs");
        JsonLoader.loadJson();
    }
}
