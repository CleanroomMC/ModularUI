package com.cleanroommc.modularui.api.screen;

import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.animation.Eases;
import com.cleanroommc.modularui.api.animation.Interpolator;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Color;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.api.widget.*;
import com.cleanroommc.modularui.common.internal.Theme;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * A window in a modular gui. Only the "main" window can exist on both, server and client.
 * All other only exist and needs to be opened on client.
 */
public class ModularWindow implements IWidgetParent {

    public static Builder builder(int width, int height) {
        return new Builder(new Size(width, height));
    }

    public static Builder builder(Size size) {
        return new Builder(size);
    }

    public static Builder builderFullScreen() {
        return new Builder(Size.ZERO);
    }

    private ModularUIContext context;
    private final List<Widget> children;
    public final ImmutableBiMap<Integer, ISyncedWidget> syncedWidgets;
    private final BiMap<Integer, ISyncedWidget> dynamicSyncedWidgets = HashBiMap.create();
    private final List<Interactable> interactionListeners = new ArrayList<>();
    protected boolean initialized = false;
    protected boolean clientOnly = true;

    private final boolean fullScreen;
    private Size size;
    private Pos2d pos = Pos2d.ZERO;
    private final Alignment alignment = Alignment.Center;
    private final IDrawable[] background;
    protected boolean draggable;
    private boolean enabled = true;
    private boolean needsRebuild = false;
    private int alpha = Color.getAlpha(Theme.INSTANCE.getBackground());
    private float scale = 1f;
    private float rotation = 0;
    private float translateX = 0, translateY = 0;

    private Interpolator openAnimation, closeAnimation;

    public ModularWindow(Size size, List<Widget> children, IDrawable... background) {
        this.fullScreen = size.isZero();
        this.size = size;
        this.children = children;
        this.background = background;
        // latest point at which synced widgets can be added
        IWidgetParent.forEachByLayer(this, widget -> {
            if (widget instanceof IWidgetParent) {
                ((IWidgetParent) widget).initChildren();
            }
        });

        ImmutableBiMap.Builder<Integer, ISyncedWidget> syncedWidgetBuilder = ImmutableBiMap.builder();
        AtomicInteger i = new AtomicInteger();
        IWidgetParent.forEachByLayer(this, widget -> {
            if (widget instanceof ISyncedWidget) {
                syncedWidgetBuilder.put(i.getAndIncrement(), (ISyncedWidget) widget);
            }
            if (i.get() == 0x10000) {
                throw new IndexOutOfBoundsException("Too many synced widgets!");
            }
            return false;
        });
        this.syncedWidgets = syncedWidgetBuilder.build();
    }

    protected void initialize(ModularUIContext context) {
        this.context = context;
        for (Widget widget : children) {
            widget.initialize(this, this, 0);
        }
    }

    public void onResize(Size screenSize) {
        if (this.fullScreen) {
            this.size = screenSize;
            this.pos = Pos2d.ZERO;
        } else {
            this.pos = alignment.getAlignedPos(screenSize, size);
        }
        markNeedsRebuild();
    }

    public static boolean anyAnimation() {
        return ModularUIConfig.animations.openCloseFade ||
                ModularUIConfig.animations.openCloseTranslateFromBottom ||
                ModularUIConfig.animations.openCloseScale ||
                ModularUIConfig.animations.openCloseRotateFast;
    }

    /**
     * The final call after the window is initialized & positioned
     */
    public void onOpen() {
        if (openAnimation == null && anyAnimation()) {
            final int startY = context.getScaledScreenSize().height - pos.y;
            openAnimation = new Interpolator(0, 1, 250, Eases.EaseQuadOut, value -> {
                float val = (float) value;
                if (ModularUIConfig.animations.openCloseFade) {
                    alpha = (int) (val * Color.getAlpha(Theme.INSTANCE.getBackground()));
                }
                if (ModularUIConfig.animations.openCloseTranslateFromBottom) {
                    translateY = startY * (1 - val);
                }
                if (ModularUIConfig.animations.openCloseScale) {
                    scale = val;
                }
                if (ModularUIConfig.animations.openCloseRotateFast) {
                    rotation = val * 360;
                }
            }, val -> {
                alpha = Color.getAlpha(Theme.INSTANCE.getBackground());
                translateX = 0;
                translateY = 0;
                scale = 1f;
                rotation = 360;
            });
            closeAnimation = openAnimation.getReversed(250, Eases.EaseQuadIn);
            openAnimation.forward();
            closeAnimation.setCallback(val -> {
                closeWindow();
                openAnimation = null;
                closeAnimation = null;
            });
        }
        //this.pos = new Pos2d(pos.x, getContext().getScaledScreenSize().height);
    }

