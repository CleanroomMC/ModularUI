package com.cleanroommc.modularui.drawable.graph;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.Platform;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Experimental
public class GraphDrawable implements IDrawable {

    private final GraphView view = new GraphView();
    //private IDrawable background;
    private int backgroundColor = Color.WHITE.main;

    private boolean borderTop = true, borderLeft = true, borderBottom = false, borderRight = false;
    private float majorTickThickness = 1f, majorTickLength = 3f, minorTickThickness = 0.5f, minorTickLength = 1.5f;
    private float gridLineWidth = 0.5f;
    private int gridLineColor = Color.withAlpha(Color.BLACK.main, 0.4f);
    private float minorGridLineWidth = 0f;
    private int minorGridLineColor = Color.withAlpha(Color.BLACK.main, 0.15f);

    private final GraphAxis x = new GraphAxis(GuiAxis.X), y = new GraphAxis(GuiAxis.Y);
    private final List<Plot> plots = new ArrayList<>();

    private boolean dirty = true;

    public void redraw() {
        this.dirty = true;
        for (Plot plot : this.plots) plot.redraw();
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        if (this.view.setScreen(x, y, x + width, y + height) | compute()) { // we always want compute() to be called,
            this.x.applyPadding(this.view);
            this.y.applyPadding(this.view);
            this.view.setGraph(this.x.min, this.y.min, this.x.max, this.y.max);
            this.view.postResize();
        }
        if (this.backgroundColor != 0) {
            GuiDraw.drawRect(this.view.sx0, this.view.sy0, this.view.getScreenWidth(), this.view.getScreenHeight(), this.backgroundColor);
        }
        Platform.setupDrawColor();
        drawGrid(context);
        Stencil.applyTransformed((int) this.view.sx0, (int) this.view.sy0, (int) (this.view.getScreenWidth() + 1), (int) (this.view.getScreenHeight() + 1));
        for (Plot plot : this.plots) {
            plot.draw(this.view);
        }
        Stencil.remove();
        drawTicks(context);
        GuiDraw.drawBorderOutsideLTRB(this.view.sx0, this.view.sy0, this.view.sx1, this.view.sy1, 0.5f, Color.BLACK.main);
        this.x.drawLabels(this.view, this.y);
        this.y.drawLabels(this.view, this.x);
    }

    public void drawGrid(GuiContext context) {
        if (this.gridLineWidth < 0 && this.minorGridLineWidth < 0) return;
        Platform.startDrawing(Platform.DrawMode.QUADS, Platform.VertexFormat.POS_COLOR, buffer -> {
            if (this.minorGridLineWidth > 0) {
                int r = Color.getRed(this.minorGridLineColor);
                int g = Color.getGreen(this.minorGridLineColor);
                int b = Color.getBlue(this.minorGridLineColor);
                int a = Color.getAlpha(this.minorGridLineColor);
                this.x.drawGridLines(buffer, this.view, this.y, false, this.minorGridLineWidth, r, g, b, a);
                this.y.drawGridLines(buffer, this.view, this.x, false, this.minorGridLineWidth, r, g, b, a);
            }
            if (this.gridLineWidth > 0) {
                int r = Color.getRed(this.gridLineColor);
                int g = Color.getGreen(this.gridLineColor);
                int b = Color.getBlue(this.gridLineColor);
                int a = Color.getAlpha(this.gridLineColor);
                this.x.drawGridLines(buffer, this.view, this.y, true, this.gridLineWidth, r, g, b, a);
                this.y.drawGridLines(buffer, this.view, this.x, true, this.gridLineWidth, r, g, b, a);
            }
        });
    }

    public void drawTicks(GuiContext context) {
        Platform.startDrawing(Platform.DrawMode.QUADS, Platform.VertexFormat.POS_COLOR, buffer -> {
            this.x.drawTicks(buffer, this.view, this.y, false, this.minorTickThickness, this.minorTickLength, 0, 0, 0, 0xFF);
            this.y.drawTicks(buffer, this.view, this.x, false, this.minorTickThickness, this.minorTickLength, 0, 0, 0, 0xFF);
            this.x.drawTicks(buffer, this.view, this.y, true, this.majorTickThickness, this.majorTickLength, 0, 0, 0, 0xFF);
            this.y.drawTicks(buffer, this.view, this.x, true, this.majorTickThickness, this.majorTickLength, 0, 0, 0, 0xFF);
        });
    }

