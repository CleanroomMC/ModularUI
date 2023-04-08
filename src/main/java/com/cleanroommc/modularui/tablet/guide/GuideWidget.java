package com.cleanroommc.modularui.tablet.guide;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.IconRenderer;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.ScrollData;
import com.cleanroommc.modularui.utils.ScrollDirection;
import com.cleanroommc.modularui.widget.ScrollWidget;

import java.util.List;

public class GuideWidget extends ScrollWidget<GuideWidget> {

    private static final IconRenderer textRenderer = new IconRenderer();

    static {
        textRenderer.setColor(IKey.TEXT_COLOR);
        textRenderer.setShadow(false);
        textRenderer.setUseWholeWidth(true);
    }

    private GuidePage currentGuidePage;
    private List<IIcon> textCache;

    public GuideWidget() {
        getScrollArea().setScrollData(new ScrollData(ScrollDirection.VERTICAL));
    }

    @Override
    public void preDraw(GuiContext context, boolean transformed) {
        super.preDraw(context, transformed);
        if (transformed) {
            if (this.currentGuidePage == null) {
                setCurrentGuidePage(GuideManager.getFirst());
            }
            textRenderer.setColor(getWidgetTheme().getTextColor());
            textRenderer.setPos(20, 10);
            textRenderer.setAlignment(Alignment.TopLeft, getArea().width - 40);
            if (this.textCache == null) {
                this.textCache = textRenderer.measureLines(this.currentGuidePage.getDrawables());
            }
            textRenderer.drawMeasuredLines(context, this.textCache);
            getScrollArea().getScrollY().scrollSize = (int) (textRenderer.getLastHeight() + 10.5);
        }
    }

    @Override
    public WidgetTheme getWidgetTheme(ITheme theme) {
        return theme.getPanelTheme();
    }

    public void clearCache() {
        this.textCache = null;
    }

    public void setCurrentGuidePage(GuidePage currentGuidePage) {
        this.currentGuidePage = currentGuidePage;
        getScrollArea().getScrollY().scroll = 0;
        clearCache();
        if (currentGuidePage.getDrawables() == null) {
            currentGuidePage.load();
        }
    }
}