    /**
     * Called when the player tries to close the ui. Starts animation or closes directly.
     */
    public void tryClose() {
        if (closeAnimation == null) {
            closeWindow();
        } else if (!closeAnimation.isRunning()) {
            closeAnimation.forward();
        }
    }

    public void update() {
        for (IDrawable drawable : background) {
            drawable.tick();
        }
        IWidgetParent.forEachByLayer(this, widget -> {
            widget.onScreenUpdate();
            Consumer<Widget> ticker = widget.getTicker();
            if (ticker != null) {
                ticker.accept(widget);
            }
            IDrawable[] background = widget.getBackground();
            if (background != null) {
                for (IDrawable drawable : background) {
                    if (drawable != null) {
                        drawable.tick();
                    }
                }
            }
        });
        if (needsRebuild) {
            rebuild();
        }
    }

    public void frameUpdate(float partialTicks) {
        if (openAnimation != null) {
            openAnimation.update(partialTicks);
        }
        if (closeAnimation != null) {
            closeAnimation.update(partialTicks);
        }
    }

    public void serverUpdate() {
        for (ISyncedWidget syncedWidget : syncedWidgets.values()) {
            syncedWidget.detectAndSendChanges();
        }
    }

    @SideOnly(Side.CLIENT)
    protected void rebuild() {
        // check auto size of each child from top to bottom
        for (Widget child : getChildren()) {
            child.buildTopToBottom(size.asDimension());
        }
        // position widgets from bottom to top
        for (Widget child : getChildren()) {
            child.buildBottomToTop();
        }
        needsRebuild = false;
    }

    public void closeWindow() {
        context.closeWindow(this);
    }

    protected void destroyWindow() {
        IWidgetParent.forEachByLayer(this, widget -> {
            if (isEnabled()) {
                widget.onPause();
            }
            widget.onDestroy();
        });
    }

