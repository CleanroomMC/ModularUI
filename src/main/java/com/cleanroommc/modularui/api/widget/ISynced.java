package com.cleanroommc.modularui.api.widget;

import com.cleanroommc.modularui.value.sync.GenericSyncValue;
import com.cleanroommc.modularui.value.sync.ModularSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Marks a widget as syncable
 *
 * @param <W> widget type
 */
public interface ISynced<W extends IWidget> {

    /**
     * @return this cast to the true widget type
     */
    @SuppressWarnings("unchecked")
    default W getThis() {
        return (W) this;
    }

    /**
     * Called when this widget gets initialised or when this widget is added to the gui
     *
     * @param syncManager sync manager
     * @param late
     */
    void initialiseSyncHandler(ModularSyncManager syncManager, boolean late);

    /**
     * Checks and return if the received sync handler is valid for this widget This is usually an instanceof check. <br />
     * <b>Synced widgets must override this!</b>
     *
     * @param syncHandler received sync handler
     * @return true if sync handler is valid
     */
    default boolean isValidSyncHandler(SyncHandler syncHandler) {
        return false;
    }

    /**
     * Checks if the given sync handler is valid for this widget and throws an exception if not.
     * Override {@link #isValidSyncHandler(SyncHandler)}
     *
     * @param syncHandler given sync handler
     * @throws IllegalStateException if the given sync handler is invalid for this widget.
     */
    @ApiStatus.NonExtendable
    default void checkValidSyncHandler(SyncHandler syncHandler) {
        if (!isValidSyncHandler(syncHandler)) {
            throw new IllegalStateException("SyncHandler of type '" + syncHandler.getClass().getSimpleName() + "' is not valid " +
                    "for widget '" + this + "'.");
        }
    }

    default <T> T castIfTypeElseNull(SyncHandler syncHandler, Class<T> clazz) {
        return castIfTypeElseNull(syncHandler, clazz, null);
    }

    @SuppressWarnings("unchecked")
    default <T> T castIfTypeElseNull(SyncHandler syncHandler, Class<T> clazz, @Nullable Consumer<T> setup) {
        if (syncHandler != null && clazz.isAssignableFrom(syncHandler.getClass())) {
            T t = (T) syncHandler;
            if (setup != null) setup.accept(t);
            return t;
        }
        return null;
    }

    default <T> GenericSyncValue<T> castIfTypeGenericElseNull(SyncHandler syncHandler, Class<T> clazz) {
        return castIfTypeGenericElseNull(syncHandler, clazz, null);
    }

    default <T> GenericSyncValue<T> castIfTypeGenericElseNull(SyncHandler syncHandler, Class<T> clazz,
                                                              @Nullable Consumer<GenericSyncValue<T>> setup) {
        if (syncHandler instanceof GenericSyncValue<?> genericSyncValue && genericSyncValue.isOfType(clazz)) {
            GenericSyncValue<T> t = genericSyncValue.cast();
            if (setup != null) setup.accept(t);
            return t;
        }
        return null;
    }

    /**
     * @return true if this widget has a valid sync handler
     */
    boolean isSynced();

    /**
     * @return the sync handler of this widget
     * @throws IllegalStateException if this widget has no valid sync handler
     */
    @NotNull
    SyncHandler getSyncHandler();

    /**
     * Sets the sync handler key. The sync handler will be obtained in {@link #initialiseSyncHandler(ModularSyncManager, boolean)}
     *
     * @param name sync handler key name
     * @param id   sync handler key id
     * @return this
     */
    W syncHandler(String name, int id);

    /**
     * Sets the sync handler key. The sync handler will be obtained in {@link #initialiseSyncHandler(ModularSyncManager, boolean)}
     *
     * @param key sync handler name
     * @return this
     */
    default W syncHandler(String key) {
        return syncHandler(key, 0);
    }

    /**
     * Sets the sync handler key. The sync handler will be obtained in {@link #initialiseSyncHandler(ModularSyncManager, boolean)}
     *
     * @param id sync handler id
     * @return this
     */
    default W syncHandler(int id) {
        return syncHandler("_", id);
    }
}
