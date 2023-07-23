package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.widget.ISynced;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widget.ParentWidget;
import it.unimi.dsi.fastutil.chars.Char2IntMap;
import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;

public class SlotGroupWidget extends ParentWidget<SlotGroupWidget> {

    public static SlotGroupWidget playerInventory() {
        SlotGroupWidget slotGroupWidget = new SlotGroupWidget();
        slotGroupWidget.flex()
                .coverChildren()
                .startDefaultMode()
                .leftRel(0.5f).bottom(7)
                .endDefaultMode();
        slotGroupWidget.debugName("player_inventory");
        String key = "player";
        for (int i = 0; i < 9; i++) {
            slotGroupWidget.child(new ItemSlot()
                    .syncHandler(key, i)
                    .pos(i * 18, 3 * 18 + 5)
                    .debugName("slot_" + i));
        }
        for (int i = 0; i < 27; i++) {
            slotGroupWidget.child(new ItemSlot()
                    .syncHandler(key, i + 9)
                    .pos(i % 9 * 18, i / 9 * 18)
                    .debugName("slot_" + (i + 9)));
        }
        return slotGroupWidget;
    }

    private String slotsKeyName;

    public void setSlotsSynced(String name) {
        this.slotsKeyName = name;
        int i = 0;
        for (IWidget widget : getChildren()) {
            if (widget instanceof ISynced) {
                ((ISynced<?>) widget).syncHandler(name, i);
            }
            i++;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String syncKey;
        private final List<String> matrix = new ArrayList<>();
        private final Char2ObjectMap<Object> keys = new Char2ObjectOpenHashMap<>();

        private Builder() {
            this.keys.put(' ', null);
        }

        public Builder synced(String name) {
            this.syncKey = name;
            return this;
        }

        public Builder matrix(String... matrix) {
            this.matrix.clear();
            Collections.addAll(this.matrix, matrix);
            return this;
        }

        public Builder row(String row) {
            this.matrix.add(row);
            return this;
        }

        public Builder key(char c, IWidget widget) {
            this.keys.put(c, widget);
            return this;
        }

        public Builder key(char c, IntFunction<IWidget> widget) {
            this.keys.put(c, widget);
            return this;
        }

        public SlotGroupWidget build() {
            SlotGroupWidget slotGroupWidget = new SlotGroupWidget();
            Char2IntMap charCount = new Char2IntOpenHashMap();
            int x = 0, y = 0, maxWidth = 0;
            int syncId = 0;
            for (String row : this.matrix) {
                for (int i = 0; i < row.length(); i++) {
                    char c = row.charAt(i);
                    int count = charCount.get(c);
                    charCount.put(c, count + 1);
                    Object o = this.keys.get(c);
                    IWidget widget;
                    if (o instanceof IWidget) {
                        widget = (IWidget) o;
                    } else if (o instanceof IntFunction) {
                        widget = ((IntFunction<IWidget>) o).apply(count);
                    } else {
                        x += 18;
                        continue;
                    }
                    widget.flex().left(x).top(y);
                    slotGroupWidget.child(widget);
                    if (this.syncKey != null && widget instanceof ISynced) {
                        ((ISynced<?>) widget).syncHandler(this.syncKey, syncId++);
                    }
                    x += 18;
                    maxWidth = Math.max(maxWidth, x);
                }
                y += 18;
                x = 0;
            }
            slotGroupWidget.flex().size(maxWidth, this.matrix.size() * 18);
            return slotGroupWidget;
        }
    }
}
