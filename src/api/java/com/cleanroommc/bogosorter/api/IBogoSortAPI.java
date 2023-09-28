package com.cleanroommc.bogosorter.api;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface IBogoSortAPI {

    static IBogoSortAPI getInstance() {
        throw new UnsupportedOperationException();
    }

    /**
     * Register a function that converts a {@link Slot} to a {@link ISlot}. Useful if modders messed up.
     *
     * @param clazz    slot class
     * @param function converter function
     * @param <T>      slot type
     */
    <T extends Slot> void addSlotGetter(Class<T> clazz, Function<T, ISlot> function);

    /**
     * Registers a function which handles slot insertions in a custom way.
     *
     * @param clazz      container class
     * @param insertable custom insertion function
     */
    void addCustomInsertable(Class<? extends Container> clazz, ICustomInsertable insertable);

    /**
     * Adds sorting compat for a container class
     *
     * @param clazz   container class
     * @param builder sorting compat builder
     * @param <T>     container type
     */
    <T extends Container> void addCompat(Class<T> clazz, BiConsumer<T, ISortingContextBuilder> builder);

    /**
     * Adds sorting compat for a container class.
     * Is useful when you don't have access to the super class of {@link T}
     *
     * @param clazz   container class
     * @param builder sorting compat builder
     * @param <T>     container type
     */
    <T> void addCompatSimple(Class<T> clazz, BiConsumer<T, ISortingContextBuilder> builder);

    /**
     * Registers a function to figure out where to place player inventory sort buttons.
     *
     * @param clazz     class of the container
     * @param buttonPos pos function or null if no buttons are desired
     * @throws IllegalArgumentException if the class is not of a container
     */
    void addPlayerSortButtonPosition(Class<?> clazz, IPosSetter buttonPos);

    /**
     * Removes sorting compat for a container class
     *
     * @param clazz container class
     * @param <T>   container type
     */
    <T extends Container> void removeCompat(Class<T> clazz);

    /**
     * Registers a sorting rule for items
     *
     * @param itemComparator comparator
     */
    void registerItemSortingRule(String key, Comparator<ItemStack> itemComparator);

    /**
     * Registers a sorting rule for NBT tags
     *
     * @param tagPath    path of the nbt tag. Separate sub tags with '/'
     * @param comparator comparator sorting the tags based on tagPath
     */
    void registerNbtSortingRule(String key, String tagPath, Comparator<NBTBase> comparator);

    /**
     * Registers a sorting rule for NBT tags
     *
     * @param tagPath      path of the nbt tag. Separate sub tags with '/'
     * @param expectedType the expected NBT tag id. Will be automatically compared
     * @see net.minecraftforge.common.util.Constants.NBT for expectedType
     */
    void registerNbtSortingRule(String key, String tagPath, int expectedType);

    /**
     * Registers a sorting rule for NBT tags
     *
     * @param tagPath      path of the nbt tag. Separate sub tags with '/'
     * @param expectedType the expected NBT tag id
     * @param comparator   comparator of the type converted by converter
     * @param converter    converts the tag found at the tagPath for the comparator
     * @see net.minecraftforge.common.util.Constants.NBT for expectedType
     */
    <T> void registerNbtSortingRule(String key, String tagPath, int expectedType, Comparator<T> comparator, Function<NBTBase, T> converter);

    /**
     * Opens the bogosort config gui
     */
    @SideOnly(Side.CLIENT)
    void openConfigGui();

    /**
     * Tries to sort a slot group with the given slot.
     *
     * @param slot slot of slot group to sort
     * @return if the slot group will be sorted (on server side)
     */
    @SideOnly(Side.CLIENT)
    boolean sortSlotGroup(Slot slot);

    /**
     * Turns a slot into a generic interface slot
     *
     * @param slot slot
     * @return generic slot
     */
    ISlot getSlot(Slot slot);

    /**
     * Transforms a list of slots using {@link #getSlot(Slot)}
     *
     * @param slots list of slots
     * @return list of generic slots
     */
    List<ISlot> getSlots(List<Slot> slots);
}
