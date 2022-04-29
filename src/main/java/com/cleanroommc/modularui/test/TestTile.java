package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.ModularUITextures;
import com.cleanroommc.modularui.api.drawable.AdaptableUITexture;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.drawable.UITexture;
import com.cleanroommc.modularui.api.drawable.shapes.Rectangle;
import com.cleanroommc.modularui.api.math.*;
import com.cleanroommc.modularui.api.screen.ITileWithModularUI;
import com.cleanroommc.modularui.api.screen.ModularWindow;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import com.cleanroommc.modularui.api.widget.IWidgetBuilder;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.common.widget.*;
import com.cleanroommc.modularui.common.widget.textfield.TextFieldWidget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.ItemStackHandler;

public class TestTile extends SyncedTileEntityBase implements ITileWithModularUI, ITickable {

    private int serverValue = 0;
    private final FluidTank fluidTank1 = new FluidTank(10000);
    private final FluidTank fluidTank2 = new FluidTank(Integer.MAX_VALUE);
    private final ItemStackHandler phantomInventory = new ItemStackHandler(2);
    private String textFieldValue = "";
    private final int duration = 60;
    private int progress = 0;
    private int ticks = 0;
    private int serverCounter = 0;
    private static final AdaptableUITexture DISPLAY = AdaptableUITexture.of("modularui:gui/background/display", 143, 75, 2);
    private static final AdaptableUITexture BACKGROUND = AdaptableUITexture.of("modularui:gui/background/background", 176, 166, 3);
    private static final UITexture PROGRESS_BAR = UITexture.fullImage("modularui", "gui/widgets/progress_bar_arrow");
    private static final UITexture PROGRESS_BAR_MIXER = UITexture.fullImage("modularui", "gui/widgets/progress_bar_mixer");

