package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.widget.IGuiAction;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widget.WidgetTree;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.Dialog;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
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
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This is the base class for all modular ui's. It only exists on client side.
 * It handles drawing the screen, all panels and widget interactions.
 */
@SideOnly(Side.CLIENT)
public class ModularScreen {

    public static boolean isScreen(@Nullable GuiScreen guiScreen, String owner, String name) {
        if (guiScreen instanceof GuiScreenWrapper screenWrapper) {
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
        if (Minecraft.getMinecraft().currentScreen instanceof GuiScreenWrapper screenWrapper) {
            return screenWrapper.getScreen();
        }
        return null;
    }

    private final String owner;
    private final String name;
    private final PanelManager panelManager;
    private final GuiContext context = new GuiContext(this);
    private final Area screenArea = new Area();
    private final Map<Class<?>, List<IGuiAction>> guiActionListeners = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectArrayMap<IWidget, Runnable> frameUpdates = new Object2ObjectArrayMap<>();

    private ITheme currentTheme;
    private GuiScreenWrapper screenWrapper;

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
    public ModularScreen(@NotNull String owner, @NotNull Function<GuiContext, ModularPanel> mainPanelCreator) {
        this(owner, Objects.requireNonNull(mainPanelCreator, "The main panel function must not be null!"), false);
    }

    private ModularScreen(@NotNull String owner, @Nullable Function<GuiContext, ModularPanel> mainPanelCreator, boolean ignored) {
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
    ModularPanel buildUI(GuiContext context) {
        throw new UnsupportedOperationException();
    }

    @MustBeInvokedByOverriders
    void construct(GuiScreenWrapper wrapper) {
        if (this.screenWrapper != null) throw new IllegalStateException("ModularScreen is already constructed!");
        if (wrapper == null) throw new NullPointerException("GuiScreenWrapper must not be null!");
        this.screenWrapper = wrapper;
        this.screenWrapper.updateArea(this.panelManager.getMainPanel().getArea());
    }

    public void onResize(int width, int height) {
        this.screenArea.set(0, 0, width, height);
        this.screenArea.z(0);

        this.context.pushViewport(null, this.screenArea);
        for (ModularPanel panel : this.panelManager.getReverseOpenPanels()) {
            WidgetTree.resize(panel);
        }

        this.context.popViewport(null);
        this.screenWrapper.updateArea(this.panelManager.getMainPanel().getArea());
    }

    public void onOpen() {
        this.panelManager.init();
    }

    @MustBeInvokedByOverriders
    public void onClose() {
        this.panelManager.closeAll();
    }

    public void close() {
        close(false);
    }

    public void close(boolean force) {
        if (isActive()) {
            if (force) {
                this.context.mc.player.closeScreen();
                return;
            }
            getMainPanel().closeIfOpen(true);
        }
    }

    public void openPanel(ModularPanel panel) {
        this.panelManager.openPanel(panel);
    }

    /**
     * Closes the given panel as soon as possible.
     *
     * @param panel panel to close
     * @throws IllegalArgumentException if the panel is not open in this screen
     */
    public void closePanel(ModularPanel panel) {
        this.panelManager.closePanel(panel);
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

    @MustBeInvokedByOverriders
    public void onUpdate() {
        this.context.tick();
        for (ModularPanel panel : this.panelManager.getOpenPanels()) {
            WidgetTree.onUpdate(panel);
        }
    }

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

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.context.updateState(mouseX, mouseY, partialTicks);

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableAlpha();

        this.context.reset();
        this.context.pushViewport(null, this.screenArea);
        for (ModularPanel panel : this.panelManager.getReverseOpenPanels()) {
            if (panel.disablePanelsBelow()) {
                GuiDraw.drawRect(0, 0, this.screenArea.w(), this.screenArea.h(), Color.argb(16, 16, 16, (int) (125 * panel.getAlpha())));
            }
            WidgetTree.drawTree(panel, this.context);
        }
        this.context.popViewport(null);

        this.context.postRenderCallbacks.forEach(element -> element.accept(this.context));
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableLighting();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableAlpha();
    }

    public void drawForeground(float partialTicks) {
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableAlpha();

        this.context.reset();
        this.context.pushViewport(null, this.screenArea);
        for (ModularPanel panel : this.panelManager.getReverseOpenPanels()) {
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

    public boolean onMousePressed(int mouseButton) {
        this.context.updateEventState();
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

    public boolean onMouseRelease(int mouseButton) {
        this.context.updateEventState();
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

    public boolean onKeyPressed(char typedChar, int keyCode) {
        this.context.updateEventState();
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

    public boolean onKeyRelease(char typedChar, int keyCode) {
        this.context.updateEventState();
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

    public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
        this.context.updateEventState();
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

    public boolean onMouseDrag(int mouseButton, long timeSinceClick) {
        this.context.updateEventState();
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

    public <T> void openDialog(String name, Consumer<Dialog<T>> dialogBuilder) {
        openDialog(name, dialogBuilder, null);
    }

    public <T> void openDialog(String name, Consumer<Dialog<T>> dialogBuilder, Consumer<T> resultConsumer) {
        Dialog<T> dialog = new Dialog<>(name, resultConsumer);
        dialog.flex().size(150, 100).align(Alignment.Center);
        dialog.background(GuiTextures.MC_BACKGROUND);
        dialogBuilder.accept(dialog);
        openPanel(dialog);
    }

    @ApiStatus.Internal
    public void setFocused(boolean focus) {
        this.screenWrapper.setFocused(focus);
    }

    public boolean isActive() {
        return getCurrent() == this;
    }

    @NotNull
    public String getOwner() {
        return this.owner;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    public ResourceLocation getResourceLocation() {
        return new ResourceLocation(this.owner, this.name);
    }

    public GuiContext getContext() {
        return this.context;
    }

    @ApiStatus.ScheduledForRemoval
    @Deprecated
    public PanelManager getWindowManager() {
        return this.panelManager;
    }

    public PanelManager getPanelManager() {
        return panelManager;
    }

    public GuiSyncManager getSyncManager() {
        return getContainer().getSyncManager();
    }

    public ModularPanel getMainPanel() {
        return this.panelManager.getMainPanel();
    }

    public GuiScreenWrapper getScreenWrapper() {
        return this.screenWrapper;
    }

    public Area getScreenArea() {
        return this.screenArea;
    }

    public boolean isClientOnly() {
        return getContainer().isClientOnly();
    }

    public ModularContainer getContainer() {
        return (ModularContainer) this.screenWrapper.inventorySlots;
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
        this.guiActionListeners.computeIfAbsent(getGuiActionClass(action), key -> new ArrayList<>()).add(action);
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

    public ModularScreen useTheme(String theme) {
        this.currentTheme = IThemeApi.get().getThemeForScreen(this, theme);
        return this;
    }

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
