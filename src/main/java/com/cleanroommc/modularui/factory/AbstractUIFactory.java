package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
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

    @NotNull
    public abstract IGuiHolder<T> getGuiHolder(T data);

    @Override
    public ModularPanel createPanel(T guiData, GuiSyncManager syncManager) {
        IGuiHolder<T> guiHolder = Objects.requireNonNull(getGuiHolder(guiData), "Gui holder must not be null!");
        return guiHolder.buildUI(guiData, syncManager, guiData.isClient());
    }

    @Override
    public ModularScreen createScreen(T guiData, ModularPanel mainPanel) {
        IGuiHolder<T> guiHolder = Objects.requireNonNull(getGuiHolder(guiData), "Gui holder must not be null!");
        return guiHolder.createScreen(guiData, mainPanel);
    }

    @SuppressWarnings("unchecked")
    protected boolean isGuiHolder(Object o) {
        if(!(o instanceof IGuiHolder)) return false;
        try {
            IGuiHolder<T> o1 = (IGuiHolder<T>) o;
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }
}
