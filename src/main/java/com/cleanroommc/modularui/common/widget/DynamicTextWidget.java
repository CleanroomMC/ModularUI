package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.drawable.Text;

import java.util.function.Supplier;

public class DynamicTextWidget extends TextWidget {

    private final Supplier<Text> textSupplier;

    public DynamicTextWidget(Supplier<Text> text) {
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
    public Text getText() {
        return textSupplier.get();
    }
}
