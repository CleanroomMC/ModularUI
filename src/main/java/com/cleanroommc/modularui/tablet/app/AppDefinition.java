package com.cleanroommc.modularui.tablet.app;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public abstract class AppDefinition<T extends TabletApp> implements IApp<T> {

    public static <T extends TabletApp> AppDefinition<T> of(String name, IDrawable icon, Function<GuiContext, T> appCreator) {
        return new AppDefinition<T>(name, icon) {
            @Override
            public @NotNull T createApp(GuiContext context) {
                return appCreator.apply(context);
            }
        };
    }

    private final String name;
    private final IDrawable icon;

    public AppDefinition(String name, IDrawable icon) {
        this.name = name;
        this.icon = icon;
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public @Nullable ServerTabletApp createServerApp(GuiSyncHandler syncHandler) {
        return null;
    }
}
