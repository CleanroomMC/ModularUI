package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.drawable.IKey;

import net.minecraft.util.text.TextFormatting;

import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

public abstract class BaseKey<K extends BaseKey<K>> implements IKey {

    private FormattingState formatting;

    @Override
    public String getFormatted(@Nullable FormattingState parentFormatting) {
        return FontRenderHelper.format(this.formatting, parentFormatting, get());
    }

    @Override
    public K style(@Nullable TextFormatting formatting) {
        if (this.formatting == null) {
            this.formatting = new FormattingState();
        }
        if (formatting == null) this.formatting.forceDefaultColor();
        else this.formatting.add(formatting, false);
        return getThis();
    }

    @Override
    public K removeStyle() {
        if (this.formatting != null) {
            this.formatting.reset();
        }
        return getThis();
    }

    @Override
    public @Nullable FormattingState getFormatting() {
        return formatting;
    }

    @SuppressWarnings("unchecked")
    public K getThis() {
        return (K) this;
    }

    @Override
    public String toString() {
        return getFormatted();
    }

    @Override
    public int hashCode() {
        throw new NotImplementedException("Implement hashCode() in subclasses");
    }
}
