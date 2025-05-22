package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.layout.IResizeable;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.value.IValue;
import com.cleanroommc.modularui.api.widget.*;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.value.sync.ModularSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widget.sizer.Flex;
import com.cleanroommc.modularui.widget.sizer.IUnResizeable;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A very modular implementation of {@link IWidget}. This is the base class for almost all UI elements.
 * This class is perfectly fine for displaying drawables (although {@link com.cleanroommc.modularui.api.drawable.IDrawable.DrawableWidget DrawableWidget}
 * is preferred) or even nothing.
 * <p>
 * References to widgets should not be stored after the screen closed. While the screen is open its usually fine to remove and a widget
 * as many times as you want.
 *
 * @param <W> the type of this widget. This is used for proper return types in builder like methodsY
 */
public class Widget<W extends Widget<W>> implements IWidget, IPositioned<W>, ITooltip<W>, ISynced<W> {

    // other
    @Nullable private String debugName;
    private boolean enabled = true;
    private boolean excludeAreaInJei = false;
    // gui context
    private boolean valid = false;
    private IWidget parent = null;
    private ModularPanel panel = null;
    private ModularGuiContext context = null;
    // sizing
    private final Area area = new Area();
    private final Flex flex = new Flex(this);
    private IResizeable resizer = this.flex;
    private BiConsumer<W, IViewportStack> transform;
    private boolean requiresResize = false;
    // syncing
    @Nullable private IValue<?> value;
    @Nullable private String syncKey;
    @Nullable private SyncHandler syncHandler;
    // rendering
    @Nullable private IDrawable background = null;
    @Nullable private IDrawable overlay = null;
    @Nullable private IDrawable hoverBackground = null;
    @Nullable private IDrawable hoverOverlay = null;
    @Nullable private RichTooltip tooltip;
    @Nullable private String widgetThemeOverride = null;
    // listener
    @Nullable private List<IGuiAction> guiActionListeners; // TODO replace with proper event system
    @Nullable private Consumer<W> onUpdateListener;

    // -----------------
    // === Lifecycle ===
    // -----------------

