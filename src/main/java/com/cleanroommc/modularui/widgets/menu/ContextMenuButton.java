package com.cleanroommc.modularui.widgets.menu;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Flex;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@ApiStatus.Experimental
public class ContextMenuButton<W extends ContextMenuButton<W>> extends Widget<W> implements IContextMenuOption, Interactable {

    private Direction direction = Direction.DOWN;
    private boolean requiresClick;

    private ContextMenuList<?> menuList;
    private boolean open, softOpen;
    private IPanelHandler panelHandler;

    public boolean isOpen() {
        return open;
    }

    public boolean isSoftOpen() {
        return softOpen;
    }

    public void toggleMenu(boolean soft) {
        if (this.open) {
            if (this.softOpen) {
                if (soft) {
                    closeMenu(true);
                } else {
                    this.softOpen = false;
                }
            } else if (!soft) {
                closeMenu(false);
            }
        } else {
            openMenu(soft);
        }
    }

    public void openMenu(boolean soft) {
        if (this.open) {
            if (this.softOpen && !soft) {
                this.softOpen = false;
            }
            return;
        }
        initMenuList();
        if (getPanel() instanceof MenuPanel menuPanel) {
            menuPanel.openSubMenu(getMenuList());
        } else {
            getPanelHandler().openPanel();
        }
        this.open = true;
        this.softOpen = soft;
    }

    public void closeMenu(boolean soft) {
        if (!this.open || (!this.softOpen && soft)) return;
        if (getPanel() instanceof MenuPanel menuPanel) {
            menuPanel.remove(getMenuList());
        } else {
            getPanelHandler().closePanel();
        }
        this.open = false;
        this.softOpen = false;
    }

    private ContextMenuList<?> getMenuList() {
        return this.menuList;
    }

    private void initMenuList() {
        if (this.menuList == null) {
            this.menuList = new ContextMenuList<>("no_list")
                    .width(50)
                    .maxSize(30)
                    .child(new ContextMenuOption<>()
                            .widthRel(50)
                            .height(12)
                            .overlay(IKey.str("No options supplied")));
        }
        this.menuList.setSource(this);
        this.menuList.relative(this);
        this.menuList.bypassLayerRestriction();
        this.direction.positioner.accept(this.menuList.flex());
    }

    private IPanelHandler getPanelHandler() {
        if (this.panelHandler == null) {
            this.panelHandler = IPanelHandler.simple(getPanel(), (parentPanel, player) -> new MenuPanel(getMenuList()), true);
        }
        return this.panelHandler;
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        toggleMenu(false);
        return Result.SUCCESS;
    }

    @Override
    public void onMouseEnterArea() {
        super.onMouseEnterArea();
        if (!this.requiresClick) {
            openMenu(true);
        }
    }

    @Override
    public void onMouseLeaveArea() {
        super.onMouseLeaveArea();
        checkClose();
    }

    public void checkClose() {
        if (!this.requiresClick && !isSelfOrChildHovered()) {
            closeMenu(true);
            if (getParent() instanceof ContextMenuList<?> parentMenuList) {
                parentMenuList.checkClose();
            }
        }
    }

    @Override
    public void closeParent() {
        closeMenu(false);
    }

    @Override
    public boolean isSelfOrChildHovered() {
        if (IContextMenuOption.super.isSelfOrChildHovered()) return true;
        if (!isOpen() || this.menuList == null) return false;
        return this.menuList.isSelfOrChildHovered();
    }

    @Override
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return isValid() && getPanel() instanceof MenuPanel ? theme.getWidgetTheme(IThemeApi.MENU_OPTION) : theme.getButtonTheme();
    }

    public W menuList(ContextMenuList<?> menuList) {
        this.menuList = menuList;
        return getThis();
    }

    public W direction(Direction direction) {
        this.direction = direction;
        return getThis();
    }

    public W requiresClick() {
        this.requiresClick = true;
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

    public enum Direction {
        UP(flex -> flex.bottomRel(1f)),
        DOWN(flex -> flex.topRel(1f)),
        LEFT_UP(flex -> flex.rightRel(1f).bottom(0)),
        LEFT_DOWN(flex -> flex.rightRel(1f).top(0)),
        RIGHT_UP(flex -> flex.leftRel(1f).bottom(0)),
        RIGHT_DOWN(flex -> flex.leftRel(1f).top(0)),
        UNDEFINED(flex -> {});

        private final Consumer<Flex> positioner;

        Direction(Consumer<Flex> positioner) {
            this.positioner = positioner;
        }
    }
}
