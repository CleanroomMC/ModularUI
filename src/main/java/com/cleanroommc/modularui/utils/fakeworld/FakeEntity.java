package com.cleanroommc.modularui.utils.fakeworld;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;

/**
 * Utility class for creating entities for rendering in gui.
 */
public class FakeEntity {

    private static final World entityWorld = new DummyWorld();

    private FakeEntity() {}

    public static <T extends Entity> T create(Class<T> entityClass) {
        return (T) create(EntityRegistry.getEntry(entityClass));
    }

    public static Entity create(EntityEntry entry) {
        return entry.newInstance(entityWorld);
    }
}
