package io.github.cleanroommc.modularui;

import io.github.cleanroommc.modularui.test.Tests;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
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

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		Tests.init();
	}

}
