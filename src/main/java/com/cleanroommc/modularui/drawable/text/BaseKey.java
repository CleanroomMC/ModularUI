package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.drawable.IKey;

import net.minecraft.util.text.TextFormatting;

import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

public abstract class BaseKey implements IKey {

    private TextFormatting[] formatting;

    @Override
    public String getFormatted() {
        if (this.formatting == null) return get();
        if (FontRenderHelper.isReset(this.formatting)) return TextFormatting.RESET + get();
        return FontRenderHelper.getFormatting(this.formatting, new StringBuilder()).append(get()).append(TextFormatting.RESET).toString();
    }

    @Override
    public BaseKey format(TextFormatting formatting) {
        if (this.formatting == null) {
            this.formatting = FontRenderHelper.createFormattingState();
        }
        FontRenderHelper.addAfter(this.formatting, formatting);
        return this;
    }

    @Nullable
    public TextFormatting[] getFormatting() {
        return formatting;
    }

    @Override
    public String toString() {
        return getFormatted();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof IKey key)) return false;
        return getFormatted().equals(key.getFormatted());
    }

    @Override
    public int hashCode() {
        throw new NotImplementedException("Implement hashCode() in subclasses");
    }
}
