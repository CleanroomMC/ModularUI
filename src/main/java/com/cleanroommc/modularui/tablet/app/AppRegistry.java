package com.cleanroommc.modularui.tablet.app;

import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.tablet.guide.GuideApp;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AppRegistry {

    private static final Map<String, IApp<? extends TabletApp>> appRegistry = new Object2ObjectOpenHashMap<>();
    private static final List<IApp<? extends TabletApp>> apps = new ArrayList<>();
    public static final List<IApp<? extends TabletApp>> appsView = Collections.unmodifiableList(apps);

    public static void registerApp(IApp<? extends TabletApp> app) {
        appRegistry.put(app.getName(), app);
        apps.add(app);
    }

    public static TabletApp createApp(String name, GuiContext context) {
        IApp<?> appSupplier = appRegistry.get(name);
        return appSupplier != null ? appSupplier.createApp(context) : null;
    }

    static {
        registerApp(AppDefinition.of("guide", new ItemDrawable(new ItemStack(Items.PAPER)), GuideApp::new));
    }
}
