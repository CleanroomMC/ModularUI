package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.GuiErrorHandler;
import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.api.MCHelper;
import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.widget.IVanillaSlot;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.core.mixins.early.minecraft.GuiAccessor;
import com.cleanroommc.modularui.core.mixins.early.minecraft.GuiContainerAccessor;
import com.cleanroommc.modularui.core.mixins.early.minecraft.GuiScreenAccessor;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.network.ModularNetwork;
import com.cleanroommc.modularui.overlay.DebugOptions;
import com.cleanroommc.modularui.overlay.OverlayManager;
import com.cleanroommc.modularui.overlay.OverlayStack;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.screen.viewport.LocatedWidget;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.FpsCounter;
import com.cleanroommc.modularui.utils.MathUtils;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.RichTextWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import com.cleanroommc.neverenoughanimations.animations.OpeningAnimation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@ApiStatus.Internal
@SideOnly(Side.CLIENT)
public class ClientScreenHandler {

    private static final GuiContext defaultContext = new GuiContext();

    private static ModularScreen currentScreen = null;
    private static Character lastChar = null;
    private static final FpsCounter fpsCounter = new FpsCounter();
    private static long ticks = 0L;

    private static IMuiScreen lastMui;
    private static final ObjectArrayList<IMuiScreen> muiStack = new ObjectArrayList<>(8);

