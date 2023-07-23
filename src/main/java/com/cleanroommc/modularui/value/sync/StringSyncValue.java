package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;
import com.cleanroommc.modularui.network.NetworkUtils;
import net.minecraft.network.PacketBuffer;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class StringSyncValue extends ValueSyncHandler<String> implements IStringSyncValue<String> {

    private final Supplier<String> getter;
    private final Consumer<String> setter;
    private String cache;

    public StringSyncValue(Supplier<String> getter, Consumer<String> setter) {
        this.getter = getter;
        this.setter = setter;
        this.cache = getter.get();
    }

    @Override
    public String getValue() {
        return this.cache;
    }

    @Override
    public String getStringValue() {
        return this.cache;
    }

    @Override
    public void setValue(String value, boolean setSource, boolean sync) {
        setStringValue(value, setSource, sync);
    }

    @Override
    public void setStringValue(String value, boolean setSource, boolean sync) {
        this.cache = value;
        if (setSource && this.setter != null) {
            this.setter.accept(value);
        }
        if (sync) {
            sync(0, this::write);
        }
    }

    @Override
    public boolean needsSync(boolean isFirstSync) {
        return isFirstSync || !this.getter.get().equals(this.cache);
    }

    @Override
    public void write(PacketBuffer buffer) {
        NetworkUtils.writeStringSafe(buffer, getValue());
    }

    @Override
    public void read(PacketBuffer buffer) {
        setValue(buffer.readString(Short.MAX_VALUE), true, false);
    }
}
