package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.layout.IResizeable;
import com.cleanroommc.modularui.api.value.IValue;
import com.cleanroommc.modularui.api.widget.*;
import com.cleanroommc.modularui.drawable.DrawableArray;
import com.cleanroommc.modularui.manager.GuiCreationContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.Tooltip;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widget.sizer.Box;
import com.cleanroommc.modularui.widget.sizer.Flex;
import com.cleanroommc.modularui.widget.sizer.IUnResizeable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Widget<W extends Widget<W>> implements IWidget, IPositioned<W>, ITooltip<W>, ISynced<W> {

    // other
    @Nullable private String debugName;
    private boolean enabled = true;
    // gui context
    private boolean valid = false;
    private IWidget parent = null;
    private ModularPanel panel = null;
    private GuiContext context = null;
    // sizing
    private final Area area = new Area();
    private final Flex flex = new Flex(this);
    private IResizeable resizer = this.flex;
    // syncing
    @Nullable private IValue<?> value;
    @Nullable private String syncKey;
    @Nullable private SyncHandler syncHandler;
    // rendering
    @Nullable private IDrawable background = null;
    @Nullable private IDrawable overlay = null;
    @Nullable private IDrawable hoverBackground = null;
    @Nullable private IDrawable hoverOverlay = null;
    @Nullable private Tooltip tooltip;
    // listener
    @Nullable private List<IGuiAction> guiActionListeners;
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
                    this.context.screen.registerGuiActionListener(action);
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
        applyTheme(this.context.getTheme());
        onInit();
        if (hasChildren()) {
            for (IWidget child : getChildren()) {
                child.initialise(this);
            }
        }
        afterInit();
    }

    @ApiStatus.OverrideOnly
    public void onInit() {
    }

    @ApiStatus.OverrideOnly
    public void afterInit() {
    }

    @Override
    public void initialiseSyncHandler(GuiSyncManager syncHandler) {
        if (this.syncKey != null) {
            this.syncHandler = syncHandler.getSyncHandler(this.syncKey);
            if (!isValidSyncHandler(this.syncHandler)) {
                String type = this.syncHandler == null ? null : this.syncHandler.getClass().getName();
                this.syncHandler = null;
                throw new IllegalStateException("SyncHandler of type " + type + " is not valid for " + getClass().getName() + ", with key " + this.syncKey);
            }
            if (this.syncHandler instanceof ValueSyncHandler && ((ValueSyncHandler<?>) this.syncHandler).getChangeListener() == null) {
                ((ValueSyncHandler<?>) this.syncHandler).setChangeListener(this::markTooltipDirty);
            }
        }
    }

    @Override
    public void dispose() {
        if (this.guiActionListeners != null) {
            for (IGuiAction action : this.guiActionListeners) {
                this.context.screen.removeGuiActionListener(action);
            }
        }
        if (!getPanel().isMainPanel() && this.syncHandler != null) {
            getScreen().getSyncManager().disposeSyncHandler(this.syncHandler);
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
    public void drawBackground(GuiContext context, WidgetTheme widgetTheme) {
        IDrawable bg = getCurrentBackground();
        if (bg != null) {
            bg.applyThemeColor(context.getTheme(), widgetTheme);
            bg.drawAtZero(context, getArea());
        }
    }

    @Override
    public void draw(GuiContext context, WidgetTheme widgetTheme) {
    }

    @Override
    public void drawOverlay(GuiContext context, WidgetTheme widgetTheme) {
        IDrawable bg = getCurrentOverlay();
        if (bg != null) {
            bg.applyThemeColor(context.getTheme(), widgetTheme);
            Box padding = getArea().getPadding();
            bg.draw(context, padding.left, padding.top, getArea().width - padding.horizontal(), getArea().height - padding.vertical());
        }
    }

    @Override
    public void drawForeground(GuiContext context) {
        Tooltip tooltip = getTooltip();
        if (tooltip != null && isHoveringFor(tooltip.getShowUpTimer())) {
            tooltip.draw(getContext());
        }
    }

    @Override
    public void applyTheme(ITheme theme) {
        WidgetTheme widgetTheme = getWidgetTheme(theme);
        if (this.background == null) {
            this.background = widgetTheme.getBackground();
        }
        if (this.hoverBackground == null) {
            this.hoverBackground = widgetTheme.getHoverBackground();
        }
    }

    /**
     * Do not override this. Override {@link IWidget#getWidgetTheme(ITheme)} instead.
     */
    public final WidgetTheme getWidgetTheme() {
        return getWidgetTheme(getContext().getTheme());
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

    public IDrawable getCurrentBackground() {
        IDrawable hoverBackground = getHoverBackground();
        return hoverBackground != null && isHovering() ? hoverBackground : getBackground();
    }

    public IDrawable getCurrentOverlay() {
        IDrawable hoverBackground = getHoverOverlay();
        return hoverBackground != null && isHovering() ? hoverBackground : getOverlay();
    }

    @Nullable
    @Override
    public Tooltip getTooltip() {
        return this.tooltip;
    }

    @Override
    public @NotNull Tooltip tooltip() {
        if (this.tooltip == null) {
            this.tooltip = new Tooltip().excludeArea(getArea());
        }
        return this.tooltip;
    }

    @Override
    public void markTooltipDirty() {
        if (this.tooltip != null) {
            this.tooltip.markDirty();
        }
    }

    public W background(IDrawable... background) {
        if (background.length == 0) {
            this.background = null;
        } else if (background.length == 1) {
            this.background = background[0];
        } else {
            this.background = new DrawableArray(background);
        }
        return getThis();
    }

    public W overlay(IDrawable... overlay) {
        if (overlay.length == 0) {
            this.overlay = null;
        } else if (overlay.length == 1) {
            this.overlay = overlay[0];
        } else {
            this.overlay = new DrawableArray(overlay);
        }
        return getThis();
    }

    public W hoverBackground(IDrawable... background) {
        if (background.length == 0) {
            this.hoverBackground = null;
        } else if (background.length == 1) {
            this.hoverBackground = background[0];
        } else {
            this.hoverBackground = new DrawableArray(background);
        }
        return getThis();
    }

    public W hoverOverlay(IDrawable... overlay) {
        if (overlay.length == 0) {
            this.hoverOverlay = null;
        } else if (overlay.length == 1) {
            this.hoverOverlay = overlay[0];
        } else {
            this.hoverOverlay = new DrawableArray(overlay);
        }
        return getThis();
    }

    // --------------
    // === Events ===
    // --------------

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
            this.context.screen.registerGuiActionListener(action);
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
        return onUpdateListener(w -> {
            setEnabled(condition.test(w));
        }, true);
    }

    // ----------------
    // === Resizing ===
    // ----------------

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

    @Override
    public IResizeable resizer() {
        return this.resizer;
    }

    @Override
    public void resizer(IResizeable resizer) {
        this.resizer = resizer != null ? resizer : IUnResizeable.INSTANCE;
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
            throw new IllegalStateException(getClass().getSimpleName() + " is not in a valid state!");
        }
        return this.panel;
    }

    @Override
    public @NotNull IWidget getParent() {
        if (!isValid()) {
            throw new IllegalStateException(getClass().getSimpleName() + " is not in a valid state!");
        }
        return this.parent;
    }

    @Override
    public GuiContext getContext() {
        if (!isValid()) {
            throw new IllegalStateException(getClass().getSimpleName() + " is not in a valid state!");
        }
        return this.context;
    }

    protected final void setContext(GuiContext context) {
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
    public W syncHandler(String key) {
        this.syncKey = key;
        return getThis();
    }

    protected void setValue(IValue<?> value) {
        this.value = value;
        if (value instanceof SyncHandler) {
            setSyncHandler((SyncHandler) value);
        }
    }

    /**
     * This intended to only be used when build the main panel in methods like {@link com.cleanroommc.modularui.api.IGuiHolder#buildUI(GuiCreationContext, GuiSyncManager, boolean)}
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

    public W disabled() {
        setEnabled(false);
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
