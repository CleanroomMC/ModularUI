package com.cleanroommc.modularui;

import com.cleanroommc.modularui.animation.AnimatorManager;
import com.cleanroommc.modularui.api.widget.ResizeDragArea;
import com.cleanroommc.modularui.drawable.DrawableSerialization;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;
import com.cleanroommc.modularui.holoui.HoloScreenEntity;
import com.cleanroommc.modularui.holoui.ScreenEntityRender;
import com.cleanroommc.modularui.keybind.KeyBindHandler;
import com.cleanroommc.modularui.screen.ClientScreenHandler;
import com.cleanroommc.modularui.test.EventHandler;
import com.cleanroommc.modularui.test.OverlayTest;
import com.cleanroommc.modularui.test.TestItem;
import com.cleanroommc.modularui.theme.ThemeManager;
import com.cleanroommc.modularui.theme.ThemeReloadCommand;
import com.cleanroommc.modularui.utils.Platform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.Timer;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;

@SideOnly(Side.CLIENT)
@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

    private final Timer timer60Fps = new Timer(60f);
    public static KeyBinding testKey;

    public static Cursor resizeCursorDiag;
    public static Cursor resizeCursorDiagInverse;
    public static Cursor resizeCursorH;
    public static Cursor resizeCursorV;
    private static Cursor currentCursor;

    @Override
    void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        MinecraftForge.EVENT_BUS.register(new ClientScreenHandler());
        MinecraftForge.EVENT_BUS.register(KeyBindHandler.class);
        AnimatorManager.init();

        if (ModularUIConfig.enableTestGuis) {
            MinecraftForge.EVENT_BUS.register(new EventHandler());
            testKey = new KeyBinding("key.test", KeyConflictContext.IN_GAME, Keyboard.KEY_NUMPAD4, "key.categories.modularui");
            ClientRegistry.registerKeyBinding(testKey);
        }
        if (ModularUIConfig.enableTestOverlays) {
            OverlayTest.init();
        }

        DrawableSerialization.init();
        RenderingRegistry.registerEntityRenderingHandler(HoloScreenEntity.class, ScreenEntityRender::new);

        // enable stencil buffer
        if (!Minecraft.getMinecraft().getFramebuffer().isStencilEnabled()) {
            Minecraft.getMinecraft().getFramebuffer().enableStencil();
        }

        // Create resize window cursors
        try {
            BufferedImage img = ImageIO.read(getClass().getClassLoader().getResourceAsStream("assets/modularui/textures/gui/icons/cursor_resize_diag.png"));
            int size = img.getHeight();
            resizeCursorDiagInverse = new Cursor(size, size, size / 2, size / 2, 1, readPixel(img, true, false), null);
            resizeCursorDiag = new Cursor(size, size, size / 2, size / 2, 1, readPixel(img, false, false), null);

            img = ImageIO.read(getClass().getClassLoader().getResourceAsStream("assets/modularui/textures/gui/icons/cursor_resize.png"));
            size = img.getHeight();
            resizeCursorH = new Cursor(size, size, size / 2, size / 2, 1, readPixel(img, false, false), null);
            resizeCursorV = new Cursor(size, size, size / 2, size / 2, 1, readPixel(img, false, true), null);
        } catch (IOException | LWJGLException e) {
            throw new RuntimeException(e);
        } catch (Throwable e) {
            ModularUI.LOGGER.info("Custom Cursors failed to load. This is likely because an incompatible LWJGL version was used like with CleanroomLoader.");
            // TODO: proper lwjgl 3 support
            // currently it seems this is not even triggered and the cursors are created correctly, but when the cursors are set nothing happens
        }
    }

    public static IntBuffer readPixel(BufferedImage img, boolean inverse, boolean transpose) {
        int size = img.getHeight();
        IntBuffer buffer = IntBuffer.allocate(size * size);
        int y = inverse ? 0 : size - 1;
        while (inverse ? y < size : y >= 0) {
            for (int x = 0; x < size; x++) {
                int a, b;
                if (transpose) {
                    a = y;
                    b = x;
                } else {
                    a = x;
                    b = y;
                }
                buffer.put(img.getRGB(a, b));
            }
            if (inverse) {
                y++;
            } else {
                y--;
            }
        }
        buffer.flip();
        return buffer;
    }

    public static void setCursorResizeIcon(ResizeDragArea dragArea) {
        if (resizeCursorV == null) return; // cursors failed to initialized
        if (dragArea == null) {
            resetCursorIcon();
            return;
        }
        try {
            Cursor cursor = switch (dragArea) {
                case TOP_LEFT, BOTTOM_RIGHT -> resizeCursorDiagInverse;
                case TOP_RIGHT, BOTTOM_LEFT -> resizeCursorDiag;
                case TOP, BOTTOM -> resizeCursorV;
                case LEFT, RIGHT -> resizeCursorH;
            };
            currentCursor = Mouse.setNativeCursor(cursor);
        } catch (LWJGLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void resetCursorIcon() {
        if (resizeCursorV == null) return; // cursors failed to initialized
        try {
            if (currentCursor == resizeCursorDiag || currentCursor == resizeCursorDiagInverse || currentCursor == resizeCursorH || currentCursor == resizeCursorV) {
                currentCursor = null;
            }
            Mouse.setNativeCursor(currentCursor);
            currentCursor = null;
        } catch (LWJGLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);

        ClientCommandHandler.instance.registerCommand(new ThemeReloadCommand());
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new ThemeManager());
    }

    @SubscribeEvent
    public void onKeyboard(InputEvent.KeyInputEvent event) {
        if (ModularUIConfig.enableTestGuis && testKey != null && testKey.isPressed() && ModularUI.Mods.BAUBLES.isLoaded()) {
            InventoryTypes.BAUBLES.visitAll(Platform.getClientPlayer(), (type, index, stack) -> {
                if (stack.getItem() instanceof TestItem) {
                    GuiFactories.playerInventory().openFromBaublesClient(index);
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    public Timer getTimer60Fps() {
        return this.timer60Fps;
    }
}
