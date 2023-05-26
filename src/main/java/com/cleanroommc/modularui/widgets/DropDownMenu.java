package com.cleanroommc.modularui.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.IGuiAction;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.keys.StringKey;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.widget.ScrollWidget;
import com.cleanroommc.modularui.widget.SingleChildWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Area;

public class DropDownMenu extends SingleChildWidget<DropDownMenu> implements Interactable {
    private static final StringKey NONE = new StringKey("None");
    private DropDownWrapper menu = new DropDownWrapper();
    private IDrawable arrowClosed;
    private IDrawable arrowOpened;

    public DropDownMenu() {
        menu.setEnabled(false);
        menu.background(GuiTextures.BUTTON);
        child(menu);
        setArrows(GuiTextures.ARROW_UP, GuiTextures.ARROW_DOWN);
    }

    public int getSelectedIndex() {
        return menu.getCurrentIndex();
    }

    public DropDownMenu setSelectedIndex(int index) {
        menu.setCurrentIndex(index);
        return getThis();
    }

    public DropDownMenu addChoice(Function<Integer, DropDownItem> itemGetter) {
        menu.addChoice(itemGetter);
        return getThis();
    }

    public DropDownMenu setArrows(IDrawable arrowClosed, IDrawable arrowOpened) {
        this.arrowClosed = arrowClosed;
        this.arrowOpened = arrowOpened;
        return getThis();
    }

    public DropDownMenu addChoice(IGuiAction.MouseReleased onSelect, IDrawable... drawable) {
        DropDownItem button = new DropDownItem();
        return addChoice(index ->
            button.onMouseReleased(m -> {
                menu.setOpened(false);
                menu.setCurrentIndex(index);
                onSelect.release(m);
                return true; 
                })
            .overlay(drawable));
    }

    public DropDownMenu addChoice(IGuiAction.MouseReleased onSelect, String text) {
        return addChoice(onSelect, new StringKey(text));
    }

    public DropDownMenu setDropDownDirection(DropDownDirection direction) {
        menu.setDropDownDirection(direction);
        return getThis();
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (!menu.isOpen()) {
            menu.setOpened(true);
            menu.setEnabled(true);
            return Result.SUCCESS;
        }
        menu.setOpened(false);
        menu.setEnabled(false);
        return Result.SUCCESS;
    }

    @Override
    public void draw(GuiContext context) {
        super.draw(context);
        Area area = getArea();
        int smallerSide = area.width > area.height ? area.height : area.width;
        if (menu.getSelectedItem() != null) {
            menu.getSelectedItem().setEnabled(true);
            menu.getSelectedItem().drawBackground(context);
            menu.getSelectedItem().draw(context);
            menu.getSelectedItem().drawForeground(context);
        } else {
            NONE.draw(context, 0, 0, area.width - smallerSide, area.height);
        }

        if (menu.isOpen()) {
            arrowOpened.draw(context, area.width - smallerSide , 0, smallerSide, smallerSide);
        } else {
            arrowClosed.draw(context, area.width - smallerSide , 0, smallerSide, smallerSide);
        }
    }

    @Override
    public DropDownMenu background(IDrawable... background) {
        menu.background(background);
        return super.background(background);
    }

    public static enum DropDownDirection {
        UP(0, -1),
        DOWN(0, 1);

        private int xOffset = 0;
        private int yOffset = 0;

        private DropDownDirection(int xOffset, int yOffset) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
        }

        public int getXOffset() {
            return xOffset;
        }

