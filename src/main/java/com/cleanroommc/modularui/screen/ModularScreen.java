package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.MCHelper;
import com.cleanroommc.modularui.api.widget.IGuiAction;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.overlay.ScreenWrapper;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.ModularSyncManager;
import com.cleanroommc.modularui.widget.WidgetTree;
import com.cleanroommc.modularui.widget.sizer.Area;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * This is the base class for all modular UIs. It only exists on client side.
 * It handles drawing the screen, all panels and widget interactions.
 */
@SideOnly(Side.CLIENT)
public class ModularScreen {

    public static boolean isScreen(@Nullable GuiScreen guiScreen, String owner, String name) {
        if (guiScreen instanceof IMuiScreen screenWrapper) {
            ModularScreen screen = screenWrapper.getScreen();
            return screen.getOwner().equals(owner) && screen.getName().equals(name);
        }
        return false;
    }

    public static boolean isActive(String owner, String name) {
        return isScreen(Minecraft.getMinecraft().currentScreen, owner, name);
    }

    @Nullable
    public static ModularScreen getCurrent() {
        if (MCHelper.getCurrentScreen() instanceof IMuiScreen screenWrapper) {
            return screenWrapper.getScreen();
        }
        return null;
    }

    private final String owner;
    private final String name;
    private final PanelManager panelManager;
    private final ModularGuiContext context = new ModularGuiContext(this);
    private final Map<Class<?>, List<IGuiAction>> guiActionListeners = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectArrayMap<IWidget, Runnable> frameUpdates = new Object2ObjectArrayMap<>();
    private boolean pausesGame = false;

    private ITheme currentTheme;
    private IMuiScreen screenWrapper;
    private boolean overlay = false;

    /**
     * Creates a new screen with a ModularUI as its owner and a given {@link ModularPanel}.
     *
     * @param mainPanel main panel of this screen
     */
    public ModularScreen(@NotNull ModularPanel mainPanel) {
        this(ModularUI.ID, mainPanel);
    }

    /**
     * Creates a new screen with a given owner and {@link ModularPanel}.
     *
     * @param owner     owner of this screen (usually a mod id)
     * @param mainPanel main panel of this screen
     */
    public ModularScreen(@NotNull String owner, @NotNull ModularPanel mainPanel) {
        this(owner, context -> mainPanel);
    }

    /**
     * Creates a new screen with the given owner and a main panel function. The function must return a non-null value.
     *
     * @param owner            owner of this screen (usually a mod id)
     * @param mainPanelCreator function which creates the main panel of this screen
     */
    public ModularScreen(@NotNull String owner, @NotNull Function<ModularGuiContext, ModularPanel> mainPanelCreator) {
        this(owner, Objects.requireNonNull(mainPanelCreator, "The main panel function must not be null!"), false);
    }

    private ModularScreen(@NotNull String owner, @Nullable Function<ModularGuiContext, ModularPanel> mainPanelCreator, boolean ignored) {
        Objects.requireNonNull(owner, "The owner must not be null!");
        this.owner = owner;
        ModularPanel mainPanel = mainPanelCreator != null ? mainPanelCreator.apply(this.context) : buildUI(this.context);
        Objects.requireNonNull(mainPanel, "The main panel must not be null!");
        this.name = mainPanel.getName();
        this.currentTheme = IThemeApi.get().getThemeForScreen(this, null);
        this.panelManager = new PanelManager(this, mainPanel);
    }

    /**
     * Intended for use in {@link CustomModularScreen}
     */
    ModularScreen(@NotNull String owner) {
        this(owner, null, false);
    }

    /**
     * Intended for use in {@link CustomModularScreen}
     */
    ModularPanel buildUI(ModularGuiContext context) {
        throw new UnsupportedOperationException();
    }

