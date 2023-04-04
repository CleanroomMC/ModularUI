package com.cleanroommc.modularui.tablet;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.IItemGuiHolder;
import com.cleanroommc.modularui.manager.GuiInfos;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ItemTablet extends Item implements IItemGuiHolder {

    public static final ItemTablet TABLET = new ItemTablet();

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        TABLET.setRegistryName(ModularUI.ID, "tablet");
        TABLET.setTranslationKey("modularui.tablet");
        registry.register(TABLET);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event) {
        ResourceLocation rl = TABLET.getRegistryName();
        ModelBakery.registerItemVariants(TABLET, rl);
        ModelResourceLocation mrl = new ModelResourceLocation(rl, "inventory");
        ModelLoader.setCustomModelResourceLocation(TABLET, 0, mrl);
    }

    @Override
    public @NotNull ActionResult<ItemStack> onItemRightClick(@NotNull World worldIn, @NotNull EntityPlayer playerIn, @NotNull EnumHand handIn) {
        if (handIn == EnumHand.MAIN_HAND) {
            GuiInfos.PLAYER_ITEM_MAIN_HAND.open(playerIn);
        } else {
            GuiInfos.PLAYER_ITEM_OFF_HAND.open(playerIn);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }

    @Override
    public void buildSyncHandler(GuiSyncHandler guiSyncHandler, EntityPlayer player, ItemStack itemStack) {

    }

    @Override
    public ModularScreen createGuiScreen(EntityPlayer player, ItemStack itemStack) {
        return new TabletScreen(itemStack);
    }

    @Override
    public void onCreated(@NotNull ItemStack stack, @NotNull World worldIn, @NotNull EntityPlayer playerIn) {
        super.onCreated(stack, worldIn, playerIn);
        getUUID(stack);
    }

    public static NBTTagCompound getNbt(ItemStack tablet) {
        if (tablet.isEmpty() || tablet.getItem().getClass() != ItemTablet.class) {
            throw new IllegalArgumentException();
        }
        NBTTagCompound nbt = tablet.getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
            tablet.setTagCompound(nbt);
        }
        return nbt;
    }

    public static String getUUID(ItemStack itemStack) {
        NBTTagCompound nbt = getNbt(itemStack);
        if (!nbt.hasKey("uuid")) {
            nbt.setString("uuid", UUID.randomUUID().toString());
        }
        return nbt.getString("uuid");
    }
}
