package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.drawable.IHoverable;
import com.cleanroommc.modularui.api.drawable.IRichTextBuilder;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.text.RichText;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.Widget;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class RichTextWidget extends Widget<RichTextWidget> implements IRichTextBuilder<RichTextWidget>, Interactable {

    private final RichText text = new RichText();
    private Consumer<RichText> builder;
    private boolean dirty = false;
    private boolean autoUpdate = false;

    public void markDirty() {
        this.dirty = true;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        super.draw(context, widgetTheme);
        if (this.autoUpdate || this.dirty) {
            this.text.clearText();
            if (this.builder != null) {
                this.builder.accept(this.text);
            }
        }
        this.text.drawAtZero(context, getArea(), widgetTheme);
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        super.drawForeground(context);
        Object o = this.text.getHoveringElement(context.getFontRenderer(), context.getMouseX(), context.getMouseY());
        //ModularUI.LOGGER.info("Mouse {}, {}", context.getMouseX(), context.getMouseY());
        if (o instanceof IHoverable hoverable) {
            hoverable.onHover();
            RichTooltip tooltip = hoverable.getTooltip();
            if (tooltip != null) {
                tooltip.draw(context);
            }
        }
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        Object o = this.text.getHoveringElement(getContext().getFontRenderer(), getContext().getMouseX(), getContext().getMouseY());
        if (o instanceof Interactable interactable) {
            return interactable.onMousePressed(mouseButton);
        }
        return Result.ACCEPT;
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        Object o = this.text.getHoveringElement(getContext().getFontRenderer(), getContext().getMouseX(), getContext().getMouseY());
        if (o instanceof Interactable interactable) {
            return interactable.onMouseRelease(mouseButton);
        }
        return false;
    }

    @Override
    public @NotNull Result onMouseTapped(int mouseButton) {
        Object o = this.text.getHoveringElement(getContext().getFontRenderer(), getContext().getMouseX(), getContext().getMouseY());
        if (o instanceof Interactable interactable) {
            return interactable.onMouseTapped(mouseButton);
        }
        return Result.IGNORE;
    }

    @Override
    public @NotNull Result onKeyPressed(char typedChar, int keyCode) {
        Object o = this.text.getHoveringElement(getContext().getFontRenderer(), getContext().getMouseX(), getContext().getMouseY());
        if (o instanceof Interactable interactable) {
            return interactable.onKeyPressed(typedChar, keyCode);
        }
        return Result.ACCEPT;
    }

    @Override
    public boolean onKeyRelease(char typedChar, int keyCode) {
        Object o = this.text.getHoveringElement(getContext().getFontRenderer(), getContext().getMouseX(), getContext().getMouseY());
        if (o instanceof Interactable interactable) {
            return interactable.onKeyRelease(typedChar, keyCode);
        }
        return false;
    }

    @Override
    public @NotNull Result onKeyTapped(char typedChar, int keyCode) {
        Object o = this.text.getHoveringElement(getContext().getFontRenderer(), getContext().getMouseX(), getContext().getMouseY());
        if (o instanceof Interactable interactable) {
            return interactable.onKeyTapped(typedChar, keyCode);
        }
        return Result.ACCEPT;
    }

    @Override
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        Object o = this.text.getHoveringElement(getContext().getFontRenderer(), getContext().getMouseX(), getContext().getMouseY());
        if (o instanceof Interactable interactable) {
            return interactable.onMouseScroll(scrollDirection, amount);
        }
        return false;
    }

    @Override
    public void onMouseDrag(int mouseButton, long timeSinceClick) {
        Object o = this.text.getHoveringElement(getContext().getFontRenderer(), getContext().getMouseX(), getContext().getMouseY());
        if (o instanceof Interactable interactable) {
            interactable.onMouseDrag(mouseButton, timeSinceClick);
        }
    }

    @Nullable
    public Object getHoveredElement() {
        if (!isHovering()) return null;
        getContext().pushMatrix();
        getContext().translate(getArea().x, getArea().y);
        Object o = this.text.getHoveringElement(getContext());
        getContext().popMatrix();
        return o;
    }

    @Override
    public IRichTextBuilder<?> getRichText() {
        return text;
    }

    /**
     * Sets the auto update property. If auto update is true the text will be deleted each time it is drawn.
     * If {@link #builder} is not null, it will then be called.
     *
     * @param autoUpdate auto update
     * @return this
     */
    public RichTextWidget autoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
        return this;
    }

    /**
     * A builder which is called every time before drawing when {@link #dirty} is true.
     *
     * @param builder text builder
     * @return this
     */
    public RichTextWidget textBuilder(Consumer<RichText> builder) {
        this.builder = builder;
        markDirty();
        return this;
    }
}
