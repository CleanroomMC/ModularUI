package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.viewport.LocatedWidget;
import com.cleanroommc.modularui.utils.ObjectList;
import com.cleanroommc.modularui.utils.ReverseIterable;
import com.cleanroommc.modularui.widget.WidgetTree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class WindowManager {

    private final ModularScreen screen;
    /**
     * At least one panel must exist always exist.
     * If this panel is closed, all panels will close.
     */
    private ModularPanel mainPanel;
    /**
     * List of all open panels from top to bottom.
     */
    private final ObjectList<ModularPanel> panels = ObjectList.create();
    private final List<ModularPanel> panelsView = Collections.unmodifiableList(this.panels);
    private final ReverseIterable<ModularPanel> reversePanels = new ReverseIterable<>(this.panelsView);
    private final List<ModularPanel> queueOpenPanels = new ArrayList<>();
    private final List<ModularPanel> queueClosePanels = new ArrayList<>();
    private boolean closed;

    public WindowManager(ModularScreen screen) {
        this.screen = screen;
    }

    void construct(ModularPanel panel) {
        if (this.mainPanel != null) {
            throw new IllegalStateException();
        }
        this.mainPanel = Objects.requireNonNull(panel, "Main panel must not be null!");
    }

    void init() {
        if (this.mainPanel == null) {
            throw new IllegalStateException("WindowManager is not yet constructed!");
        }
        openPanel(this.mainPanel, false);
    }

    public boolean isMainPanel(ModularPanel panel) {
        return this.mainPanel == panel;
    }

    void clearQueue() {
        if (!this.queueOpenPanels.isEmpty()) {
            for (ModularPanel panel : this.queueOpenPanels) {
                openPanel(panel, true);
            }
            this.queueOpenPanels.clear();
        }

        if (!this.queueClosePanels.isEmpty()) {
            if (this.queueClosePanels.contains(this.mainPanel)) {
                this.panels.removeIf(panel -> {
                    panel.onClose();
                    return true;
                });
                this.screen.close(true);
            } else {
                for (ModularPanel panel : this.queueClosePanels) {
                    if (!this.panels.contains(panel)) throw new IllegalStateException();
                    if (this.panels.remove(panel)) {
                        panel.onClose();
                    }
                }
            }
            this.queueClosePanels.clear();
        }
    }

    private void openPanel(ModularPanel panel, boolean resize) {
        if (this.panels.size() == 127) {
            throw new IllegalStateException("Too many panels are open!");
        }
        if (this.panels.contains(panel) || isPanelOpen(panel.getName())) {
            throw new IllegalStateException("Panel " + panel.getName() + " is already open.");
        }
        panel.setPanelGuiContext(this.screen.context);
        this.panels.addFirst(panel);
        panel.getArea().setPanelLayer((byte) this.panels.size());
        panel.onOpen(this.screen);
        if (resize) {
            WidgetTree.resize(panel);
        }
    }

    public boolean isPanelOpen(String name) {
        for (ModularPanel panel : this.panels) {
            if (panel.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    public ModularScreen getScreen() {
        return this.screen;
    }

    @NotNull
    public ModularPanel getMainPanel() {
        if (this.mainPanel == null) {
            throw new IllegalStateException("WindowManager has not been initialised yet!");
        }
        if (this.closed) {
            throw new IllegalStateException("Screen has been closed");
        }
        return this.mainPanel;
    }

    public ModularPanel getTopMostPanel() {
        return this.panels.peekFirst();
    }

    @Nullable
    public IWidget getTopWidget() {
        for (ModularPanel panel : this.panels) {
            IWidget widget = panel.getTopHovering();
            if (widget != null) {
                return widget;
            }
        }
        return null;
    }

    @Nullable
    public LocatedWidget getTopWidgetLocated() {
        for (ModularPanel panel : this.panels) {
            LocatedWidget widget = panel.getTopHoveringLocated();
            if (widget != null) {
                return widget;
            }
        }
        return null;
    }

    public void openPanel(@NotNull ModularPanel panel) {
        if (!this.queueOpenPanels.contains(panel)) {
            this.queueOpenPanels.add(panel);
        }
    }

    public void closePanel(@NotNull ModularPanel panel) {
        if (!this.queueClosePanels.contains(panel)) {
            this.queueClosePanels.add(panel);
        }
    }

    public void closeTopPanel(boolean alsoCloseMain, boolean animate) {
        ModularPanel panel = getTopMostPanel();
        if (panel == getMainPanel() && !alsoCloseMain) return;
        if (animate) {
            panel.animateClose();
            return;
        }
        panel.closeIfOpen();
    }

    public void closeAll() {
        for (ModularPanel panel : this.panels) {
            panel.onClose();
        }
        this.panels.clear();
    }

    public void pushUp(@NotNull ModularPanel window) {
        int index = this.panels.indexOf(window);
        if (index < 0) throw new IllegalStateException();
        if (index == 0) return;
        this.panels.remove(index);
        this.panels.add(index - 1, window);
    }

    public void pushDown(@NotNull ModularPanel window) {
        int index = this.panels.indexOf(window);
        if (index < 0) throw new IllegalStateException();
        if (index == this.panels.size() - 1) return;
        this.panels.remove(index);
        this.panels.add(index + 1, window);
    }

    public void pushToTop(@NotNull ModularPanel window) {
        int index = this.panels.indexOf(window);
        if (index < 0) throw new IllegalStateException();
        if (index == 0) return;
        this.panels.remove(index);
        this.panels.addFirst(window);
    }

    public void pushToBottom(@NotNull ModularPanel window) {
        int index = this.panels.indexOf(window);
        if (index < 0) throw new IllegalStateException();
        if (index == this.panels.size() - 1) return;
        this.panels.remove(index);
        this.panels.addLast(window);
    }

    @NotNull
    @UnmodifiableView
    public List<ModularPanel> getOpenPanels() {
        return this.panelsView;
    }

    @NotNull
    @UnmodifiableView
    public Iterable<ModularPanel> getReverseOpenPanels() {
        return this.reversePanels;
    }

    public boolean isClosed() {
        return this.closed;
    }

    public boolean isAboutToClose(ModularPanel panel) {
        for (ModularPanel panel1 : this.queueClosePanels) {
            if (panel == panel1) {
                return true;
            }
        }
        return false;
    }
}
