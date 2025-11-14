package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.widget.INotifyEnabled;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widget.sizer.Flex;
import com.cleanroommc.modularui.widget.sizer.ResizeNode;
import com.cleanroommc.modularui.widget.sizer.StandardResizer;
import com.cleanroommc.modularui.widget.sizer.WidgetResizeNode;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class AbstractWidget implements IWidget {

    // gui context
    private boolean valid = false;
    private IWidget parent = null;
    private ModularPanel panel = null;
    private ModularGuiContext context = null;

    @Nullable private String name;
    private boolean enabled = true;
    private int timeHovered = -1;
    private int timeBelowMouse = -1;
    private boolean excludeAreaInRecipeViewer = false;

    private final Area area = new Area();
    private WidgetResizeNode resizer;

    /**
     * Returns the screen of the panel of this widget is being opened in.
     *
     * @return the screen of this widget
     * @throws IllegalStateException if {@link #isValid()} returns false
     */
    @Override
    public ModularScreen getScreen() {
        return getPanel().getScreen();
    }

    @Override
    public void scheduleResize() {
        this.resizer.markDirty();
    }

    @Override
    public boolean requiresResize() {
        return this.resizer.requiresResize();
    }

    @MustBeInvokedByOverriders
    @Override
    public void onResized() {
        this.requiresResize = false;
    }

    /**
     * Called when a panel is opened. Use {@link #onInit()} and {@link #afterInit()} for custom logic.
     *
     * @param parent the parent this element belongs to
     * @param late   true if this is called some time after the widget tree of the parent has been initialised
     */
    @ApiStatus.Internal
    @Override
    public final void initialise(@NotNull IWidget parent, boolean late) {
        this.timeHovered = -1;
        this.timeBelowMouse = -1;
        if (!(this instanceof ModularPanel)) {
            this.parent = parent;
            this.panel = parent.getPanel();
            this.context = parent.getContext();
            getArea().setPanelLayer(this.panel.getArea().getPanelLayer());
            getArea().z(parent.getArea().z() + 1);
            /*if (this.guiActionListeners != null) {
                for (IGuiAction action : this.guiActionListeners) {
                    this.context.getScreen().registerGuiActionListener(action);
                }
            }*/
        }
        /*if (this.value != null && this.syncKey != null) {
            throw new IllegalStateException("Widget has a value and a sync key for a synced value. This is not allowed!");
        }
        this.valid = true;
        if (!getScreen().isClientOnly()) {
            initialiseSyncHandler(getScreen().getSyncManager(), late);
        }
        if (isExcludeAreaInRecipeViewer()) {
            getContext().getRecipeViewerSettings().addExclusionArea(this);
        }*/
        onInit();
        if (hasChildren()) {
            for (IWidget child : getChildren()) {
                child.initialise(this, false);
            }
        }
        afterInit();
        this.resizer.onResized();
    }

    /**
     * Called after this widget is initialised and before the children are initialised.
     */
    @ApiStatus.OverrideOnly
    public void onInit() {}

    /**
     * Called after this widget is initialised and after the children are initialised.
     */
    @ApiStatus.OverrideOnly
    public void afterInit() {}

    /**
     * Called when this widget is removed from the widget tree or after the panel is closed.
     * Overriding this is fine, but super must be called.
     */
    @MustBeInvokedByOverriders
    @Override
    public void dispose() {
        if (isValid()) {
            /*if (this.guiActionListeners != null) {
                for (IGuiAction action : this.guiActionListeners) {
                    this.context.getScreen().removeGuiActionListener(action);
                }
            }
            if (isExcludeAreaInRecipeViewer()) {
                getContext().getRecipeViewerSettings().removeExclusionArea(this);
            }*/
        }
        if (hasChildren()) {
            for (IWidget child : getChildren()) {
                child.dispose();
            }
        }
        if (!(this instanceof ModularPanel)) {
            this.panel = null;
            this.parent = null;
            this.context = null;
        }
        this.timeHovered = -1;
        this.timeBelowMouse = -1;
        this.valid = false;
    }

    // -------------------
    // === Gui context ===
    // -------------------

    /**
     * Returns if this widget is currently part of an open panel. Only if this is true information about parent, panel and gui context can
     * be obtained.
     *
     * @return true if this widget is part of an open panel
     */
    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public void onUpdate() {
        if (isHovering()) this.timeHovered++;
        if (isBelowMouse()) this.timeBelowMouse++;
    }

    @MustBeInvokedByOverriders
    @Override
    public void onMouseStartHover() {
        this.timeHovered = 0;
    }

    @MustBeInvokedByOverriders
    @Override
    public void onMouseEndHover() {
        this.timeHovered = -1;
    }

    @MustBeInvokedByOverriders
    @Override
    public void onMouseEnterArea() {
        this.timeBelowMouse = 0;
    }

    @MustBeInvokedByOverriders
    @Override
    public void onMouseLeaveArea() {
        this.timeBelowMouse = -1;
    }

    @Override
    public boolean isHoveringFor(int ticks) {
        return timeHovered >= ticks;
    }

    @Override
    public boolean isBelowMouseFor(int ticks) {
        return timeBelowMouse >= ticks;
    }

    public int getTicksHovered() {
        return timeHovered;
    }

    public int getTicksBelowMouse() {
        return timeBelowMouse;
    }

    /**
     * Returns the area of this widget. This contains information such as position, size, relative position to parent, padding and margin.
     * Even tho this is a mutable object, you should refrain from modifying the values.
     *
     * @return area of this widget
     */
    @Override
    public Area getArea() {
        return area;
    }

    /**
     * Returns if this widget is currently enabled. Disabled widgets (and all its children) are not rendered and can't be interacted with.
     *
     * @return true if this widget is enabled.
     */
    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Sets enabled state. Disabled widgets (and all its children) are not rendered and can't be interacted with.
     *
     * @param enabled enabled state
     */
    @Override
    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (isValid() && getParent() instanceof INotifyEnabled notifyEnabled) {
                notifyEnabled.onChildChangeEnabled(this, enabled);
            }
        }
    }

    /**
     * Returns the parent of this widget. If this is a {@link ModularPanel} this will always return null contrary to the annotation.
     *
     * @return the screen of this widget
     * @throws IllegalStateException if {@link #isValid()} returns false
     */
    @Override
    public @NotNull IWidget getParent() {
        if (!isValid()) {
            throw new IllegalStateException(this + " is not in a valid state!");
        }
        return parent;
    }

    /**
     * Returns the gui context of the screen this widget is part of.
     *
     * @return the screen of this widget
     * @throws IllegalStateException if {@link #isValid()} returns false
     */
    @Override
    public ModularGuiContext getContext() {
        if (!isValid()) {
            throw new IllegalStateException(this + " is not in a valid state!");
        }
        return context;
    }

    /**
     * Used to set the gui context on panels internally.
     */
    @ApiStatus.Internal
    protected final void setContext(ModularGuiContext context) {
        this.context = context;
    }

    /**
     * Returns the panel of this widget is being opened in.
     *
     * @return the screen of this widget
     * @throws IllegalStateException if {@link #isValid()} returns false
     */
    @Override
    public @NotNull ModularPanel getPanel() {
        if (!isValid()) {
            throw new IllegalStateException(this + " is not in a valid state!");
        }
        return panel;
    }

    @Override
    public @NotNull ResizeNode resizer() {
        if (this.resizer == null) {
            this.resizer = new StandardResizer(this);
        }
        return this.resizer;
    }

    public void resizer(WidgetResizeNode resizer) {
        this.resizer = Objects.requireNonNull(resizer);
    }

    /**
     * Returns the flex of this widget. This is responsible for calculating size, pos and relative pos.
     * Originally this was intended to be modular for custom flex class. May come back to this in the future.
     * Same as {@link #flex()}.
     *
     * @return flex of this widget
     */
    @Override
    public StandardResizer getFlex() {
        return null;
    }

    @Override
    public @Nullable String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    /**
     * This is only used in {@link #toString()}.
     *
     * @return the simple class name or other fitting name
     */
    protected String getTypeName() {
        return getClass().getSimpleName();
    }

    /**
     * @return the simple class plus the debug name if set
     */
    @Override
    public String toString() {
        if (getName() != null) {
            return getTypeName() + "#" + getName();
        }
        return getTypeName();
    }
}
