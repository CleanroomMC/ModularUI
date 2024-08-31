package com.cleanroommc.modularui.overlay;

import com.cleanroommc.modularui.screen.ModularScreen;

import net.minecraft.client.gui.GuiScreen;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Predicate;

@ApiStatus.Experimental
public class OverlayHandler implements Comparable<OverlayHandler> {

    private final Predicate<GuiScreen> test;
    private final Function<GuiScreen, ModularScreen> overlayFunction;
    private final int priority;

    public OverlayHandler(Predicate<GuiScreen> test, Function<GuiScreen, ModularScreen> overlayFunction) {
        this(test, overlayFunction, 1000);
    }

    public OverlayHandler(Predicate<GuiScreen> test, Function<GuiScreen, ModularScreen> overlayFunction, int priority) {
        this.test = test;
        this.overlayFunction = overlayFunction;
        this.priority = priority;
    }

    public boolean isValidFor(GuiScreen screen) {
        return this.test.test(screen);
    }

    public ModularScreen createOverlay(GuiScreen screen) {
        return this.overlayFunction.apply(screen);
    }

    @Override
    public int compareTo(@NotNull OverlayHandler o) {
        return Integer.compare(this.priority, o.priority);
    }
}
