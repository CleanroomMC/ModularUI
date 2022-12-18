package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.IDrawable;
import com.cleanroommc.modularui.api.IWidget;
import com.cleanroommc.modularui.api.SyncHandler;
import com.cleanroommc.modularui.screen.GuiContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import com.cleanroommc.modularui.sync.MapKey;
import com.cleanroommc.modularui.widget.resizer.Box;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widget.sizer.Flex;
import com.cleanroommc.modularui.widget.sizer.IResizeable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class Widget<W extends Widget<W>> implements IWidget {

    public static final IDrawable[] EMPTY_BACKGROUND = {};

    private final Area area = new Area();
    private boolean enabled = true;
    private boolean valid = false;

    private IWidget parent = null;
    private ModularPanel panel = null;
    private GuiContext context = null;

    private Flex flex;
    private IResizeable resizer;
    private Box margin = new Box();
    private Box padding = new Box();
    private String debugName;

    @Nullable
    private MapKey syncKey;
    @Nullable
    private SyncHandler syncHandler;

    @NotNull
    private IDrawable[] background = EMPTY_BACKGROUND;

    protected final void setContext(GuiContext context) {
        this.context = context;
    }

    @ApiStatus.Internal
    @Override
    public final void initialise(@NotNull IWidget parent) {
        if (this instanceof ModularPanel) {
            this.area.z(1);
        } else {
            this.parent = parent;
            this.panel = parent.getPanel();
            this.context = parent.getContext();
            this.area.z(parent.getArea().z() + 1);
        }
        this.valid = true;
        onInit();
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

    public IDrawable[] getBackground() {
        return background;
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

    @Override
    public Box getMargin() {
        return margin;
    }

    @Override
    public Box getPadding() {
        return padding;
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

    @SuppressWarnings("all")
    protected W getThis() {
        return (W) this;
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

    public W flex(Consumer<Flex> flexConsumer) {
        flexConsumer.accept(flex());
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
