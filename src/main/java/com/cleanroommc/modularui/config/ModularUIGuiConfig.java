package com.cleanroommc.modularui.config;

import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.Tags;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("rawtypes")
public class ModularUIGuiConfig extends GuiConfig {

    public ModularUIGuiConfig(GuiScreen parentScreen) {
        super(
                parentScreen,
                getConfigElements(),
                Tags.MODID,
                false,
                false,
                GuiConfig.getAbridgedConfigPath(ModularUIConfig.config.toString()));
    }

    private static List<IConfigElement> getConfigElements() {
        List<IConfigElement> list = new ArrayList<>();

        for (String category : ModularUIConfig.CATEGORIES) {
            list.add(new ConfigElement(ModularUIConfig.config.getCategory(category.toLowerCase(Locale.US))));
        }

        return list;
    }
}
