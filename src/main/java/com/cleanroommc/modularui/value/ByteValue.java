package com.cleanroommc.modularui.value;

import com.cleanroommc.modularui.api.value.IByteValue;

public class ByteValue implements IByteValue<Byte> {

    protected byte value;

    @Override
    public void setByteValue(byte b) {
        value = b;
    }

    @Override
    public byte getByteValue() {
        return value;
    }

    @Override
    public Byte getValue() {
        return getByteValue();
    }

    @Override
    public void setValue(Byte value) {
        setByteValue(value);
    }

    public static class Dynamic extends ByteValue {

        private Supplier getter;
        private Consumer setter;

        public Dynamic(Supplier getter, Consumer setter) {
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public void setByteValue(byte b) {
            this.setter.setByte(b);
        }

        @Override
        public byte getByteValue() {
            return this.getter.getByte();
        }
    }

    public interface Supplier {
        byte getByte();
    }
    public interface Consumer {
        void setByte(byte b);
    }
}
