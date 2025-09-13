package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.IGuiHolder;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EntityGuiFactory extends AbstractUIFactory<EntityGuiData> {

    public static EntityGuiFactory INSTANCE = new EntityGuiFactory();

    protected EntityGuiFactory() {
        super("mui:entity");
    }

    public <E extends Entity & IGuiHolder<EntityGuiData>> void open(EntityPlayer player, E entity) {
        Objects.requireNonNull(player);
        verifyEntity(player, entity);
        GuiManager.open(this, new EntityGuiData(player, entity), (EntityPlayerMP) player);
    }

    private static <E extends Entity & IGuiHolder<EntityGuiData>> void verifyEntity(EntityPlayer player, E entity) {
        Objects.requireNonNull(entity);
        if (!entity.isEntityAlive()) {
            throw new IllegalArgumentException("Can't open dead Entity GUI!");
        } else if (player.world != entity.world) {
            throw new IllegalArgumentException("Entity must be in same dimension as the player!");
        }
    }

    @Override
    public @NotNull IGuiHolder<EntityGuiData> getGuiHolder(EntityGuiData guiData) {
        return Objects.requireNonNull(castGuiHolder(guiData.getGuiHolder()), "Found Entity is not a gui holder!");
    }

    @Override
    public void writeGuiData(EntityGuiData guiData, PacketBuffer packetBuffer) {
        packetBuffer.writeInt(guiData.getGuiHolder().getEntityId());
    }

    @Override
    public @NotNull EntityGuiData readGuiData(EntityPlayer entityPlayer, PacketBuffer packetBuffer) {
        return new EntityGuiData(entityPlayer, entityPlayer.world.getEntityByID(packetBuffer.readInt()));
    }

    @Override
    public boolean canInteractWith(EntityPlayer player, EntityGuiData guiData) {
        Entity guiHolder = guiData.getGuiHolder();
        return super.canInteractWith(player, guiData) &&
                guiHolder != null &&
                player.getDistanceSq(guiHolder.posX, guiHolder.posY, guiHolder.posZ) <= 64 &&
                player.world == guiHolder.world &&
                guiHolder.isEntityAlive();
    }
}
