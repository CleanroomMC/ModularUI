package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IKey;
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
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * This is the base class for all modular ui's. It only exists on client side.
 * It handles drawing the screen, all panels and widget interactions.
 */
@SideOnly(Side.CLIENT)
public class ModularScreen {

    public static boolean isScreen(@Nullable GuiScreen guiScreen, String owner, String name) {
        if (guiScreen instanceof GuiScreenWrapper) {
            ModularScreen screen = ((GuiScreenWrapper) guiScreen).getScreen();
            return screen.getOwner().equals(owner) && screen.getName().equals(name);
        }
        return false;
    }

    public static boolean isScreen(@Nullable GuiScreen guiScreen, String name) {
        return isScreen(guiScreen, ModularUI.ID, name);
    }

    public static boolean isActive(String owner, String name) {
        return isScreen(Minecraft.getMinecraft().currentScreen, owner, name);
    }

    public static boolean isActive(String name) {
        return isActive(ModularUI.ID, name);
    }

    @Nullable
    public static ModularScreen getCurrent() {
        if (Minecraft.getMinecraft().currentScreen instanceof GuiScreenWrapper) {
            return ((GuiScreenWrapper) Minecraft.getMinecraft().currentScreen).getScreen();
        }
        return null;
    }

    private static final ModularPanel DEFAULT = new ModularPanel("default") {
        @Override
        public boolean addChild(IWidget child, int index) {
            return false;
        }
    }.overlay(IKey.str(TextFormatting.RED + "Either use a ModularScreen constructor, that accepts a ModularPanel directly or override the 'buildUI(GuiContext context)' function!"));

    private final String owner;
    private final String name;
    private final WindowManager windowManager;
    public final GuiContext context;
    private final Area screenArea = new Area();
    private final Map<Class<?>, List<IGuiAction>> guiActionListeners = new Object2ObjectOpenHashMap<>();

    private ITheme currentTheme;
    private GuiScreenWrapper screenWrapper;

    /**
     * If this constructor is used the method {@link #buildUI(GuiContext)} should be overriden!
     */
    public ModularScreen() {
        this(ModularUI.ID);
    }

    /**
     * If this constructor is used the method {@link #buildUI(GuiContext)} should be overriden!
     *
     * @param owner the mod that owns this screen
     */
    public ModularScreen(@NotNull String owner) {
        Objects.requireNonNull(owner, "The owner must not be null!");
        this.owner = owner;
        this.windowManager = new WindowManager(this);
        this.context = new GuiContext(this);
        this.currentTheme = IThemeApi.get().getThemeForScreen(this, null);

        ModularPanel mainPanel = buildUI(this.context);
        Objects.requireNonNull(mainPanel, "The main panel must not be null!");
        this.name = mainPanel.getName();
        this.windowManager.construct(mainPanel);
    }

    public ModularScreen(@NotNull ModularPanel mainPanel) {
        this(ModularUI.ID, mainPanel);
    }

    public ModularScreen(@NotNull String owner, @NotNull ModularPanel mainPanel) {
        Objects.requireNonNull(owner, "The owner must not be null!");
        Objects.requireNonNull(mainPanel, "The main panel must not be null!");
        this.owner = owner;
        this.name = mainPanel.getName();
        this.windowManager = new WindowManager(this);
        this.context = new GuiContext(this);
        this.currentTheme = IThemeApi.get().getThemeForScreen(this, null);
        this.windowManager.construct(mainPanel);
    }

    @MustBeInvokedByOverriders
    void construct(GuiScreenWrapper wrapper) {
        if (this.screenWrapper != null) throw new IllegalStateException("ModularScreen is already constructed!");
        if (wrapper == null) throw new NullPointerException("GuiScreenWrapper must not be null!");
        this.screenWrapper = wrapper;
        this.screenWrapper.updateArea(this.windowManager.getMainPanel().getArea());
    }

    /**
     * If a constructor without a modular panel is used, then this method should be overriden.
     * This must also return a non-null value.
     *
     * @param context context used to build the panel
     * @return the created panel
     */
    @NotNull
    @ApiStatus.OverrideOnly
    @SideOnly(Side.CLIENT)
    public ModularPanel buildUI(GuiContext context) {
        return DEFAULT.size(176, 166);
    }

    public void onResize(int width, int height) {

        this.screenArea.set(0, 0, width, height);
        this.screenArea.z(0);
        this.viewportSet();

        this.context.pushViewport(null, this.screenArea);
        for (ModularPanel panel : this.windowManager.getReverseOpenPanels()) {
            WidgetTree.resize(panel);
        }

        //this.ROOT.resize();
        this.context.popViewport(null);

        this.screenWrapper.updateArea(this.windowManager.getMainPanel().getArea());
    }

    public void onOpen() {
        this.windowManager.init();
    }

    @MustBeInvokedByOverriders
    public void onClose() {
        this.windowManager.closeAll();
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
            if (!getMainPanel().isOpening() && !getMainPanel().isClosing()) {
                for (ModularPanel panel : this.windowManager.getOpenPanels()) {
                    panel.animateClose();
                }
            }
        }
    }

    public void openPanel(ModularPanel panel) {
        this.windowManager.openPanel(panel);
    }

    public void closePanel(ModularPanel panel) {
        this.windowManager.closePanel(panel);
    }

    public boolean isPanelOpen(String name) {
        return this.windowManager.isPanelOpen(name);
    }

    @MustBeInvokedByOverriders
    public void onUpdate() {
        this.context.tick();
    }

    @MustBeInvokedByOverriders
    public void onFrameUpdate() {
        this.windowManager.clearQueue();
        for (ModularPanel panel : this.windowManager.getOpenPanels()) {
            WidgetTree.onFrameUpdate(panel);
        }
        this.context.onFrameUpdate();
    }

    protected void viewportSet() {
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
        for (ModularPanel panel : this.windowManager.getReverseOpenPanels()) {
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
        for (ModularPanel panel : this.windowManager.getReverseOpenPanels()) {
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
        for (ModularPanel panel : this.windowManager.getOpenPanels()) {
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
        for (ModularPanel panel : this.windowManager.getOpenPanels()) {
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
        for (ModularPanel panel : this.windowManager.getOpenPanels()) {
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
        for (ModularPanel panel : this.windowManager.getOpenPanels()) {
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
        for (ModularPanel panel : this.windowManager.getOpenPanels()) {
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
        for (ModularPanel panel : this.windowManager.getOpenPanels()) {
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
        dialog.background(GuiTextures.BACKGROUND);
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

    public WindowManager getWindowManager() {
        return this.windowManager;
    }

    public GuiSyncManager getSyncHandler() {
        return getContainer().getSyncManager();
    }

    public ModularPanel getMainPanel() {
        return this.windowManager.getMainPanel();
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

    public void registerGuiActionListener(IGuiAction action) {
        this.guiActionListeners.computeIfAbsent(getGuiActionClass(action), key -> new ArrayList<>()).add(action);
    }

    @ApiStatus.Internal
    public void removeGuiActionListener(IGuiAction action) {
        this.guiActionListeners.getOrDefault(getGuiActionClass(action), Collections.emptyList()).remove(action);
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

    public ModularScreen useJeiSettings(JeiSettings jeiSettings) {
        this.context.setJeiSettings(jeiSettings);
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