    /**
     * Should be called in custom {@link GuiScreen GuiScreen} constructors which implement {@link IMuiScreen}.
     *
     * @param wrapper the gui screen wrapping this screen
     */
    @MustBeInvokedByOverriders
    public void construct(IMuiScreen wrapper) {
        if (this.screenWrapper != null) throw new IllegalStateException("ModularScreen is already constructed!");
        if (wrapper == null) throw new NullPointerException("GuiScreenWrapper must not be null!");
        this.screenWrapper = wrapper;
        if (this.screenWrapper.getGuiScreen() instanceof GuiContainer container) {
            ((ModularContainer) container.inventorySlots).initializeClient(this);
        }
        this.screenWrapper.updateGuiArea(this.panelManager.getMainPanel().getArea());
        this.overlay = false;
    }

    @ApiStatus.Internal
    @MustBeInvokedByOverriders
    public void constructOverlay(GuiScreen screen) {
        if (this.screenWrapper != null) throw new IllegalStateException("ModularScreen is already constructed!");
        if (screen == null) throw new NullPointerException("GuiScreenWrapper must not be null!");
        this.screenWrapper = new ScreenWrapper(screen, this);
        this.overlay = true;
    }

    /**
     * Called everytime the Game window changes its size. Overriding for additional logic is allowed, but super must be called.
     * This method resizes the entire widget tree of every panel currently open and then updates the size of the {@link IMuiScreen} wrapper.
     * <p>
     * Do not call this method except in an override!
     *
     * @param width  with of the resized game window
     * @param height height of the resized game window
     */
    @MustBeInvokedByOverriders
    public void onResize(int width, int height) {
        this.context.updateScreenArea(width, height);
        if (this.panelManager.tryInit()) {
            onOpen();
        }

        this.context.pushViewport(null, this.context.getScreenArea());
        for (ModularPanel panel : this.panelManager.getReverseOpenPanels()) {
            WidgetTree.resizeInternal(panel, true);
        }

        this.context.popViewport(null);
        if (!isOverlay()) {
            this.screenWrapper.updateGuiArea(this.panelManager.getMainPanel().getArea());
        }
    }

    /**
     * Called when another screen opens, but this screen is still open or this screen an overlay is and the gui screen parent closes.
     */
    @ApiStatus.Internal
    public final void onCloseParent() {
        this.panelManager.closeAll();
    }

    /**
     * Called after the screen is opened, but before the screen and all widgets are resized.
     */
    @ApiStatus.OverrideOnly
    public void onOpen() {}

    /**
     * Called after the last panel (always the main panel) closes which closes the screen.
     */
    @ApiStatus.OverrideOnly
    public void onClose() {}

    /**
     * Gently closes all open panels and this screen. If NeverEnoughAnimations is installed and open/close is enabled this will play the
     * animation for all open panels and closes the screen after the animation is finished.
     */
    public void close() {
        close(false);
    }

    /**
     * Closes all open panels and this screen. If {@code force} is true, the screen will immediately close and skip all lifecycle steps to
     * properly close panels and this screen. <b>This should be avoided in most situations</b>.
     * If {@code force} is false, the panels are gently closed. If NeverEnoughAnimations is installed and open/close is enabled this will
     * play the animation for all open panels and closes the screen after the animation is finished.
     *
     * @param force true if the screen should be closed immediately without going through remaining lifecycle steps.
     */
    @ApiStatus.Internal
    public void close(boolean force) {
        if (isActive()) {
            if (force) {
                MCHelper.closeScreen();
                return;
            }
            getMainPanel().closeIfOpen();
        }
    }

    /**
     * Checks if a panel with a given name is currently open in this screen.
     *
     * @param name name of the panel
     * @return true if a panel with the name is open
     */
    public boolean isPanelOpen(String name) {
        return this.panelManager.isPanelOpen(name);
    }

    /**
     * Checks if a panel is currently open in this screen.
     *
     * @param panel panel to check
     * @return true if the panel is open
     */
    public boolean isPanelOpen(ModularPanel panel) {
        return this.panelManager.hasOpenPanel(panel);
    }

