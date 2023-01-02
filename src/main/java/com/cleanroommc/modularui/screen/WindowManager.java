package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widget.WidgetTree;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

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
    private final LinkedList<ModularPanel> panels = new LinkedList<>();
    private final List<ModularPanel> panelsView = Collections.unmodifiableList(panels);
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
        if (panel == null) {
            throw new NullPointerException();
        }
        this.mainPanel = panel;
    }

    void init() {
        if (this.mainPanel == null) {
            throw new IllegalStateException("WindowManager is not yet constructed!");
        }
        openPanel(this.mainPanel, false);
    }

    @ApiStatus.Internal
    public void clearQueue() {
        if (!this.queueOpenPanels.isEmpty()) {
            for (ModularPanel panel : this.queueOpenPanels) {
                openPanel(panel, true);
            }
            this.queueOpenPanels.clear();
        }

        if (!this.queueClosePanels.isEmpty()) {
            for (ModularPanel panel : this.queueClosePanels) {
                if (!this.panels.contains(panel)) throw new IllegalStateException();
                if (this.panels.remove(panel)) {
                    if (panel == this.mainPanel) {
                        this.screen.close();
                        return;
                    }
                    panel.onClose();
                }
            }
            this.queueClosePanels.clear();
        }
    }

    private void openPanel(ModularPanel panel, boolean resize) {
        if (this.panels.contains(panel)) throw new IllegalStateException();
        this.panels.addLast(panel);
        panel.onOpen(this.screen);
        if (resize) {
            WidgetTree.resize(panel);
        }
    }

    @NotNull
    public ModularScreen getScreen() {
        return screen;
    }

    @NotNull
    public ModularPanel getMainPanel() {
        if (this.mainPanel == null) {
            throw new IllegalStateException("WindowManager has not been initialised yet!");
        }
        if (this.closed) {
            throw new IllegalStateException("Screen has been closed");
        }
        return mainPanel;
    }

    public ModularPanel getTopMostPanel() {
        return this.panels.peekFirst();
    }

    @Nullable
    public IWidget getTopWidget() {
        Iterator<ModularPanel> panelIterator = this.panels.descendingIterator();
        while (panelIterator.hasNext()) {
            ModularPanel panel = panelIterator.next();
            IWidget widget = panel.getTopHovering();
            if (widget != null) {
                return widget;
            }
        }
        return null;
    }

    public void openPanel(@NotNull ModularPanel panel) {
        this.queueOpenPanels.add(panel);
    }

    public void closePanel(@NotNull ModularPanel panel) {
        this.queueClosePanels.add(panel);
    }

    public void closeAll() {
        for (ModularPanel panel : this.panels) {
            panel.onClose();
        }
        //this.panels.clear();
        //this.closed = true;
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
    public List<ModularPanel> getOpenWindows() {
        return panelsView;
    }

    @NotNull
    @UnmodifiableView
    public Iterator<ModularPanel> getOpenWindowsReversed() {
        return new Iterator<ModularPanel>() {
            final ListIterator<ModularPanel> it = panelsView.listIterator(panelsView.size());

            @Override
            public boolean hasNext() {
                return it.hasPrevious();
            }

            @Override
            public ModularPanel next() {
                return it.previous();
            }
        };
    }

    public boolean isClosed() {
        return closed;
    }
}
