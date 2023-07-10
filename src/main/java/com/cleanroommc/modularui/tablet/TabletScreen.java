package com.cleanroommc.modularui.tablet;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.Stencil;
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
        this.context.useTheme("tablet");
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
        this.context.pushViewport(null, getScreenArea());

        Area desktopArea = this.desktop.getArea();
        Stencil.applyTransformed(desktopArea.x, desktopArea.y, desktopArea.width, desktopArea.height);
        for (ModularPanel panel : getWindowManager().getReverseOpenPanels()) {
            if (panel.disablePanelsBelow()) {
                GuiDraw.drawRect(0, 0, getScreenArea().w(), getScreenArea().h(), Color.argb(16, 16, 16, (int) (125 * panel.getAlpha())));
            }
            WidgetTree.drawTree(panel, this.context);
        }
        Stencil.remove();

        this.context.popViewport(null);
        this.context.postRenderCallbacks.forEach(element -> element.accept(this.context));
        // draw frame
        this.context.pushViewport(this.desktop, this.desktop.getArea());
        this.desktop.transform(this.context);
        GlStateManager.pushMatrix();
        this.context.applyToOpenGl();
        this.desktop.drawFrame(this.context);
        GlStateManager.popMatrix();
        this.context.popViewport(this.desktop);

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableLighting();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableAlpha();
    }

    public TabletDesktop getDesktop() {
        return desktop;
    }
}