    @Override
    public ModularWindow createWindow(UIBuildContext buildContext) {
        Text[] TEXT = {new Text("Blue \u00a7nUnderlined\u00a7rBlue ").color(0x3058B8), new Text("Mint").color(0x469E8F)};
        ModularWindow.Builder builder = ModularWindow.builder(new Size(176, 272));
        //.addFromJson("modularui:test", buildContext);
        /*buildContext.applyToWidget("background", DrawableWidget.class, widget -> {
            widget.addTooltip("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum.")
                    .addTooltip("Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.")
                    .addTooltip("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet");
        });*/
        builder.setBackground(ModularUITextures.VANILLA_BACKGROUND)
                .bindPlayerInventory(buildContext.getPlayer());
        Column column = new Column();
        addInfo(column);
        ChangeableWidget changeableWidget = new ChangeableWidget(this::dynamicWidget);
        buildContext.addSyncedWindow(1, this::createAnotherWindow);
        return builder
                .widget(new TabContainer()
                        .setButtonSize(new Size(28, 32))
                        .addTabButton(new TabButton(0)
                                .setBackground(false, ModularUITextures.VANILLA_TAB_TOP_START.getSubArea(0, 0, 1f, 0.5f))
                                .setBackground(true, ModularUITextures.VANILLA_TAB_TOP_START.getSubArea(0, 0.5f, 1f, 1f))
                                .setPos(0, -28))
                        .addTabButton(new TabButton(1)
                                .setBackground(false, ModularUITextures.VANILLA_TAB_TOP_MIDDLE.getSubArea(0, 0, 1f, 0.5f))
                                .setBackground(true, ModularUITextures.VANILLA_TAB_TOP_MIDDLE.getSubArea(0, 0.5f, 1f, 1f))
                                .setPos(28, -28))
                        .addTabButton(new TabButton(2)
                                .setBackground(false, ModularUITextures.VANILLA_TAB_TOP_MIDDLE.getSubArea(0, 0, 1f, 0.5f))
                                .setBackground(true, ModularUITextures.VANILLA_TAB_TOP_MIDDLE.getSubArea(0, 0.5f, 1f, 1f))
                                .setPos(56, -28))
                        .addTabButton(new TabButton(3)
                                .setBackground(false, ModularUITextures.VANILLA_TAB_TOP_MIDDLE.getSubArea(0, 0, 1f, 0.5f))
                                .setBackground(true, ModularUITextures.VANILLA_TAB_TOP_MIDDLE.getSubArea(0, 0.5f, 1f, 1f))
                                .setPos(84, -28))
                        .addPage(new MultiChildWidget()
                                .addChild(new TextWidget("Page 1"))
                                .addChild(new SlotWidget(phantomInventory, 0)
                                        .setChangeListener(() -> {
                                            serverCounter = 0;
                                            changeableWidget.notifyChangeServer();
                                        }).setShiftClickPrio(0)
                                        .setPos(10, 30))
                                .addChild(SlotWidget.phantom(phantomInventory, 1)
                                        .setShiftClickPrio(1)
                                        .setPos(28, 30))
                                .addChild(changeableWidget
                                        .setPos(12, 55))
                                .setPos(10, 10)
                                .setDebugLabel("Page1"))
                        .addPage(new MultiChildWidget()
                                .addChild(new TextWidget("Page 2")
                                        .setPos(10, 10))
                                .addChild(column.setPos(7, 19))
                                .addChild(new ButtonWidget()
                                        .setOnClick((clickData, widget) -> {
                                            if (!widget.isClient())
                                                widget.getContext().openSyncedWindow(1);
                                        })
                                        .setBackground(ModularUITextures.VANILLA_BACKGROUND, new Text("Window"))
                                        .setSize(80, 20)
                                        .setPos(20, 100))
                                .setDebugLabel("Page2"))
                        .addPage(new MultiChildWidget()
                                .addChild(new TextWidget("Page 3"))
                                .addChild(new CycleButtonWidget()
                                        .setLength(3)
                                        .setGetter(() -> serverValue)
                                        .setSetter(val -> this.serverValue = val)
                                        .setTexture(UITexture.fullImage("modularui", "gui/widgets/cycle_button_demo"))
                                        .setPos(new Pos2d(68, 0))
                                        .addTooltip("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum.")
                                        .addTooltip("Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.")
                                        .addTooltip("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet"))
                                .addChild(new TextFieldWidget()
                                        .setGetter(() -> textFieldValue)
                                        .setSetter(val -> textFieldValue = val)
                                        .setNumbers(val -> val)
                                        .setTextColor(Color.WHITE.dark(1))
                                        .setTextAlignment(Alignment.CenterLeft)
                                        .setScrollBar(new ScrollBar().setBarTexture(new Rectangle().setColor(Color.WHITE.normal).setCornerRadius(1)))
                                        .setBackground(DISPLAY.withOffset(-2, -2, 4, 4))
                                        .setSize(92, 20)
                                        .setPos(20, 25))
                                .addChild(new ProgressBar()
                                        .setProgress(() -> progress * 1f / duration)
                                        .setDirection(ProgressBar.Direction.LEFT)
                                        .setTexture(PROGRESS_BAR_MIXER, 20)
                                        .setPos(7, 85))
                                .addChild(new ProgressBar()
                                        .setProgress(() -> progress * 1f / duration)
                                        .setDirection(ProgressBar.Direction.RIGHT)
                                        .setTexture(PROGRESS_BAR_MIXER, 20)
                                        .setPos(30, 85))
                                .addChild(new ProgressBar()
                                        .setProgress(() -> progress * 1f / duration)
                                        .setDirection(ProgressBar.Direction.UP)
                                        .setTexture(PROGRESS_BAR_MIXER, 20)
                                        .setPos(53, 85))
                                .addChild(new ProgressBar()
                                        .setProgress(() -> progress * 1f / duration)
                                        .setDirection(ProgressBar.Direction.DOWN)
                                        .setTexture(PROGRESS_BAR_MIXER, 20)
                                        .setPos(76, 85))
                                .addChild(new ProgressBar()
                                        .setProgress(() -> progress * 1f / duration)
                                        .setDirection(ProgressBar.Direction.CIRCULAR_CW)
                                        .setTexture(PROGRESS_BAR_MIXER, 20)
                                        .setPos(99, 85))
                                .addChild(FluidSlotWidget.phantom(fluidTank2, true).setPos(38, 47))
                                .addChild(new FluidSlotWidget(fluidTank1).setPos(20, 47))
                                .addChild(new ButtonWidget()
                                        .setOnClick((clickData, widget) -> {
                                            if (++serverValue == 3) {
                                                serverValue = 0;
                                            }
                                        })
                                        .setSynced(true, false)
                                        .setBackground(DISPLAY, new Text("jTest Textg"))
                                        .setSize(80, 20)
                                        .setPos(10, 65))
                                .addChild(new TextWidget(new Text("modularui.test").localise()).setPos(10, 110))
                                .addChild(new Row()
                                        .setAlignment(MainAxisAlignment.SPACE_BETWEEN, CrossAxisAlignment.CENTER)
                                        .widget(new TextWidget(new Text("Some Text")))
                                        .widget(new ButtonWidget().setBackground(DISPLAY))
                                        .widget(new TextWidget(new Text("More Text")))
                                        .setMaxWidth(156)
                                        .setPos(0, 130))
                                .setPos(10, 10))
                        .addPage(new MultiChildWidget()
                                .addChild(new Scrollable()
                                        .setHorizontalScroll()
                                        .setVerticalScroll()
                                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(0, 0))
                                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(20, 20))
                                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(40, 40))
                                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(60, 60))
                                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(80, 80))
                                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(100, 100))
                                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(120, 120))
                                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(140, 140))
                                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(160, 160))
                                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(180, 180))
                                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(200, 200))
                                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(220, 220))
                                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(240, 240))
                                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(260, 260))
                                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(280, 280))
                                        .widget(new TextFieldWidget()
                                                .setGetter(() -> textFieldValue)
                                                .setSetter(val -> textFieldValue = val)
                                                .setNumbers(val -> val)
                                                .setTextColor(Color.WHITE.dark(1))
                                                .setTextAlignment(Alignment.CenterLeft)
                                                .setScrollBar(new ScrollBar().setBarTexture(new Rectangle().setColor(Color.WHITE.normal).setCornerRadius(1)))
                                                .setBackground(DISPLAY.withOffset(-2, -2, 4, 4))
                                                .setSize(92, 20)
                                                .setPos(20, 25))
                                        .setSize(156, 150))
                                .setPos(10, 10)))
                .widget(new ExpandTab()
                        .setNormalTexture(ModularUITextures.ICON_INFO.withFixedSize(14, 14, 3, 3))
                        .widget(new DrawableWidget()
                                .setDrawable(ModularUITextures.ICON_INFO)
                                .setSize(14, 14)
                                .setPos(3, 3))
                        .widget(new Column()
                                .widget(new TextWidget("Line 1"))
                                .widget(new TextWidget("Line 2"))
                                .widget(new TextWidget("Line 3"))
                                .widget(new TextWidget("Line 4"))
                                .widget(new TextWidget("Line 5"))
                                .widget(new TextWidget("Line 6"))
                                .setPos(5, 20))
                        .setExpandedSize(60, 160)
                        .setBackground(BACKGROUND)
                        .setSize(20, 20)
                        .setPos(177, 5))
                .build();
    }

    public ModularWindow createAnotherWindow(EntityPlayer player) {
        return ModularWindow.builder(100, 100)
                .setBackground(ModularUITextures.VANILLA_BACKGROUND)
                .widget(new ButtonWidget()
                        .setOnClick((clickData, widget) -> {
                            if (!widget.isClient())
                                widget.getWindow().closeWindow();
                        })
                        .setBackground(ModularUITextures.VANILLA_BACKGROUND, new Text("x"))
                        .setSize(12, 12)
                        .setPos(85, 5))
                .widget(new SlotWidget(phantomInventory, 0)
                        .setShiftClickPrio(0)
                        .setPos(30, 30))
                .build();
    }

    public <T extends Widget & IWidgetBuilder<T>> void addInfo(T builder) {
        builder.widget(new TextWidget("Probably a Machine Name"))
                .widget(new TextWidget("Invalid Structure or whatever")
                        .addTooltip("This has a tooltip"));
        if (true) {
            builder.widget(new TextWidget("Maintanance Problems"));
        }
        builder.widget(new Row()
                .widget(new TextWidget("Here you can click a button"))
                .widget(new ButtonWidget()
                        .setOnClick(((clickData, widget) -> ModularUI.LOGGER.info("Clicked Button")))
                        .setSize(20, 9)
                        .setBackground(new Text("[O]"))));
    }

    public Widget dynamicWidget() {
        ItemStack stack = phantomInventory.getStackInSlot(0);
        if (stack.isEmpty()) {
            return null;
        }
        MultiChildWidget widget = new MultiChildWidget();
        widget.addChild(new TextWidget(new Text("Item: " + stack.getDisplayName()).format(TextFormatting.BLUE)))
                .addChild(new CycleButtonWidget()
                        .setGetter(() -> serverCounter)
                        .setSetter(value -> serverCounter = value)
                        .setLength(10)
                        .setTextureGetter(value -> new Text(value + ""))
                        .setPos(5, 11));

        return widget;
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        buf.writeVarInt(serverValue);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        serverValue = buf.readVarInt();
    }

    @Override
    public void receiveCustomData(int discriminator, PacketBuffer buf) {

    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("Val", serverValue);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.serverValue = nbt.getInteger("Val");
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            ticks++;
            if (ticks % 20 == 0) {
                if (++serverCounter == 10) {
                    serverCounter = 0;
                }
            }
        } else {
            if (++progress == duration) {
                progress = 0;
            }
        }
    }
}
