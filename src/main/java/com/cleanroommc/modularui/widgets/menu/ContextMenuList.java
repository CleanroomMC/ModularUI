package com.cleanroommc.modularui.widgets.menu;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widgets.ListWidget;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class ContextMenuList<W extends ContextMenuList<W>> extends ListWidget<IWidget, W> {

    private ContextMenuButton<?> source;

    public ContextMenuList(String name) {
        name(name);
        padding(2);
    }

    public void close() {
        if (this.source != null) {
            this.source.closeMenu(false);
        }
    }

    public boolean isSelfOrChildHovered() {
        if (isBelowMouse()) return true;
        for (IWidget option : getTypeChildren()) {
            if ((option instanceof IContextMenuOption menuOption && menuOption.isSelfOrChildHovered()) || option.isBelowMouse()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onMouseLeaveArea() {
        super.onMouseLeaveArea();
        checkClose();
    }

    @Override
    protected void onChildAdd(IWidget child) {
        super.onChildAdd(child);
        if (!child.resizer().hasHeight()) {
            child.resizer().height(12);
        }
        if (!child.resizer().hasWidth()) {
            child.resizer().widthRel(1f);
        }
    }

    @Override
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getWidgetTheme(IThemeApi.PANEL);
    }

    public void checkClose() {
        if (this.source != null && !this.source.isBelowMouse() && !isSelfOrChildHovered()) {
            this.source.closeMenu(true);
            this.source.checkClose();
        }
    }

    void setSource(ContextMenuButton<?> menuButton) {
        this.source = menuButton;
    }

    protected ContextMenuButton<?> getSource() {
        return source;
    }
}
