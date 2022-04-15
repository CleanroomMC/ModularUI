package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.widget.IWidgetParent;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.api.widget.Widget;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PageControlWidget extends Widget implements IWidgetParent {

    private int currentPage = 0;
    private final List<Widget> pages = new ArrayList<>();

    @Override
    public void onInit() {
        if (pages.isEmpty()) {
            throw new IllegalStateException("PageControlWidget must have at least one child!");
        }
        for (int i = 0; i < pages.size(); i++) {
            if (i != currentPage) {
                setPage(i, false);
            }
        }
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        return pages.isEmpty() ? super.determineSize(maxWidth, maxHeight) : MultiChildWidget.getSizeOf(pages);
    }

    protected List<Widget> getPages() {
        return pages;
    }

    @Override
    public List<Widget> getChildren() {
        return Collections.unmodifiableList(pages);
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void nextPage() {
        if (currentPage + 1 >= pages.size()) {
            setActivePage(0);
        } else {
            setActivePage(currentPage + 1);
        }
    }

    public void prevPage() {
        if (currentPage == 0) {
            setActivePage(pages.size() - 1);
        } else {
            setActivePage(currentPage - 1);
        }
    }

    public void setActivePage(int page) {
        if (page > pages.size() - 1 || page < 0) {
            throw new IndexOutOfBoundsException("Tried setting active page to " + page + " while only 0 - " + (pages.size() - 1) + " is allowed");
        }
        if (!isInitialised()) {
            this.currentPage = page;
            return;
        }
        setPage(currentPage, false);
        this.currentPage = page;
        setPage(currentPage, true);
    }

    private void setPage(int page, boolean active) {
        Widget widget = pages.get(page);
        widget.setEnabled(active);
        if (widget instanceof IWidgetParent) {
            IWidgetParent.forEachByLayer((IWidgetParent) widget, widget1 -> {
                widget1.setEnabled(active);
                return false;
            });
        }
    }

    public PageControlWidget addPage(Widget page) {
        if (page == this) {
            ModularUI.LOGGER.error("Can't add self!");
            return this;
        }
        if (isInitialised()) {
            ModularUI.LOGGER.error("Can't add child after initialised!");
        } else {
            pages.add(page);
        }
        return this;
    }
}
