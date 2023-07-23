package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.value.IValue;
import com.cleanroommc.modularui.api.widget.*;
import com.cleanroommc.modularui.drawable.DrawableArray;
import com.cleanroommc.modularui.manager.GuiCreationContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.Tooltip;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.value.sync.GuiSyncHandler;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widget.sizer.Box;
import com.cleanroommc.modularui.widget.sizer.Flex;
import com.cleanroommc.modularui.widget.sizer.IResizeable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Widget<W extends Widget<W>> implements IWidget, IPositioned<W>, ITooltip<W>, ISynced<W> {

    public static final IDrawable[] EMPTY_BACKGROUND = {};

    private final Area area = new Area();
    private boolean enabled = true;
    private boolean valid = false;
    private List<IGuiAction> guiActionListeners;

    private IWidget parent = null;
    private ModularPanel panel = null;
    private GuiContext context = null;

    private Flex flex = new Flex(this);
    private IResizeable resizer = this.flex;
    private String debugName;

    @Nullable
    private IValue<?> value;
    @Nullable
    private String syncKey;
    @Nullable
    private SyncHandler syncHandler;

    @Nullable
    private IDrawable background = null;
    @Nullable
    private IDrawable overlay = null;
    @Nullable
    private IDrawable hoverBackground = null;
    @Nullable
    private IDrawable hoverOverlay = null;
    @Nullable
    private Tooltip tooltip;

    @ApiStatus.Internal
    @Override
    public void initialise(@NotNull IWidget parent) {
        if (this instanceof ModularPanel) {
            getArea().z(2);
        } else {
            this.parent = parent;
            this.panel = parent.getPanel();
            this.context = parent.getContext();
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
            initialiseSyncHandler(getScreen().getSyncHandler());
        }
        applyTheme(this.context.getTheme());
        onInit();
        if (this.tooltip != null && this.tooltip.getExcludeArea() == null && ModularUIConfig.placeTooltipNextToPanel()) {
            this.tooltip.excludeArea(getPanel().getArea());
        }
        if (hasChildren()) {
            for (IWidget child : getChildren()) {
                child.initialise(this);
            }
        }
        if (getScreen().getMainPanel() == this) {
            getArea().z(1);
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
    public void initialiseSyncHandler(GuiSyncHandler syncHandler) {
        if (this.syncKey != null) {
            this.syncHandler = syncHandler.getSyncHandler(this.syncKey);
            if (!isValidSyncHandler(this.syncHandler)) {
                String type = this.syncHandler == null ? null : this.syncHandler.getClass().getName();
                this.syncHandler = null;
                throw new IllegalStateException("SyncHandler of type " + type + " is not valid for " + getClass().getName() + ", with key " + this.syncKey);
            }
            if (this.syncHandler instanceof ValueSyncHandler && ((ValueSyncHandler<?>) this.syncHandler).getChangeListener() == null) {
                ((ValueSyncHandler<?>) this.syncHandler).setChangeListener(this::markDirty);
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
        if (!(this instanceof ModularPanel)) {
            this.panel = null;
            this.parent = null;
            this.context = null;
        }
        this.valid = false;
        if (hasChildren()) {
            for (IWidget child : getChildren()) {
                child.dispose();
            }
        }
    }

    @Override
    public void drawBackground(GuiContext context) {
        WidgetTheme widgetTheme = getWidgetTheme(context.getTheme());
        IDrawable bg = getCurrentBackground();
        if (bg != null) {
            bg.applyThemeColor(context.getTheme(), widgetTheme);
            bg.drawAtZero(context, getArea());
        }
        bg = getCurrentOverlay();
        if (bg != null) {
            bg.applyThemeColor(context.getTheme(), widgetTheme);
            Box padding = getArea().getPadding();
            bg.draw(context, padding.left, padding.top, getArea().width - padding.horizontal(), getArea().height - padding.vertical());
        }
    }

    @Override
    public void draw(GuiContext context) {
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
        applyThemeBackground(false, widgetTheme.getBackground());
        applyThemeBackground(true, widgetTheme.getHoverBackground());
    }

    @Override
    public void onFrameUpdate() {
    }

    @Override
    public Area getArea() {
        return area;
    }

    @SuppressWarnings("unchecked")
    @Override
    public W getThis() {
        return (W) this;
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
        return panel;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public void markDirty() {
        if (this.tooltip != null) {
            this.tooltip.markDirty();
        }
    }

    @Override
    public @NotNull IWidget getParent() {
        if (!isValid()) {
            throw new IllegalStateException(getClass().getSimpleName() + " is not in a valid state!");
        }
        return parent;
    }

    @Override
    public GuiContext getContext() {
        if (!isValid()) {
            throw new IllegalStateException(getClass().getSimpleName() + " is not in a valid state!");
        }
        return context;
    }

    /**
     * Do not override this. Override {@link IWidget#getWidgetTheme(ITheme)} instead.
     */
    public final WidgetTheme getWidgetTheme() {
        return getWidgetTheme(getContext().getTheme());
    }

    protected final void setContext(GuiContext context) {
        this.context = context;
    }

    protected void applyThemeBackground(boolean hover, IDrawable drawable) {
        if (hover) {
            if (this.hoverBackground == null) {
                this.hoverBackground = drawable;
            }
        } else {
            if (this.background == null) {
                this.background = drawable;
            }
        }
    }

    public @Nullable IDrawable getBackground() {
        return background;
    }

    public @Nullable IDrawable getOverlay() {
        return overlay;
    }

    public @Nullable IDrawable getHoverBackground() {
        return hoverBackground;
    }

    public @Nullable IDrawable getHoverOverlay() {
        return hoverOverlay;
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
        return tooltip;
    }

    @Override
    public @NotNull Tooltip tooltip() {
        if (this.tooltip == null) {
            this.tooltip = new Tooltip();
            if (!ModularUIConfig.placeTooltipNextToPanel()) {
                this.tooltip.excludeArea(getArea());
            }
        }
        return this.tooltip;
    }

    @Override
    public Flex getFlex() {
        return flex;
    }

    @Override
    public Flex flex() {
        if (this.flex == null) {
            this.flex = new Flex(this);

            if (this.resizer == null) {
                this.resizer = flex;
            }
        }
        return this.flex;
    }

    @Override
    public IResizeable resizer() {
        return resizer;
    }

    @Override
    public void resizer(IResizeable resizer) {
        this.resizer = resizer;
    }

    @Override
    public boolean isSynced() {
        return this.syncHandler != null;
    }

    @Override
    public @NotNull SyncHandler getSyncHandler() {
        if (this.syncHandler == null) {
            throw new IllegalStateException("Widget is not initialised or not synced!");
        }
        return syncHandler;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Nullable
    public IValue<?> getValue() {
        return value;
    }

    public W disabled() {
        setEnabled(false);
        return getThis();
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

    public W debugName(String name) {
        this.debugName = name;
        return getThis();
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
     * This intended to only be used when build the main panel in methods like {@link com.cleanroommc.modularui.api.IGuiHolder#buildUI(GuiCreationContext, GuiSyncHandler, boolean)}
     * since it's called on server and client. Otherwise, this will not work.
     */
    protected void setSyncHandler(@Nullable SyncHandler syncHandler) {
        this.syncHandler = syncHandler;
    }

    @Override
    public String toString() {
        if (debugName != null) {
            return getClass().getSimpleName() + "#" + debugName;
        }
        return getClass().getSimpleName();
    }
}
