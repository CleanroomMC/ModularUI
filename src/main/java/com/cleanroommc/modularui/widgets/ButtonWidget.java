package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.widget.IGuiAction;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.SingleChildWidget;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ButtonWidget<W extends ButtonWidget<W>> extends SingleChildWidget<W> implements Interactable {

    public static ButtonWidget<?> panelCloseButton() {
        ButtonWidget<?> buttonWidget = new ButtonWidget<>();
        return buttonWidget.widgetTheme(IThemeApi.CLOSE_BUTTON)
                .top(4).right(4)
                .overlay(GuiTextures.CROSS_TINY)
                .onMousePressed(mouseButton -> {
                    if (mouseButton == 0 || mouseButton == 1) {
                        buttonWidget.getPanel().closeIfOpen();
                        return true;
                    }
                    return false;
                });
    }

    private boolean playClickSound = true;
    private Runnable clickSound;
    private IGuiAction.MousePressed mousePressed;
    private IGuiAction.MouseReleased mouseReleased;
    private IGuiAction.MousePressed mouseTapped;
    private IGuiAction.MouseScroll mouseScroll;
    private IGuiAction.KeyPressed keyPressed;
    private IGuiAction.KeyReleased keyReleased;
    private IGuiAction.KeyPressed keyTapped;

    private InteractionSyncHandler syncHandler;

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        return syncHandler instanceof InteractionSyncHandler;
    }

    @Override
    public WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getButtonTheme();
    }

    public void playClickSound() {
        if (this.playClickSound) {
            if (this.clickSound != null) {
                this.clickSound.run();
            } else {
                Interactable.playButtonClickSound();
            }
        }
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (this.mousePressed != null && this.mousePressed.press(mouseButton)) {
            playClickSound();
            return Result.SUCCESS;
        }
        if (this.syncHandler != null && this.syncHandler.onMousePressed(mouseButton)) {
            playClickSound();
            return Result.SUCCESS;
        }
        return Result.ACCEPT;
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        return (this.mouseReleased != null && this.mouseReleased.release(mouseButton)) ||
                (this.syncHandler != null && this.syncHandler.onMouseReleased(mouseButton));
    }

    @NotNull
    @Override
    public Result onMouseTapped(int mouseButton) {
        if (this.mouseTapped != null && this.mouseTapped.press(mouseButton)) {
            playClickSound();
            return Result.SUCCESS;
        }
        if (this.syncHandler != null && this.syncHandler.onMouseTapped(mouseButton)) {
            playClickSound();
            return Result.SUCCESS;
        }
        return Result.IGNORE;
    }

    @Override
    public @NotNull Result onKeyPressed(char typedChar, int keyCode) {
        if (this.keyPressed != null && this.keyPressed.press(typedChar, keyCode)) {
            return Result.SUCCESS;
        }
        if (this.syncHandler != null && this.syncHandler.onKeyPressed(typedChar, keyCode)) {
            return Result.SUCCESS;
        }
        return Result.ACCEPT;
    }

    @Override
    public boolean onKeyRelease(char typedChar, int keyCode) {
        return (this.keyReleased != null && this.keyReleased.release(typedChar, keyCode)) ||
                (this.syncHandler != null && this.syncHandler.onKeyReleased(typedChar, keyCode));
    }

    @NotNull
    @Override
    public Result onKeyTapped(char typedChar, int keyCode) {
        if (this.keyTapped != null && this.keyTapped.press(typedChar, keyCode)) {
            return Result.SUCCESS;
        }
        if (this.syncHandler != null && this.syncHandler.onKeyTapped(typedChar, keyCode)) {
            return Result.SUCCESS;
        }
        return Result.IGNORE;
    }

    @Override
    public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
        return (this.mouseScroll != null && this.mouseScroll.scroll(scrollDirection, amount)) ||
                (this.syncHandler != null && this.syncHandler.onMouseScroll((int) Math.copySign(amount, scrollDirection.modifier)));
    }

    public W onMousePressed(IGuiAction.MousePressed mousePressed) {
        this.mousePressed = mousePressed;
        return getThis();
    }

    public W onMouseReleased(IGuiAction.MouseReleased mouseReleased) {
        this.mouseReleased = mouseReleased;
        return getThis();
    }

    public W onMouseTapped(IGuiAction.MousePressed mouseTapped) {
        this.mouseTapped = mouseTapped;
        return getThis();
    }

    public W onMouseScrolled(IGuiAction.MouseScroll mouseScroll) {
        this.mouseScroll = mouseScroll;
        return getThis();
    }

    public W onKeyPressed(IGuiAction.KeyPressed keyPressed) {
        this.keyPressed = keyPressed;
        return getThis();
    }

    public W onKeyReleased(IGuiAction.KeyReleased keyReleased) {
        this.keyReleased = keyReleased;
        return getThis();
    }

    public W onKeyTapped(IGuiAction.KeyPressed keyTapped) {
        this.keyTapped = keyTapped;
        return getThis();
    }

    public W syncHandler(InteractionSyncHandler interactionSyncHandler) {
        setSyncHandler(interactionSyncHandler);
        return getThis();
    }

    @Override
    protected void setSyncHandler(@Nullable SyncHandler syncHandler) {
        this.syncHandler = castIfTypeElseNull(syncHandler, InteractionSyncHandler.class);
        super.setSyncHandler(syncHandler);
    }

    public W playClickSound(boolean play) {
        this.playClickSound = play;
        return getThis();
    }

    public W clickSound(Runnable clickSound) {
        this.clickSound = clickSound;
        return getThis();
    }
}
