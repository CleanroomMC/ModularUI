package com.cleanroommc.modularui;

import com.cleanroommc.modularui.common.widget.WidgetRegistry;
import com.cleanroommc.modularui.test.Tests;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = ModularUIMod.ID, name = ModularUIMod.NAME, version = ModularUIMod.VERSION)
public class ModularUIMod {

	public static final String ID = "modularui";
	public static final String NAME = "Modular UI";
	public static final String VERSION = "1.0";
	public static final Logger LOGGER = LogManager.getLogger(ID);

	@Mod.Instance
	public static ModularUIMod INSTANCE;

	@SidedProxy(modId = ID, clientSide = "com.cleanroommc.modularui.ClientProxy", serverSide = "com.cleanroommc.modularui.CommonProxy")
	public static CommonProxy proxy;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		proxy.preInit();
		WidgetRegistry.init();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		Tests.init();
	}

	@Mod.EventHandler
	public void onPostInit(FMLPostInitializationEvent event) {
		proxy.postInit();
	}

}
