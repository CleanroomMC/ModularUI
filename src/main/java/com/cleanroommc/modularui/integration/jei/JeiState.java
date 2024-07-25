package com.cleanroommc.modularui.integration.jei;

import com.cleanroommc.modularui.screen.ModularScreen;

import java.util.function.Predicate;

public enum JeiState implements Predicate<ModularScreen> {

    ENABLED {
        @Override
        public boolean test(ModularScreen screen) {
            return true;
        }
    },
    DISABLED {
        @Override
        public boolean test(ModularScreen screen) {
            return false;
        }
    },
    DEFAULT {
        @Override
        public boolean test(ModularScreen screen) {
            return !screen.isClientOnly();
        }
    }
}
