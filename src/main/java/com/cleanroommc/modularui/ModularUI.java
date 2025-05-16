package com.cleanroommc.modularui;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.mariuszgromada.math.mxparser.License;

import java.util.function.Predicate;

@Mod(modid = ModularUI.ID,
        name = ModularUI.NAME,
        version = ModularUI.VERSION,
        acceptedMinecraftVersions = "[1.12,)",
        dependencies = "required-after:mixinbooter@[8.0,);" +
                "after:bogorter@[1.4.0,);" +
                "after-client:neverenoughanimations@[1.0.6,)")
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

    static {
        // confirm mXparser license
        License.iConfirmNonCommercialUse("CleanroomMC");
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void onServerLoad(FMLServerStartingEvent event) {
        proxy.onServerLoad(event);
    }

    public enum Mods {

        BLUR(ModIds.BLUR),
        BOGOSORTER(ModIds.BOGOSORTER),
        JEI(ModIds.JEI),
        NEA(ModIds.NEA);

        public final String id;
        private boolean loaded = false;
        private boolean initialized = false;
        private final Predicate<ModContainer> extraLoadedCheck;

        Mods(String id) {
            this(id, null);
        }

        Mods(String id, @Nullable Predicate<ModContainer> extraLoadedCheck) {
            this.id = id;
            this.extraLoadedCheck = extraLoadedCheck;
        }

        public boolean isLoaded() {
            if (!this.initialized) {
                this.loaded = Loader.isModLoaded(this.id);
                if (this.loaded && this.extraLoadedCheck != null) {
                    this.loaded = this.extraLoadedCheck.test(Loader.instance().getIndexedModList().get(this.id));
                }
                this.initialized = true;
            }
            return this.loaded;
        }
    }

    public static class ModIds {

        public static final String BLUR = "blur";
        public static final String BOGOSORTER = "bogosorter";
        public static final String JEI = "jei";
        public static final String NEA = "neverenoughanimations";
    }
}
