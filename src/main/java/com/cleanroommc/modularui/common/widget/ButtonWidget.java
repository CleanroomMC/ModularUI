package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.ModularUITextures;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.common.internal.Theme;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class ButtonWidget extends SyncedWidget implements Interactable {

    public static ButtonWidget openSyncedWindowButton(int id) {
        return (ButtonWidget) new ButtonWidget()
                .setOnClick((clickData, widget) -> {
                    if (!widget.isClient())
                        widget.getContext().openSyncedWindow(id);
                })
                .setBackground(ModularUITextures.VANILLA_BACKGROUND, new Text("Window"));
    }

    public static ButtonWidget closeWindowButton(boolean syncedWindow) {
        return (ButtonWidget) new ButtonWidget()
                .setOnClick((clickData, widget) -> {
                    if (!syncedWindow || !widget.isClient()) {
                        widget.getWindow().closeWindow();
                    }
                })
                .setBackground(ModularUITextures.VANILLA_BACKGROUND, new Text("x"))
                .setSize(12, 12);
    }

    private BiConsumer<ClickData, Widget> clickAction;

    public ButtonWidget setOnClick(BiConsumer<ClickData, Widget> clickAction) {
        this.clickAction = clickAction;
        return this;
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        return new Size(20, 20);
    }

    @Override
    public @Nullable String getBackgroundColorKey() {
        return Theme.KEY_BUTTON;
    }

    @Override
    public ClickResult onClick(int buttonId, boolean doubleClick) {
        if (clickAction != null) {
            ClickData clickData = ClickData.create(buttonId, doubleClick);
            clickAction.accept(clickData, this);
            if (syncsToServer()) {
                syncToServer(1, clickData::writeToPacket);
            }
            Interactable.playButtonClickSound();
            return ClickResult.ACCEPT;
        }
        return ClickResult.ACKNOWLEDGED;
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
