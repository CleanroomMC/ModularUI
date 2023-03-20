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
import java.util.regex.Pattern;

public class ColorPickerDialog extends Dialog<Integer> {

    private int color;
    private final boolean controlAlpha;

    public ColorPickerDialog(GuiContext context, Consumer<Integer> resultConsumer, int startColor) {
        this(context, resultConsumer, startColor, false);
    }

    public ColorPickerDialog(GuiContext context, Consumer<Integer> resultConsumer, int startColor, boolean controlAlpha) {
        super(context, resultConsumer);
        name("color_picker");
        this.color = startColor;
        this.controlAlpha = controlAlpha;
        size(140, controlAlpha ? 116 : 102).background(GuiTextures.BACKGROUND);
        align(Alignment.Center);
        PagedWidget.Controller controller = new PagedWidget.Controller();
        int alpha = Color.getAlpha(startColor);
        child(new Column()
                .left(5).right(5).top(5).bottom(5)
                .child(new Row().width(1f).height(14)
                        .child(new PageButton(0, controller)
                                .size(0.5f, 1f)
                                .background(GuiTextures.BUTTON, IKey.str("RGB").color(Color.WHITE.normal).shadow(true)))
                        .child(new PageButton(1, controller)
                                .size(0.5f, 1f)
                                .background(GuiTextures.BUTTON, IKey.str("HSV").color(Color.WHITE.normal).shadow(true))))
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
                                        this.color = Integer.decode(val);
                                    } catch (NumberFormatException ignored) {
                                    }
                                    if (!controlAlpha) {
                                        this.color = Color.withAlpha(this.color, alpha);
                                    }
                                })))
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
                                .background(GuiTextures.BUTTON, IKey.str("Cancel").color(Color.WHITE.normal).shadow(true))
                                .onMousePressed(button -> {
                                    closeIfOpen();
                                    return true;
                                }))
                        .child(new ButtonWidget<>()
                                .height(1f).width(50)
                                .background(GuiTextures.BUTTON, IKey.str("Confirm").color(Color.WHITE.normal).shadow(true))
                                .onMousePressed(button -> {
                                    closeWith(this.color);
                                    return true;
                                }))));
    }

    private IWidget createRGBPage(GuiContext context) {
        IDrawable sliderBackground = new Rectangle().setColor(Color.withAlpha(Color.BLACK.normal, 0.5f)).asIcon().size(0, 4);
        IDrawable handleBackground = new Rectangle().setColor(Color.WHITE.normal);
        Column parentWidget = new Column()
                .size(1f, 1f)
                .child(new Row()
                        .width(1f).height(12)
                        .child(IKey.str("R: ").asWidget().height(1f))
                        .child(new SliderWidget()
                                .expanded()
                                .height(1f)
                                .background(sliderBackground)
                                .sliderTexture(handleBackground)
                                .sliderSize(2, 8)
                                .bounds(0, 255)
                                .setter(val -> {
                                    this.color = Color.withRed(this.color, (int) val);
                                })
                                .getter(() -> Color.getRed(this.color))))
                .child(new Row()
                        .width(1f).height(12)
                        .child(IKey.str("G: ").asWidget().height(1f))
                        .child(new SliderWidget()
                                .expanded()
                                .height(1f)
                                .background(sliderBackground)
                                .sliderTexture(handleBackground)
                                .sliderSize(2, 8)
                                .bounds(0, 255)
                                .setter(val -> {
                                    this.color = Color.withGreen(this.color, (int) val);
                                })
                                .getter(() -> Color.getGreen(this.color))))
                .child(new Row()
                        .width(1f).height(12)
                        .child(IKey.str("B: ").asWidget().height(1f))
                        .child(new SliderWidget()
                                .expanded()
                                .height(1f)
                                .background(sliderBackground)
                                .sliderTexture(handleBackground)
                                .sliderSize(2, 8)
                                .bounds(0, 255)
                                .setter(val -> {
                                    this.color = Color.withBlue(this.color, (int) val);
                                })
                                .getter(() -> Color.getBlue(this.color))));

        if (this.controlAlpha) {
            parentWidget.child(new Row()
                    .width(1f).height(12)
                    .child(IKey.str("A: ").asWidget().height(1f))
                    .child(new SliderWidget()
                            .expanded()
                            .height(1f)
                            .background(sliderBackground)
                            .sliderTexture(handleBackground)
                            .sliderSize(2, 8)
                            .bounds(0, 255)
                            .setter(val -> {
                                this.color = Color.withAlpha(this.color, (int) val);
                            })
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
        if (!raw.startsWith("#") && Pattern.compile("[a-fA-F]").matcher(raw).find()) {
            return "#" + raw;
        }
        return raw;
    }
}
