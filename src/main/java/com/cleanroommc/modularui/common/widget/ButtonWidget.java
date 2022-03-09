package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.Interactable;
import net.minecraft.network.PacketBuffer;

import java.util.function.BiConsumer;

public class ButtonWidget extends SyncedWidget implements Interactable {

    private BiConsumer<ClickData, Widget> clickAction;

    public ButtonWidget setOnClick(BiConsumer<ClickData, Widget> clickAction) {
        this.clickAction = clickAction;
        return this;
    }

    @Override
    public void onClick(int buttonId, boolean doubleClick) {
        if (clickAction != null) {
            ClickData clickData = ClickData.create(buttonId, doubleClick);
            clickAction.accept(clickData, this);
            if (handlesClient()) {
                syncToServer(1, clickData::writeToPacket);
            }
            Interactable.playButtonClickSound();
        }
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        if (id == 1) {
            ClickData data = ClickData.readPacket(buf);
            clickAction.accept(data, this);
        }
    }
}
