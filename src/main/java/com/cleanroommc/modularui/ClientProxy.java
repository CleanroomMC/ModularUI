package com.cleanroommc.modularui;

import com.cleanroommc.modularui.animation.AnimatorManager;
import com.cleanroommc.modularui.drawable.DrawableSerialization;
import com.cleanroommc.modularui.holoui.HoloScreenEntity;
import com.cleanroommc.modularui.holoui.ScreenEntityRender;
import com.cleanroommc.modularui.keybind.KeyBindHandler;
import com.cleanroommc.modularui.overlay.OverlayManager;
import com.cleanroommc.modularui.screen.ClientScreenHandler;
import com.cleanroommc.modularui.test.EventHandler;
import com.cleanroommc.modularui.test.OverlayTest;
import com.cleanroommc.modularui.theme.ThemeManager;
import com.cleanroommc.modularui.theme.ThemeReloadCommand;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.util.Timer;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

    private final Timer timer60Fps = new Timer(60f);

    @Override
    void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        MinecraftForge.EVENT_BUS.register(ClientScreenHandler.class);
        MinecraftForge.EVENT_BUS.register(KeyBindHandler.class);
        AnimatorManager.init();

        if (ModularUIConfig.enableTestGuis) {
            MinecraftForge.EVENT_BUS.register(EventHandler.class);
        }
        if (ModularUIConfig.enableTestOverlays) {
            OverlayTest.init();
        }

        DrawableSerialization.init();
        RenderingRegistry.registerEntityRenderingHandler(HoloScreenEntity.class, ScreenEntityRender::new);

        // enable stencil buffer
        if (!Minecraft.getMinecraft().getFramebuffer().isStencilEnabled()) {
            Minecraft.getMinecraft().getFramebuffer().enableStencil();
        }
    }

    @Override
    void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);

        ClientCommandHandler.instance.registerCommand(new ThemeReloadCommand());
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new ThemeManager());
    }

    @Override
    public Timer getTimer60Fps() {
        return this.timer60Fps;
    }
}
