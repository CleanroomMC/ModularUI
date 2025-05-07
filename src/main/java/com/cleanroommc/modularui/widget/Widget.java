package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.layout.IResizeable;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.value.IValue;
import com.cleanroommc.modularui.api.widget.*;
import com.cleanroommc.modularui.factory.GuiData;
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

    @ApiStatus.Internal
    @Override
    public void initialise(@NotNull IWidget parent) {
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
        onUpdate();
        this.requiresResize = false;
    }

    @ApiStatus.OverrideOnly
    public void onInit() {}

    @ApiStatus.OverrideOnly
    public void afterInit() {}

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

    @Override
    public void drawBackground(ModularGuiContext context, WidgetTheme widgetTheme) {
        IDrawable bg = getCurrentBackground(context.getTheme(), widgetTheme);
        if (bg != null) {
            bg.drawAtZero(context, getArea().width, getArea().height, widgetTheme);
        }
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {}

    @Override
    public void drawOverlay(ModularGuiContext context, WidgetTheme widgetTheme) {
        IDrawable bg = getCurrentOverlay(context.getTheme(), widgetTheme);
        if (bg != null) {
            bg.drawAtZero(context, getArea(), widgetTheme);
        }
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        RichTooltip tooltip = getTooltip();
        if (tooltip != null && isHoveringFor(tooltip.getShowUpTimer())) {
            tooltip.draw(context);
        }
    }

    public @Nullable IDrawable getBackground() {
        return this.background;
    }

    public @Nullable IDrawable getOverlay() {
        return this.overlay;
    }

    public @Nullable IDrawable getHoverBackground() {
        return this.hoverBackground;
    }

    public @Nullable IDrawable getHoverOverlay() {
        return this.hoverOverlay;
    }

    public IDrawable getCurrentBackground(ITheme theme, WidgetTheme widgetTheme) {
        if (isHovering()) {
            IDrawable hoverBackground = getHoverBackground();
            if (hoverBackground == null) hoverBackground = widgetTheme.getHoverBackground();
            if (hoverBackground != null && hoverBackground != IDrawable.NONE) return hoverBackground;
        }
        IDrawable background = getBackground();
        return background == null ? widgetTheme.getBackground() : background;
    }

    public IDrawable getCurrentOverlay(ITheme theme, WidgetTheme widgetTheme) {
        IDrawable hoverBackground = getHoverOverlay();
        return hoverBackground != null && hoverBackground != IDrawable.NONE && isHovering() ? hoverBackground : getOverlay();
    }

    @Nullable
    @Override
    public RichTooltip getTooltip() {
        return this.tooltip;
    }

    @Override
    public @NotNull RichTooltip tooltip() {
        if (this.tooltip == null) {
            this.tooltip = new RichTooltip(this);
        }
        return this.tooltip;
    }

    @Override
    public W tooltip(RichTooltip tooltip) {
        this.tooltip = tooltip;
        return getThis();
    }

    @Override
    public void markTooltipDirty() {
        if (this.tooltip != null) {
            this.tooltip.markDirty();
        }
    }

    @ApiStatus.OverrideOnly
    protected WidgetTheme getWidgetThemeInternal(ITheme theme) {
        return theme.getFallback();
    }

    @ApiStatus.NonExtendable
    @Override
    public final WidgetTheme getWidgetTheme(ITheme theme) {
        if (this.widgetThemeOverride != null) {
            return theme.getWidgetTheme(this.widgetThemeOverride);
        }
        return getWidgetThemeInternal(theme);
    }

    public W background(IDrawable... background) {
        this.background = IDrawable.of(background);
        return getThis();
    }

    public W overlay(IDrawable... overlay) {
        this.overlay = IDrawable.of(overlay);
        return getThis();
    }

    public W hoverBackground(IDrawable... background) {
        this.hoverBackground = IDrawable.of(background);
        return getThis();
    }

    public W hoverOverlay(IDrawable... overlay) {
        this.hoverOverlay = IDrawable.of(overlay);
        return getThis();
    }

    public W disableHoverBackground() {
        return hoverBackground(IDrawable.NONE);
    }

    public W disableHoverOverlay() {
        return hoverOverlay(IDrawable.NONE);
    }

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

    @MustBeInvokedByOverriders
    @Override
    public void onUpdate() {
        if (this.onUpdateListener != null) {
            this.onUpdateListener.accept(getThis());
        }
    }

    @Nullable
    public Consumer<W> getOnUpdateListener() {
        return this.onUpdateListener;
    }

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

    public W onUpdateListener(Consumer<W> listener) {
        return onUpdateListener(listener, false);
    }

    public W onUpdateListener(Consumer<W> listener, boolean merge) {
        if (merge && this.onUpdateListener != null) {
            if (listener != null) {
                this.onUpdateListener = w -> {
                    this.onUpdateListener.accept(w);
                    listener.accept(w);
                };
            }
        } else {
            this.onUpdateListener = listener;
        }
        return getThis();
    }

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

    @Override
    public Area getArea() {
        return this.area;
    }

    @Override
    public Flex getFlex() {
        return this.flex;
    }

    @Override
    public Flex flex() {
        return this.flex;
    }

    @NotNull
    @Override
    public IResizeable resizer() {
        return this.resizer;
    }

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

    @Override
    public boolean isValid() {
        return this.valid;
    }

    @Override
    public ModularScreen getScreen() {
        return getPanel().getScreen();
    }

    @Override
    public @NotNull ModularPanel getPanel() {
        if (!isValid()) {
            throw new IllegalStateException(this + " is not in a valid state!");
        }
        return this.panel;
    }

    @Override
    public @NotNull IWidget getParent() {
        if (!isValid()) {
            throw new IllegalStateException(this + " is not in a valid state!");
        }
        return this.parent;
    }

    @Override
    public ModularGuiContext getContext() {
        if (!isValid()) {
            throw new IllegalStateException(this + " is not in a valid state!");
        }
        return this.context;
    }

    @ApiStatus.Internal
    protected final void setContext(ModularGuiContext context) {
        this.context = context;
    }

    // ---------------
    // === Syncing ===
    // --------------

    @Override
    public boolean isSynced() {
        return this.syncHandler != null;
    }

    @Override
    public @NotNull SyncHandler getSyncHandler() {
        if (this.syncHandler == null) {
            throw new IllegalStateException("Widget is not initialised or not synced!");
        }
        return this.syncHandler;
    }

    @Nullable
    public IValue<?> getValue() {
        return this.value;
    }

    @Override
    public W syncHandler(String name, int id) {
        this.syncKey = ModularSyncManager.makeSyncKey(name, id);
        return getThis();
    }

    protected void setValue(IValue<?> value) {
        this.value = value;
        if (value instanceof SyncHandler syncHandler1) {
            setSyncHandler(syncHandler1);
        }
    }

    /**
     * This intended to only be used when build the main panel in methods like {@link com.cleanroommc.modularui.api.IGuiHolder#buildUI(GuiData, com.cleanroommc.modularui.value.sync.PanelSyncManager, com.cleanroommc.modularui.screen.UISettings)}
     * since it's called on server and client. Otherwise, this will not work.
     */
    protected void setSyncHandler(@Nullable SyncHandler syncHandler) {
        this.syncHandler = syncHandler;
    }

    // -------------
    // === Other ===
    // -------------

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isExcludeAreaInJei() {
        return this.excludeAreaInJei;
    }

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

    public W debugName(String name) {
        this.debugName = name;
        return getThis();
    }

    @SuppressWarnings("unchecked")
    @Override
    public W getThis() {
        return (W) this;
    }

    @Override
    public String toString() {
        if (this.debugName != null) {
            return getClass().getSimpleName() + "#" + this.debugName;
        }
        return getClass().getSimpleName();
    }
}
