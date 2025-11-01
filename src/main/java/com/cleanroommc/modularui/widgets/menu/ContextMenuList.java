package com.cleanroommc.modularui.widgets.menu;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widgets.ListWidget;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public class ContextMenuList<W extends ContextMenuList<W>> extends ListWidget<IWidget, W> {

    private final String name;
    private ContextMenuButton<?> source;

    public ContextMenuList(String name) {
        this.name = name;
        padding(2);
    }

    public void close() {
        if (this.source != null) {
            this.source.closeMenu(false);
        }
    }

    @NotNull
    @Override
    public String getName() {
        return name;
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
        if (!child.flex().hasHeight()) {
            child.flex().height(12);
        }
        if (!child.flex().hasWidth()) {
            child.flex().widthRel(1f);
        }
    }

    @Override
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getWidgetTheme(IThemeApi.CONTEXT_MENU);
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
