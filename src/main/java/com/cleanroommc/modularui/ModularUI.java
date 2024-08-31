package com.cleanroommc.modularui;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.overlay.OverlayHandler;
import com.cleanroommc.modularui.overlay.OverlayManager;
import com.cleanroommc.modularui.screen.CustomModularScreen;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.GuiContext;

import com.cleanroommc.modularui.utils.Color;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@Mod(modid = ModularUI.ID,
        name = ModularUI.NAME,
        version = ModularUI.VERSION,
        dependencies = "required-after:mixinbooter@[8.0,);" +
                "after:bogorter@[1.4.0,);")
public class ModularUI {

    public static final String ID = MuiTags.MODID;
    public static final String NAME = "Modular UI";
    public static final String VERSION = MuiTags.VERSION;
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final String BOGO_SORT = "bogosorter";

    @SidedProxy(
            modId = ID,
            clientSide = "com.cleanroommc.modularui.ClientProxy",
            serverSide = "com.cleanroommc.modularui.CommonProxy")
    public static CommonProxy proxy;

    @Mod.Instance
    public static ModularUI INSTANCE;

    private static boolean blurLoaded = false;
    private static boolean sorterLoaded = false;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        blurLoaded = Loader.isModLoaded("blur");
        sorterLoaded = Loader.isModLoaded(BOGO_SORT);
        proxy.preInit(event);

        OverlayManager.register(new OverlayHandler(screen -> screen instanceof GuiMainMenu, screen -> {
            GuiMainMenu gui = (GuiMainMenu) screen;
            return new CustomModularScreen() {
                @Override
                public @NotNull ModularPanel buildUI(GuiContext context) {
                    return ModularPanel.defaultPanel("overlay").sizeRel(1f)
                            .background(IDrawable.EMPTY)
                            .child(IKey.str("ModularUI")
                                    .scale(5f)
                                    .shadow(true)
                                    .color(Color.WHITE.main)
                                    .asWidget().leftRel(0.5f).topRel(0.07f));
                }
            };
        }));

        OverlayManager.register(new OverlayHandler(screen -> screen instanceof GuiContainer, screen -> {
            GuiContainer gui = (GuiContainer) screen;
            return new CustomModularScreen() {

                @Override
                public @NotNull ModularPanel buildUI(GuiContext context) {
                    return ModularPanel.defaultPanel("watermark_overlay", gui.getXSize(), gui.getYSize())
                            .pos(gui.getGuiLeft(), gui.getGuiTop())
                            .background(IDrawable.EMPTY)
                            .child(GuiTextures.MUI_LOGO.asIcon().asWidget()
                                    .top(5).right(5)
                                    .size(18));
                }

                @Override
                public void onResize(int width, int height) {
                    getMainPanel().pos(gui.getGuiLeft(), gui.getGuiTop())
                            .size(gui.getXSize(), gui.getYSize());
                    super.onResize(width, height);
                }
            };
        }));
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void onLoadComplete(FMLLoadCompleteEvent event) {
    }

    @Mod.EventHandler
    public void onServerLoad(FMLServerStartingEvent event) {
        proxy.onServerLoad(event);
    }

    public static boolean isBlurLoaded() {
        return blurLoaded;
    }

    public static boolean isSortModLoaded() {
        return sorterLoaded;
    }
}
