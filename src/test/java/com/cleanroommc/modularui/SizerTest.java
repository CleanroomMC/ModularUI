package com.cleanroommc.modularui;

import com.cleanroommc.modularui.overlay.ScreenWrapper;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.ButtonWidget;

import org.junit.jupiter.api.AssertionFailureBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SizerTest {

    static final int W = 800, H = 450;

    @Test
    void test() {
        Bootstrap.perform();
        ModularPanel panel = panel().child(new ButtonWidget<>().center());
        testPanel(panel);
        assertArea(panel.getArea(), W / 2 - 176 / 2, H / 2 - 166 / 2, 176, 166);
    }

    ModularPanel panel(int w, int h) {
        return ModularPanel.defaultPanel("main", w, h);
    }

    ModularPanel panel() {
        return ModularPanel.defaultPanel("main");
    }

    void assertArea(Area area, int x, int y, int w, int h) {
        Area.SHARED.set(x, y, w, h);
        if (!isAreaSame(Area.SHARED, area)) {
            AssertionFailureBuilder.assertionFailure()
                    .message(null)
                    .actual(area)
                    .expected(Area.SHARED)
                    .buildAndThrow();
        }
    }

    ModularScreen testPanel(ModularPanel panel) {
        ModularScreen screen = new ModularScreen(panel);
        screen.getContext().setSettings(new UISettings());
        ScreenWrapper wrapper = new ScreenWrapper(null, screen);
        screen.construct(wrapper);
        screen.onResize(W, H);
        return screen;
    }

    boolean isAreaSame(Area a, Area b) {
        return a.x == b.x && a.y == b.y && a.width == b.width && a.height == b.height;
    }
}
