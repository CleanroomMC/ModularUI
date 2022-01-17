package io.github.cleanroommc.modularui;

import net.minecraftforge.fml.common.Mod;

@Mod(modid = ModularUI.ID, name = ModularUI.NAME, version = ModularUI.VERSION)
public class ModularUI {

	public static final String ID = "modularui";
	public static final String NAME = "Modular UI";
	public static final String VERSION = "1.0";

	@Mod.Instance
	public static ModularUI INSTANCE;

}
