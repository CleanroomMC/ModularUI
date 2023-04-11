package com.cleanroommc.modularui.screen;

import codechicken.nei.VisiblityData;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.api.TaggedInventoryArea;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerDrawHandler;
import com.cleanroommc.modularui.GuiErrorHandler;
import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.widget.IGuiElement;
import com.cleanroommc.modularui.api.widget.IVanillaSlot;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.Scissor;
import com.cleanroommc.modularui.mixins.GuiContainerAccessor;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.screen.viewport.LocatedWidget;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.*;
import java.util.List;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class GuiScreenWrapper extends GuiContainer implements INEIGuiHandler {

    private final ModularScreen screen;
    private boolean init = true;
    private char lastChar;

    private int fps, frameCount = 0;
    private long timer = Minecraft.getSystemTime();

    public GuiScreenWrapper(ModularContainer container, ModularScreen screen) {
        super(container);
        this.screen = screen;
        this.screen.construct(this, container.isClientOnly() ? null : container.getSyncHandler());
    }

    @Override
    public void initGui() {
        GuiErrorHandler.INSTANCE.clear();
        super.initGui();
        if (this.init) {
            this.screen.onOpen();
            this.init = false;
        }
        this.screen.onResize(width, height);
    }

    public void updateArea(Area mainViewport) {
        this.guiLeft = mainViewport.x;
        this.guiTop = mainViewport.y;
        this.xSize = mainViewport.width;
        this.ySize = mainViewport.height;
    }

    public GuiContainerAccessor getAccessor() {
        return (GuiContainerAccessor) this;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        frameCount++;
        long time = Minecraft.getSystemTime();
        if (time - timer >= 1000) {
            fps = frameCount;
            frameCount = 0;
            timer += 1000;
        }

        Scissor.scissorTransformed(this.screen.getViewport(), this.screen.context);
        drawDefaultBackground();
        int i = this.guiLeft;
        int j = this.guiTop;

        this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        this.screen.drawScreen(mouseX, mouseY, partialTicks);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        // mainly for invtweaks compat
        drawVanillaElements(mouseX, mouseY, partialTicks);
        GL11.glPushMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        getAccessor().setHoveredSlot(null);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.enableGUIStandardItemLighting();
        if (this.screen.context.isNEIEnabled()) {
            // Copied from GuiContainerManager#renderObjects but without translation
            for (IContainerDrawHandler drawHandler : GuiContainerManager.drawHandlers) {
                drawHandler.renderObjects(this, mouseX, mouseY);
            }
            for (IContainerDrawHandler drawHandler : GuiContainerManager.drawHandlers) {
                drawHandler.postRenderObjects(this, mouseX, mouseY);
            }

//            if (!shouldRenderOurTooltip()) {
            // nh todo?
            if (true) {
                GuiContainerManager.getManager().renderToolTips(mouseX, mouseY);
            }
        }
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.disableStandardItemLighting();
        this.drawGuiContainerForegroundLayer(mouseX, mouseY);
        this.screen.drawForeground(partialTicks);
        RenderHelper.enableGUIStandardItemLighting();

        getAccessor().setHoveredSlot(null);
        IGuiElement hovered = this.screen.context.getHovered();
        if (hovered instanceof IVanillaSlot) {
            getAccessor().setHoveredSlot(((IVanillaSlot) hovered).getVanillaSlot());
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPushMatrix();
        GL11.glTranslatef(i, j, 0);
        GL11.glPopMatrix();

        InventoryPlayer inventoryplayer = this.mc.thePlayer.inventory;
        ItemStack itemstack = getAccessor().getDraggedStack() == null ? inventoryplayer.getItemStack() : getAccessor().getDraggedStack();
        GL11.glTranslatef(i, j, 0.0F);
        if (itemstack != null) {
            int k2 = getAccessor().getDraggedStack() == null ? 8 : 16;
            String s = null;

            if (getAccessor().getDraggedStack() != null && getAccessor().getIsRightMouseClick()) {
                itemstack = itemstack.copy();
                itemstack.stackSize = MathHelper.ceiling_double_int((float) itemstack.stackSize / 2.0F);
            } else if (this.isDragSplitting() && this.getDragSlots().size() > 1) {
                itemstack = itemstack.copy();
                itemstack.stackSize = getAccessor().getDragSplittingRemnant();

                if (itemstack.stackSize < 1) {
                    s = EnumChatFormatting.YELLOW + "0";
                }
            }

            this.drawItemStack(itemstack, mouseX - i - 8, mouseY - j - k2, s);
        }

        if (getAccessor().getReturningStack() != null) {
            float f = (float) (Minecraft.getSystemTime() - getAccessor().getReturningStackTime()) / 100.0F;

            if (f >= 1.0F) {
                f = 1.0F;
                getAccessor().setReturningStack(null);
            }

            int l2 = getAccessor().getReturningStackDestSlot().xDisplayPosition - getAccessor().getTouchUpX();
            int i3 = getAccessor().getReturningStackDestSlot().yDisplayPosition - getAccessor().getTouchUpY();
            int l1 = getAccessor().getTouchUpX() + (int) ((float) l2 * f);
            int i2 = getAccessor().getTouchUpY() + (int) ((float) i3 * f);
            this.drawItemStack(getAccessor().getReturningStack(), l1, i2, null);
        }

        GL11.glPopMatrix();

        if (ModularUIConfig.guiDebugMode) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND);
            drawDebugScreen();
            GL11.glColor4f(1f, 1f, 1f, 1f);
        }
        GuiErrorHandler.INSTANCE.drawErrors(0, 0);

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableStandardItemLighting();

        Scissor.unscissor(this.screen.context);
    }

    @Override
    public void drawWorldBackground(int tint) {
        if (this.mc.theWorld == null) {
            super.drawWorldBackground(tint);
            return;
        }
        float alpha = this.screen.getMainPanel().getAlpha();
        // vanilla color values as hex
        int color = 0x101010;
        int startAlpha = 0xc0;
        int endAlpha = 0xd0;
        this.drawGradientRect(0, 0, this.width, this.height, Color.withAlpha(color, (int) (startAlpha * alpha)), Color.withAlpha(color, (int) (endAlpha * alpha)));
    }

    private void drawItemStack(ItemStack stack, int x, int y, String altText) {
        GL11.glTranslatef(0.0F, 0.0F, 32.0F);
        this.zLevel = 200.0F;
        itemRender.zLevel = 200.0F;
        FontRenderer font = stack.getItem().getFontRenderer(stack);
        if (font == null) font = getFontRenderer();
        itemRender.renderItemAndEffectIntoGUI(font, mc.getTextureManager(), stack, x, y);
        itemRender.renderItemOverlayIntoGUI(font, mc.getTextureManager(), stack, x, y - (getAccessor().getDraggedStack() == null ? 0 : 8), altText);
        GuiDraw.afterRenderItemAndEffectIntoGUI(stack);
        this.zLevel = 0.0F;
        itemRender.zLevel = 0.0F;
    }

    protected void drawVanillaElements(int mouseX, int mouseY, float partialTicks) {
        for (Object guiButton : this.buttonList) {
            ((GuiButton) guiButton).drawButton(this.mc, mouseX, mouseY);
        }
        for (Object guiLabel : this.labelList) {
            ((GuiLabel) guiLabel).func_146159_a(this.mc, mouseX, mouseY);
        }
    }

    public void drawDebugScreen() {
        GuiContext context = screen.context;
        int mouseX = context.getAbsMouseX(), mouseY = context.getAbsMouseY();
        int screenW = this.screen.getViewport().width, screenH = this.screen.getViewport().height;
        int color = Color.rgb(180, 40, 115);
        int lineY = screenH - 13 - (this.screen.context.isNEIEnabled() ? 20 : 0);
        drawString(getFontRenderer(), "Mouse Pos: " + mouseX + ", " + mouseY, 5, lineY, color);
        lineY -= 11;
        drawString(getFontRenderer(), "FPS: " + fps, 5, lineY, color);
        lineY -= 11;
        LocatedWidget locatedHovered = this.screen.getWindowManager().getTopWidgetLocated();
        if (locatedHovered != null) {
            IGuiElement hovered = locatedHovered.getElement();
            locatedHovered.applyMatrix(context);
            GL11.glPushMatrix();
            context.applyToOpenGl();

            Area area = hovered.getArea();
            IGuiElement parent = hovered.getParent();

            GuiDraw.drawBorder(0, 0, area.width, area.height, color, 1f);
            if (parent != null) {
                GuiDraw.drawBorder(-area.rx, -area.ry, parent.getArea().width, parent.getArea().height, Color.withAlpha(color, 0.3f), 1f);
            }
            GL11.glPopMatrix();
            locatedHovered.unapplyMatrix(context);
            GuiDraw.drawText("Pos: " + area.x + ", " + area.y, 5, lineY, 1, color, false);
            lineY -= 11;
            GuiDraw.drawText("Size: " + area.width + ", " + area.height, 5, lineY, 1, color, false);
            lineY -= 11;
            if (parent != null) {
                GuiDraw.drawText("Parent: " + parent, 5, lineY, 1, color, false);
                lineY -= 11;
            }
            GuiDraw.drawText("Class: " + hovered, 5, lineY, 1, color, false);
            lineY -= 11;
            if (hovered instanceof ItemSlot) {
                ItemSlot slotWidget = (ItemSlot) hovered;
                Slot slot = slotWidget.getSlot();
                GuiDraw.drawText("Slot Index: " + slot.getSlotIndex(), 5, lineY, 1, color, false);
                lineY -= 11;
                GuiDraw.drawText("Slot Number: " + slot.slotNumber, 5, lineY, 1, color, false);
                lineY -= 11;
                if (slotWidget.isSynced()) {
                    SlotGroup slotGroup = ((ModularContainer) inventorySlots).getSlotGroup(slotWidget.getSyncHandler());
                    boolean allowShiftTransfer = slotGroup != null && slotGroup.allowShiftTransfer();
                    GuiDraw.drawText("Shift-Click Priority: " + (allowShiftTransfer ? slotGroup.getShiftClickPriority() : "DISABLED"), 5, lineY, 1, color, false);
                }
            }
        }
        color = Color.withAlpha(color, 25);
        for (int i = 5; i < screenW; i += 5) {
            drawVerticalLine(i, 0, screenH, color);
        }

        for (int i = 5; i < screenH; i += 5) {
            drawHorizontalLine(0, screenW, i, color);
        }
        drawRect(mouseX, mouseY, mouseX + 1, mouseY + 1, Color.withAlpha(Color.GREEN.normal, 0.8f));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.screen.onUpdate();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        this.screen.onClose();
    }

    public ModularScreen getScreen() {
        return screen;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (this.screen.onMousePressed(mouseButton)) return;
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void clickSlot() {
        super.mouseClicked(screen.context.getAbsMouseX(), screen.context.getAbsMouseY(), screen.context.getMouseButton());
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        if (this.screen.onMouseRelease(state)) return;
        super.mouseMovedOrUp(mouseX, mouseY, state);
    }

    public void releaseSlot() {
        super.mouseMovedOrUp(screen.context.getAbsMouseX(), screen.context.getAbsMouseY(), screen.context.getMouseButton());
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (this.screen.onMouseDrag(clickedMouseButton, timeSinceLastClick)) return;
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    public void dragSlot(long timeSinceLastClick) {
        super.mouseClickMove(screen.context.getAbsMouseX(), screen.context.getAbsMouseY(), screen.context.getMouseButton(), timeSinceLastClick);
    }

    /**
     * This replicates vanilla behavior while also injecting custom behavior for consistency
     */
    @Override
    public void handleKeyboardInput() {
        char c0 = Keyboard.getEventCharacter();
        int key = Keyboard.getEventKey();
        boolean state = Keyboard.getEventKeyState();

        if (state) {
            this.lastChar = c0;
            if (this.screen.onKeyPressed(c0, key)) return;
            keyTyped(c0, key);
        } else {
            // when the key is released, the event char is empty
            if (this.screen.onKeyRelease(this.lastChar, key)) return;
            if (key == 0 && c0 >= ' ') {
                keyTyped(c0, key);
            }
        }

        this.mc.func_152348_aa();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        // debug mode C + CTRL + SHIFT + ALT
        if (keyCode == Keyboard.KEY_C && isCtrlKeyDown() && isShiftKeyDown() && Interactable.hasAltDown()) {
            ModularUIConfig.guiDebugMode = !ModularUIConfig.guiDebugMode;
            return;
        }
        if (keyCode == Keyboard.KEY_ESCAPE || keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
            this.screen.close();
        }

        this.checkHotbarKeys(keyCode);
        Slot hoveredSlot = getAccessor().getHoveredSlot();
        if (hoveredSlot != null && hoveredSlot.getHasStack()) {
            if (keyCode == this.mc.gameSettings.keyBindPickBlock.getKeyCode()) {
                this.handleMouseClick(hoveredSlot, hoveredSlot.slotNumber, 0, 3);
            } else if (keyCode == this.mc.gameSettings.keyBindDrop.getKeyCode()) {
                this.handleMouseClick(hoveredSlot, hoveredSlot.slotNumber, isCtrlKeyDown() ? 1 : 0, 4);
            }
        }
    }

    public boolean isDragSplitting() {
        return getAccessor().isDragSplittingInternal();
    }

    public Set<Slot> getDragSlots() {
        return getAccessor().getDragSplittingSlots();
    }

    public static RenderItem getItemRenderer() {
        return itemRender;
    }

    public float getZ() {
        return zLevel;
    }

    public void setZ(float z) {
        this.zLevel = z;
    }

    public FontRenderer getFontRenderer() {
        return Minecraft.getMinecraft().fontRenderer;
    }

    // === NEI overrides ===

    @Override
    public VisiblityData modifyVisiblity(GuiContainer gui, VisiblityData currentVisibility) {
        return null;
    }

    @Override
    public Iterable<Integer> getItemSpawnSlots(GuiContainer gui, ItemStack item) {
        return null;
    }

    @Override
    public List<TaggedInventoryArea> getInventoryAreas(GuiContainer gui) {
        return null;
    }

    @Override
    public boolean handleDragNDrop(GuiContainer gui, int mousex, int mousey, ItemStack draggedStack, int button) {
        return false;
    }

    @Override
    public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h) {
        if (!(gui instanceof GuiScreenWrapper)) return false;
        if (!this.screen.context.isNEIEnabled()) return false;
        // nh todo dragged things
        return this.screen.context.getAllNEIExclusionAreas().stream().anyMatch(
            a -> a.intersects(new Rectangle(x, y, w, h))
        );
    }
}
