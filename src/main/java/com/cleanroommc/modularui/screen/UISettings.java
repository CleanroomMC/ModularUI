package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.JeiSettings;
import com.cleanroommc.modularui.api.UIFactory;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.factory.PosGuiData;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class UISettings {

    public static final double DEFAULT_INTERACT_RANGE = 8.0;

    private Supplier<ModularContainer> containerSupplier;
    private Predicate<EntityPlayer> canInteractWith;
    private final JeiSettings jeiSettings;

    public UISettings() {
        this(new JeiSettingsImpl());
    }

    public UISettings(JeiSettings jeiSettings) {
        this.jeiSettings = jeiSettings;
    }

    /**
     * A function for a custom {@link ModularContainer} implementation. This overrides {@link UIFactory#createContainer()}.
     *
     * @param containerSupplier container creator function. Must return a new instance.
     */
    public void customContainer(Supplier<ModularContainer> containerSupplier) {
        this.containerSupplier = containerSupplier;
    }

    /**
     * Overrides the default can interact check of {@link UIFactory#canInteractWith(EntityPlayer, GuiData)}.
     *
     * @param canInteractWith function to test if a player can interact with the ui. This is called every tick while UI is open. Once this
     *                        function returns false, the UI is immediately closed.
     */
    public void canInteractWith(Predicate<EntityPlayer> canInteractWith) {
        this.canInteractWith = canInteractWith;
    }

    @ApiStatus.Internal
    public <D extends GuiData> void defaultCanInteractWith(UIFactory<D> factory, D guiData) {
        canInteractWith(player -> factory.canInteractWith(player, guiData));
    }

    public void canInteractWithinRange(double x, double y, double z, double range) {
        canInteractWith(player -> player.getDistanceSq(x, y, z) <= range * range);
    }

    public void canInteractWithinRange(BlockPos pos, double range) {
        canInteractWith(player -> player.getDistanceSqToCenter(pos) <= range * range);
    }

    public void canInteractWithinRange(PosGuiData guiData, double range) {
        canInteractWithinRange(guiData.getX() + 0.5, guiData.getY() + 0.5, guiData.getZ() + 0.5, range);
    }

    public void canInteractWithinDefaultRange(double x, double y, double z) {
        canInteractWithinRange(x, y, z, DEFAULT_INTERACT_RANGE);
    }

    public void canInteractWithinDefaultRange(BlockPos pos) {
        canInteractWithinRange(pos, DEFAULT_INTERACT_RANGE);
    }

    public void canInteractWithinDefaultRange(PosGuiData guiData) {
        canInteractWithinRange(guiData, DEFAULT_INTERACT_RANGE);
    }

    public JeiSettings getJeiSettings() {
        return jeiSettings;
    }

    @ApiStatus.Internal
    public ModularContainer createContainer() {
        return containerSupplier.get();
    }

    public boolean hasContainer() {
        return containerSupplier != null;
    }

    public boolean canPlayerInteractWithUI(EntityPlayer player) {
        return canInteractWith == null || canInteractWith.test(player);
    }
}
