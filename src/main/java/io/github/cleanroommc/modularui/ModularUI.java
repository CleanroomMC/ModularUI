package io.github.cleanroommc.modularui;

import io.github.cleanroommc.modularui.test.Tests;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = ModularUI.ID, name = ModularUI.NAME, version = ModularUI.VERSION)
public class ModularUI {

	public static final String ID = "modularui";
	public static final String NAME = "Modular UI";
	public static final String VERSION = "1.0";

	@Mod.Instance
	public static ModularUI INSTANCE;

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		Tests.init();
	}

}