        public int getYOffset() {
            return yOffset;
        }
    }

    private static class DropDownWrapper extends ScrollWidget<DropDownWrapper> {
        private DropDownDirection direction = DropDownDirection.DOWN;
        private int maxItemsOnDisplay = 10;
        private List<IWidget> children = new ArrayList<>();
        private boolean open;
        private int count = 0;
        private int currentIndex = -1;

        public void setDropDownDirection(DropDownDirection direction) {
            this.direction = direction;
        }

        @Override
        public void onFrameUpdate() {
            if (!open) {
                setEnabled(false);
            }
        }

        @Override
        public void afterInit() {
            super.afterInit();
            rebuild();
        }

        public void setOpened(boolean open) {
            this.open = open;
            rebuild();
        }

        public boolean isOpen() {
            return open;
        }

        public void addChoice(Function<Integer, DropDownItem> itemGetter) {
            children.add(itemGetter.apply(count));
            count++;
        }

        public int getCurrentIndex() {
            return currentIndex;
        }

        public void setCurrentIndex(int currentIndex) {
            this.currentIndex = currentIndex;
        }

        @Override
        public @NotNull List<IWidget> getChildren() {
            return children;
        }

        public IWidget getSelectedItem() {
            if (currentIndex < 0 || currentIndex >= count) {
                return null;
            }
            return getChildren().get(currentIndex);
        }

        @Override
        public DropDownWrapper background(IDrawable... background) {
            for (IWidget child : getChildren()) {
                if (!(child instanceof Widget<?>)) continue;
                Widget<?> childAsWidget = (Widget<?>) child;
                childAsWidget.background(background);
            }
            return super.background();
        }

        private void rebuild() {
            if (!isValid()) return;
            Area parentArea = getParent().getArea();
            size(parentArea.width, parentArea.height * maxItemsOnDisplay);
            pos(0, direction == DropDownDirection.UP ? parentArea.height * (maxItemsOnDisplay + 1) : parentArea.height);

            List<IWidget> children = getChildren();
            for (int i = 0; i < children.size(); i++) {
                IWidget child = children.get(i);
                child.getArea().setSize(parentArea.width, parentArea.height);
                child.getFlex().left(0).top(parentArea.height * i);
                child.setEnabled(open);
            }
        }
    }

    public static class DropDownItem extends Widget<DropDownItem> implements Interactable {
        private IGuiAction.MousePressed mousePressed;
        private IGuiAction.MouseReleased mouseReleased;
        private IGuiAction.MousePressed mouseTapped;
        private IGuiAction.MouseScroll mouseScroll;
        private IGuiAction.KeyPressed keyPressed;
        private IGuiAction.KeyReleased keyReleased;
        private IGuiAction.KeyPressed keyTapped;

        @Override
        public @NotNull Result onMousePressed(int mouseButton) {
            if (this.mousePressed != null && this.mousePressed.press(mouseButton)) {
                return Result.SUCCESS;
            }
            return Result.ACCEPT;
        }

        @Override
        public boolean onMouseRelease(int mouseButton) {
            return this.mouseReleased != null && this.mouseReleased.release(mouseButton);
        }

        @NotNull
        @Override
        public Result onMouseTapped(int mouseButton) {
            if (this.mouseTapped != null && this.mouseTapped.press(mouseButton)) {
                return Result.SUCCESS;
            }
            return Result.IGNORE;
        }

        @Override
        public @NotNull Result onKeyPressed(char typedChar, int keyCode) {
            if (this.keyPressed != null && this.keyPressed.press(typedChar, keyCode)) {
                return Result.SUCCESS;
            }
            return Result.ACCEPT;
        }

        @Override
        public boolean onKeyRelease(char typedChar, int keyCode) {
            return this.keyReleased != null && this.keyReleased.release(typedChar, keyCode);
        }

        @NotNull
        @Override
        public Result onKeyTapped(char typedChar, int keyCode) {
            if (this.keyTapped != null && this.keyTapped.press(typedChar, keyCode)) {
                return Result.SUCCESS;
            }
            return Result.IGNORE;
        }

        @Override
        public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
            return this.mouseScroll != null && this.mouseScroll.scroll(scrollDirection, amount);
        }

        public DropDownItem onMousePressed(IGuiAction.MousePressed mousePressed) {
            this.mousePressed = mousePressed;
            return getThis();
        }

        public DropDownItem onMouseReleased(IGuiAction.MouseReleased mouseReleased) {
            this.mouseReleased = mouseReleased;
            return getThis();
        }

        public DropDownItem onMouseTapped(IGuiAction.MousePressed mouseTapped) {
            this.mouseTapped = mouseTapped;
            return getThis();
        }

        public DropDownItem onMouseScrolled(IGuiAction.MouseScroll mouseScroll) {
            this.mouseScroll = mouseScroll;
            return getThis();
        }

        public DropDownItem onKeyPressed(IGuiAction.KeyPressed keyPressed) {
            this.keyPressed = keyPressed;
            return getThis();
        }

        public DropDownItem onKeyReleased(IGuiAction.KeyReleased keyReleased) {
            this.keyReleased = keyReleased;
            return getThis();
        }

        public DropDownItem onKeyTapped(IGuiAction.KeyPressed keyTapped) {
            this.keyTapped = keyTapped;
            return getThis();
        }
    }
}
