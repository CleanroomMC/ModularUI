package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.MathUtils;
import com.cleanroommc.modularui.utils.NumberFormat;
import com.cleanroommc.modularui.utils.SIPrefix;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Box;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

public abstract class AbstractFluidDisplayWidget<W extends AbstractFluidDisplayWidget<W>> extends Widget<W> {

    public static final String UNIT_BUCKET = "B";
    public static final String UNIT_LITER = "L";

    private final Box contentPadding = new Box().all(1);
    private String unit = UNIT_BUCKET;
    private SIPrefix baseUnitPrefix = SIPrefix.Milli;
    private boolean flipLighterThanAir = true;

    protected AbstractFluidDisplayWidget() {
        size(18);
    }

    @Override
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getFluidSlotTheme();
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        FluidStack fluid = getFluidStack();
        if (fluid == null) return;
        int x = this.contentPadding.getLeft();
        int y = this.contentPadding.getTop();
        int w = getArea().width - this.contentPadding.horizontal();
        int h = getArea().height - this.contentPadding.vertical();
        float c = getCapacity();
        if (c > 0 && fluid.amount > 0) {
            int newH = (int) MathUtils.rescaleLinear(fluid.amount, 0, c, 1, h);
            if (!this.flipLighterThanAir || !fluid.getFluid().isLighterThanAir()) y += h - newH;
            h = newH;
        }
        GuiDraw.drawFluidTexture(fluid, x, y, w, h, context.getCurrentDrawingZ());
    }

    @Override
    public void drawOverlay(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.drawOverlay(context, widgetTheme);
        FluidStack fluid = getFluidStack();
        if (fluid != null && displayAmountText()) {
            String s = NumberFormat.format(getBaseUnitAmount(fluid.amount), NumberFormat.AMOUNT_TEXT) + getBaseUnit();
            // mc doesn't consider the 1px border in item slots for amount text, but it looks weird when it touches the left border, so
            // we only apply padding there
            GuiDraw.drawScaledAlignedTextInBox(s, this.contentPadding.getLeft(), 0, getArea().width - this.contentPadding.getLeft(), getArea().height, Alignment.BottomRight);
        }
    }

    protected abstract boolean displayAmountText();

    @Nullable
    protected abstract FluidStack getFluidStack();

    /**
     * Return a positive value if the fluid should be drawn partly depending on the amount filled.
     *
     * @return capacity in milli buckets or zero/negative if fluid should always be drawn full
     */
    protected int getCapacity() {
        return 0;
    }

    public double getBaseUnitAmount(double amount) {
        return amount * getBaseUnitSiPrefix().factor;
    }

    public final String getUnit() {
        return getBaseUnitSiPrefix().stringSymbol + getBaseUnit();
    }

    public String getBaseUnit() {
        return this.unit;
    }

    public SIPrefix getBaseUnitSiPrefix() {
        return this.baseUnitPrefix;
    }

    public boolean isFlipLighterThanAir() {
        return flipLighterThanAir;
    }

    public W contentPadding(int left, int right, int top, int bottom) {
        this.contentPadding.all(left, right, top, bottom);
        return getThis();
    }

    public W contentPadding(int horizontal, int vertical) {
        this.contentPadding.all(horizontal, vertical);
        return getThis();
    }

    public W contentPadding(int all) {
        this.contentPadding.all(all);
        return getThis();
    }

    public W contentPaddingLeft(int val) {
        this.contentPadding.left(val);
        return getThis();
    }

    public W contentPaddingRight(int val) {
        this.contentPadding.right(val);
        return getThis();
    }

    public W contentPaddingTop(int val) {
        this.contentPadding.top(val);
        return getThis();
    }

    public W contentPaddingBottom(int val) {
        this.contentPadding.bottom(val);
        return getThis();
    }

    public W fluidUnit(String baseUnitSymbol, SIPrefix baseUnitPrefix) {
        this.unit = baseUnitSymbol;
        this.baseUnitPrefix = baseUnitPrefix;
        return getThis();
    }

    /**
     * Determines if a partially filled fluid should be drawn from the top instead of the bottom if the current fluid is lighter than air.
     * When the full fluid is drawn (when {@link #getCapacity()} returns 0) this does nothing.
     */
    public W flipLighterThanAir(boolean flipLighterThanAir) {
        this.flipLighterThanAir = flipLighterThanAir;
        return getThis();
    }
}
