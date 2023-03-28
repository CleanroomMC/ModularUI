package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.layout.MainAxisAlignment;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import java.util.function.Consumer;

public class ColorPickerDialog extends Dialog<Integer> {

    private int color;
    private final int alpha;
    private final boolean controlAlpha;

    private final Rectangle preview = new Rectangle();
    private final Rectangle sliderBackgroundR = new Rectangle();
    private final Rectangle sliderBackgroundG = new Rectangle();
    private final Rectangle sliderBackgroundB = new Rectangle();
    private final Rectangle sliderBackgroundA = new Rectangle();

    public ColorPickerDialog(GuiContext context, Consumer<Integer> resultConsumer, int startColor) {
        this(context, resultConsumer, startColor, false);
    }

    public ColorPickerDialog(GuiContext context, Consumer<Integer> resultConsumer, int startColor, boolean controlAlpha) {
        super(context, resultConsumer);
        name("color_picker");
        this.alpha = Color.getAlpha(startColor);
        updateColor(startColor);
        this.controlAlpha = controlAlpha;
        size(140, controlAlpha ? 106 : 94).background(GuiTextures.BACKGROUND);
        align(Alignment.Center);
        PagedWidget.Controller controller = new PagedWidget.Controller();
        child(new Column()
                .left(5).right(5).top(5).bottom(5)
                .child(new Row()
                        .left(5).right(5).height(14)
                        .child(new PageButton(0, controller)
                                .size(0.5f, 1f)
                                .background(true, GuiTextures.BUTTON)
                                .background(false, GuiTextures.SLOT_DARK)
                                .overlay(IKey.str("RGB")))
                        .child(new PageButton(1, controller)
                                .size(0.5f, 1f)
                                .background(true, GuiTextures.BUTTON)
                                .background(false, GuiTextures.SLOT_DARK)
                                .overlay(IKey.str("HSV"))))
                .child(new Row().width(1f).height(12).marginTop(4)
                        .child(IKey.str("Hex: ").asWidget().height(1f))
                        .child(new TextFieldWidget()
                                .expanded()
                                .setValidator(this::validateRawColor)
                                .getter(() -> {
                                    if (controlAlpha) {
                                        return "#" + Integer.toHexString(this.color);
                                    }
                                    return "#" + Integer.toHexString(Color.withAlpha(this.color, 0));
                                })
                                .setter(val -> {
                                    try {
                                        updateColor(Integer.decode(val));
                                    } catch (NumberFormatException ignored) {
                                    }
                                }))
                        .child(this.preview.asWidget().background(GuiTextures.CHECKBOARD).size(10, 10).margin(1)))
                .child(new PagedWidget<>()
                        .left(5).right(5)
                        .expanded()
                        .controller(controller)
                        .addPage(createRGBPage(context))
                        .addPage(createHSVPage(context)))
                .child(new Row()
                        .left(10).right(10).height(14)
                        .mainAxisAlignment(MainAxisAlignment.SPACE_BETWEEN)
                        .child(new ButtonWidget<>()
                                .height(1f).width(50)
                                .overlay(IKey.str("Cancel"))
                                .onMousePressed(button -> {
                                    animateClose();
                                    return true;
                                }))
                        .child(new ButtonWidget<>()
                                .height(1f).width(50)
                                .overlay(IKey.str("Confirm"))
                                .onMousePressed(button -> {
                                    closeWith(this.color);
                                    return true;
                                }))));
    }

    private IWidget createRGBPage(GuiContext context) {
        IDrawable handleBackground = new Rectangle().setColor(Color.WHITE.normal);
        Column parentWidget = new Column()
                .size(1f, 1f)
                .child(new Row()
                        .width(1f).height(12)
                        .child(IKey.str("R: ").asWidget().height(1f))
                        .child(new SliderWidget()
                                .expanded()
                                .height(1f)
                                .background(this.sliderBackgroundR.asIcon().size(0, 4))
                                .sliderTexture(handleBackground)
                                .sliderSize(2, 8)
                                .bounds(0, 255)
                                .setter(val -> updateColor(Color.withRed(this.color, (int) val)))
                                .getter(() -> Color.getRed(this.color))))
                .child(new Row()
                        .width(1f).height(12)
                        .child(IKey.str("G: ").asWidget().height(1f))
                        .child(new SliderWidget()
                                .expanded()
                                .height(1f)
                                .background(this.sliderBackgroundG.asIcon().size(0, 4))
                                .sliderTexture(handleBackground)
                                .sliderSize(2, 8)
                                .bounds(0, 255)
                                .setter(val -> updateColor(Color.withGreen(this.color, (int) val)))
                                .getter(() -> Color.getGreen(this.color))))
                .child(new Row()
                        .width(1f).height(12)
                        .child(IKey.str("B: ").asWidget().height(1f))
                        .child(new SliderWidget()
                                .expanded()
                                .height(1f)
                                .background(this.sliderBackgroundB.asIcon().size(0, 4))
                                .sliderTexture(handleBackground)
                                .sliderSize(2, 8)
                                .bounds(0, 255)
                                .setter(val -> updateColor(Color.withBlue(this.color, (int) val)))
                                .getter(() -> Color.getBlue(this.color))));

        if (this.controlAlpha) {
            parentWidget.child(new Row()
                    .width(1f).height(12)
                    .child(IKey.str("A: ").asWidget().height(1f))
                    .child(new SliderWidget()
                            .expanded()
                            .height(1f)
                            .background(this.sliderBackgroundA.asIcon().size(0, 4))
                            .sliderTexture(handleBackground)
                            .sliderSize(2, 8)
                            .bounds(0, 255)
                            .setter(val -> updateColor(Color.withAlpha(this.color, (int) val)))
                            .getter(() -> Color.getAlpha(this.color))));
        }
        return parentWidget;
    }

    private IWidget createHSVPage(GuiContext context) {
        return new ParentWidget<>()
                .size(1f, 1f)
                .child(IKey.str("WIP").asWidget().size(1f, 1f).alignment(Alignment.Center));
    }

    private String validateRawColor(String raw) {
        if (!raw.startsWith("#")) {
            if (raw.startsWith("0x") || raw.startsWith("0X")) {
                raw = raw.substring(2);
            }
            return "#" + raw;
        }
        return raw;
    }

    public void updateColor(int color) {
        this.color = color;
        if (!this.controlAlpha) {
            this.color = Color.withAlpha(this.color, this.alpha);
        }
        color = Color.withAlpha(color, 255);
        int rs = Color.withRed(color, 0), re = Color.withRed(color, 255);
        int gs = Color.withGreen(color, 0), ge = Color.withGreen(color, 255);
        int bs = Color.withBlue(color, 0), be = Color.withBlue(color, 255);
        int as = Color.withAlpha(color, 0), ae = Color.withAlpha(color, 255);
        this.sliderBackgroundR.setHorizontalGradient(rs, re);
        this.sliderBackgroundG.setHorizontalGradient(gs, ge);
        this.sliderBackgroundB.setHorizontalGradient(bs, be);
        this.sliderBackgroundA.setHorizontalGradient(as, ae);
        this.preview.setColor(this.color);
    }
}
