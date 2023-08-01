package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

public class PagedWidget<W extends PagedWidget<W>> extends Widget<W> {

    private final List<IWidget> pages = new ArrayList<>();
    private IWidget currentPage;
    private int currentPageIndex = 0;

    @Override
    public void afterInit() {
        setPage(0);
    }

    public void setPage(int page) {
        if (page < 0 || page >= this.pages.size()) {
            throw new IndexOutOfBoundsException();
        }
        this.currentPageIndex = page;
        if (this.currentPage != null) {
            this.currentPage.setEnabled(false);
        }
        this.currentPage = this.pages.get(this.currentPageIndex);
        this.currentPage.setEnabled(true);
    }

    public void nextPage() {
        if (++this.currentPageIndex == this.pages.size()) {
            this.currentPageIndex = 0;
        }
        setPage(this.currentPageIndex);
    }

    public void previousPage() {
        if (--this.currentPageIndex == -1) {
            this.currentPageIndex = this.pages.size() - 1;
        }
        setPage(this.currentPageIndex);
    }

    public List<IWidget> getPages() {
        return this.pages;
    }

    public IWidget getCurrentPage() {
        return this.currentPage;
    }

    public int getCurrentPageIndex() {
        return this.currentPageIndex;
    }

    @Override
    public @Unmodifiable @NotNull List<IWidget> getChildren() {
        return this.pages;
    }

    @Override
    public void resize() {
        int page = this.currentPageIndex;
        this.currentPageIndex = -1;
        super.resize();
        this.currentPageIndex = page;

    }

    public W addPage(IWidget widget) {
        this.pages.add(widget);
        widget.setEnabled(false);
        return getThis();
    }

    public W controller(Controller controller) {
        controller.setPagedWidget(this);
        return getThis();
    }

    public static class Controller {

        private PagedWidget<?> pagedWidget;

        public boolean isInitialised() {
            return this.pagedWidget != null && this.pagedWidget.isValid();
        }

        private void validate() {
            if (!isInitialised()) {
                throw new IllegalStateException("PagedWidget controller does not have a valid PagedWidget! Current PagedWidget: " + this.pagedWidget);
            }
        }

        private void setPagedWidget(PagedWidget<?> pagedWidget) {
            this.pagedWidget = pagedWidget;
        }

        public void setPage(int page) {
            validate();
            this.pagedWidget.setPage(page);
        }

        public void nextPage() {
            validate();
            this.pagedWidget.nextPage();
        }

        public void previousPage() {
            validate();
            this.pagedWidget.previousPage();
        }

        public IWidget getActivePage() {
            validate();
            return this.pagedWidget.getCurrentPage();
        }

        public int getActivePageIndex() {
            validate();
            return this.pagedWidget.getCurrentPageIndex();
        }
    }
}
