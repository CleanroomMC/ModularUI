package com.cleanroommc.modularui.drawable.keys;

import com.cleanroommc.modularui.ClientEventHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import net.minecraft.client.resources.I18n;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class LangKey implements IKey {

    private final String key;
    private final Object[] args;
    private String string;
    private long time = 0;

    public LangKey(@NotNull String key) {
        this(key, null);
    }

    public LangKey(@NotNull String key, @Nullable Object[] args) {
        this.key = Objects.requireNonNull(key);
        this.args = args == null || args.length == 0 ? null : args;
    }

    public String getKey() {
        return this.key;
    }

    public Object[] getArgs() {
        return this.args;
    }

    @Override
    public String get() {
        if (this.string == null || (this.args != null && this.time != ClientEventHandler.getTicks())) {
            this.time = ClientEventHandler.getTicks();
            this.string = I18n.format(this.key, this.args);
        }
        return this.string;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof LangKey) {
            return this.get().equals(((LangKey) obj).get());
        }
        return false;
    }

    @Override
    public String toString() {
        return this.get();
    }
}