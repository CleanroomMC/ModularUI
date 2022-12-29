package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.sync.SyncHandler;
import com.cleanroommc.modularui.api.sync.ValueSyncHandler;
import com.cleanroommc.modularui.api.widget.IGuiAction;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.GuiContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.Tooltip;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import com.cleanroommc.modularui.sync.MapKey;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widget.sizer.Flex;
import com.cleanroommc.modularui.widget.sizer.IResizeable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class Widget<W extends Widget<W>> implements IWidget {

    public static final IDrawable[] EMPTY_BACKGROUND = {};

    private final Area area = new Area();
    private boolean enabled = true;
    private boolean valid = false;
    private List<IGuiAction> guiActionListeners;

    private IWidget parent = null;
    private ModularPanel panel = null;
    private GuiContext context = null;

    private Flex flex;
    private IResizeable resizer;
    private String debugName;

    @Nullable
    private MapKey syncKey;
    @Nullable
    private SyncHandler syncHandler;

    @NotNull
    private IDrawable[] background = EMPTY_BACKGROUND;
    @Nullable
    private Tooltip tooltip;

    @ApiStatus.Internal
    @Override
    public final void initialise(@NotNull IWidget parent) {
        if (this instanceof ModularPanel) {
            getArea().z(1);
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
        this.valid = true;
        onInit();
        if (this.tooltip != null && this.tooltip.getExcludeArea() == null && ModularUIConfig.placeTooltipNextToPanel()) {
            this.tooltip.excludeArea(getPanel().getArea());
        }
        if (hasChildren()) {
            for (IWidget child : getChildren()) {
                child.initialise(this);
            }
        }
    }

    @ApiStatus.OverrideOnly
    public void onInit() {
    }

    public void initialiseSyncHandler(GuiSyncHandler syncHandler) {
        if (this.syncKey != null) {
            this.syncHandler = syncHandler.getSyncHandler(this.syncKey);
            if (!isValidSyncHandler(this.syncHandler)) {
                this.syncHandler = null;
                throw new IllegalStateException();
            }
            if (this.syncHandler instanceof ValueSyncHandler && ((ValueSyncHandler<?>) this.syncHandler).getChangeListener() == null) {
                ((ValueSyncHandler<?>) this.syncHandler).setChangeListener(this::markDirty);
            }
        }
        if (hasChildren()) {
            for (IWidget child : getChildren()) {
                if (child instanceof Widget) {
                    ((Widget<?>) child).initialiseSyncHandler(syncHandler);
                }
            }
        }
    }

    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        return true;
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
    public void drawBackground(float partialTicks) {
        for (IDrawable drawable : getBackground()) {
            drawable.draw(0, 0, getArea().width, getArea().height);
        }
    }

    @Override
    public void draw(float partialTicks) {
    }

    @Override
    public void drawForeground(float partialTicks) {
        if (this.tooltip != null && isHoveringFor(this.tooltip.getShowUpTimer())) {
            this.tooltip.draw(getContext(), partialTicks);
        }
    }

    @Override
    public void onFrameUpdate() {
    }

    @Override
    public Area getArea() {
        return area;
    }

    @Override
    public ModularScreen getScreen() {
        return getPanel().getScreen();
    }

    @Override
    public @NotNull ModularPanel getPanel() {
        if (!isValid()) {
            throw new IllegalStateException("Widget is not in a valid state!");
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
    public boolean canBeSeen() {
        return true; // TODO
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
            throw new IllegalStateException("Widget is not in a valid state!");
        }
        return parent;
    }

    @Override
    public GuiContext getContext() {
        if (!isValid()) {
            throw new IllegalStateException("Widget is not in a valid state!");
        }
        return context;
    }

    protected final void setContext(GuiContext context) {
        this.context = context;
    }

    public IDrawable[] getBackground() {
        return background;
    }

    @Nullable
    @Override
    public Tooltip getTooltip() {
        return tooltip;
    }

    @Nullable
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

    public SyncHandler getSyncHandler() {
        if (this.syncKey == null) {
            throw new IllegalStateException("Widget is not synced!");
        }
        if (this.syncHandler == null) {
            throw new IllegalStateException("Widget is not initialised!");
        }
        return syncHandler;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @SuppressWarnings("all")
    protected W getThis() {
        return (W) this;
    }

    public W disabled() {
        setEnabled(false);
        return getThis();
    }

    public W background(IDrawable... background) {
        this.background = background == null ? EMPTY_BACKGROUND : background;
        return getThis();
    }

    public W pos(int x, int y) {
        flex().left(x).top(y);
        return getThis();
    }

    public W size(int w, int h) {
        flex().width(w).height(h);
        return getThis();
    }

    public W coverChildren() {
        flex().coverChildren();
        return getThis();
    }

    public W coverChildrenWidth() {
        flex().coverChildrenWidth();
        return getThis();
    }

    public W coverChildrenHeight() {
        flex().coverChildrenHeight();
        return getThis();
    }

    public W flex(Consumer<Flex> flexConsumer) {
        flexConsumer.accept(flex());
        return getThis();
    }

    public W padding(int left, int right, int top, int bottom) {
        getArea().getPadding().all(left, right, top, bottom);
        return getThis();
    }

    public W padding(int horizontal, int vertical) {
        getArea().getPadding().all(horizontal, vertical);
        return getThis();
    }

    public W padding(int all) {
        getArea().getPadding().all(all);
        return getThis();
    }

    public W paddingLeft(int val) {
        getArea().getPadding().left(val);
        return getThis();
    }

    public W paddingRight(int val) {
        getArea().getPadding().right(val);
        return getThis();
    }

    public W paddingTop(int val) {
        getArea().getPadding().top(val);
        return getThis();
    }

    public W paddingBottom(int val) {
        getArea().getPadding().bottom(val);
        return getThis();
    }

    public W margin(int left, int right, int top, int bottom) {
        getArea().getMargin().all(left, right, top, bottom);
        return getThis();
    }

    public W margin(int horizontal, int vertical) {
        getArea().getMargin().all(horizontal, vertical);
        return getThis();
    }

    public W margin(int all) {
        getArea().getMargin().all(all);
        return getThis();
    }

    public W marginLeft(int val) {
        getArea().getMargin().left(val);
        return getThis();
    }

    public W marginRight(int val) {
        getArea().getMargin().right(val);
        return getThis();
    }

    public W marginTop(int val) {
        getArea().getMargin().top(val);
        return getThis();
    }

    public W marginBottom(int val) {
        getArea().getMargin().bottom(val);
        return getThis();
    }

    public Tooltip tooltip() {
        if (this.tooltip == null) {
            this.tooltip = new Tooltip();
            if (!ModularUIConfig.placeTooltipNextToPanel()) {
                this.tooltip.excludeArea(getArea());
            }
        }
        return this.tooltip;
    }

    public W tooltip(Consumer<Tooltip> tooltipConsumer) {
        tooltipConsumer.accept(tooltip());
        return getThis();
    }

    public W tooltipBuilder(Consumer<Tooltip> tooltipBuilder) {
        tooltip().tooltipBuilder(tooltipBuilder);
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

    public W setSynced(MapKey key) {
        this.syncKey = key;
        return getThis();
    }

    public W setSynced(String name, int id) {
        return setSynced(new MapKey(name, id));
    }

    public W setSynced(String name) {
        return setSynced(new MapKey(name));
    }

    public W setSynced(int id) {
        return setSynced(new MapKey(id));
    }

    @Override
    public String toString() {
        if (debugName != null) {
            return getClass().getSimpleName() + "#" + debugName;
        }
        return getClass().getSimpleName();
    }
}
