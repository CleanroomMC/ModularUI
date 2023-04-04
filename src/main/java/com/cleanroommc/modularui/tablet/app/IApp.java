package com.cleanroommc.modularui.tablet.app;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IApp<T extends TabletApp> {

    @NotNull
    String getName();

    @NotNull
    IDrawable getIcon();

    @NotNull
    T createApp(GuiContext context);

    @Nullable
    ServerTabletApp createServerApp(GuiSyncHandler syncHandler);
}
