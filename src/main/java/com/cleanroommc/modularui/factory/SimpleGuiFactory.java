package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.IGuiHolder;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Sometimes you don't want to open guis which are bound to a TileEntity or an Item.
 * For example by a command. You are supposed to create one simple factory per GUI to make sure they are same
 * on client and server.
 * These factories are registered automatically.
 */
public class SimpleGuiFactory extends AbstractUIFactory<GuiData> {

    private final Supplier<IGuiHolder<GuiData>> guiHolderSupplier;
    private IGuiHolder<GuiData> guiHolder;

    /**
     * Creates a simple gui factory.
     *
     * @param name      name of the factory
     * @param guiHolder gui holder
     */
    public SimpleGuiFactory(String name, IGuiHolder<GuiData> guiHolder) {
        this(name, () -> guiHolder);
    }

    /**
     * Creates a simple gui factory.
     *
     * @param name              name of the factory
     * @param guiHolderSupplier a function which retrieves a gui holder. This is only called once and then cached.
     */
    public SimpleGuiFactory(String name, Supplier<IGuiHolder<GuiData>> guiHolderSupplier) {
        super(name);
        this.guiHolderSupplier = guiHolderSupplier;
        GuiManager.registerFactory(this);
    }

    public void init() {}

    public void open(EntityPlayerMP player) {
        GuiManager.open(this, new GuiData(player), player);
    }

    @Override
    public void writeGuiData(GuiData guiData, PacketBuffer buffer) {}

    @Override
    public @NotNull GuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        return new GuiData(player);
    }

    @Override
    public @NotNull IGuiHolder<GuiData> getGuiHolder(GuiData data) {
        if (this.guiHolder == null) {
            this.guiHolder = this.guiHolderSupplier.get();
            Objects.requireNonNull(this.guiHolder, "IGuiHolder must not be null");
        }
        return this.guiHolder;
    }
}
