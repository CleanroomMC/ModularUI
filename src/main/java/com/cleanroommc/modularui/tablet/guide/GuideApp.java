package com.cleanroommc.modularui.tablet.guide;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.tablet.app.TabletApp;
import com.cleanroommc.modularui.utils.Animator;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.Interpolation;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.WidgetTree;
import com.cleanroommc.modularui.widget.sizer.Unit;
import com.cleanroommc.modularui.widgets.ButtonWidget;

public class GuideApp extends TabletApp {

    private static final int categoryListWidth = 100;

    private final Widget<?> categoryList;
    private final ButtonWidget<?> expandButton;
    private final GuideWidget guideWidget;
    private boolean listExpanded;
    private int listRightSide;

    private final Animator listAnimator = new Animator(15, Interpolation.QUINT_OUT);

    private static final IDrawable expandedOverlay = GuiTextures.MOVE_LEFT.asIcon().size(6, 12);
    private static final IDrawable collapsedOverlay = GuiTextures.MOVE_RIGHT.asIcon().size(6, 12);

    public GuideApp(GuiContext context) {
        super(context);
        this.listExpanded = true;
        this.listRightSide = categoryListWidth + 4;
        this.categoryList = GuideManager.getCategory().buildGui(context, this)
                .left(() -> listRightSide - categoryListWidth - 4, Unit.Measure.PIXEL)
                .width(categoryListWidth).height(1f)
                .paddingLeft(0)
                .background(new Rectangle().setColor(Color.withAlpha(0, 0.65f)));

        this.expandButton = new ButtonWidget<>()
                .top(0).left(() -> listRightSide, Unit.Measure.PIXEL)
                .size(14, 14)
                .overlay(expandedOverlay)
                .onMousePressed(mouseButton -> {
                    expandList(!this.listExpanded);
                    return true;
                });
        this.guideWidget = new GuideWidget()
                .left(() -> listRightSide, Unit.Measure.PIXEL).right(0)
                .height(1f);
        child(this.categoryList);
        child(this.expandButton);
        child(this.guideWidget);
        this.listAnimator.setValueBounds(0, categoryListWidth + 4);
        this.listAnimator.setCallback(val -> {
            this.listRightSide = (int) val;
            this.guideWidget.clearCache();
            WidgetTree.resize(this);
        });
    }

    public void expandList(boolean expanded) {
        if (expanded != this.listExpanded) {
            this.listExpanded = expanded;
            if (expanded) {
                this.expandButton.overlay(expandedOverlay);
                this.listAnimator.setValueBounds(0, categoryListWidth + 4);
                this.listAnimator.forward();
            } else {
                this.expandButton.overlay(collapsedOverlay);
                this.listAnimator.setValueBounds(categoryListWidth + 4, 0);
                this.listAnimator.forward();
            }
        }
    }

    public void setCurrentGuidePage(GuidePage currentGuidePage) {
        this.guideWidget.setCurrentGuidePage(currentGuidePage);
        if (currentGuidePage.getDrawables() == null) {
            currentGuidePage.load();
        }
    }
}
