package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import com.cleanroommc.modularui.sync.MapKey;
import com.cleanroommc.modularui.theme.Theme;
import com.cleanroommc.modularui.widget.WidgetTree;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widget.sizer.IResizeable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private static GuiContext current;

    private final String owner;
    private final String name;
    private final WindowManager windowManager;
    public final GuiContext context;
    private final Area viewport = new Area();
    private Theme currentTheme; //= Theme.VANILLA;

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

    @ApiStatus.OverrideOnly
    public void onResize(int width, int height) {
        current = this.context;

        /*if (!this.context.keybinds.hasParent()) {
            this.ROOT.add(this.context.keybinds);
        }*/

        this.viewport.set(0, 0, width, height);
        this.viewport.z(0);
        this.viewportSet();

        this.context.pushViewport(this.viewport);
        for (ModularPanel panel : this.windowManager.getOpenWindows()) {
            // resize each widget and calculate their relative pos
            panel.resize();
            // now apply the calculated pos
            WidgetTree.foreachChildByLayer(panel, child -> {
                IResizeable resizer = child.resizer();
                if (resizer != null) {
                    resizer.applyPos(child);
                }
                return true;
            }, true);
        }

        //this.ROOT.resize();
        this.context.popViewport();

        this.screenWrapper.updateArea(this.windowManager.getMainPanel().getArea());
    }

    @ApiStatus.OverrideOnly
    public abstract ModularPanel buildUI(GuiContext context);

    @ApiStatus.OverrideOnly
    public void onOpen() {
        windowManager.init();
        if (!getContainer().isClientOnly()) {
            windowManager.getOpenWindows().forEach(panel -> panel.initialiseSyncHandler(getSyncHandler()));
        }
    }

    @ApiStatus.OverrideOnly
    @MustBeInvokedByOverriders
    public void onClose() {
        current = null;
        this.windowManager.closeAll();
    }

    public void close() {
        close(false);
    }

    public void close(boolean force) {
        if (isActive()) {
            //if(!force /*TODO check for animation and stuff*/)
            context.mc.player.closeScreen();
        }
    }

    @ApiStatus.OverrideOnly
    public void onUpdate() {
        this.context.tick();
    }

    public void onFrameUpdate() {
        for (ModularPanel panel : this.windowManager.getOpenWindows()) {
            WidgetTree.onFrameUpdate(panel);
        }
        context.onFrameUpdate();
    }

    protected void viewportSet() {
    }

    @ApiStatus.OverrideOnly
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.context.setMouse(mouseX, mouseY);
        this.context.partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        this.context.reset();
        this.context.pushViewport(this.viewport);
        for (ModularPanel panel : this.windowManager.getOpenWindows()) {
            if (panel.isEnabled()) {
                WidgetTree.drawInternal(panel, this.context, partialTicks);
            }
        }
        this.context.popViewport();

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableLighting();
        RenderHelper.enableStandardItemLighting();
        //this.context.drawTooltip();
        this.context.postRenderCallbacks.forEach(element -> element.accept(this.context));
    }

    public void drawForeground(float partialTicks) {
        // TODO draw tooltip

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        this.context.reset();
        this.context.pushViewport(this.viewport);
        for (ModularPanel panel : this.windowManager.getOpenWindows()) {
            if (panel.isEnabled()) {
                WidgetTree.drawForegroundInternal(panel, partialTicks);
            }
        }
        this.context.popViewport();

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableLighting();
        RenderHelper.enableStandardItemLighting();
    }

    public void showTooltip(Consumer<Tooltip> tooltipBuilder) {

    }

    @ApiStatus.OverrideOnly
    public boolean onMousePressed(int mouseButton) {
        for (ModularPanel panel : this.windowManager.getOpenWindows()) {
            if (panel.onMousePressed(mouseButton)) {
                return true;
            }
        }
        return false;
    }

    @ApiStatus.OverrideOnly
    public boolean onMouseRelease(int mouseButton) {
        for (ModularPanel panel : this.windowManager.getOpenWindows()) {
            if (panel.onMouseRelease(mouseButton)) {
                return true;
            }
        }
        return false;
    }

    @ApiStatus.OverrideOnly
    public boolean onMouseTapped(int mouseButton) {

        return false;
    }

    @ApiStatus.OverrideOnly
    public boolean onKeyPressed(char typedChar, int keyCode) {
        for (ModularPanel panel : this.windowManager.getOpenWindows()) {
            if (panel.onKeyPressed(typedChar, keyCode)) {
                return true;
            }
        }
        return false;
    }

    @ApiStatus.OverrideOnly
    public boolean onKeyRelease(char typedChar, int keyCode) {
        for (ModularPanel panel : this.windowManager.getOpenWindows()) {
            if (panel.onKeyRelease(typedChar, keyCode)) {
                return true;
            }
        }
        return false;
    }

    @ApiStatus.OverrideOnly
    public boolean onKeyTapped(int keyCode) {

        return false;
    }

    @ApiStatus.OverrideOnly
    public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
        for (ModularPanel panel : this.windowManager.getOpenWindows()) {
            if (panel.onMouseScroll(scrollDirection, amount)) {
                return true;
            }
        }
        return false;
    }

    @ApiStatus.OverrideOnly
    public boolean onMouseDrag(int mouseButton, long timeSinceClick) {
        for (ModularPanel panel : this.windowManager.getOpenWindows()) {
            if (panel.onMouseDrag(mouseButton, timeSinceClick)) {
                return true;
            }
        }
        return false;
    }

    @ApiStatus.Internal
    public void setFocused(boolean focus) {
        this.screenWrapper.setFocused(focus);
    }

    @ApiStatus.Internal
    public void receiveValueUpdate(MapKey key, PacketBuffer buffer) {

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

    public Theme getTheme() {
        return currentTheme;
    }

    public void setTheme(Theme theme) {
        this.currentTheme = theme;
    }

    public void registerItemSlot(Slot slot) {
        getContainer().addSlotToContainer(slot);
    }

    protected ModularContainer getContainer() {
        return (ModularContainer) this.screenWrapper.inventorySlots;
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