package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.api.MCHelper;
import com.cleanroommc.modularui.api.widget.IGuiElement;
import com.cleanroommc.modularui.api.widget.IVanillaSlot;
import com.cleanroommc.modularui.core.mixin.GuiAccessor;
import com.cleanroommc.modularui.core.mixin.GuiContainerAccessor;
import com.cleanroommc.modularui.core.mixin.GuiScreenAccessor;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.overlay.OverlayStack;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.screen.viewport.LocatedWidget;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.utils.Animator;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.FpsCounter;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.RichTextWidget;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

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
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
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

import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Predicate;

@SideOnly(Side.CLIENT)
public class ClientScreenHandler {

    private static final GuiContext defaultContext = new GuiContext();

    private static ModularScreen currentScreen = null;
    private static Character lastChar = null;
    private static final FpsCounter fpsCounter = new FpsCounter();

    @SubscribeEvent
    public static void onGuiOpen(GuiOpenEvent event) {
        defaultContext.reset();
        if (event.getGui() instanceof IMuiScreen muiScreen) {
            Objects.requireNonNull(muiScreen.getScreen(), "ModularScreen must not be null!");
            if (currentScreen != muiScreen.getScreen()) {
                if (hasScreen()) {
                    currentScreen.onCloseParent();
                    currentScreen = null;
                    lastChar = null;
                }
                currentScreen = muiScreen.getScreen();
                fpsCounter.reset();
            }
        } else if (hasScreen() && getMCScreen() != null && event.getGui() != getMCScreen()) {
            currentScreen.onCloseParent();
            currentScreen = null;
            lastChar = null;
        }
    }

