package com.cleanroommc.modularui.common.internal.wrapper;

import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.widget.IGhostIngredientTarget;
import com.cleanroommc.modularui.api.widget.Widget;
import mezz.jei.api.gui.IGhostIngredientHandler;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class GhostIngredientWrapper<W extends Widget & IGhostIngredientTarget<I>, I> implements IGhostIngredientHandler.Target<I> {

    private final W widget;

    public GhostIngredientWrapper(W widget) {
        this.widget = widget;
    }

    @Override
    public @NotNull Rectangle getArea() {
        Pos2d pos = widget.getAbsolutePos();
        return new Rectangle(pos.x, pos.y, widget.getSize().width, widget.getSize().height);
    }

    @Override
    public void accept(@NotNull I ingredient) {
        widget.accept(ingredient);
    }
}
