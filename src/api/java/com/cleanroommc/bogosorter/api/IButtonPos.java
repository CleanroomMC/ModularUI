package com.cleanroommc.bogosorter.api;

/**
 * Determines the true sort button pos for slot groups.
 */
public interface IButtonPos {

    void setEnabled(boolean enabled);

    /**
     * Sets position where the buttons will be placed.
     *
     * @param x x pos
     * @param y y pos
     */
    void setPos(int x, int y);

    /**
     * Sets the alignment of the buttons. Determines where the buttons are placed relative to the pos.
     * For example if the alignment is bottom left, then the pos will be the bottom left corner of the buttons.
     *
     * @param alignment alignment
     */
    void setAlignment(Alignment alignment);

    /**
     * Sets the layout of the buttons. Horizontal is next to each other and vertical is on top of each other.
     *
     * @param layout layout
     */
    void setLayout(Layout layout);

    default void setTopLeft() {
        setAlignment(Alignment.TOP_LEFT);
    }

    default void setTopRight() {
        setAlignment(Alignment.TOP_RIGHT);
    }

    default void setBottomLeft() {
        setAlignment(Alignment.BOTTOM_LEFT);
    }

    default void setBottomRight() {
        setAlignment(Alignment.BOTTOM_RIGHT);
    }

    default void setHorizontal() {
        setLayout(Layout.HORIZONTAL);
    }

    default void setVertical() {
        setLayout(Layout.VERTICAL);
    }

    boolean isEnabled();

    int getX();

    int getY();

    Alignment getAlignment();

    Layout getLayout();

    enum Alignment {
        TOP_RIGHT, TOP_LEFT, BOTTOM_RIGHT, BOTTOM_LEFT
    }

    enum Layout {
        HORIZONTAL, VERTICAL
    }
}
