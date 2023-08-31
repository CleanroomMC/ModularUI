package com.cleanroommc.modularui.widgets.layout;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.widget.ParentWidget;

public class OrganizedPanel extends ModularPanel {

    private ParentWidget<?> header;
    private ParentWidget<?> leftSide;
    private ParentWidget<?> rightSide;
    private ParentWidget<?> footer;
    private final ParentWidget<?> body = new ParentWidget<>();

    private int headerHeight = 20;
    private int footerHeight = 12;
    private int leftSideWidth = 60;
    private int rightSideWidth = 60;

    public OrganizedPanel(String name) {
        super(name);
        getChildren().add(this.body);
    }

    @Override
    public boolean addChild(IWidget child, int index) {
        return this.body.addChild(child, index);
    }

    private ParentWidget<?> getHeader() {
        if (this.header != null) {
            this.header = new ParentWidget<>();
            getChildren().add(this.header);
        }
        return this.header;
    }

    private ParentWidget<?> getLeftSide() {
        if (this.leftSide != null) {
            this.leftSide = new ParentWidget<>();
            getChildren().add(this.leftSide);
        }
        return this.leftSide;
    }

    private ParentWidget<?> getRightSide() {
        if (this.rightSide != null) {
            this.rightSide = new ParentWidget<>();
            getChildren().add(this.rightSide);
        }
        return this.rightSide;
    }

    private ParentWidget<?> getFooter() {
        if (this.footer != null) {
            this.footer = new ParentWidget<>();
            getChildren().add(this.footer);
        }
        return this.footer;
    }

    public ParentWidget<?> getBody() {
        return this.body;
    }

    @Override
    public void beforeResize() {
        int top = 0;
        int bot = 0;
        this.body.flex().reset();
        this.body.left(0).right(0).top(0).bottom(0);
        if (this.header != null) {
            this.header.flex().reset();
            this.header.left(0).right(0).top(0).height(this.headerHeight);
            this.body.top(this.headerHeight);
            top = this.headerHeight;
        }
        if (this.footer != null) {
            this.footer.flex().reset();
            this.footer.left(0).right(0).bottom(0).height(this.footerHeight);
            this.body.bottom(this.footerHeight);
            bot = this.footerHeight;
        }
        if (this.leftSide != null) {
            this.leftSide.flex().reset();
            this.leftSide.left(0).width(this.leftSideWidth).top(top).bottom(bot);
            this.body.left(this.leftSideWidth);
        }
        if (this.rightSide != null) {
            this.rightSide.flex().reset();
            this.rightSide.right(0).width(this.rightSideWidth).top(top).bottom(bot);
            this.body.right(this.rightSideWidth);
        }
    }

    public OrganizedPanel header(IWidget widget) {
        getHeader().child(widget);
        return this;
    }

    public OrganizedPanel footer(IWidget widget) {
        getFooter().child(widget);
        return this;
    }

    public OrganizedPanel leftSide(IWidget widget) {
        getLeftSide().child(widget);
        return this;
    }

    public OrganizedPanel rightSide(IWidget widget) {
        getRightSide().child(widget);
        return this;
    }

    public OrganizedPanel setHeaderHeight(int headerHeight) {
        this.headerHeight = headerHeight;
        return this;
    }

    public OrganizedPanel setFooterHeight(int footerHeight) {
        this.footerHeight = footerHeight;
        return this;
    }

    public OrganizedPanel setLeftSideWidth(int leftSideWidth) {
        this.leftSideWidth = leftSideWidth;
        return this;
    }

    public OrganizedPanel setRightSideWidth(int rightSideWidth) {
        this.rightSideWidth = rightSideWidth;
        return this;
    }

    @Override
    public OrganizedPanel bindPlayerInventory() {
        return (OrganizedPanel) super.bindPlayerInventory();
    }
}