    /**
     * Called at the start of every client tick (20 times per second).
     */
    @MustBeInvokedByOverriders
    public void onUpdate() {
        for (ModularPanel panel : this.panelManager.getOpenPanels()) {
            WidgetTree.onUpdate(panel);
        }
    }

    /**
     * Called 60 times per second in custom ticks. This logic is separate from rendering.
     *
     * @deprecated logic may be moved to rendering
     */
    @Deprecated
    @MustBeInvokedByOverriders
    public void onFrameUpdate() {
        this.panelManager.checkDirty();
        for (ObjectIterator<Object2ObjectMap.Entry<IWidget, Runnable>> iterator = this.frameUpdates.object2ObjectEntrySet().fastIterator(); iterator.hasNext(); ) {
            Object2ObjectMap.Entry<IWidget, Runnable> entry = iterator.next();
            if (!entry.getKey().isValid()) {
                iterator.remove();
                continue;
            }
            entry.getValue().run();
        }
        this.context.onFrameUpdate();
    }

    /**
     * Draws this screen and all open panels with their whole widget tree.
     * <p>
     * Do not call, only override!
     */
    public void drawScreen() {
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableAlpha();

        this.context.reset();
        this.context.pushViewport(null, this.context.getScreenArea());
        for (ModularPanel panel : this.panelManager.getReverseOpenPanels()) {
            this.context.updateZ(panel.getArea().getPanelLayer() * 20);
            if (panel.disablePanelsBelow()) {
                GuiDraw.drawRect(0, 0, this.context.getScreenArea().w(), this.context.getScreenArea().h(), Color.argb(16, 16, 16, (int) (125 * panel.getAlpha())));
            }
            WidgetTree.drawTree(panel, this.context);
        }
        this.context.updateZ(0);
        this.context.popViewport(null);

        this.context.postRenderCallbacks.forEach(element -> element.accept(this.context));
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableLighting();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableAlpha();
    }

    /**
     * Called after all panels with their whole widget trees and potential additional elements are drawn.
     * <p>
     * Do not call, only override!
     */
    public void drawForeground() {
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableAlpha();

        this.context.reset();
        this.context.pushViewport(null, this.context.getScreenArea());
        for (ModularPanel panel : this.panelManager.getReverseOpenPanels()) {
            this.context.updateZ(100 + panel.getArea().getPanelLayer() * 20);
            if (panel.isEnabled()) {
                WidgetTree.drawTreeForeground(panel, this.context);
            }
        }
        this.context.drawDraggable();
        this.context.popViewport(null);

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableLighting();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableAlpha();
    }

    /**
     * Called when a mouse button is pressed or released. Used to handle dropping of currently dragged elements.
     */
    @ApiStatus.Internal
    public final boolean onMouseInputPre(int button, boolean pressed) {
        if (this.context.hasDraggable()) {
            if (pressed) {
                this.context.onMousePressed(button);
            } else {
                this.context.onMouseReleased(button);
            }
            return true;
        }
        return false;
    }

    /**
     * Called when a mouse button is pressed. Tries to invoke
     * {@link com.cleanroommc.modularui.api.widget.Interactable#onMousePressed(int) Interactable#onMousePressed(int)} on every widget under
     * the mouse after gui action listeners have been called. Will try to focus widgets that have been interacted with.
     * Focused widgets will be interacted with first in other interaction methods (mouse scroll, release and drag, key press and release).
     *
     * @param mouseButton mouse button (0 = left button, 1 = right button, 2 = scroll button, 4 and 5 = side buttons)
     * @return true if the action was consumed and further processing should be canceled
     */
    public boolean onMousePressed(int mouseButton) {
        for (IGuiAction.MousePressed action : getGuiActionListeners(IGuiAction.MousePressed.class)) {
            action.press(mouseButton);
        }
        if (this.context.onMousePressed(mouseButton)) {
            return true;
        }
        for (ModularPanel panel : this.panelManager.getOpenPanels()) {
            if (panel.onMousePressed(mouseButton)) {
                return true;
            }
            if (panel.disablePanelsBelow()) {
                break;
            }
        }
        return false;
    }

