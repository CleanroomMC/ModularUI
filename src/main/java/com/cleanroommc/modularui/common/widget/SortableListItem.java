package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.ModularUITextures;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.math.Color;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.api.widget.IDraggable;
import com.cleanroommc.modularui.api.widget.IWidgetParent;
import com.cleanroommc.modularui.api.widget.Widget;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SortableListItem<T> extends Widget implements IWidgetParent, IDraggable {

    private final Widget upButton;
    private final Widget downButton;
    private final Widget removeButton;
    private Widget content;
    private final List<Widget> allChildren = new ArrayList<>();
    private int currentIndex;
    private SortableListWidget<T> listWidget;
    private final T value;
    private Function<T, Widget> widgetCreator;

    public SortableListItem(T value) {
        this.value = value;
        this.widgetCreator = t -> new TextWidget(t.toString());
        this.upButton = new ButtonWidget()
                .setOnClick((clickData, widget) -> listWidget.moveElementUp(this.currentIndex))
                .setBackground(ModularUITextures.BASE_BUTTON, new Text("^"))
                .setSize(10, 10);
        this.downButton = new ButtonWidget()
                .setOnClick((clickData, widget) -> listWidget.moveElementDown(this.currentIndex))
                .setBackground(ModularUITextures.BASE_BUTTON, new Text("v"))
                .setSize(10, 10);
        this.removeButton = new ButtonWidget()
                .setOnClick((clickData, widget) -> listWidget.removeElement(this.currentIndex))
                .setBackground(ModularUITextures.BASE_BUTTON, new Text("X").color(Color.RED.normal))
                .setSize(10, 20);
    }

    protected void init(SortableListWidget<T> listWidget) {
        this.listWidget = listWidget;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    @Override
    public void initChildren() {
        this.content = this.widgetCreator.apply(this.value);
        makeChildrenList();
    }

    @Override
    public void onRebuild() {
        makeChildrenList();
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        int w = content.getSize().width + 20;
        int h = Math.max(content.getSize().height, 20);
        return new Size(w, h);
    }

    @Override
    public void layoutChildren(int maxWidth, int maxHeight) {
        if (content.getSize().height >= 20) {
            this.content.setPosSilent(Pos2d.ZERO);
        } else {
            this.content.setPosSilent(new Pos2d(0, getSize().height / 2 - content.getSize().height / 2));
        }
        this.upButton.setPosSilent(new Pos2d(content.getSize().width, 0));
        this.downButton.setPosSilent(new Pos2d(content.getSize().width, getSize().height - 10));
        this.removeButton.setSize(10, getSize().height);
        this.removeButton.setPosSilent(new Pos2d(getSize().width - 10, 0));
    }

    private void makeChildrenList() {
        this.allChildren.clear();
        this.allChildren.add(content);
        this.allChildren.add(upButton);
        this.allChildren.add(downButton);
        this.allChildren.add(removeButton);
    }

    @Override
    public void renderMovingState(float partialTicks) {
        drawInternal(partialTicks);
    }

    @Override
    public boolean onDragStart(int button) {
        return false;
    }

    @Override
    public void onDragEnd(boolean successful) {

    }

    @Override
    public void onDrag(int mouseButton, long timeSinceLastClick) {

    }

    @Override
    public boolean isMoving() {
        return false;
    }

    @Override
    public void setMoving(boolean moving) {

    }

    public T getValue() {
        return value;
    }

    @Override
    public List<Widget> getChildren() {
        return this.allChildren;
    }

    public SortableListItem<T> setWidgetCreator(Function<T, Widget> widgetCreator) {
        this.widgetCreator = widgetCreator;
        return this;
    }
}
