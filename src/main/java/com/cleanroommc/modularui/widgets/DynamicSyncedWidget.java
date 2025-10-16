package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.value.sync.DynamicSyncHandler;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.WidgetTree;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * A widget which can update its child based on a function in {@link DynamicSyncHandler}.
 * Such a sync handler must be supplied or else this widget has no effect.
 * The dynamic child can be a widget tree of any size which can also contain {@link SyncHandler}s. These sync handlers MUST be registered
 * via {@link com.cleanroommc.modularui.value.sync.PanelSyncManager#getOrCreateSyncHandler(String, Class, Supplier)}
 * @param <W>
 */
public class DynamicSyncedWidget<W extends DynamicSyncedWidget<W>> extends Widget<W> {

    private DynamicSyncHandler syncHandler;
    private IWidget child;

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        if (syncHandler instanceof DynamicSyncHandler dynamicSyncHandler) {
            this.syncHandler = dynamicSyncHandler;
            dynamicSyncHandler.onWidgetUpdate(this::updateChild);
            return true;
        }
        return false;
    }

    @Override
    public @NotNull List<IWidget> getChildren() {
        if (this.child == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(this.child);
        }
    }

    private void updateChild(IWidget widget) {
        if (this.child != null) {
            this.child.dispose();
        }
        this.child = widget;
        if (isValid()) {
            this.child.initialise(this, true);
            scheduleResize();
        }
    }

    public W syncHandler(DynamicSyncHandler syncHandler) {
        this.syncHandler = syncHandler;
        setSyncHandler(syncHandler);
        syncHandler.onWidgetUpdate(this::updateChild);
        return getThis();
    }
}
