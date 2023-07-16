package com.cleanroommc.modularui.api.value;

public interface IEnumValue<T extends Enum<T>> {

    Class<T> getEnumClass();
}