    private boolean compute() {
        if (!this.dirty) return false;
        this.dirty = false;
        this.x.compute(this.plots);
        this.y.compute(this.plots);
        int colorIndex = 0;
        for (Plot plot : this.plots) {
            if (plot.defaultColor) {
                plot.color = Plot.DEFAULT_PLOT_COLORS[colorIndex];
                if (++colorIndex == Plot.DEFAULT_PLOT_COLORS.length) {
                    colorIndex = 0;
                }
            }
        }
        return true;
    }

    public GraphAxis getX() {
        return x;
    }

    public GraphAxis getY() {
        return y;
    }

    public GraphDrawable autoXLim() {
        this.x.autoLimits = true;
        redraw();
        return this;
    }

    public GraphDrawable autoYLim() {
        this.y.autoLimits = true;
        redraw();
        return this;
    }

    public GraphDrawable xLim(float min, float max) {
        this.x.min = min;
        this.x.max = max;
        this.x.autoLimits = false;
        redraw();
        return this;
    }

    public GraphDrawable yLim(float min, float max) {
        this.y.min = min;
        this.y.max = max;
        this.y.autoLimits = false;
        redraw();
        return this;
    }

    public GraphDrawable majorTickStyle(float thickness, float length) {
        this.majorTickThickness = thickness;
        this.majorTickLength = length;
        return this;
    }

    public GraphDrawable minorTickStyle(float thickness, float length) {
        this.minorTickThickness = thickness;
        this.minorTickLength = length;
        return this;
    }

    public GraphDrawable xTickFinder(MajorTickFinder majorTickFinder, MinorTickFinder minorTickFinder) {
        this.x.majorTickFinder = majorTickFinder;
        this.x.minorTickFinder = minorTickFinder;
        redraw();
        return this;
    }

    public GraphDrawable yTickFinder(MajorTickFinder majorTickFinder, MinorTickFinder minorTickFinder) {
        this.y.majorTickFinder = majorTickFinder;
        this.y.minorTickFinder = minorTickFinder;
        redraw();
        return this;
    }

    public GraphDrawable xTickFinder(float majorMultiples, int minorTicksBetweenMajors) {
        return xTickFinder(new AutoMajorTickFinder(majorMultiples), new AutoMinorTickFinder(minorTicksBetweenMajors));
    }

    public GraphDrawable yTickFinder(float majorMultiples, int minorTicksBetweenMajors) {
        return yTickFinder(new AutoMajorTickFinder(majorMultiples), new AutoMinorTickFinder(minorTicksBetweenMajors));
    }

    public GraphDrawable backgroundColor(int color) {
        if (color != 0 && Color.getAlpha(color) == 0) {
            color = Color.withAlpha(color, 0xFF);
        }
        this.backgroundColor = color;
        return this;
    }

    public GraphDrawable plot(float[] x, float[] y) {
        return plot(new Plot().data(x, y));
    }

    public GraphDrawable plot(float[] x, float[] y, int color) {
        return plot(new Plot()
                .data(x, y)
                .color(color));
    }

    public GraphDrawable plot(float[] x, float[] y, float thickness) {
        return plot(new Plot()
                .data(x, y)
                .thickness(thickness));
    }

    public GraphDrawable plot(float[] x, float[] y, float thickness, int color) {
        return plot(new Plot()
                .data(x, y)
                .thickness(thickness)
                .color(color));
    }

    public GraphDrawable plot(Plot plot) {
        this.plots.add(plot);
        return this;
    }


    public GraphDrawable majorGridStyle(float thickness, int color) {
        this.gridLineWidth = thickness;
        this.gridLineColor = color;
        return this;
    }

    public GraphDrawable minorGridStyle(float thickness, int color) {
        this.minorGridLineWidth = thickness;
        this.minorGridLineColor = color;
        return this;
    }

    public GraphDrawable disableMajorGrid() {
        return majorGridLineThickness(0);
    }

    public GraphDrawable disableMinorGrid() {
        return minorGridLineThickness(0);
    }

    public GraphDrawable enableMajorGrid() {
        return majorGridLineThickness(0.5f);
    }

    public GraphDrawable enableMinorGrid() {
        return majorGridLineThickness(0.25f);
    }

    public GraphDrawable majorGridLineThickness(float thickness) {
        this.gridLineWidth = thickness;
        return this;
    }

    public GraphDrawable minorGridLineThickness(float thickness) {
        this.minorGridLineWidth = thickness;
        return this;
    }

    public GraphDrawable majorGridLineColor(int color) {
        this.gridLineColor = color;
        return this;
    }

    public GraphDrawable minorGridLineColor(int color) {
        this.minorGridLineColor = color;
        return this;
    }

    public GraphDrawable graphAspectRatio(float aspectRatio) {
        this.view.setAspectRatio(aspectRatio);
        return this;
    }
}
