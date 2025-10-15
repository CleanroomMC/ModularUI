package com.cleanroommc.modularui;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.SingleChildWidget;
import com.cleanroommc.modularui.widget.WidgetTree;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WidgetTreeTest {

    @Test
    void testBasicTree() {
        ModularPanel root = new ModularPanel("root")
                .child(Flow.column().name("col 1")
                        .child(Flow.row().name("row 1")
                                .child(IKey.str("text").asWidget().name("text 1")))
                        .child(Flow.row().name("row 2")
                                .child(IKey.str("text").asWidget().name("text 2")))
                        .child(Flow.row().name("row 3")
                                .child(IKey.str("text").asWidget().name("text 3")))
                        .child(Flow.row().name("row 4")
                                .child(IKey.str("text").asWidget().name("text 4")))
                )
                .child(Flow.row().name("row 5")
                        .child(new ButtonWidget<>().name("button 1")
                                .child(new SingleChildWidget<>().name("button 2")
                                        .child(new ButtonWidget<>().name("button 3")
                                                .child(new SingleChildWidget<>().name("button 4")
                                                        .child(new ButtonWidget<>().name("button 5")
                                                                .child(new SingleChildWidget<>().name("button 6")
                                                                        .child(new ButtonWidget<>().name("button 7")))))))));

        assertNotNull(WidgetTree.findFirstWithNameNullable(root, "text 2"));
        assertNotNull(WidgetTree.findFirstWithNameNullable(root, "text 3", TextWidget.class));
        assertNull(WidgetTree.findFirstWithNameNullable(root, "text 5"));
        assertNull(WidgetTree.findFirstWithNameNullable(root, "text 6", TextWidget.class));
        assertNotNull(WidgetTree.findChildAtNullable(root, "row 5", "button 1", "button 2", "button 3", "button 4", "button 5", "button 6", "button 7"));
        assertNotNull(WidgetTree.findChildAtNullable(root, ButtonWidget.class, "row 5", "button 1", "button 2", "button 3", "button 4", "button 5", "button 6", "button 7"));
        assertNotNull(WidgetTree.findChildAtNullable(root, SingleChildWidget.class, "row 5", "button 1", "button 2", "button 3", "button 4", "button 5", "button 6"));
        assertNull(WidgetTree.findChildAtNullable(root, SingleChildWidget.class, "row 5", "button 1", "button 2", "button 4", "button 5", "button 6"));
        assertThrows(ClassCastException.class, () -> WidgetTree.findChildAt(root, ParentWidget.class, "row 5", "button 1", "button 2", "button 3", "button 4", "button 5", "button 6", "button 7"));
        assertThrows(NoSuchElementException.class, () -> WidgetTree.findChildAt(root, ParentWidget.class, "row 5", "button 1", "button 2", "button 4", "button 5", "button 6", "button 7"));
    }
}