    // we need to know the actual gui and not some fake screen some other mod overwrites
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGuiChange(GuiOpenEvent event) {
        onGuiChanged(getMCScreen(), event.getGui());
    }

    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        defaultContext.updateScreenArea(event.getGui().width, event.getGui().height);
        if (validateGui(event.getGui())) {
            currentScreen.onResize(event.getGui().width, event.getGui().height);
        }
        OverlayStack.foreach(ms -> ms.onResize(event.getGui().width, event.getGui().height), false);
    }

    // before recipe viewer
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onGuiInputHigh(GuiScreenEvent.KeyboardInputEvent.Pre event) throws IOException {
        defaultContext.updateEventState();
        inputEvent(event, InputPhase.EARLY);
    }

    // after recipe viewer
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onGuiInputLow(GuiScreenEvent.KeyboardInputEvent.Pre event) throws IOException {
        inputEvent(event, InputPhase.LATE);
    }

    private static void inputEvent(GuiScreenEvent.KeyboardInputEvent.Pre event, InputPhase phase) throws IOException {
        if (validateGui(event.getGui())) currentScreen.getContext().updateEventState();
        if (handleKeyboardInput(currentScreen, event.getGui(), phase)) {
            event.setCanceled(true);
        }
    }

    // before recipe viewer
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onGuiInputHigh(GuiScreenEvent.MouseInputEvent.Pre event) throws IOException {
        defaultContext.updateEventState();
        if (validateGui(event.getGui())) currentScreen.getContext().updateEventState();
        if (handleMouseInput(Mouse.getEventButton(), currentScreen, event.getGui())) {
            Platform.unFocusRecipeViewer();
            event.setCanceled(true);
            return;
        }
        int w = Mouse.getEventDWheel();
        if (w == 0) return;
        UpOrDown upOrDown = w > 0 ? UpOrDown.UP : UpOrDown.DOWN;
        validateGui(event.getGui());
        if (doAction(currentScreen, ms -> ms.onMouseScroll(upOrDown, Math.abs(w)))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onGuiDraw(GuiScreenEvent.DrawScreenEvent.Pre event) {
        int mx = event.getMouseX(), my = event.getMouseY();
        float pt = event.getRenderPartialTicks();
        defaultContext.updateState(mx, my, pt);
        defaultContext.reset();
        if (validateGui(event.getGui())) {
            currentScreen.getContext().updateState(mx, my, pt);
            drawScreen(currentScreen, currentScreen.getScreenWrapper().getGuiScreen(), mx, my, pt);
            event.setCanceled(true);
        }
        Platform.setupDrawTex(); // recipe viewer and other mods may expect this state
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onGuiDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        OverlayStack.draw(event.getMouseX(), event.getMouseY(), event.getRenderPartialTicks());
        Platform.setupDrawTex(); // recipe viewer and other mods may expect this state
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            OverlayStack.onTick();
            defaultContext.tick();
            if (validateGui()) {
                currentScreen.onUpdate();
            }
            ticks++;
        }
    }

    @SubscribeEvent
    public void preDraw(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            GL11.glEnable(GL11.GL_STENCIL_TEST);
        }
        Stencil.reset();
    }

    public static long getTicks() {
        return ticks;
    }

    public static void onFrameUpdate() {
        OverlayStack.foreach(ModularScreen::onFrameUpdate, true);
        if (currentScreen != null) currentScreen.onFrameUpdate();
    }

    private static void onGuiChanged(GuiScreen oldScreen, GuiScreen newScreen) {
        if (oldScreen == newScreen) return;
        defaultContext.reset();
        fpsCounter.reset();
        GuiErrorHandler.INSTANCE.clear();

        IMuiScreen lastLastMui = lastMui;
        if (lastMui != null) {
            // called on open and close
            // invalidate last mui screen, but keep it in stack
            invalidateCurrentScreen();
        }

        if (newScreen instanceof IMuiScreen muiScreen) {
            lastMui = muiScreen;
            currentScreen = muiScreen.getScreen();
            muiStack.remove(muiScreen);
            muiStack.add(muiScreen); // put screen to the top of the stack
            GuiScreen lastParent = lastLastMui != null ? lastLastMui.getScreen().getContext().getParentScreen() : null;
            if (lastParent != muiScreen) {
                // new screen in the stack
                currentScreen.getContext().setParentScreen(oldScreen);
            } else {
                // last parent is equal to new screen -> effectively popping the current screen from the stack
                // the current screen will disconnect from the stack and therefore need to dispose it
                muiStack.remove(lastLastMui);
                lastLastMui.getScreen().getPanelManager().dispose();
            }
        } else if (newScreen == null) {
            // closing -> clear stack and dispose every screen
            invalidateMuiStack();
            // only when all screens are closed dispose all containers in the stack
            ModularNetwork.CLIENT.closeAll();
        }

        OverlayManager.onGuiOpen(newScreen);
    }

    private static void invalidateCurrentScreen() {
        // reset mouse inputs, relevant when screen gets reopened
        if (lastMui != null) {
            ((GuiScreenAccessor) lastMui.getGuiScreen()).setEventButton(-1);
            ((GuiScreenAccessor) lastMui.getGuiScreen()).setLastMouseEvent(-1);
            lastMui.getScreen().getPanelManager().closeScreen();
            lastMui = null;
        }
        currentScreen = null;
        lastChar = null;
    }

    private static void invalidateMuiStack() {
        muiStack.forEach(muiScreen -> muiScreen.getScreen().getPanelManager().dispose());
        muiStack.clear();
    }

    private static boolean doAction(@Nullable ModularScreen muiScreen, Predicate<ModularScreen> action) {
        return OverlayStack.interact(action, true) || (muiScreen != null && action.test(muiScreen));
    }

    private static boolean handleMouseInput(int button, @Nullable ModularScreen muiScreen, GuiScreen mcScreen) throws IOException {
        GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;
        GuiScreenAccessor acc = (GuiScreenAccessor) mcScreen;
        if (Mouse.getEventButtonState()) {
            if (gameSettings.touchscreen) {
                int val = acc.getTouchValue();
                if (val > 0) {
                    // we will cancel the event now, so we have to set the value
                    // otherwise the screen will handle it
                    acc.setTouchValue(val + 1);
                    return true;
                }
            }
            acc.setEventButton(button);
            acc.setLastMouseEvent(Minecraft.getSystemTime());
            if (muiScreen != null && muiScreen.onMouseInputPre(button, true)) return true;
            return doAction(muiScreen, ms -> ms.onMousePressed(button));
        }
        if (button != -1) {
            if (gameSettings.touchscreen) {
                int val = acc.getTouchValue();
                if (val - 1 > 0) {
                    // we will cancel the event now, so we have to set the value
                    // otherwise the screen will handle it
                    acc.setTouchValue(val - 1);
                    return true;
                }
            }
            acc.setEventButton(-1);
            if (muiScreen != null && muiScreen.onMouseInputPre(button, false)) return true;
            return doAction(muiScreen, ms -> ms.onMouseRelease(button));
        }
        if (acc.getEventButton() != -1 && acc.getLastMouseEvent() > 0L) {
            long l = Minecraft.getSystemTime() - acc.getLastMouseEvent();
            return doAction(muiScreen, ms -> ms.onMouseDrag(acc.getEventButton(), l));
        }
        return false;
    }

    /**
     * This replicates vanilla behavior while also injecting custom behavior for consistency
     */
    private static boolean handleKeyboardInput(@Nullable ModularScreen muiScreen, GuiScreen mcScreen, InputPhase inputPhase) throws IOException {
        char c0 = Keyboard.getEventCharacter();
        int key = Keyboard.getEventKey();
        boolean state = Keyboard.getEventKeyState();

        if (state) {
            // pressing a key
            lastChar = c0;
            return inputPhase.isEarly() ? doAction(muiScreen, ms -> ms.onKeyPressed(c0, key)) : keyTyped(mcScreen, c0, key);
        } else {
            // releasing a key
            // for some reason when you press E after joining a world the button will not trigger the press event,
            // but only the release event, causing this to be null
            if (lastChar == null) return false;
            // when the key is released, the event char is empty
            if (inputPhase.isEarly() && doAction(muiScreen, ms -> ms.onKeyRelease(lastChar, key))) {
                return true;
            }
            if (inputPhase.isLate() && key == 0 && c0 >= ' ') {
                return keyTyped(mcScreen, c0, key);
            }
        }
        return false;
    }

    private static boolean keyTyped(GuiScreen screen, char typedChar, int keyCode) throws IOException {
        if (currentScreen == null) return false;
        // debug mode C + CTRL + SHIFT + ALT
        if (keyCode == 46 && GuiScreen.isCtrlKeyDown() && GuiScreen.isShiftKeyDown() && GuiScreen.isAltKeyDown()) {
            ModularUIConfig.guiDebugMode = !ModularUIConfig.guiDebugMode;
            return true;
        }
        if (keyCode == 1 || Minecraft.getMinecraft().gameSettings.keyBindInventory.isActiveAndMatches(keyCode)) {
            if (currentScreen.getContext().hasDraggable()) {
                currentScreen.getContext().dropDraggable(true);
            } else {
                currentScreen.getPanelManager().closeTopPanel();
            }
            return true;
        }
        return false;
    }

    public static void dragSlot(long timeSinceLastClick) {
        if (hasScreen() && getMCScreen() instanceof GuiScreenAccessor container) {
            ModularGuiContext ctx = currentScreen.getContext();
            container.invokeMouseClickMove(ctx.getAbsMouseX(), ctx.getAbsMouseY(), ctx.getMouseButton(), timeSinceLastClick);
        }
    }

    public static void clickSlot(ModularScreen ms, Slot slot) {
        GuiScreen screen = ms.getScreenWrapper().getGuiScreen();
        if (screen instanceof GuiScreenAccessor acc && screen instanceof IClickableGuiContainer clickableGuiContainer && validateGui(screen)) {
            ModularGuiContext ctx = ms.getContext();
            List<GuiButton> buttonList = acc.getButtonList();
            try {
                // remove buttons to make sure they are not clicked
                acc.setButtonList(Collections.emptyList());
                // set clicked slot to make sure the container clicks the desired slot
                clickableGuiContainer.modularUI$setClickedSlot(slot);
                acc.invokeMouseClicked(ctx.getAbsMouseX(), ctx.getAbsMouseY(), ctx.getMouseButton());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                // undo modifications
                clickableGuiContainer.modularUI$setClickedSlot(null);
                acc.setButtonList(buttonList);
            }
        }
    }

    public static void releaseSlot() {
        if (hasScreen() && getMCScreen() instanceof GuiScreenAccessor screen) {
            ModularGuiContext ctx = currentScreen.getContext();
            screen.invokeMouseReleased(ctx.getAbsMouseX(), ctx.getAbsMouseY(), ctx.getMouseButton());
        }
    }

    public static boolean shouldDrawWorldBackground() {
        return ModularUI.Mods.BLUR.isLoaded() || Minecraft.getMinecraft().world == null;
    }

    public static void drawDarkBackground(GuiScreen screen, int tint) {
        if (hasScreen()) {
            float alpha = ModularUI.Mods.NEA.isLoaded() ? OpeningAnimation.getValue(screen) : 1f;
            // vanilla color values as hex
            int color = 0x101010;
            int start = (int) (0xc0 * alpha);
            int end = (int) (0xd0 * alpha);
            start = Color.withAlpha(color, start);
            end = Color.withAlpha(color, end);
            GuiDraw.drawVerticalGradientRect(0, 0, screen.width, screen.height, start, end);
        }
    }

    public static void drawScreen(ModularScreen muiScreen, GuiScreen mcScreen, int mouseX, int mouseY, float partialTicks) {
        if (mcScreen instanceof GuiContainer container) {
            drawContainer(muiScreen, container, mouseX, mouseY, partialTicks);
        } else {
            drawScreenInternal(muiScreen, mcScreen, mouseX, mouseY, partialTicks);
        }
    }

    public static void drawScreenInternal(ModularScreen muiScreen, GuiScreen mcScreen, int mouseX, int mouseY, float partialTicks) {
        GlStateManager.pushMatrix(); // needed for open animation currently
        Stencil.reset();
        Stencil.apply(muiScreen.getScreenArea(), null);
        Platform.setupDrawTex();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableStandardItemLighting();
        handleAnimationScale(mcScreen);
        muiScreen.drawScreen();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        drawVanillaElements(mcScreen, mouseX, mouseY, partialTicks);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        RenderHelper.disableStandardItemLighting();
        muiScreen.drawForeground();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableStandardItemLighting();
        Stencil.remove();
        GlStateManager.popMatrix();
    }

    public static void drawContainer(ModularScreen muiScreen, GuiContainer mcScreen, int mouseX, int mouseY, float partialTicks) {
        GuiContainerAccessor acc = (GuiContainerAccessor) mcScreen;

        Stencil.reset();
        Stencil.apply(muiScreen.getScreenArea(), null);
        Platform.setupDrawTex();
        mcScreen.drawDefaultBackground();
        int x = mcScreen.getGuiLeft();
        int y = mcScreen.getGuiTop();

        //handleAnimationScale(mcScreen);
        acc.invokeDrawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        muiScreen.drawScreen();

        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        // mainly for invtweaks compat
        drawVanillaElements(mcScreen, mouseX, mouseY, partialTicks);
        acc.setHoveredSlot(null);
        GlStateManager.pushMatrix();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GlStateManager.enableRescaleNormal();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.disableStandardItemLighting();
        acc.invokeDrawGuiContainerForegroundLayer(mouseX, mouseY);
        muiScreen.drawForeground();

        acc.setHoveredSlot(null);
        IWidget hovered = muiScreen.getContext().getTopHovered();
        if (hovered instanceof IVanillaSlot vanillaSlot && vanillaSlot.handleAsVanillaSlot()) {
            acc.setHoveredSlot(vanillaSlot.getVanillaSlot());
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        MinecraftForge.EVENT_BUS.post(new GuiContainerEvent.DrawForeground(mcScreen, mouseX, mouseY));
        GlStateManager.popMatrix();

        InventoryPlayer inventoryplayer = Platform.getClientPlayer().inventory;
        ItemStack itemstack = acc.getDraggedStack().isEmpty() ? inventoryplayer.getItemStack() : acc.getDraggedStack();
        GlStateManager.translate((float) x, (float) y, 0.0F);
        if (!itemstack.isEmpty()) {
            int k2 = acc.getDraggedStack().isEmpty() ? 8 : 16;
            String s = null;

            if (!acc.getDraggedStack().isEmpty() && acc.getIsRightMouseClick()) {
                itemstack = itemstack.copy();
                itemstack.setCount(MathUtils.ceil((float) itemstack.getCount() / 2.0F));
            } else if (acc.getDragSplitting() && acc.getDragSplittingSlots().size() > 1) {
                itemstack = itemstack.copy();
                itemstack.setCount(acc.getDragSplittingRemnant());

                if (itemstack.isEmpty()) {
                    s = TextFormatting.YELLOW + "0";
                }
            }

            drawItemStack(mcScreen, NEAAnimationHandler.injectVirtualCursorStack(mcScreen, itemstack), mouseX - x - 8, mouseY - y - k2, s);
        }

        if (!acc.getReturningStack().isEmpty()) {
            float f = (float) (Minecraft.getSystemTime() - acc.getReturningStackTime()) / 100.0F;

            if (f >= 1.0F) {
                f = 1.0F;
                acc.setReturningStack(ItemStack.EMPTY);
            }

            int l2 = acc.getReturningStackDestSlot().xPos - acc.getTouchUpX();
            int i3 = acc.getReturningStackDestSlot().yPos - acc.getTouchUpY();
            int l1 = acc.getTouchUpX() + (int) ((float) l2 * f);
            int i2 = acc.getTouchUpY() + (int) ((float) i3 * f);
            drawItemStack(mcScreen, acc.getReturningStack(), l1, i2, null);
        }

        NEAAnimationHandler.drawItemAnimation(mcScreen);
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableStandardItemLighting();
        Stencil.remove();
    }

    private static void drawItemStack(GuiContainer mcScreen, ItemStack stack, int x, int y, String altText) {
        GlStateManager.translate(0.0F, 0.0F, 32.0F);
        ((GuiAccessor) mcScreen).setZLevel(200f);
        ((GuiScreenAccessor) mcScreen).getItemRender().zLevel = 200.0F;
        FontRenderer font = stack.getItem().getFontRenderer(stack);
        if (font == null) font = ((GuiScreenAccessor) mcScreen).getFontRenderer();
        Platform.setupDrawItem();
        ((GuiScreenAccessor) mcScreen).getItemRender().renderItemAndEffectIntoGUI(stack, x, y);
        ((GuiScreenAccessor) mcScreen).getItemRender().renderItemOverlayIntoGUI(font, stack, x, y - (((GuiContainerAccessor) mcScreen).getDraggedStack().isEmpty() ? 0 : 8), altText);
        GlStateManager.disableDepth();
        ((GuiAccessor) mcScreen).setZLevel(0f);
        ((GuiScreenAccessor) mcScreen).getItemRender().zLevel = 0.0F;
    }

    private static void drawVanillaElements(GuiScreen mcScreen, int mouseX, int mouseY, float partialTicks) {
        for (GuiButton guiButton : ((GuiScreenAccessor) mcScreen).getButtonList()) {
            guiButton.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, partialTicks);
        }
        for (GuiLabel guiLabel : ((GuiScreenAccessor) mcScreen).getLabelList()) {
            guiLabel.drawLabel(Minecraft.getMinecraft(), mouseX, mouseY);
        }
    }

    public static void drawDebugScreen(@Nullable ModularScreen muiScreen, @Nullable ModularScreen fallback) {
        fpsCounter.onDraw();
        if (!ModularUIConfig.guiDebugMode) return;
        if (muiScreen == null) {
            if (validateGui()) {
                muiScreen = currentScreen;
            } else {
                if (fallback == null) return;
                muiScreen = fallback;
            }
        }
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();

        ModularGuiContext context = muiScreen.getContext();
        int mouseX = context.getAbsMouseX(), mouseY = context.getAbsMouseY();
        int screenH = muiScreen.getScreenArea().height;
        int color = Color.argb(180, 40, 115, 220);
        float scale = 0.80f;
        int shift = (int) (11 * scale + 0.5f);
        int lineY = screenH - shift - 2;
        GuiDraw.drawText("Mouse Pos: " + mouseX + ", " + mouseY, 5, lineY, scale, color, true);
        lineY -= shift;
        GuiDraw.drawText("FPS: " + fpsCounter.getFps(), 5, lineY, scale, color, true);
        lineY -= shift;
        GuiDraw.drawText("Theme ID: " + context.getTheme().getId(), 5, lineY, scale, color, true);
        LocatedWidget locatedHovered = muiScreen.getPanelManager().getTopWidgetLocated(true);
        if (locatedHovered != null) {
            drawSegmentLine(lineY -= 4, scale, color);
            lineY -= 10;

            IWidget hovered = locatedHovered.getElement();
            locatedHovered.applyMatrix(context);
            GlStateManager.pushMatrix();
            context.applyToOpenGl();

            Area area = hovered.getArea();
            IWidget parent = hovered.getParent();

            GuiDraw.drawBorderOutsideXYWH(0, 0, area.width, area.height, scale, color);
            if (hovered.hasParent()) {
                GuiDraw.drawBorderOutsideXYWH(-area.rx, -area.ry, parent.getArea().width, parent.getArea().height, scale, Color.withAlpha(color, 0.3f));
            }
            GlStateManager.popMatrix();
            locatedHovered.unapplyMatrix(context);
            GuiDraw.drawText("Widget Theme: " + hovered.getWidgetTheme(muiScreen.getCurrentTheme()).getKey().getFullName(), 5, lineY, scale, color, true);
            lineY -= shift;
            if (DebugOptions.INSTANCE.showSize) {
                GuiDraw.drawText("Size: " + area.width + ", " + area.height, 5, lineY, scale, color, true);
                lineY -= shift;
            }
            GuiDraw.drawText("Pos: " + area.x + ", " + area.y + "  Rel: " + area.rx + ", " + area.ry, 5, lineY, scale, color, true);
            lineY -= shift;
            GuiDraw.drawText("Class: " + hovered, 5, lineY, scale, color, true);
            if (hovered.hasParent()) {
                drawSegmentLine(lineY -= 4, scale, color);
                lineY -= 10;
                GuiDraw.drawText("Widget Theme: " + parent.getWidgetTheme(muiScreen.getCurrentTheme()).getKey().getFullName(), 5, lineY, scale, color, true);
                lineY -= shift;
                area = parent.getArea();
                GuiDraw.drawText("Parent size: " + area.width + ", " + area.height, 5, lineY, scale, color, true);
                lineY -= shift;
                GuiDraw.drawText("Parent: " + parent, 5, lineY, scale, color, true);
            }
            if (hovered instanceof ItemSlot slotWidget) {
                drawSegmentLine(lineY -= 4, scale, color);
                lineY -= 10;
                ModularSlot slot = slotWidget.getSlot();
                GuiDraw.drawText("Slot Index: " + slot.getSlotIndex(), 5, lineY, scale, color, false);
                lineY -= shift;
                GuiDraw.drawText("Slot Number: " + slot.slotNumber, 5, lineY, scale, color, false);
                lineY -= shift;
                if (slotWidget.isSynced()) {
                    SlotGroup slotGroup = slot.getSlotGroup();
                    boolean allowShiftTransfer = slotGroup != null && slotGroup.allowShiftTransfer();
                    GuiDraw.drawText("Shift-Click Priority: " + (allowShiftTransfer ? slotGroup.getShiftClickPriority() : "DISABLED"), 5, lineY, scale, color, true);
                }
            } else if (hovered instanceof RichTextWidget richTextWidget) {
                drawSegmentLine(lineY -= 4, scale, color);
                lineY -= 10;
                locatedHovered.applyMatrix(context);
                Object hoveredElement = richTextWidget.getHoveredElement();
                locatedHovered.unapplyMatrix(context);
                GuiDraw.drawText("Hovered: " + hoveredElement, 5, lineY, scale, color, true);
            }
        }
        // dot at mouse pos
        GuiDraw.drawRect(mouseX, mouseY, 1, 1, Color.withAlpha(Color.GREEN.main, 0.8f));
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    private static void drawSegmentLine(int y, float scale, int color) {
        GuiDraw.drawRect(5, y, 140 * scale, 1 * scale, color);
    }

    public static void updateGuiArea(GuiContainer container, Rectangle area) {
        GuiContainerAccessor acc = (GuiContainerAccessor) container;
        acc.setGuiLeft(area.x);
        acc.setGuiTop(area.y);
        acc.setXSize(area.width);
        acc.setYSize(area.height);
    }

    public static boolean hasScreen() {
        return currentScreen != null;
    }

    @Nullable
    public static GuiScreen getMCScreen() {
        return MCHelper.getCurrentScreen();
    }

    @Nullable
    public static ModularScreen getMuiScreen() {
        return currentScreen;
    }

    @UnmodifiableView
    public static List<IMuiScreen> getMuiStack() {
        return Collections.unmodifiableList(muiStack);
    }

    private static boolean validateGui() {
        return MCHelper.hasMc() && validateGui(Minecraft.getMinecraft().currentScreen);
    }

    private static boolean validateGui(GuiScreen screen) {
        if (!MCHelper.hasMc() || currentScreen == null || !(screen instanceof IMuiScreen muiScreen)) {
            // no mui screen currently open
            return false;
        }
        if (screen != Minecraft.getMinecraft().currentScreen || muiScreen.getScreen() != currentScreen) {
            // mui screen doesn't match the events screen -> invalidate
            defaultContext.reset();
            invalidateCurrentScreen();
            if (MCHelper.getCurrentScreen() == null) {
                invalidateMuiStack();
            }
            return false;
        }
        return true;
    }

    public static GuiContext getDefaultContext() {
        return defaultContext;
    }

    public static GuiContext getBestContext() {
        if (validateGui()) {
            return currentScreen.getContext();
        }
        return defaultContext;
    }

    private enum InputPhase {
        // for mui interactions
        EARLY,
        // for mc interactions (like E and ESC)
        LATE;

        public boolean isEarly() {
            return this == EARLY;
        }

        public boolean isLate() {
            return this == LATE;
        }
    }

    public static void handleAnimationScale(GuiScreen screen) {
        if (ModularUI.Mods.NEA.isLoaded()) {
            OpeningAnimation.handleScale(screen, true);
        }
    }
}
