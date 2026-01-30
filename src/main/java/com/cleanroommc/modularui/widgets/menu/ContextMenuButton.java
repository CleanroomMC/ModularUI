package com.cleanroommc.modularui.widgets.menu;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widgets.ListWidget;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

@ApiStatus.Experimental
public class ContextMenuButton<W extends ContextMenuButton<W>> extends AbstractMenuButton<W> {

    public ContextMenuButton(String panelName) {
        super(panelName);
        this.openOnHover = true;
    }

    @Override
    protected Menu<?> createMenu() {
        return null;
    }

    public W menu(Menu<?> menu) {
        setMenu(menu);
        return getThis();
    }

    public W menuList(Consumer<ListWidget<IWidget, ?>> builder) {
        ListWidget<IWidget, ?> l = new ListWidget<>().widthRel(1f);
        builder.accept(l);
        return menu(new Menu<>()
                .widthRel(1f)
                .coverChildrenHeight()
                .child(l));
    }

    public W direction(Direction direction) {
        this.direction = direction;
        return getThis();
    }

    public W requiresClick() {
        this.openOnHover = false;
        return getThis();
    }

    public W openUp() {
        return direction(Direction.UP);
    }

    public W openDown() {
        return direction(Direction.DOWN);
    }

    public W openLeftUp() {
        return direction(Direction.LEFT_UP);
    }

    public W openLeftDown() {
        return direction(Direction.LEFT_DOWN);
    }

    public W openRightUp() {
        return direction(Direction.RIGHT_UP);
    }

    public W openRightDown() {
        return direction(Direction.RIGHT_DOWN);
    }

    public W openCustom() {
        return direction(Direction.UNDEFINED);
    }

}