    /**
     * Called when a mouse button is released. Tries to invoke
     * {@link com.cleanroommc.modularui.api.widget.Interactable#onMouseRelease(int) Interactable#onMouseRelease(int)} on every widget under
     * the mouse after gui action listeners have been called.
     *
     * @param mouseButton mouse button (0 = left button, 1 = right button, 2 = scroll button, 4 and 5 = side buttons)
     * @return true if the action was consumed and further processing should be canceled
     */
    public boolean onMouseRelease(int mouseButton) {
        for (IGuiAction.MouseReleased action : getGuiActionListeners(IGuiAction.MouseReleased.class)) {
            action.release(mouseButton);
        }
        if (this.context.onMouseReleased(mouseButton)) {
            return true;
        }
        for (ModularPanel panel : this.panelManager.getOpenPanels()) {
            if (panel.onMouseRelease(mouseButton)) {
                return true;
            }
            if (panel.disablePanelsBelow()) {
                break;
            }
        }
        return false;
    }

    /**
     * Called when a keyboard key is pressed. Tries to invoke
     * {@link com.cleanroommc.modularui.api.widget.Interactable#onKeyPressed(char, int) Interactable#onKeyPressed(char, int)} on every
     * widget under the mouse after gui action listeners have been called.
     *
     * @param typedChar the character of the pressed key or {@link Character#MIN_VALUE} for keys without a character
     * @param keyCode   the key code of the pressed key (see constants at {@link org.lwjgl.input.Keyboard})
     * @return true if the action was consumed and further processing should be canceled
     */
    public boolean onKeyPressed(char typedChar, int keyCode) {
        for (IGuiAction.KeyPressed action : getGuiActionListeners(IGuiAction.KeyPressed.class)) {
            action.press(typedChar, keyCode);
        }
        for (ModularPanel panel : this.panelManager.getOpenPanels()) {
            if (panel.onKeyPressed(typedChar, keyCode)) {
                return true;
            }
            if (panel.disablePanelsBelow()) {
                break;
            }
        }
        return false;
    }

    /**
     * Called when a keyboard key is released. Tries to invoke
     * {@link com.cleanroommc.modularui.api.widget.Interactable#onKeyRelease(char, int) Interactable#onKeyRelease(char, int)} on every
     * widget under the mouse after gui action listeners have been called.
     *
     * @param typedChar the character of the released key or {@link Character#MIN_VALUE} for keys without a character
     * @param keyCode   the key code of the released key (see constants at {@link org.lwjgl.input.Keyboard})
     * @return true if the action was consumed and further processing should be canceled
     */
    public boolean onKeyRelease(char typedChar, int keyCode) {
        for (IGuiAction.KeyReleased action : getGuiActionListeners(IGuiAction.KeyReleased.class)) {
            action.release(typedChar, keyCode);
        }
        for (ModularPanel panel : this.panelManager.getOpenPanels()) {
            if (panel.onKeyRelease(typedChar, keyCode)) {
                return true;
            }
            if (panel.disablePanelsBelow()) {
                break;
            }
        }
        return false;
    }

