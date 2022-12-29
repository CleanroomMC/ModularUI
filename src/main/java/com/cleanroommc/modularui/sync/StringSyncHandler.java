package com.cleanroommc.modularui.sync;

import com.cleanroommc.modularui.api.sync.IValueSyncHandler;
import com.cleanroommc.modularui.api.sync.ValueSyncHandler;
import com.cleanroommc.modularui.network.NetworkUtils;
import net.minecraft.network.PacketBuffer;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class StringSyncHandler extends ValueSyncHandler<String> implements IValueSyncHandler.IStringValueSyncHandler<String> {

    private final Supplier<String> getter;
    private final Consumer<String> setter;
    private String cache;

    public StringSyncHandler(Supplier<String> getter, Consumer<String> setter) {
        this.getter = getter;
        this.setter = setter;
        this.cache = getter.get();
    }

    @Override
    public String getCachedValue() {
        return cache;
    }

    @Override
    public void setValue(String value) {
        this.cache = value;
    }

    @Override
    public boolean needsSync(boolean isFirstSync) {
        return isFirstSync || !this.getter.get().equals(cache);
    }

    @Override
    public void updateAndWrite(PacketBuffer buffer) {
        setValue(this.getter.get());
        NetworkUtils.writeStringSafe(buffer, getCachedValue());
    }

    @Override
    public void read(PacketBuffer buffer) {
        setValue(buffer.readString(Short.MAX_VALUE));
        this.setter.accept(getCachedValue());
    }

    @Override
    public void updateFromClient(String value) {
        this.setter.accept(value);
        syncToServer(0, this::updateAndWrite);
    }

    @Override
    public String fromString(String value) {
        return value;
    }
}
