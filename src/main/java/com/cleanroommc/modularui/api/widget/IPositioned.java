package com.cleanroommc.modularui.api.widget;

import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widget.sizer.Flex;
import com.cleanroommc.modularui.widget.sizer.Unit;

import java.util.function.Consumer;

public interface IPositioned<W extends IPositioned<W>> {

    Flex flex();

    Area getArea();

    @SuppressWarnings("unchecked")
    default W getThis() {
        return (W) this;
    }

    default W coverChildrenWidth() {
        flex().coverChildrenWidth();
        return getThis();
    }

    default W coverChildrenHeight() {
        flex().coverChildrenHeight();
        return getThis();
    }

    default W coverChildren() {
        return coverChildrenWidth().coverChildrenHeight();
    }

    default W expanded() {
        flex().expanded();
        return getThis();
    }

    default W relative(IGuiElement guiElement) {
        return relative(guiElement.getArea());
    }

    default W relative(Area guiElement) {
        flex().relative(guiElement);
        return getThis();
    }

    default W relativeToScreen() {
        flex().relativeToScreen();
        return getThis();
    }

    default W relativeToParent() {
        flex().relativeToParent();
        return getThis();
    }

    default W left(int x) {
        flex().left(x, 0, 0, Unit.Measure.PIXEL, true);
        return getThis();
    }

    default W left(float x) {
        flex().left(x, 0, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    default W left(float x, int offset) {
        flex().left(x, offset, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    default W left(float x, float anchor) {
        flex().left(x, 0, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    default W left(float x, int offset, float anchor, Unit.Measure measure) {
        flex().left(x, offset, anchor, measure, false);
        return getThis();
    }

    default W right(int x) {
        flex().right(x, 0, 0, Unit.Measure.PIXEL, true);
        return getThis();
    }

    default W right(float x) {
        flex().right(x, 0, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    default W right(float x, int offset) {
        flex().right(x, offset, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    default W right(float x, float anchor) {
        flex().right(x, 0, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    default W right(float x, int offset, float anchor, Unit.Measure measure) {
        flex().right(x, offset, anchor, measure, false);
        return getThis();
    }

    default W top(int y) {
        flex().top(y, 0, 0, Unit.Measure.PIXEL, true);
        return getThis();
    }

    default W top(float y) {
        flex().top(y, 0, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    default W top(float y, int offset) {
        flex().top(y, offset, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    default W top(float y, float anchor) {
        flex().top(y, 0, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    default W top(float y, int offset, float anchor, Unit.Measure measure) {
        flex().top(y, offset, anchor, measure, false);
        return getThis();
    }

    default W bottom(int y) {
        flex().bottom(y, 0, 0, Unit.Measure.PIXEL, true);
        return getThis();
    }

    default W bottom(float y) {
        flex().bottom(y, 0, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    default W bottom(float y, int offset) {
        flex().bottom(y, offset, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    default W bottom(float y, float anchor) {
        flex().bottom(y, 0, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    default W bottom(float y, int offset, float anchor, Unit.Measure measure) {
        flex().bottom(y, offset, anchor, measure, false);
        return getThis();
    }

    default W pos(int x, int y) {
        left(x).top(y);
        return getThis();
    }

    default W pos(float x, float y) {
        left(x).top(y);
        return getThis();
    }

    default W width(int w) {
        width(w, Unit.Measure.PIXEL);
        return getThis();
    }

    default W width(float w) {
        width(w, Unit.Measure.RELATIVE);
        return getThis();
    }

    default W width(float w, Unit.Measure measure) {
        flex().width(w, measure);
        return getThis();
    }

    default W height(int h) {
        height(h, Unit.Measure.PIXEL);
        return getThis();
    }

    default W height(float h) {
        height(h, Unit.Measure.RELATIVE);
        return getThis();
    }

    default W height(float h, Unit.Measure measure) {
        flex().height(h, measure);
        return getThis();
    }

    default W size(int w, int h) {
        width(w).height(h);
        return getThis();
    }

    default W size(float w, float h) {
        width(w).height(h);
        return getThis();
    }

    default W anchorLeft(float val) {
        flex().anchorLeft(val);
        return getThis();
    }

    default W anchorRight(float val) {
        flex().anchorRight(val);
        return getThis();
    }

    default W anchorTop(float val) {
        flex().anchorTop(val);
        return getThis();
    }

    default W anchorBottom(float val) {
        flex().anchorBottom(val);
        return getThis();
    }

    default W anchor(Alignment alignment) {
        flex().anchor(alignment);
        return getThis();
    }

    default W alignX(float val) {
        left(val).anchorLeft(val);
        return getThis();
    }

    default W alignY(float val) {
        top(val).anchorTop(val);
        return getThis();
    }

    default W align(Alignment alignment) {
        return alignX(alignment.x).
                alignY(alignment.y);
    }

    default W flex(Consumer<Flex> flexConsumer) {
        flexConsumer.accept(flex());
        return getThis();
    }

    default W padding(int left, int right, int top, int bottom) {
        getArea().getPadding().all(left, right, top, bottom);
        return getThis();
    }

    default W padding(int horizontal, int vertical) {
        getArea().getPadding().all(horizontal, vertical);
        return getThis();
    }

    default W padding(int all) {
        getArea().getPadding().all(all);
        return getThis();
    }

    default W paddingLeft(int val) {
        getArea().getPadding().left(val);
        return getThis();
    }

    default W paddingRight(int val) {
        getArea().getPadding().right(val);
        return getThis();
    }

    default W paddingTop(int val) {
        getArea().getPadding().top(val);
        return getThis();
    }

    default W paddingBottom(int val) {
        getArea().getPadding().bottom(val);
        return getThis();
    }

    default W margin(int left, int right, int top, int bottom) {
        getArea().getMargin().all(left, right, top, bottom);
        return getThis();
    }

    default W margin(int horizontal, int vertical) {
        getArea().getMargin().all(horizontal, vertical);
        return getThis();
    }

    default W margin(int all) {
        getArea().getMargin().all(all);
        return getThis();
    }

    default W marginLeft(int val) {
        getArea().getMargin().left(val);
        return getThis();
    }

    default W marginRight(int val) {
        getArea().getMargin().right(val);
        return getThis();
    }

    default W marginTop(int val) {
        getArea().getMargin().top(val);
        return getThis();
    }

    default W marginBottom(int val) {
        getArea().getMargin().bottom(val);
        return getThis();
    }
}
