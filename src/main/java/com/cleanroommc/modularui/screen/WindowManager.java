package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.IWidget;
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
     * List of all open windows from top to bottom.
     */
    private final LinkedList<ModularPanel> panels = new LinkedList<>();
    private final List<ModularPanel> panelsView = Collections.unmodifiableList(panels);
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
        openWindow(this.mainPanel);
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
        for (ModularPanel panel : panels) {
            IWidget widget = panel.getTopHovering();
            if (widget != null) {
                return widget;
            }
        }
        return null;
    }

    public void openWindow(@NotNull ModularPanel window) {
        if (this.panels.contains(window)) throw new IllegalStateException();
        this.panels.addFirst(window);
        window.onOpen(this.screen);
    }

    public void closeWindow(@NotNull ModularPanel window) {
        if (!this.panels.contains(window)) throw new IllegalStateException();
        if (this.panels.remove(window)) {
            if (window == this.mainPanel) {
                this.screen.close();
                return;
            }
            window.onClose();
        }
    }

    public void closeAll() {
        for (ModularPanel panel : this.panels) {
            panel.onClose();
        }
        this.panels.clear();
        this.closed = true;
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