    /**
     * Called when a mouse button is released. Tries to invoke
     * {@link com.cleanroommc.modularui.api.widget.Interactable#onMouseScroll(UpOrDown, int) Interactable#onMouseScroll(UpOrDown,int)} on every widget under
     * the mouse after gui action listeners have been called.
     *
     * @param scrollDirection the direction of the scroll
     * @param amount          the amount that has been scrolled. This value will always be > 0. It is not recommended to use this value to determine
     *                        scroll speed
     * @return true if the action was consumed and further processing should be canceled
     */
    public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
        for (IGuiAction.MouseScroll action : getGuiActionListeners(IGuiAction.MouseScroll.class)) {
            action.scroll(scrollDirection, amount);
        }
        for (ModularPanel panel : this.panelManager.getOpenPanels()) {
            if (panel.onMouseScroll(scrollDirection, amount)) {
                return true;
            }
            if (panel.disablePanelsBelow()) {
                break;
            }
        }
        return false;
    }

    /**
     * Called every time the mouse pos changes and a mouse button is held down. Invokes
     * {@link com.cleanroommc.modularui.api.widget.Interactable#onMouseDrag(int, long) Interactable#onMouseDrag(int,long)} on every widget
     * under the mouse after gui action listeners have been called.
     *
     * @param mouseButton    mouse button that is held down (0 = left button, 1 = right button, 2 = scroll button, 4 and 5 = side buttons)
     * @param timeSinceClick how long the button has been held down until now in milliseconds
     * @return true if the action was consumed and further processing should be canceled
     */
    public boolean onMouseDrag(int mouseButton, long timeSinceClick) {
        for (IGuiAction.MouseDrag action : getGuiActionListeners(IGuiAction.MouseDrag.class)) {
            action.drag(mouseButton, timeSinceClick);
        }
        for (ModularPanel panel : this.panelManager.getOpenPanels()) {
            if (panel.onMouseDrag(mouseButton, timeSinceClick)) {
                return true;
            }
            if (panel.disablePanelsBelow()) {
                break;
            }
        }
        return false;
    }

    /**
     * Called with {@code true} after a widget which implements {@link com.cleanroommc.modularui.api.widget.IFocusedWidget IFocusedWidget}
     * has consumed a mouse press and called with {@code false} if a widget is currently focused and anything else has consumed a mouse
     * press. This is required for other mods like recipe viewer to not interfere with inputs.
     *
     * @param focus true if the gui screen will be focused
     */
    @ApiStatus.Internal
    public void setFocused(boolean focus) {
        this.screenWrapper.setFocused(focus);
    }

    /**
     * @return true if this screen is currently open and displayed on the screen
     */
    public boolean isActive() {
        return getCurrent() == this;
    }

    /**
     * The owner of this screen. Usually a modid. This is mainly used to find theme overrides.
     *
     * @return owner of this screen
     */
    @NotNull
    public String getOwner() {
        return this.owner;
    }

    /**
     * The name of this screen, which is also the name of the panel. Every UI under one owner should have a different name.
     * Unfortunately there is no good way to verify this, so it's the UI implementors responsibility to set a proper name for the main panel.
     * This is mainly used to find theme overrides.
     *
     * @return name of this screen
     */
    @NotNull
    public String getName() {
        return this.name;
    }

    /**
     * @return the owner and name as a {@link ResourceLocation}
     * @see #getOwner()
     * @see #getName()
     */
    public ResourceLocation getResourceLocation() {
        return new ResourceLocation(this.owner, this.name);
    }

    /**
     * @return true if this is an overlay for another screen
     */
    public boolean isOverlay() {
        return overlay;
    }

    public ModularGuiContext getContext() {
        return this.context;
    }

    public PanelManager getPanelManager() {
        return panelManager;
    }

    public ModularSyncManager getSyncManager() {
        return getContainer().getSyncManager();
    }

    public ModularPanel getMainPanel() {
        return this.panelManager.getMainPanel();
    }

    public IMuiScreen getScreenWrapper() {
        return this.screenWrapper;
    }

    public Area getScreenArea() {
        return this.context.getScreenArea();
    }

    public boolean isClientOnly() {
        return isOverlay() || !this.screenWrapper.isGuiContainer() || getContainer().isClientOnly();
    }

    public ModularContainer getContainer() {
        if (isOverlay()) {
            throw new IllegalStateException("Can't get ModularContainer for overlay");
        }
        if (this.screenWrapper.getGuiScreen() instanceof GuiContainer container) {
            return (ModularContainer) container.inventorySlots;
        }
        throw new IllegalStateException("Screen does not extend GuiContainer!");
    }

    public boolean doesPauseGame() {
        return pausesGame;
    }

    @SuppressWarnings("unchecked")
    private <T extends IGuiAction> List<T> getGuiActionListeners(Class<T> clazz) {
        return (List<T>) this.guiActionListeners.getOrDefault(clazz, Collections.emptyList());
    }

    /**
     * Registers an interaction listener. This is useful when you want to listen to any GUI interactions and not just
     * for a specific widget. <br>
     * <b>Do NOT register listeners which are bound to a widget here!</b>
     * Use {@link com.cleanroommc.modularui.widget.Widget#listenGuiAction(IGuiAction) Widget#listenGuiAction(IGuiAction)} for that!
     *
     * @param action action listener
     */
    public void registerGuiActionListener(IGuiAction action) {
        List<IGuiAction> list = this.guiActionListeners.computeIfAbsent(getGuiActionClass(action), key -> new ArrayList<>());
        if (!list.contains(action)) list.add(action);
    }

    /**
     * Removes an interaction listener
     *
     * @param action action listener to remove
     */
    public void removeGuiActionListener(IGuiAction action) {
        this.guiActionListeners.getOrDefault(getGuiActionClass(action), Collections.emptyList()).remove(action);
    }

    /**
     * Registers a frame update listener which runs approximately 60 times per second.
     * Listeners are automatically removed if the widget becomes invalid.
     * If a listener is already registered from the given widget, the listeners get merged.
     *
     * @param widget   widget the listener is bound to
     * @param runnable listener function
     */
    public void registerFrameUpdateListener(IWidget widget, Runnable runnable) {
        registerFrameUpdateListener(widget, runnable, true);
    }

    /**
     * Registers a frame update listener which runs approximately 60 times per second.
     * Listeners are automatically removed if the widget becomes invalid.
     * If a listener is already registered from the given widget and <code>merge</code> is true, the listeners get merged.
     * Otherwise, the current listener is overwritten (if any)
     *
     * @param widget   widget the listener is bound to
     * @param runnable listener function
     * @param merge    if listener should be merged with existing listener
     */
    public void registerFrameUpdateListener(IWidget widget, Runnable runnable, boolean merge) {
        Objects.requireNonNull(runnable);
        if (merge) {
            this.frameUpdates.merge(widget, runnable, (old, now) -> () -> {
                old.run();
                now.run();
            });
        } else {
            this.frameUpdates.put(widget, runnable);
        }
    }

    /**
     * Removes all frame update listeners for a widget.
     *
     * @param widget widget to remove listeners from
     */
    public void removeFrameUpdateListener(IWidget widget) {
        this.frameUpdates.remove(widget);
    }

    private static Class<?> getGuiActionClass(IGuiAction action) {
        Class<?>[] classes = action.getClass().getInterfaces();
        for (Class<?> clazz : classes) {
            if (IGuiAction.class.isAssignableFrom(clazz)) {
                return clazz;
            }
        }
        throw new IllegalArgumentException();
    }

    public ITheme getCurrentTheme() {
        return this.currentTheme;
    }

    /**
     * Tries to use a specific theme for this screen. If the theme for this screen has been overriden via resource packs, this method does
     * nothing.
     *
     * @param theme id of theme to use
     * @return this for builder like usage
     */
    public ModularScreen useTheme(String theme) {
        this.currentTheme = IThemeApi.get().getThemeForScreen(this, theme);
        return this;
    }

    /**
     * Sets if the gui should pause the game in the background. Pausing means every ticking will halt. If the client is connected to a
     * dedicated server the UI will NEVER pause the game.
     *
     * @param pausesGame true if the ui should pause the game in the background.
     * @return this for builder like usage
     */
    public ModularScreen pausesGame(boolean pausesGame) {
        this.pausesGame = pausesGame;
        return this;
    }

    // TODO move this to a more appropriate place
    public enum UpOrDown {
        UP(1), DOWN(-1);

        public final int modifier;

        UpOrDown(int modifier) {
            this.modifier = modifier;
        }

        public boolean isUp() {
            return this == UP;
        }

        public boolean isDown() {
            return this == DOWN;
        }
    }
}
