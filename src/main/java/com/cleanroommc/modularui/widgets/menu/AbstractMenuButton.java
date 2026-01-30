package com.cleanroommc.modularui.widgets.menu;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.StandardResizer;

import net.minecraft.util.text.TextFormatting;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

@ApiStatus.Experimental
public abstract class AbstractMenuButton<W extends AbstractMenuButton<W>> extends Widget<W> implements IMenuPart, Interactable {

    protected Direction direction = Direction.DOWN;
    protected boolean openOnHover = true;

    private Menu<?> menu;
    private boolean open, softOpen;
    private IPanelHandler panelHandler;
    private final String panelName;

    public AbstractMenuButton(String panelName) {
        this.panelName = Objects.requireNonNull(panelName);
        name(panelName);
    }

    public boolean isOpen() {
        return open;
    }

    protected boolean isSoftOpen() {
        return softOpen;
    }

    protected void toggleMenu(boolean soft) {
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

    protected void openMenu(boolean soft) {
        if (this.open) {
            if (this.softOpen && !soft) {
                this.softOpen = false;
            }
            return;
        }
        if (getPanel() instanceof MenuPanel menuPanel) {
            menuPanel.openSubMenu(getMenu());
        } else {
            getPanelHandler().openPanel();
        }
        this.open = true;
        this.softOpen = soft;
    }

    protected void closeMenu(boolean soft) {
        if (!this.open || (!this.softOpen && soft)) return;
        if (getPanel() instanceof MenuPanel menuPanel) {
            menuPanel.remove(getMenu());
        } else {
            getPanelHandler().closePanel();
        }
        this.open = false;
        this.softOpen = false;
    }

    protected Menu<?> getMenu() {
        if (this.menu == null) {
            this.menu = createMenu();
            if (this.menu == null) {
                this.menu = new Menu<>()
                        .child(IKey.str("No Menu supplied")
                                .style(TextFormatting.RED)
                                .asWidget()
                                .center())
                        .widthRel(1f)
                        .height(16);
                if (this.direction == null) Direction.DOWN.positioner.accept(this.menu.resizer());
            }
        }
        if (!this.menu.resizer().hasParentOverride()) {
            this.menu.resizer().relative(this);
        }
        if (this.direction != null) {
            this.direction.positioner.accept(this.menu.resizer());
        }
        this.menu.setMenuSource(this);
        return this.menu;
    }

    protected void setMenu(Menu<?> menu) {
        this.menu = menu;
    }

    protected abstract Menu<?> createMenu();

    private IPanelHandler getPanelHandler() {
        if (this.panelHandler == null) {
            this.panelHandler = IPanelHandler.simple(getPanel(), (parentPanel, player) -> new MenuPanel(this.panelName, getMenu()), true);
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
        if (this.openOnHover) {
            openMenu(true);
        }
    }

    @Override
    public void onMouseLeaveArea() {
        super.onMouseLeaveArea();
        checkClose();
    }

    protected void checkClose() {
        if (this.openOnHover && !isSelfOrChildHovered()) {
            closeMenu(true);
            if (getParent() instanceof Menu<?> parentMenuList) {
                parentMenuList.checkClose();
            }
        }
    }

    @Override
    public boolean isSelfOrChildHovered() {
        if (isBelowMouse()) return true;
        if (!isOpen() || this.menu == null) return false;
        return this.menu.isSelfOrChildHovered();
    }

    @Override
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getButtonTheme();
    }

    public enum Direction {
        UP(flex -> flex.bottomRel(1f)),
        DOWN(flex -> flex.topRel(1f)),
        LEFT_UP(flex -> flex.rightRel(1f).bottom(0)),
        LEFT_DOWN(flex -> flex.rightRel(1f).top(0)),
        RIGHT_UP(flex -> flex.leftRel(1f).bottom(0)),
        RIGHT_DOWN(flex -> flex.leftRel(1f).top(0)),
        UNDEFINED(flex -> {});

        private final Consumer<StandardResizer> positioner;

        Direction(Consumer<StandardResizer> positioner) {
            this.positioner = positioner;
        }
    }
}
