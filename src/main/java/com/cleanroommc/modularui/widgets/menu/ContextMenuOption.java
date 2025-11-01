package com.cleanroommc.modularui.widgets.menu;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.DelegatingSingleChildWidget;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class ContextMenuOption<W extends ContextMenuOption<W>> extends DelegatingSingleChildWidget<W> implements IContextMenuOption {

    @Override
    protected void onChildAdd(IWidget child) {
        if (!child.flex().hasHeight()) {
            child.flex().height(12);
        }
        if (!child.flex().hasWidth()) {
            child.flex().widthRel(1f);
        }
        /*if (child instanceof Widget<?> widget && widget.getWidgetThemeOverride() == null) {
            widget.widgetTheme(IThemeApi.MENU_OPTION);
        }*/
    }

    @Override
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getWidgetTheme(IThemeApi.MENU_OPTION);
    }
}
