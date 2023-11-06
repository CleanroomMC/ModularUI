package com.cleanroommc.modularui.factory;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class AbstractUIFactory<T extends GuiData> implements UIFactory<T> {

    private final String name;

    protected AbstractUIFactory(String name) {
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public final @NotNull String getFactoryName() {
        return this.name;
    }
}
