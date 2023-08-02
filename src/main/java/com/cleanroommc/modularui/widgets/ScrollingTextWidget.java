package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.TextRenderer;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Alignment;

public class ScrollingTextWidget extends TextWidget {

    private static final int pauseTime = 60;

    private TextRenderer.Line line = new TextRenderer.Line("", 0);
    private long time = 0;
    private int scroll = 0;
    private boolean hovering = false;
    private int pauseTimer = 0;

    public ScrollingTextWidget(IKey key) {
        super(key);
        tooltipBuilder(tooltip -> {
            tooltip.excludeArea(getArea())
                    .showUpTimer(10);
            if (this.line.getWidth() > getArea().width) {
                tooltip.addLine(key);
            }
        });
    }

    @Override
    public void onMouseStartHover() {
        this.hovering = true;
    }

    @Override
    public void onMouseEndHover() {
        this.hovering = false;
        this.scroll = 0;
        this.time = 0;
    }

    @Override
    public void onFrameUpdate() {
        if (this.pauseTimer > 0) {
            if (++this.pauseTimer == pauseTime) {
                this.pauseTimer = this.scroll == 0 ? 0 : 1;
                this.scroll = 0;
            }
            return;
        }
        if (this.hovering && ++this.time % 2 == 0 && ++this.scroll == this.line.upperWidth() - getArea().width - 1) {
            this.pauseTimer = 1;
        }
    }

    @Override
    public void draw(GuiContext context) {
        checkString();
        TextRenderer renderer = TextRenderer.SHARED;
        renderer.setColor(getColor());
        renderer.setAlignment(getAlignment(), getArea().w() + 1, getArea().h());
        renderer.setShadow(isShadow());
        renderer.setPos(getArea().getPadding().left, getArea().getPadding().top);
        renderer.setScale(getScale());
        renderer.setSimulate(false);
        if (this.hovering) {
            renderer.drawScrolling(this.line, this.scroll, getArea(), context);
        } else {
            renderer.drawCut(this.line);
        }
    }

    private void checkString() {
        String s = getKey().get();
        if (!s.equals(this.line.getText())) {
            TextRenderer.SHARED.setScale(getScale());
            this.line = TextRenderer.SHARED.line(s);
            this.scroll = 0;
            markTooltipDirty();
        }
    }

    @Override
    public ScrollingTextWidget alignment(Alignment alignment) {
        return (ScrollingTextWidget) super.alignment(alignment);
    }

    @Override
    public ScrollingTextWidget color(int color) {
        return (ScrollingTextWidget) super.color(color);
    }

    @Override
    public ScrollingTextWidget scale(float scale) {
        return (ScrollingTextWidget) super.scale(scale);
    }

    @Override
    public ScrollingTextWidget shadow(boolean shadow) {
        return (ScrollingTextWidget) super.shadow(shadow);
    }

    @Override
    public ScrollingTextWidget widgetTheme(String widgetTheme) {
        return (ScrollingTextWidget) super.widgetTheme(widgetTheme);
    }
}
