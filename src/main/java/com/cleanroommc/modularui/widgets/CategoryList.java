package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.layout.ILayoutWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.WidgetTree;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CategoryList extends ParentWidget<CategoryList> implements Interactable, ILayoutWidget {

    private final List<CategoryList> subCategories = new ArrayList<>();
    private boolean expanded = false;
    private int totalHeight = 0;
    private IDrawable expandedOverlay;
    private IDrawable collapsedOverlay;

    @Override
    public void drawBackground(GuiContext context) {
        super.drawBackground(context);
        if (this.expanded) {
            this.expandedOverlay.drawAtZero(context, getArea());
        } else {
            this.collapsedOverlay.drawAtZero(context, getArea());
        }
    }

    @Override
    public void onInit() {
        super.onInit();
        if (this.expandedOverlay == null) {
            if (getParent() instanceof CategoryList) {
                this.expandedOverlay = ((CategoryList) getParent()).expandedOverlay;
            } else if (getParent() instanceof Root) {
                this.expandedOverlay = ((Root) getParent()).expandedOverlay;
            } else {
                this.expandedOverlay = IDrawable.EMPTY;
            }
        }
        if (this.collapsedOverlay == null) {
            if (getParent() instanceof CategoryList) {
                this.collapsedOverlay = ((CategoryList) getParent()).collapsedOverlay;
            } else if (getParent() instanceof Root) {
                this.collapsedOverlay = ((Root) getParent()).collapsedOverlay;
            } else {
                this.collapsedOverlay = IDrawable.EMPTY;
            }
        }
    }

    @Override
    public void onChildAdd(IWidget child) {
        if (child instanceof CategoryList) {
            this.subCategories.add((CategoryList) child);
        }
        child.setEnabled(this.expanded);
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (mouseButton == 0 || mouseButton == 1) {
            expanded(!this.expanded);
            return Result.SUCCESS;
        }
        return Result.ACCEPT;
    }

    public void expanded(boolean expanded) {
        if (expanded == this.expanded) return;
        this.expanded = expanded;
        for (IWidget widget : getChildren()) {
            widget.setEnabled(expanded);
        }
        calculateHeightAndLayout(true);
    }

    public void calculateHeightAndLayout(boolean calculateParents) {
        if (this.expanded) {
            int y = getArea().height;
            for (IWidget widget : getChildren()) {
                widget.getArea().ry = y;
                widget.resizer().setYResized(true);
                y += widget instanceof CategoryList && ((CategoryList) widget).expanded ?
                        ((CategoryList) widget).totalHeight : widget.getArea().height;
            }
            this.totalHeight = y;
        } else {
            this.totalHeight = getArea().height;
        }

        if (!calculateParents) return;
        if (getParent() instanceof CategoryList) {
            ((CategoryList) getParent()).calculateHeightAndLayout(true);
        } else if (getParent() instanceof Root) {
            ((Root) getParent()).updateHeight();
        }
    }

    @Override
    public void layoutWidgets() {
        calculateHeightAndLayout(false);
    }

    public CategoryList setCollapsedOverlay(IDrawable collapsedOverlay) {
        this.collapsedOverlay = collapsedOverlay;
        return this;
    }

    public CategoryList setExpandedOverlay(IDrawable expandedOverlay) {
        this.expandedOverlay = expandedOverlay;
        return this;
    }

    public static class Root extends ListWidget<Void, IWidget, Root> {

        private final List<CategoryList> categories = new ArrayList<>();

        private IDrawable expandedOverlay = GuiTextures.MOVE_DOWN.asIcon().size(16, 8).alignment(Alignment.CenterRight).marginRight(4);
        private IDrawable collapsedOverlay = GuiTextures.MOVE_RIGHT.asIcon().size(8, 16).alignment(Alignment.CenterRight).marginRight(8);

        @Override
        public void onChildAdd(IWidget child) {
            if (child instanceof CategoryList) {
                this.categories.add((CategoryList) child);
            }
        }

        private void updateHeight() {
            layoutWidgets();
            WidgetTree.applyPos(this);
        }

        @Override
        public void layoutWidgets() {
            int y = 0;
            for (IWidget widget : getChildren()) {
                widget.getArea().ry = y;
                widget.resizer().setYResized(true);
                y += widget instanceof CategoryList && ((CategoryList) widget).expanded ?
                        ((CategoryList) widget).totalHeight : widget.getArea().height;
            }
            getScrollArea().getScrollY().scrollSize = y;
        }

        public Root setCollapsedOverlay(IDrawable collapsedOverlay) {
            this.collapsedOverlay = collapsedOverlay;
            return this;
        }

        public Root setExpandedOverlay(IDrawable expandedOverlay) {
            this.expandedOverlay = expandedOverlay;
            return this;
        }
    }
}
