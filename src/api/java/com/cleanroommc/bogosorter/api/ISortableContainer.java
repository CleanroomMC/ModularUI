package com.cleanroommc.bogosorter.api;

import java.util.function.BiConsumer;

/**
 * This interface marks a {@link net.minecraft.inventory.Container} as sortable.
 * Implementing this interface takes priority over {@link IBogoSortAPI#addCompat(Class, BiConsumer)} and
 * {@link IBogoSortAPI#addPlayerSortButtonPosition(Class, IPosSetter)}, but has the same effect.
 */
public interface ISortableContainer {

    /**
     * Is called when the container is opened. Add slot groups this container adds here.
     * Do not add the player inventory here (except if Bogosorter doesn't do it automatically).
     *
     * @param builder builder to build slot groups
     */
    void buildSortingContext(ISortingContextBuilder builder);

    /**
     * Determines where the buttons of the player inventory (if exists) should be placed.
     * Returning null will result in no sort buttons.
     *
     * @return player inventory sort button position function
     */
    default IPosSetter getPlayerButtonPosSetter() {
        return IPosSetter.TOP_RIGHT_HORIZONTAL;
    }
}
