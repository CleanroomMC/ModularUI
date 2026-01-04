package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.api.RecipeViewerSettings;
import com.cleanroommc.modularui.api.UIFactory;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.network.NetworkUtils;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class UISettings {

    public static final double DEFAULT_INTERACT_RANGE = 8.0;

    private Supplier<ModularContainer> containerSupplier;
    @SideOnly(Side.CLIENT)
    private GuiCreator guiSupplier;
    private Predicate<EntityPlayer> canInteractWith;
    private String theme;
    private final RecipeViewerSettings recipeViewerSettings;

    public UISettings() {
        this(new RecipeViewerSettingsImpl());
    }

    public UISettings(RecipeViewerSettings recipeViewerSettings) {
        this.recipeViewerSettings = recipeViewerSettings;
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
     * A function for a custom {@link IMuiScreen} implementation. This overrides
     * {@link UIFactory#createScreenWrapper(ModularContainer, ModularScreen)}. Note that {@link IMuiScreen#getGuiScreen()} has to be an
     * instance of {@link GuiContainer} otherwise an exception is thrown, when the UI opens.
     *
     * @param guiSupplier a supplier for a gui creator function. It has to be a double function because it crashes on server otherwise.
     */
    public void customGui(Supplier<GuiCreator> guiSupplier) {
        if (NetworkUtils.isDedicatedClient()) {
            this.guiSupplier = guiSupplier.get();
        }
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

    public void useTheme(String theme) {
        this.theme = theme;
    }

    public RecipeViewerSettings getRecipeViewerSettings() {
        return recipeViewerSettings;
    }

    @ApiStatus.Internal
    public ModularContainer createContainer() {
        return containerSupplier.get();
    }

    @ApiStatus.Internal
    @SideOnly(Side.CLIENT)
    public IMuiScreen createGui(ModularContainer container, ModularScreen screen) {
        return guiSupplier.create(container, screen);
    }

    public boolean hasCustomContainer() {
        return containerSupplier != null;
    }

    @SideOnly(Side.CLIENT)
    public boolean hasCustomGui() {
        return guiSupplier != null;
    }

    public boolean canPlayerInteractWithUI(EntityPlayer player) {
        return canInteractWith == null || canInteractWith.test(player);
    }

    public @Nullable String getTheme() {
        return theme;
    }

    public interface GuiCreator {

        IMuiScreen create(ModularContainer container, ModularScreen screen);
    }
}