    @SubscribeEvent
    public static void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        defaultContext.updateScreenArea(event.getGui().width, event.getGui().height);
        if (checkGui(event.getGui())) {
            currentScreen.onResize(event.getGui().width, event.getGui().height);
        }
        OverlayStack.foreach(ms -> ms.onResize(event.getGui().width, event.getGui().height), false);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onGuiInputLow(GuiScreenEvent.KeyboardInputEvent.Pre event) throws IOException {
        defaultContext.updateEventState();
        if (checkGui(event.getGui())) currentScreen.getContext().updateEventState();
        if (handleKeyboardInput(currentScreen, event.getGui())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onGuiInputLow(GuiScreenEvent.MouseInputEvent.Pre event) throws IOException {
        defaultContext.updateEventState();
        if (checkGui(event.getGui())) currentScreen.getContext().updateEventState();
        if (handleMouseInput(Mouse.getEventButton(), currentScreen, event.getGui())) {
            event.setCanceled(true);
            return;
        }
        int w = Mouse.getEventDWheel();
        if (w == 0) return;
        ModularScreen.UpOrDown upOrDown = w > 0 ? ModularScreen.UpOrDown.UP : ModularScreen.UpOrDown.DOWN;
        checkGui(event.getGui());
        if (doAction(currentScreen, ms -> ms.onMouseScroll(upOrDown, Math.abs(w)))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onGuiDraw(GuiScreenEvent.DrawScreenEvent.Pre event) {
        int mx = event.getMouseX(), my = event.getMouseY();
        float pt = event.getRenderPartialTicks();
        defaultContext.updateState(mx, my, pt);
        defaultContext.reset();
        if (checkGui(event.getGui())) {
            currentScreen.getContext().updateState(mx, my, pt);
            drawScreen(currentScreen, currentScreen.getScreenWrapper().getGuiScreen(), mx, my, pt);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onGuiDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        OverlayStack.draw(event.getMouseX(), event.getMouseY(), event.getRenderPartialTicks());
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            OverlayStack.onTick();
            defaultContext.tick();
            if (checkGui()) {
                currentScreen.onUpdate();
            }
        }
    }

    public static void onFrameUpdate() {
        OverlayStack.foreach(ModularScreen::onFrameUpdate, true);
        if (currentScreen != null) currentScreen.onFrameUpdate();
        Animator.advance();
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
            if (muiScreen != null && muiScreen.handleDraggableInput(button, true)) return true;
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
            if (muiScreen != null && muiScreen.handleDraggableInput(button, false)) return true;
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
    private static boolean handleKeyboardInput(@Nullable ModularScreen muiScreen, GuiScreen mcScreen) throws IOException {
        char c0 = Keyboard.getEventCharacter();
        int key = Keyboard.getEventKey();
        boolean state = Keyboard.getEventKeyState();

        if (state) {
            // pressing a key
            lastChar = c0;
            return doAction(muiScreen, ms -> ms.onKeyPressed(c0, key)) || keyTyped(mcScreen, c0, key);
        } else {
            // releasing a key
            // for some reason when you press E after joining a world the button will not trigger the press event,
            // but ony the release event, causing this to be null
            if (lastChar == null) return false;
            // when the key is released, the event char is empty
            if (doAction(muiScreen, ms -> ms.onKeyRelease(lastChar, key))) return true;
            if (key == 0 && c0 >= ' ') {
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
                currentScreen.getContext().dropDraggable();
            } else {
                currentScreen.getPanelManager().closeTopPanel(true);
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

    public static void clickSlot() {
        if (hasScreen() && getMCScreen() instanceof GuiScreenAccessor screen) {
            ModularGuiContext ctx = currentScreen.getContext();
            try {
                screen.invokeMouseClicked(ctx.getAbsMouseX(), ctx.getMouseY(), ctx.getMouseButton());
            } catch (IOException e) {
                throw new RuntimeException(e);
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
        return ModularUI.isBlurLoaded() || Minecraft.getMinecraft().world == null;
    }

    public static void drawDarkBackground(GuiScreen screen, int tint) {
        if (hasScreen()) {
            float alpha = currentScreen.getMainPanel().getAlpha();
            // vanilla color values as hex
            int color = 0x101010;
            int startAlpha = 0xc0;
            int endAlpha = 0xd0;
            GuiDraw.drawVerticalGradientRect(0, 0, screen.width, screen.height, Color.withAlpha(color, (int) (startAlpha * alpha)), Color.withAlpha(color, (int) (endAlpha * alpha)));
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
        Stencil.reset();
        Stencil.apply(muiScreen.getScreenArea(), null);
        muiScreen.drawScreen(mouseX, mouseY, partialTicks);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        drawVanillaElements(mcScreen, mouseX, mouseY, partialTicks);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        RenderHelper.disableStandardItemLighting();
        muiScreen.drawForeground(partialTicks);
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableStandardItemLighting();
        Stencil.remove();
    }

    public static void drawContainer(ModularScreen muiScreen, GuiContainer mcScreen, int mouseX, int mouseY, float partialTicks) {
        GuiContainerAccessor acc = (GuiContainerAccessor) mcScreen;

        Stencil.reset();
        Stencil.apply(muiScreen.getScreenArea(), null);
        mcScreen.drawDefaultBackground();
        int x = mcScreen.getGuiLeft();
        int y = mcScreen.getGuiTop();

        acc.invokeDrawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        muiScreen.drawScreen(mouseX, mouseY, partialTicks);

        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        // mainly for invtweaks compat
        drawVanillaElements(mcScreen, mouseX, mouseY, partialTicks);
        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        acc.setHoveredSlot(null);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GlStateManager.enableRescaleNormal();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.disableStandardItemLighting();
        acc.invokeDrawGuiContainerForegroundLayer(mouseX, mouseY);
        muiScreen.drawForeground(partialTicks);
        RenderHelper.enableGUIStandardItemLighting();

        acc.setHoveredSlot(null);
        IGuiElement hovered = muiScreen.getContext().getHovered();
        if (hovered instanceof IVanillaSlot vanillaSlot) {
            acc.setHoveredSlot(vanillaSlot.getVanillaSlot());
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        MinecraftForge.EVENT_BUS.post(new GuiContainerEvent.DrawForeground(mcScreen, mouseX, mouseY));
        GlStateManager.popMatrix();

        InventoryPlayer inventoryplayer = Minecraft.getMinecraft().player.inventory;
        ItemStack itemstack = acc.getDraggedStack().isEmpty() ? inventoryplayer.getItemStack() : acc.getDraggedStack();
        GlStateManager.translate((float) x, (float) y, 0.0F);
        if (!itemstack.isEmpty()) {
            int k2 = acc.getDraggedStack().isEmpty() ? 8 : 16;
            String s = null;

            if (!acc.getDraggedStack().isEmpty() && acc.getIsRightMouseClick()) {
                itemstack = itemstack.copy();
                itemstack.setCount(MathHelper.ceil((float) itemstack.getCount() / 2.0F));
            } else if (acc.getDragSplitting() && acc.getDragSplittingSlots().size() > 1) {
                itemstack = itemstack.copy();
                itemstack.setCount(acc.getDragSplittingRemnant());

                if (itemstack.isEmpty()) {
                    s = TextFormatting.YELLOW + "0";
                }
            }

            drawItemStack(mcScreen, itemstack, mouseX - x - 8, mouseY - y - k2, s);
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
        GlStateManager.enableDepth();
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
            if (checkGui()) {
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
        int lineY = screenH - 13;
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow("Mouse Pos: " + mouseX + ", " + mouseY, 5, lineY, color);
        lineY -= 11;
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow("FPS: " + fpsCounter.getFps(), 5, screenH - 24, color);
        LocatedWidget locatedHovered = muiScreen.getPanelManager().getTopWidgetLocated(true);
        if (locatedHovered != null) {
            drawSegmentLine(lineY -= 4, color);
            lineY -= 10;

            IGuiElement hovered = locatedHovered.getElement();
            locatedHovered.applyMatrix(context);
            GlStateManager.pushMatrix();
            context.applyToOpenGl();

            Area area = hovered.getArea();
            IGuiElement parent = hovered.getParent();

            GuiDraw.drawBorder(0, 0, area.width, area.height, color, 1f);
            if (hovered.hasParent()) {
                GuiDraw.drawBorder(-area.rx, -area.ry, parent.getArea().width, parent.getArea().height, Color.withAlpha(color, 0.3f), 1f);
            }
            GlStateManager.popMatrix();
            locatedHovered.unapplyMatrix(context);
            GuiDraw.drawText("Pos: " + area.x + ", " + area.y + "  Rel: " + area.rx + ", " + area.ry, 5, lineY, 1, color, false);
            lineY -= 11;
            GuiDraw.drawText("Size: " + area.width + ", " + area.height, 5, lineY, 1, color, false);
            lineY -= 11;
            GuiDraw.drawText("Class: " + hovered, 5, lineY, 1, color, false);
            if (hovered.hasParent()) {
                drawSegmentLine(lineY -= 4, color);
                lineY -= 10;
                area = parent.getArea();
                GuiDraw.drawText("Parent size: " + area.width + ", " + area.height, 5, lineY, 1, color, false);
                lineY -= 11;
                GuiDraw.drawText("Parent: " + parent, 5, lineY, 1, color, false);
            }
            if (hovered instanceof ItemSlot slotWidget) {
                drawSegmentLine(lineY -= 4, color);
                lineY -= 10;
                ModularSlot slot = slotWidget.getSlot();
                GuiDraw.drawText("Slot Index: " + slot.getSlotIndex(), 5, lineY, 1, color, false);
                lineY -= 11;
                GuiDraw.drawText("Slot Number: " + slot.slotNumber, 5, lineY, 1, color, false);
                lineY -= 11;
                if (slotWidget.isSynced()) {
                    SlotGroup slotGroup = slot.getSlotGroup();
                    boolean allowShiftTransfer = slotGroup != null && slotGroup.allowShiftTransfer();
                    GuiDraw.drawText("Shift-Click Priority: " + (allowShiftTransfer ? slotGroup.getShiftClickPriority() : "DISABLED"), 5, lineY, 1, color, false);
                }
            } else if (hovered instanceof RichTextWidget richTextWidget) {
                drawSegmentLine(lineY -= 4, color);
                lineY -= 10;
                Object hoveredElement = richTextWidget.getHoveredElement();
                GuiDraw.drawText("Hovered: " + hoveredElement, 5, lineY, 1, color, false);
            }
        }
        // dot at mouse pos
        GuiDraw.drawRect(mouseX, mouseY, 1, 1, Color.withAlpha(Color.GREEN.main, 0.8f));
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    private static void drawSegmentLine(int y, int color) {
        GuiDraw.drawRect(5, y, 140, 1, color);
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

    private static boolean checkGui() {
        return MCHelper.hasMc() && checkGui(Minecraft.getMinecraft().currentScreen);
    }

    private static boolean checkGui(GuiScreen screen) {
        if (!MCHelper.hasMc() || currentScreen == null || !(screen instanceof IMuiScreen muiScreen)) return false;
        if (screen != Minecraft.getMinecraft().currentScreen || muiScreen.getScreen() != currentScreen) {
            defaultContext.reset();
            currentScreen = null;
            lastChar = null;
            return false;
        }
        return true;
    }

    public static GuiContext getDefaultContext() {
        return defaultContext;
    }

    public static GuiContext getBestContext() {
        if (checkGui()) {
            return currentScreen.getContext();
        }
        return defaultContext;
    }
}
