package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.animation.Eases;
import com.cleanroommc.modularui.api.animation.Interpolator;
import com.cleanroommc.modularui.api.drawable.GuiHelper;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.api.widget.IWidgetBuilder;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.api.widget.Widget;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExpandTab extends MultiChildWidget implements Interactable, IWidgetBuilder<ExpandTab> {

    private boolean expanded = false, animating = false, firstBuild = true;
    private Interpolator openAnimator;
    private Interpolator closeAnimator;
    private Size expandedSize;
    private Size normalSize;
    private Pos2d expandedPos;
    private Pos2d normalPos;
    private int animateDuration = 250;
    private float animateX, animateY, animateWidth, animateHeight;
    @Nullable
    private IDrawable[] normalTexture;

    @Override
    public void onInit() {
        this.openAnimator = new Interpolator(0, 1, this.animateDuration, Eases.EaseQuadOut, value -> {
            float val = (float) value;
            this.animateX = (this.expandedPos.x - this.normalPos.x) * val + this.normalPos.x;
            this.animateY = (this.expandedPos.y - this.normalPos.y) * val + this.normalPos.y;
            this.animateWidth = (this.expandedSize.width - this.normalSize.width) * val + this.normalSize.width;
            this.animateHeight = (this.expandedSize.height - this.normalSize.height) * val + this.normalSize.height;
        }, val -> {
            this.animateX = this.expandedPos.x;
            this.animateY = this.expandedPos.y;
            this.animateWidth = this.expandedSize.width;
            this.animateHeight = this.expandedSize.height;
            this.animating = false;
        });
        this.closeAnimator = this.openAnimator.getReversed(this.animateDuration, Eases.EaseQuadIn);
        this.closeAnimator.setCallback(val -> {
            this.animateX = this.normalPos.x;
            this.animateY = this.normalPos.y;
            this.animateWidth = this.normalSize.width;
            this.animateHeight = this.normalSize.height;
            this.animating = false;
            for (Widget widget : getChildren()) {
                widget.setEnabled(false);
            }
        });
        for (Widget widget : getChildren()) {
            widget.setEnabled(false);
        }
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        return new Size(20, 20);
    }

    @Override
    public void onRebuild() {
        if (firstBuild) {
            if (this.normalPos == null) {
                this.normalPos = getPos();
            }
            if (this.normalSize == null) {
                this.normalSize = getSize();
            }
            if (this.expandedPos == null) {
                this.expandedPos = this.normalPos;
            }
            if (this.expandedSize == null) {
                this.expandedSize = new Size(this.normalSize.width * 3, this.normalSize.height * 3);
            }
            this.animateX = getPos().x;
            this.animateY = getPos().y;
            this.animateWidth = getSize().width;
            this.animateHeight = getSize().height;
            this.firstBuild = false;
        }
    }

    @Override
    public void onFrameUpdate() {
        if (this.animating) {
            if (expanded) {
                this.openAnimator.update(Minecraft.getMinecraft().getTickLength());
            } else {
                this.closeAnimator.update(Minecraft.getMinecraft().getTickLength());
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawBackground(float partialTicks) {
        IDrawable[] background = getBackground();
        if (background != null) {
            for (IDrawable drawable : background) {
                if (drawable != null) {
                    drawable.draw(animateX - getPos().x, animateY - getPos().y, animateWidth, animateHeight, partialTicks);
                }
            }
        }
    }

    @Override
    public void draw(float partialTicks) {
        if (!isExpanded() && this.normalTexture != null) {
            for (IDrawable drawable : this.normalTexture) {
                if (drawable != null) {
                    drawable.draw(Pos2d.ZERO, this.normalSize, partialTicks);
                }
            }
        }
    }

    @Override
    public void drawChildren(float partialTicks) {
        if (isExpanded() || animating) {
            Pos2d parentPos = getParent().getAbsolutePos();
            if (animating) {
                GuiHelper.useScissor((int) (parentPos.x + this.animateX), (int) (parentPos.y + this.animateY), (int) this.animateWidth, (int) this.animateHeight, () -> {
                    super.drawChildren(partialTicks);
                });
            } else {
                super.drawChildren(partialTicks);
            }
        }
    }

    @Override
    public ClickResult onClick(int buttonId, boolean doubleClick) {
        if (buttonId == 0) {
            setExpanded(!isExpanded());
            return ClickResult.ACCEPT;
        }
        return ClickResult.ACKNOWLEDGED;
    }

    public void setExpanded(boolean expanded) {
        if (this.expanded != expanded) {
            this.expanded = expanded;
            this.animating = true;

            if (isExpanded()) {
                for (Widget widget : getChildren()) {
                    widget.setEnabled(true);
                }
                openAnimator.forward();
                this.size = expandedSize;
                this.pos = expandedPos;
            } else {
                closeAnimator.forward();
                this.size = normalSize;
                this.pos = normalPos;
            }
            checkNeedsRebuild();
        }
    }

    public boolean isExpanded() {
        return expanded;
    }

    @Override
    public void addWidgetInternal(Widget widget) {
        addChild(widget);
    }

    public ExpandTab setExpandedPos(int x, int y) {
        return setExpandedPos(new Pos2d(x, y));
    }

    public ExpandTab setExpandedPos(Pos2d expandedPos) {
        this.expandedPos = expandedPos;
        return this;
    }

    public ExpandTab setExpandedSize(int width, int height) {
        return setExpandedSize(new Size(width, height));
    }

    public ExpandTab setExpandedSize(Size expandedSize) {
        this.expandedSize = expandedSize;
        return this;
    }

    @Override
    public ExpandTab setSize(Size size) {
        super.setSize(size);
        this.normalSize = size;
        return this;
    }

    @Override
    public ExpandTab setPos(Pos2d relativePos) {
        super.setPos(relativePos);
        this.normalPos = relativePos;
        return this;
    }

    public ExpandTab setAnimateDuration(int animateDuration) {
        this.animateDuration = animateDuration;
        return this;
    }

    public ExpandTab setNormalTexture(IDrawable... normalTexture) {
        this.normalTexture = normalTexture;
        return this;
    }
}