    public void drawWidgets(float partialTicks, boolean foreground) {
        if (foreground) {
            IWidgetParent.forEachByLayer(this, widget -> {
                widget.drawInForeground(partialTicks);
                return false;
            });

        } else {
            GlStateManager.pushMatrix();
            // rotate around center
            if (ModularUIConfig.animations.openCloseRotateFast) {
                GlStateManager.translate(pos.x + size.width / 2f, pos.y + size.height / 2f, 0);
                GlStateManager.rotate(rotation, 0, 0, 1);
                GlStateManager.translate(-(pos.x + size.width / 2f), -(pos.y + size.height / 2f), 0);
            }
            GlStateManager.translate(translateX, translateY, 0);
            GlStateManager.scale(scale, scale, 1);

            GlStateManager.pushMatrix();
            float x = (pos.x + size.width / 2f * (1 - scale)) / scale;
            float y = (pos.y + size.height / 2f * (1 - scale)) / scale;
            GlStateManager.translate(x, y, 0);
            int color = Color.withAlpha(Theme.INSTANCE.getBackground(), alpha);
            for (IDrawable drawable : background) {
                drawable.applyThemeColor(color);
                drawable.draw(Pos2d.ZERO, size, partialTicks);
            }
            GlStateManager.popMatrix();

            for (Widget widget : getChildren()) {
                widget.drawInternal(partialTicks);
            }
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public Size getSize() {
        return size;
    }

    @Override
    public Pos2d getAbsolutePos() {
        return pos;
    }

    @Override
    public Pos2d getPos() {
        return pos;
    }

    @Override
    public List<Widget> getChildren() {
        return children;
    }

    public boolean isDraggable() {
        return draggable;
    }

    public ModularUIContext getContext() {
        return context;
    }

    public void markNeedsRebuild() {
        this.needsRebuild = true;
    }

    public void setPos(Pos2d pos) {
        this.pos = pos;
    }

    public boolean doesNeedRebuild() {
        return needsRebuild;
    }

    public float getScale() {
        return scale;
    }

    public int getAlpha() {
        return alpha;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (this.enabled) {
                IWidgetParent.forEachByLayer(this, Widget::onResume);
            } else {
                IWidgetParent.forEachByLayer(this, Widget::onPause);
            }
        }
    }

    public Rectangle getRectangle() {
        return new Rectangle(pos.x, pos.y, size.width, size.height);
    }

    /**
     * The events of the added listeners are always called.
     */
    public void addInteractionListener(Interactable interactable) {
        interactionListeners.add(interactable);
    }

    public List<Interactable> getInteractionListeners() {
        return interactionListeners;
    }

    /**
     * Adds a dynamic synced widget.
     *
     * @param id           a id for the synced widget. Must be 32768 > id > 0
     * @param syncedWidget dynamic synced widget
     * @param parent       the synced widget that added the dynamic widget
     * @throws IllegalArgumentException if id is > 32767 or < 1
     * @throws NullPointerException     if dynamic widget or parent is null
     * @throws IllegalStateException    if parent is a dynamic widget
     */
    public void addDynamicSyncedWidget(int id, ISyncedWidget syncedWidget, ISyncedWidget parent) {
        if (id <= 0 || id > 0xFFFF) {
            throw new IllegalArgumentException("Dynamic Synced widget id must be greater than 0 and smaller than 65535 (0xFFFF)");
        }
        if (syncedWidget == null || parent == null) {
            throw new NullPointerException("Can't add dynamic null widget or with null parent!");
        }
        if (dynamicSyncedWidgets.containsValue(syncedWidget)) {
            dynamicSyncedWidgets.inverse().remove(syncedWidget);
        }
        int parentId = getSyncedWidgetId(parent);
        if ((parentId & ~0xFFFF) != 0) {
            throw new IllegalStateException("Dynamic synced widgets can't have other dynamic widgets as parent! It's possible with some trickery tho.");
        }
        // generate unique id
        // first 2 bytes is passed id, last 2 bytes is parent id
        id = ((id << 16) & ~0xFFFF) | parentId;
        dynamicSyncedWidgets.put(id, syncedWidget);
    }

    public int getSyncedWidgetId(ISyncedWidget syncedWidget) {
        Integer id = syncedWidgets.inverse().get(syncedWidget);
        if (id == null) {
            id = dynamicSyncedWidgets.inverse().get(syncedWidget);
            if (id == null) {
                throw new NoSuchElementException("Can't find id for ISyncedWidget " + syncedWidget);
            }
        }
        return id;
    }

    public ISyncedWidget getSyncedWidget(int id) {
        ISyncedWidget syncedWidget = syncedWidgets.get(id);
        if (syncedWidget == null) {
            syncedWidget = dynamicSyncedWidgets.get(id);
            if (syncedWidget == null) {
                throw new NoSuchElementException("Can't find ISyncedWidget for id " + id);
            }
        }
        return syncedWidget;
    }

    public static class Builder implements IWidgetBuilder<Builder> {

        private final List<Widget> widgets = new ArrayList<>();
        private IDrawable[] background = {};
        private Size size;
        private boolean draggable = true;

        private Builder(Size size) {
            this.size = size;
        }

        public Builder setBackground(IDrawable... background) {
            this.background = background;
            return this;
        }

        public Builder setSize(Size size) {
            this.size = size;
            return this;
        }

        public Builder setSize(int width, int height) {
            return setSize(new Size(width, height));
        }

        public Builder setDraggable(boolean draggable) {
            this.draggable = draggable;
            return this;
        }

        public Builder bindPlayerInventory(EntityPlayer player, int marginBottom) {
            return bindPlayerInventory(player, new Pos2d(size.width / 2 - 81, size.height - marginBottom - 76));
        }

        public Builder bindPlayerInventory(EntityPlayer player) {
            return bindPlayerInventory(player, 7);
        }

        @Override
        public void addWidgetInternal(Widget widget) {
            widgets.add(widget);
        }

        public ModularWindow build() {
            ModularWindow window = new ModularWindow(size, widgets, background);
            window.draggable = draggable;
            return window;
        }
    }
}
