package com.cleanroommc.modularui.theme;

import net.minecraftforge.eventbus.api.Event;

public class ReloadThemeEvent extends Event {

    public static class Pre extends ReloadThemeEvent {
    }

    public static class Post extends ReloadThemeEvent {
    }
}
