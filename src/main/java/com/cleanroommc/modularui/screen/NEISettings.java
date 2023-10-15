package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.integration.nei.NEIState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Keeps track of everything related to NEI in a Modular GUI.
 * By default, NEI is disabled in client only GUIs.
 * This class can be safely interacted with even when NEI is not installed.
 */
public class NEISettings {

    private NEIState neiState = NEIState.DEFAULT;
    private final List<IWidget> neiExclusionWidgets = new ArrayList<>();
    private final List<Rectangle> neiExclusionAreas = new ArrayList<>();

    /**
     * Force NEI to be enabled
     */
    public void enableNEI() {
        this.neiState = NEIState.ENABLED;
    }

    /**
     * Force NEI to be disabled
     */
    public void disableNEI() {
        this.neiState = NEIState.DISABLED;
    }

    /**
     * Only enabled NEI in synced GUIs
     */
    public void defaultNEI() {
        this.neiState = NEIState.DEFAULT;
    }

    /**
     * Checks if NEI is enabled for a given screen
     *
     * @param screen modular screen
     * @return true if NEI is enabled
     */
    public boolean isNEIEnabled(ModularScreen screen) {
        return this.neiState.test(screen);
    }

    /**
     * Adds an exclusion zone. NEI will always try to avoid exclusion zones. <br>
     * <b>If a widgets wishes to have an exclusion zone it should use {@link #addNEIExclusionArea(IWidget)}!</b>
     *
     * @param area exclusion area
     */
    public void addNEIExclusionArea(Rectangle area) {
        if (!this.neiExclusionAreas.contains(area)) {
            this.neiExclusionAreas.add(area);
        }
    }

    /**
     * Removes an exclusion zone.
     *
     * @param area exclusion area to remove (must be the same instance)
     */
    public void removeNEIExclusionArea(Rectangle area) {
        this.neiExclusionAreas.remove(area);
    }

    /**
     * Adds an exclusion zone of a widget. NEI will always try to avoid exclusion zones. <br>
     * Useful when a widget is outside its panel.
     *
     * @param area widget
     */
    public void addNEIExclusionArea(IWidget area) {
        if (!this.neiExclusionWidgets.contains(area)) {
            this.neiExclusionWidgets.add(area);
        }
    }

    /**
     * Removes a widget exclusion area.
     *
     * @param area widget
     */
    public void removeNEIExclusionArea(IWidget area) {
        this.neiExclusionWidgets.remove(area);
    }

    @UnmodifiableView
    public List<Rectangle> getNEIExclusionAreas() {
        return Collections.unmodifiableList(this.neiExclusionAreas);
    }

    @UnmodifiableView
    public List<IWidget> getNEIExclusionWidgets() {
        return Collections.unmodifiableList(this.neiExclusionWidgets);
    }

    @ApiStatus.Internal
    public List<Rectangle> getAllNEIExclusionAreas() {
        this.neiExclusionWidgets.removeIf(widget -> !widget.isValid());
        List<Rectangle> areas = new ArrayList<>(this.neiExclusionAreas);
        areas.addAll(this.neiExclusionWidgets.stream()
                .filter(IWidget::isEnabled)
                .map(IWidget::getArea)
                .collect(Collectors.toList()));
        return areas;
    }
}
