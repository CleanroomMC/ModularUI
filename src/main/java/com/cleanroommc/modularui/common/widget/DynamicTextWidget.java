package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.drawable.TextSpan;

import java.util.function.Supplier;

public class DynamicTextWidget extends TextWidget {

    private final Supplier<TextSpan> textSupplier;

    public DynamicTextWidget(Supplier<TextSpan> text) {
        this.textSupplier = text;
    }

    @Override
    public void onScreenUpdate() {
        String l = textSupplier.get().getFormatted();
        if (!l.equals(localised)) {
            checkNeedsRebuild();
            localised = l;
        }
    }

    @Override
    public TextSpan getText() {
        return textSupplier.get();
    }
}
