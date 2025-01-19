package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.factory.GuiFactories;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class TestBlock extends Block implements ITileEntityProvider {

    public static final Block testBlock = new TestBlock(TestTile::new);
    public static final Block testBlock2 = new TestBlock(TestTile2::new);
    public static final ItemBlock testItemBlock = new ItemBlock(testBlock);
    public static final ItemBlock testItemBlock2 = new ItemBlock(testBlock2);

    public static void preInit() {
        ResourceLocation rl = new ResourceLocation(ModularUI.ID, "test_block");
        testBlock.setRegistryName(rl);
        testItemBlock.setRegistryName(rl);
        GameRegistry.registerTileEntity(TestTile.class, rl);
        rl = new ResourceLocation(ModularUI.ID, "test_block_2");
        testBlock2.setRegistryName(rl);
        testItemBlock2.setRegistryName(rl);
        GameRegistry.registerTileEntity(TestTile2.class, rl);
        TestItem.testItem.setRegistryName(ModularUI.ID, "test_item");
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();
        registry.register(testBlock);
        registry.register(testBlock2);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(testItemBlock);
        registry.register(testItemBlock2);
        registry.register(TestItem.testItem);
    }

    @SubscribeEvent
    public static void registerModel(ModelRegistryEvent event) {
        ModelResourceLocation mrl = new ModelResourceLocation(new ResourceLocation("diamond"), "inventory");
        ModelLoader.setCustomModelResourceLocation(TestItem.testItem, 0, mrl);
    }

    private final Supplier<TileEntity> tileEntitySupplier;

    public TestBlock(Supplier<TileEntity> tileEntitySupplier) {
        super(Material.ROCK);
        this.tileEntitySupplier = tileEntitySupplier;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@NotNull World worldIn, int meta) {
        return this.tileEntitySupplier.get();
    }

    @Override
    public boolean onBlockActivated(World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer playerIn, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            GuiFactories.tileEntity().open(playerIn, pos);
        }
        return true;
    }
}
