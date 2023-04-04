package com.cleanroommc.modularui.tablet;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.WidgetTree;
import com.cleanroommc.modularui.widget.sizer.Area;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class TabletScreen extends ModularScreen {


    private final ItemStack tabletItem;
    private TabletDesktop desktop;
    private Area frameArea;

    public TabletScreen(ItemStack tabletItem) {
        super(ModularUI.ID, "tablet");
        this.tabletItem = tabletItem;
    }

    @Override
    public ModularPanel buildUI(GuiContext context) {
        this.desktop = new TabletDesktop(context);
        return this.desktop;
    }

    @Override
    public void onResize(int width, int height) {
        super.onResize(width, height);
        this.context.removeJeiExclusionArea(this.frameArea);
        // jei should respect frame
        this.frameArea = this.desktop.getArea().createCopy();
        this.frameArea.expand(5);
        this.context.addJeiExclusionArea(this.frameArea);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.context.updateState(mouseX, mouseY, partialTicks);

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableAlpha();

        this.context.reset();
        this.context.pushViewport(null, getViewport());

        Area desktopArea = this.desktop.getArea();
        GuiDraw.scissorTransformed(desktopArea.x, desktopArea.y, desktopArea.width, desktopArea.height, this.context);
        for (ModularPanel panel : getWindowManager().getReverseOpenPanels()) {
            if (panel.disablePanelsBelow()) {
                GuiDraw.drawSolidRect(0, 0, getViewport().w(), getViewport().h(), Color.argb(16, 16, 16, (int) (125 * panel.getAlpha())));
            }
            WidgetTree.drawTree(panel, this.context);
        }

        GuiDraw.unscissor(this.context);

        this.context.popViewport(null);
        this.context.postRenderCallbacks.forEach(element -> element.accept(this.context));
        // draw frame
        this.desktop.apply(this.context, IViewport.DRAWING);
        GlStateManager.pushMatrix();
        GlStateManager.translate(desktopArea.x, desktopArea.y, 0);
        this.context.applyToOpenGl();
        this.desktop.drawFrame(this.context);
        GlStateManager.popMatrix();
        this.desktop.unapply(this.context, IViewport.DRAWING);

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableLighting();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableAlpha();
    }

    public TabletDesktop getDesktop() {
        return desktop;
    }
}
