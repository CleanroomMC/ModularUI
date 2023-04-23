package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.widget.IGuiAction;
import com.cleanroommc.modularui.api.widget.ISynced;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import com.cleanroommc.modularui.sync.ItemSlotSH;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.WidgetTree;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.Dialog;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@SideOnly(Side.CLIENT)
public abstract class ModularScreen {

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

    private final String owner;
    private final String name;
    private final WindowManager windowManager;
    public final GuiContext context;
    private final Area viewport = new Area();
    private final Map<Class<?>, List<IGuiAction>> guiActionListeners = new Object2ObjectOpenHashMap<>();

    private GuiScreenWrapper screenWrapper;
    private GuiSyncHandler syncHandler;

    public ModularScreen(@NotNull String owner, @NotNull String name) {
        this.owner = owner;
        this.name = name;
        this.windowManager = new WindowManager(this);
        this.context = new GuiContext(this);

        ModularPanel panel = buildUI(this.context);
        if (panel.getFlex() == null) {
            panel.flex().size(1f, 1f);
        }
        this.windowManager.construct(panel);
    }

    public ModularScreen(@NotNull String name) {
        this(ModularUI.ID, name);
    }

    @ApiStatus.Internal
    @MustBeInvokedByOverriders
    public void construct(GuiScreenWrapper wrapper, GuiSyncHandler syncHandler) {
        if (this.screenWrapper != null) throw new IllegalStateException("ModularScreen is already constructed!");
        if (wrapper == null) throw new NullPointerException("GuiScreenWrapper must not be null!");
        this.screenWrapper = wrapper;
        this.syncHandler = syncHandler;
        this.screenWrapper.updateArea(this.windowManager.getMainPanel().getArea());
    }

    public void onResize(int width, int height) {

        this.viewport.set(0, 0, width, height);
        this.viewport.z(0);
        this.viewportSet();

        this.context.pushViewport(null, this.viewport);
        for (ModularPanel panel : this.windowManager.getReverseOpenPanels()) {
            WidgetTree.resize(panel);
        }

        //this.ROOT.resize();
        this.context.popViewport(null);

        this.screenWrapper.updateArea(this.windowManager.getMainPanel().getArea());
    }

    @ApiStatus.OverrideOnly
    public abstract ModularPanel buildUI(GuiContext context);

    public void onOpen() {
        windowManager.init();
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
                getMainPanel().animateClose();
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
        context.onFrameUpdate();
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
        this.context.pushViewport(null, this.viewport);
        for (ModularPanel panel : this.windowManager.getReverseOpenPanels()) {
            if (panel.disablePanelsBelow()) {
                GuiDraw.drawRect(0, 0, this.viewport.w(), this.viewport.h(), Color.argb(16, 16, 16, (int) (125 * panel.getAlpha())));
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
        this.context.pushViewport(null, this.viewport);
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

    public <T> void openDialog(Consumer<Dialog<T>> dialogBuilder) {
        openDialog(dialogBuilder, null);
    }

    public <T> void openDialog(Consumer<Dialog<T>> dialogBuilder, Consumer<T> resultConsumer) {
        Dialog<T> dialog = new Dialog<>(this.context, resultConsumer);
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
        return owner;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public ResourceLocation getResourceLocation() {
        return new ResourceLocation(owner, name);
    }

    public WindowManager getWindowManager() {
        return windowManager;
    }

    public GuiSyncHandler getSyncHandler() {
        return getContainer().getSyncHandler();
    }

    public ModularPanel getMainPanel() {
        return this.windowManager.getMainPanel();
    }

    public GuiScreenWrapper getScreenWrapper() {
        return screenWrapper;
    }

    public Area getViewport() {
        return viewport;
    }

    public void registerItemSlot(ItemSlotSH syncHandler) {
        getContainer().registerSlot(syncHandler);
    }

    public boolean isClientOnly() {
        return getSyncHandler() == null;
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

    public static ModularScreen simple(@NotNull String name, Function<GuiContext, ModularPanel> panel) {
        return simple(ModularUI.ID, name, panel);
    }

    public static ModularScreen simple(@NotNull String owner, @NotNull String name, Function<GuiContext, ModularPanel> panel) {
        return new ModularScreen(owner, name) {
            @Override
            public ModularPanel buildUI(GuiContext context) {
                return panel.apply(context);
            }
        };
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
