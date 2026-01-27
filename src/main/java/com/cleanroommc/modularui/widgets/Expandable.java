package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.animation.Animator;
import com.cleanroommc.modularui.animation.MutableObjectAnimator;
import com.cleanroommc.modularui.api.drawable.IInterpolation;
import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.utils.Interpolation;
import com.cleanroommc.modularui.widget.EmptyWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Area;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public class Expandable extends Widget<Expandable> implements Interactable, IViewport {

    private IWidget normalView = new EmptyWidget();
    private IWidget expandedView = new EmptyWidget();
    private final List<IWidget> children = Arrays.asList(normalView, expandedView);
    private List<IWidget> currentChildren = children;
    private boolean expanded = false;
    private Area areaSnapshot;
    private Animator animator;
    private BiConsumer<Rectangle, Boolean> stencilTransform;
    private int animationDuration = 300;
    private IInterpolation interpolation = Interpolation.SINE_OUT;

    public Expandable() {
        coverChildren();
    }

    @Override
    public void onInit() {
        this.children.set(0, normalView);
        this.children.set(1, expandedView);
        this.normalView.setEnabled(!this.expanded);
        this.expandedView.setEnabled(this.expanded);
    }

    @Override
    public void beforeResize(boolean onOpen) {
        super.beforeResize(onOpen);
        if (resizer().getChildren().isEmpty() || resizer().getChildren().size() > 2)
            throw new IllegalStateException("Invalid Expandable children size");
        if (resizer().getChildren().size() > 1) {
            resizer().getChildren().remove(1);
        }
        resizer().getChildren().set(0, this.expanded ? this.expandedView.resizer() : this.normalView.resizer());
        this.currentChildren = Collections.singletonList(this.expanded ? this.expandedView : this.normalView);
    }

    @Override
    public void postResize() {
        super.postResize();
        currentChildren = children;
        if (this.animator != null) {
            this.animator.stop(true);
            this.animator = null;
        }
        if (this.areaSnapshot != null) {
            if (this.animationDuration <= 0) {
                if (!this.expanded) {
                    this.normalView.setEnabled(true);
                    this.expandedView.setEnabled(false);
                }
            } else {
                this.animator = new MutableObjectAnimator<>(getArea(), this.areaSnapshot, getArea().copyOrImmutable())
                        .duration(this.animationDuration)
                        .curve(this.interpolation)
                        .onFinish(() -> {
                            if (!this.expanded) {
                                this.normalView.setEnabled(true);
                                this.expandedView.setEnabled(false);
                            }
                        });
                this.animator.animate();
            }
            this.areaSnapshot = null;
        }
    }

    @Override
    @NotNull
    public List<IWidget> getChildren() {
        return currentChildren;
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        toggle();
        return Result.SUCCESS;
    }

    public void toggle() {
        expanded(!expanded);
    }

    @Override
    public void preDraw(ModularGuiContext context, boolean transformed) {
        if (!transformed) {
            Rectangle rect = new Rectangle(getArea());
            rect.x = 0;
            rect.y = 0;
            if (this.stencilTransform != null) {
                this.stencilTransform.accept(rect, this.expanded);
            }
            Stencil.apply(rect, context);
        }
    }

    @Override
    public void postDraw(ModularGuiContext context, boolean transformed) {
        if (!transformed) {
            Stencil.remove();
        }
    }

    public Expandable expanded(boolean expanded) {
        if (this.expanded == expanded) return this;
        this.expanded = expanded;
        if (isValid()) {
            if (expanded) {
                this.normalView.setEnabled(false);
                this.expandedView.setEnabled(true);
            }
            this.areaSnapshot = getArea().copyOrImmutable();
            scheduleResize();
        }
        return this;
    }

    public Expandable collapsedView(IWidget normalView) {
        this.normalView = normalView;
        this.children.set(0, normalView);
        if (isValid()) {
            this.normalView.initialise(this, true);
        }
        return this;
    }

    public Expandable expandedView(IWidget expandedView) {
        this.expandedView = expandedView;
        this.children.set(1, expandedView);
        if (isValid()) {
            this.expandedView.initialise(this, true);
        }
        return this;
    }

    public Expandable stencilTransform(BiConsumer<Rectangle, Boolean> stencilTransform) {
        this.stencilTransform = stencilTransform;
        return this;
    }

    public Expandable animationDuration(int animationDuration) {
        this.animationDuration = animationDuration;
        return this;
    }

    public Expandable interpolation(IInterpolation interpolation) {
        this.interpolation = interpolation;
        return this;
    }
}