    /**
     * Called when a panel is opened. Use {@link #onInit()} and {@link #afterInit()} for custom logic.
     *
     * @param parent the parent this element belongs to
     */
    @ApiStatus.Internal
    @Override
    public final void initialise(@NotNull IWidget parent) {
        if (!(this instanceof ModularPanel)) {
            this.parent = parent;
            this.panel = parent.getPanel();
            this.context = parent.getContext();
            getArea().setPanelLayer(this.panel.getArea().getPanelLayer());
            getArea().z(parent.getArea().z() + 1);
            if (this.guiActionListeners != null) {
                for (IGuiAction action : this.guiActionListeners) {
                    this.context.getScreen().registerGuiActionListener(action);
                }
            }
        }
        if (this.value != null && this.syncKey != null) {
            throw new IllegalStateException("Widget has a value and a sync key for a synced value. This is not allowed!");
        }
        this.valid = true;
        if (!getScreen().isClientOnly()) {
            initialiseSyncHandler(getScreen().getSyncManager());
        }
        if (isExcludeAreaInJei()) {
            getContext().getJeiSettings().addJeiExclusionArea(this);
        }
        onInit();
        if (hasChildren()) {
            for (IWidget child : getChildren()) {
                child.initialise(this);
            }
        }
        afterInit();
        this.requiresResize = false;
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
     * Retrieves, initialises and verifies a linked sync handler.
     * Custom logic should be handled in {@link #isValidSyncHandler(SyncHandler)}.
     */
    @Override
    public void initialiseSyncHandler(ModularSyncManager syncManager) {
        if (this.syncKey != null) {
            this.syncHandler = syncManager.getSyncHandler(getPanel().getName(), this.syncKey);
        }
        if ((this.syncKey != null || this.syncHandler != null) && !isValidSyncHandler(this.syncHandler)) {
            String type = this.syncHandler == null ? null : this.syncHandler.getClass().getName();
            this.syncHandler = null;
            throw new IllegalStateException("SyncHandler of type " + type + " is not valid for " + getClass().getName() + ", with key " + this.syncKey);
        }
        if (this.syncHandler instanceof ValueSyncHandler<?> valueSyncHandler && valueSyncHandler.getChangeListener() == null) {
            valueSyncHandler.setChangeListener(this::markTooltipDirty);
        }
    }

    /**
     * Called when this widget is removed from the widget tree or after the panel is closed.
     * Overriding this is fine, but super must be called.
     */
    @MustBeInvokedByOverriders
    @Override
    public void dispose() {
        if (isValid()) {
            if (this.guiActionListeners != null) {
                for (IGuiAction action : this.guiActionListeners) {
                    this.context.getScreen().removeGuiActionListener(action);
                }
            }
            if (isExcludeAreaInJei()) {
                getContext().getJeiSettings().removeJeiExclusionArea(this);
            }
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
        this.valid = false;
    }

    // -----------------
    // === Rendering ===
    // -----------------

    /**
     * Called directly before {@link #draw(ModularGuiContext, WidgetTheme)}. Draws background textures.
     * It is highly recommended to at least replicate this behaviour when overriding.
     * Overriding {@link #draw(ModularGuiContext, WidgetTheme)} for custom visuals is preferred.
     * If a parent of this widget is disabled, this widget will not be drawn.
     *
     * @param context     gui context
     * @param widgetTheme widget theme of this widget
     */
    @Override
    public void drawBackground(ModularGuiContext context, WidgetTheme widgetTheme) {
        IDrawable bg = getCurrentBackground(context.getTheme(), widgetTheme);
        if (bg != null) {
            bg.drawAtZero(context, getArea().width, getArea().height, widgetTheme);
        }
    }

    /**
     * Called between {@link #drawBackground(ModularGuiContext, WidgetTheme)} and {@link #drawOverlay(ModularGuiContext, WidgetTheme)}.
     * Custom visuals should be drawn here. For example the {@link com.cleanroommc.modularui.widgets.slot.ItemSlot ItemSlot} draws its item
     * here. If a parent of this widget is disabled, this widget will not be drawn.
     *
     * @param context     gui context
     * @param widgetTheme widget theme
     */
    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {}

    /**
     * Called directly after {@link #draw(ModularGuiContext, WidgetTheme)}. Draws overlay textures.
     * It is highly recommended to at least replicate this behaviour when overriding.
     * Overriding {@link #draw(ModularGuiContext, WidgetTheme)} for custom visuals is preferred.
     * If a parent of this widget is disabled, this widget will not be drawn.
     *
     * @param context     gui context
     * @param widgetTheme widget theme
     */
    @Override
    public void drawOverlay(ModularGuiContext context, WidgetTheme widgetTheme) {
        IDrawable bg = getCurrentOverlay(context.getTheme(), widgetTheme);
        if (bg != null) {
            bg.drawAtZero(context, getArea(), widgetTheme);
        }
    }

    /**
     * Called after every widget of every panel and screen has been drawn. This is usually used to draw a tooltip, which is the default
     * behaviour. If a parent of this widget is disabled, this widget will not be drawn.
     *
     * @param context gui context
     */
    @Override
    public void drawForeground(ModularGuiContext context) {
        RichTooltip tooltip = getTooltip();
        if (tooltip != null && isHoveringFor(tooltip.getShowUpTimer())) {
            tooltip.draw(context);
        }
    }

    /**
     * The current set background. This is not an accurate representation of what is actually being displayed currently.
     * Usually background is handled by the theme, which is when this is null.
     * Backgrounds are drawn in {@link #drawBackground(ModularGuiContext, WidgetTheme)}.
     *
     * @return background of this widget
     */
    public @Nullable IDrawable getBackground() {
        return this.background;
    }

    /**
     * The current set overlay. This is used when the widget is not hovered or no hovered overlay is set.
     * Overlays are drawn in {@link #drawOverlay(ModularGuiContext, WidgetTheme)}.
     *
     * @return overlay of this widget
     */
    public @Nullable IDrawable getOverlay() {
        return this.overlay;
    }

    /**
     * The current set hover background. Usually this is handled by the theme.
     *
     * @return hover background of this widget
     */
    public @Nullable IDrawable getHoverBackground() {
        return this.hoverBackground;
    }

    /**
     * The current set hover overlay.
     *
     * @return hover background of this widget
     */
    public @Nullable IDrawable getHoverOverlay() {
        return this.hoverOverlay;
    }

    /**
     * Returns the actual currently displayed background.
     *
     * @param theme       current theme
     * @param widgetTheme widget theme which is used by this widget
     * @return currently displayed background
     */
    public @Nullable IDrawable getCurrentBackground(ITheme theme, WidgetTheme widgetTheme) {
        if (isHovering()) {
            IDrawable hoverBackground = getHoverBackground();
            if (hoverBackground == null) hoverBackground = widgetTheme.getHoverBackground();
            if (hoverBackground != null && hoverBackground != IDrawable.NONE) return hoverBackground;
        }
        IDrawable background = getBackground();
        return background == null ? widgetTheme.getBackground() : background;
    }

    /**
     * Returns the actual currently displayed overlay.
     *
     * @param theme       current theme
     * @param widgetTheme widget theme which is used by this widget
     * @return currently displayed background
     */
    public @Nullable IDrawable getCurrentOverlay(ITheme theme, WidgetTheme widgetTheme) {
        IDrawable hoverBackground = getHoverOverlay();
        return hoverBackground != null && hoverBackground != IDrawable.NONE && isHovering() ? hoverBackground : getOverlay();
    }

    /**
     * @return the tooltip object of this widget, might have not been created yet
     */
    @Nullable
    @Override
    public RichTooltip getTooltip() {
        return this.tooltip;
    }

    /**
     * @return the tooltip object of this widget and creates a new one if there is currently none.
     */
    @Override
    public @NotNull RichTooltip tooltip() {
        if (this.tooltip == null) {
            this.tooltip = new RichTooltip().parent(this);
        }
        return this.tooltip;
    }

    /**
     * Sets a tooltip object.
     *
     * @param tooltip new tooltip
     * @return this
     */
    @Override
    public W tooltip(RichTooltip tooltip) {
        this.tooltip = tooltip;
        return getThis();
    }

    /**
     * Should be called when information which is displayed in the tooltip via {@link ITooltip#tooltipDynamic(Consumer)}.
     * It will invalidate the current tooltip and be caused to rebuild.
     */
    @Override
    public void markTooltipDirty() {
        if (this.tooltip != null) {
            this.tooltip.markDirty();
        }
    }

    /**
     * Returns the widget theme this widget class would like to use. Overriding is fine.
     *
     * @param theme theme to get widget theme from
     * @return widget theme this widget wishes to use
     */
    @ApiStatus.OverrideOnly
    protected WidgetTheme getWidgetThemeInternal(ITheme theme) {
        return theme.getFallback();
    }

    /**
     * Returns the actual used widget theme. Uses {@link #widgetTheme(String)} if it has been set, otherwise calls
     * {@link #getWidgetThemeInternal(ITheme)}
     *
     * @param theme theme to get widget theme from
     * @return widget theme this widget will use
     */
    @ApiStatus.NonExtendable
    @Override
    public final WidgetTheme getWidgetTheme(ITheme theme) {
        if (this.widgetThemeOverride != null) {
            return theme.getWidgetTheme(this.widgetThemeOverride);
        }
        return getWidgetThemeInternal(theme);
    }

    /**
     * Sets a background override. Ideally this is set in the used theme. Also consider using {@link #overlay(IDrawable...)} instead.
     * Using {@link IDrawable#EMPTY} will make the background invisible while still overriding the widget theme.
     * Background are drawn before the widget and overlays are drawn.
     *
     * @param background background to use.
     * @return this
     */
    public W background(IDrawable... background) {
        this.background = IDrawable.of(background);
        return getThis();
    }

    /**
     * Sets an overlay. Does not interfere with themes. Overlays are drawn after the widget and backgrounds.
     *
     * @param overlay overlay to use.
     * @return this
     */
    public W overlay(IDrawable... overlay) {
        this.overlay = IDrawable.of(overlay);
        return getThis();
    }

    /**
     * Sets a hover background override. Ideally this is set in the used theme. Also consider using {@link #hoverOverlay(IDrawable...)} instead.
     * Using {@link IDrawable#EMPTY} will make the background invisible while still overriding the widget theme.
     * Background are drawn before the widget and overlays are drawn.
     * <p>
     * Following argument special cases should be considered:
     * <ul>
     *     <li>{@code null} will fallback to {@link WidgetTheme#getHoverBackground()}</li>
     *     <li>{@link IDrawable#EMPTY} will make the hover background invisible</li>
     *     <li>{@link IDrawable#NONE} will use the normal background instead (which is also achieved using {@link #disableHoverBackground()})</li>
     *     <li>multiple drawables, will result in them being drawn on top of each other in the order they are passed to the method</li>
     * </ul>
     *
     * @param background hover background to use.
     * @return this
     */
    public W hoverBackground(IDrawable... background) {
        this.hoverBackground = IDrawable.of(background);
        return getThis();
    }

    /**
     * Sets a hover overlay.
     * Using {@link IDrawable#EMPTY} will make the background invisible while still overriding the widget theme.
     * Background are drawn before the widget and overlays are drawn.
     * <p>
     * Following argument special cases should be considered:
     * <ul>
     *     <li>{@link IDrawable#EMPTY} will make the hover overlay invisible</li>
     *     <li>{@code null} and {@link IDrawable#NONE} will use the normal overlay instead (which is also achieved using {@link #disableHoverOverlay()} ()})</li>
     *     <li>multiple drawables, will result in them being drawn on top of each other in the order they are passed to the method</li>
     * </ul>
     *
     * @param overlay hover overlay to use.
     * @return this
     */
    public W hoverOverlay(IDrawable... overlay) {
        this.hoverOverlay = IDrawable.of(overlay);
        return getThis();
    }

    /**
     * Forces the hover background to use the normal background instead.
     *
     * @return this
     */
    public W disableHoverBackground() {
        return hoverBackground(IDrawable.NONE);
    }

    /**
     * Forces the hover overlay to use the normal overlay instead.
     *
     * @return this
     */
    public W disableHoverOverlay() {
        return hoverOverlay(IDrawable.NONE);
    }

    /**
     * Sets an override widget theme. This will change of the appearance of this widget according to the widget theme.
     *
     * @param s id of the widget theme (see constants in {@link IThemeApi})
     * @return this
     */
    public W widgetTheme(String s) {
        if (!IThemeApi.get().hasWidgetTheme(s)) {
            throw new IllegalArgumentException("No widget theme for id '" + s + "' exists.");
        }
        this.widgetThemeOverride = s;
        return getThis();
    }

    // --------------
    // === Events ===
    // --------------

    /**
     * Called once every tick (20 times per second). Overriding is fine, but super should be called. This will be called even of the widget
     * is not enabled.
     * By default, this will invoke update listeners set via setters.
     */
    @MustBeInvokedByOverriders
    @Override
    public void onUpdate() {
        if (this.onUpdateListener != null) {
            this.onUpdateListener.accept(getThis());
        }
    }

    /**
     * @return the current tick listener which will be called every tick
     */
    @Nullable
    public Consumer<W> getOnUpdateListener() {
        return this.onUpdateListener;
    }

    /**
     * Registers a gui action this widget can listen to. Gui action listeners can listen to several mouse and keyboard input events.
     * The listeners are called first, before any widgets are interacted with. The listeners will always be called, even if the widget
     * is disabled or not hovered!
     * <p>
     * Lambdas must be cast to the appropriate functional interface.
     * These actions are automatically unregistered when the widget is removed from the widget tree.
     *
     * @param action gui action to register
     * @return this
     */
    public W listenGuiAction(IGuiAction action) {
        if (this.guiActionListeners == null) {
            this.guiActionListeners = new ArrayList<>();
        }
        this.guiActionListeners.add(action);
        if (isValid()) {
            this.context.getScreen().registerGuiActionListener(action);
        }
        return getThis();
    }

    /**
     * Sets an update listener which is called once every tick even when this widget is disabled.
     *
     * @param listener update listener
     * @return this
     */
    public W onUpdateListener(Consumer<W> listener) {
        return onUpdateListener(listener, false);
    }

    /**
     * Sets an update listener which is called once every tick even when this widget is disabled.
     * If a listener is already set and {@code merge} is true, the listeners will be merged, so that both will be called on tick.
     *
     * @param listener update listener
     * @return this
     */
    public W onUpdateListener(Consumer<W> listener, boolean merge) {
        if (merge && this.onUpdateListener != null) {
            final Consumer<W> oldListener = this.onUpdateListener;
            if (listener != null) {
                this.onUpdateListener = w -> {
                    oldListener.accept(w);
                    listener.accept(w);
                };
            }
        } else {
            this.onUpdateListener = listener;
        }
        return getThis();
    }

    /**
     * Sets a condition for when to enable/disable this widget. This register an update listener which checks the condition every tick.
     * Careful not to overwrite this when calling {@link #onUpdateListener(Consumer)} afterward!
     *
     * @param condition condition when to enable this widget
     * @return this
     */
    public W setEnabledIf(Predicate<W> condition) {
        return onUpdateListener(w -> setEnabled(condition.test(w)), true);
    }

    // ----------------
    // === Resizing ===
    // ----------------

    @Override
    public void scheduleResize() {
        this.requiresResize = true;
    }

    @Override
    public boolean requiresResize() {
        return this.requiresResize;
    }

    @MustBeInvokedByOverriders
    @Override
    public void onResized() {
        this.requiresResize = false;
    }

    /**
     * Returns the area of this widget. This contains information such as position, size, relative position to parent, padding and margin.
     * Even tho this is a mutable object, you should refrain from modifying the values.
     *
     * @return area of this widget
     */
    @Override
    public Area getArea() {
        return this.area;
    }

    /**
     * Returns the flex of this widget. This is responsible for calculating size, pos and relative pos.
     * Originally this was intended to be modular for custom flex class. May come back to this in the future.
     * Same as {@link #flex()}.
     *
     * @return flex of this widget
     */
    @Override
    public Flex getFlex() {
        return this.flex;
    }

    /**
     * Returns the flex of this widget. This is responsible for calculating size, pos and relative pos.
     * Originally this was intended to be modular for custom flex class. May come back to this in the future.
     * Same as {@link #getFlex()}.
     *
     * @return flex of this widget
     */
    @Override
    public Flex flex() {
        return this.flex;
    }

    /**
     * Returns the resizer of this widget. This is actually the field responsible for resizing this widget.
     * Within MUI this is always the same as {@link #flex()}. Custom resizer have not been tested.
     * The relevance of separating flex and resizer is left to be investigated in the future.
     *
     * @return the resizer of this widget
     */
    @NotNull
    @Override
    public IResizeable resizer() {
        return this.resizer;
    }

    /**
     * Sets the resizer of this widget, which is responsible for resizing this widget.
     * Within MUI this setter is never used. Custom resizer have not been tested.
     * The relevance of separating flex and resizer is left to be investigated in the future.
     *
     * @param resizer resizer
     */
    @Override
    public void resizer(IResizeable resizer) {
        this.resizer = resizer != null ? resizer : IUnResizeable.INSTANCE;
    }

    @Override
    public void transform(IViewportStack stack) {
        IWidget.super.transform(stack);
        if (this.transform != null) {
            this.transform.accept(getThis(), stack);
        }
    }

    public W transform(BiConsumer<W, IViewportStack> transform) {
        this.transform = transform;
        return getThis();
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
    public final boolean isValid() {
        return this.valid;
    }

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
        return this.panel;
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
        return this.parent;
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
        return this.context;
    }

    /**
     * Used to set the gui context on panels internally.
     */
    @ApiStatus.Internal
    protected final void setContext(ModularGuiContext context) {
        this.context = context;
    }

    // ---------------
    // === Syncing ===
    // --------------

    /**
     * Returns if this widget has a valid sync handler.
     *
     * @return true if this widget has a valid sync handler
     */
    @Override
    public boolean isSynced() {
        return this.syncHandler != null;
    }

    /**
     * Returns the sync handler of this widget.
     *
     * @return sync handler of this widget
     * @throws IllegalStateException if this widget has no sync handler ({@link #isSynced()} returns false)
     */
    @Override
    public @NotNull SyncHandler getSyncHandler() {
        if (this.syncHandler == null) {
            throw new IllegalStateException("Widget is not initialised or not synced!");
        }
        return this.syncHandler;
    }

    /**
     * Returns the value handler of this widget. Value handlers can provide and update any kind of objects like numbers and strings.
     * For example text fields uses this get the current set string and updates the string after it is unfocused.
     *
     * @return the value handler of this widget
     */
    public @Nullable IValue<?> getValue() {
        return this.value;
    }

    /**
     * Sets a sync handler id. A sync handler with the same id must have been registered to the appropriate
     * {@link com.cleanroommc.modularui.value.sync.PanelSyncManager PanelSyncManager} for this to work.
     * This method is preferred over setting a sync handler directly since this does not require the widget to be defined on both sides.
     *
     * @param name sync handler key name
     * @param id   sync handler key id
     * @return this
     */
    @Override
    public W syncHandler(String name, int id) {
        this.syncKey = ModularSyncManager.makeSyncKey(name, id);
        return getThis();
    }

    /**
     * Used for widgets to set a value handler. Can also be a sync handler
     */
    protected void setValue(IValue<?> value) {
        this.value = value;
        if (value instanceof SyncHandler syncHandler1) {
            setSyncHandler(syncHandler1);
        }
    }

    /**
     * Used for widgets to set a sync handler.
     */
    protected void setSyncHandler(@Nullable SyncHandler syncHandler) {
        this.syncHandler = syncHandler;
    }

    // -------------
    // === Other ===
    // -------------

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

    public boolean isExcludeAreaInJei() {
        return this.excludeAreaInJei;
    }

    /**
     * Disables the widget from start. Useful inside widget tree creation, where widget references are usually not stored.
     *
     * @return this
     */
    public W disabled() {
        setEnabled(false);
        return getThis();
    }

    public W excludeAreaInJei() {
        return excludeAreaInJei(true);
    }

    public W excludeAreaInJei(boolean val) {
        this.excludeAreaInJei = val;
        if (isValid()) {
            getContext().getJeiSettings().addJeiExclusionArea(this);
        }
        return getThis();
    }

    /**
     * Sets a debug name. This is only used in {@link #toString()}, which is displayed in the mui debug info. Useful for identifying widgets
     * for debugging. This has no other effect.
     *
     * @param name debug name to use
     * @return this
     */
    public W debugName(String name) {
        this.debugName = name;
        return getThis();
    }

    /**
     * Returns this widget with proper generic type.
     *
     * @return this
     */
    @SuppressWarnings("unchecked")
    @Override
    public W getThis() {
        return (W) this;
    }

    /**
     * @return the simple class plus the debug name if set
     */
    @Override
    public String toString() {
        if (this.debugName != null) {
            return getClass().getSimpleName() + "#" + this.debugName;
        }
        return getClass().getSimpleName();
    }
}
