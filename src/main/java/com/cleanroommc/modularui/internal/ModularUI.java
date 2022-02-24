package com.cleanroommc.modularui.internal;

import com.google.common.collect.ImmutableBiMap;
import com.cleanroommc.modularui.ModularUIMod;
import com.cleanroommc.modularui.api.ISyncedWidget;
import com.cleanroommc.modularui.api.IVanillaSlot;
import com.cleanroommc.modularui.api.IWidgetParent;
import com.cleanroommc.modularui.api.Interactable;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.widget.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class ModularUI implements IWidgetParent {

    public static boolean isClient() {
        return FMLCommonHandler.instance().getSide() == Side.CLIENT;
    }

    @SideOnly(Side.CLIENT)
    public static final Minecraft MC = Minecraft.getMinecraft();

    public final List<Widget> children;
    public final ImmutableBiMap<Integer, ISyncedWidget> syncedWidgets;
    public final List<Interactable> interactables = new ArrayList<>();
    public final Set<Interactable> listeners = new HashSet<>();
    private Size screenSize = new Size(MC.displayWidth, MC.displayHeight);
    private final Size size;
    private Pos2d pos = Pos2d.zero();
    private final Alignment alignment;
    private boolean initialised = false;
    public final EntityPlayer player;

    @SideOnly(Side.CLIENT)
    private ModularGui guiScreen;
    private ModularUIContainer container;
    @SideOnly(Side.CLIENT)
    private Pos2d mousePos = Pos2d.zero();

    public ModularUI(Size size, Alignment alignment, List<Widget> children, EntityPlayer player) {
        if (size == null || children == null || player == null) {
            throw new NullPointerException("Illegal ModularUI creation");
        }
        ModularUIMod.LOGGER.info("Creating modular ui with {} children", children.size());
        this.size = size;
        this.alignment = alignment;
        this.children = Collections.unmodifiableList(children);
        this.player = player;
        ImmutableBiMap.Builder<Integer, ISyncedWidget> syncedWidgetBuilder = ImmutableBiMap.builder();
        AtomicInteger i = new AtomicInteger();
        IWidgetParent.forEachByLayer(this, widget -> {
            if (widget instanceof ISyncedWidget) {
                syncedWidgetBuilder.put(i.getAndIncrement(), (ISyncedWidget) widget);
            }
            if (widget instanceof Interactable) {
                interactables.add((Interactable) widget);
            }
            return false;
        });
        this.syncedWidgets = syncedWidgetBuilder.build();
    }

    public void setScreen(ModularGui guiScreen) {
        this.guiScreen = guiScreen;
    }

    public void setContainer(ModularUIContainer container) {
        this.container = container;
        IWidgetParent.forEachByLayer(this, widget -> {
            if (widget instanceof IVanillaSlot) {
                container.addSlotToContainer(((IVanillaSlot) widget).getMcSlot());
            }
        });
    }

    public void initialise() {
        if (isInitialised()) {
            throw new IllegalStateException("Can't initialise ModularUI twice!!");
        }

        for (Widget widget : children) {
            widget.initialize(this, this, 0);
        }

        AtomicInteger count = new AtomicInteger(0);
        IWidgetParent.forEachByLayer(this, widget -> {
            count.getAndIncrement();
            return false;
        });

        // put widgets with higher layers first
        interactables.sort(Comparator.comparingInt(interactable -> ((Widget) interactable).getLayer()).reversed());

        initialised = true;
        ModularUIMod.LOGGER.info("Initialising ModularUI. {} widgets found", count.toString());
    }

    public void onResize(Size scaledSize) {
        screenSize = scaledSize;
        pos = alignment.getAlignedPos(screenSize, size);
        if (isInitialised()) {
            children.forEach(Widget::checkNeedsRebuild);
        }
    }

    public void updateMousePos() {
        float x = Mouse.getEventX() * guiScreen.width / (float) MC.displayWidth;
        float y = guiScreen.height - Mouse.getEventY() * guiScreen.height / (float) MC.displayHeight - 1;
        mousePos = new Pos2d(x, y);
    }

    public void update() {
        IWidgetParent.forEachByLayer(this, widget -> {
            widget.screenUpdateInternal();
            return false;
        });
    }

    public int getSyncId(ISyncedWidget syncedWidget) {
        Integer id = syncedWidgets.inverse().get(Objects.requireNonNull(syncedWidget, "ISyncedWidget is null!!"));
        if (id == null) {
            throw new NoSuchElementException("Can't find id for widget " + syncedWidget.toString());
        }
        return id;
    }

    public ISyncedWidget getSyncedWidget(int id) {
        ISyncedWidget syncedWidget = syncedWidgets.get(id);
        if (syncedWidget == null) {
            throw new NoSuchElementException("Can't find synced widget for id " + id);
        }
        return syncedWidget;
    }

    @Nullable
    public Interactable getTopInteractable(Pos2d pos) {
        for (Interactable interactable : interactables) {
            Widget widget = (Widget) interactable;
            if (widget.isEnabled() && widget.getArea().contains(pos)) {
                return interactable;
            }
        }
        return null;
    }

    /**
     * The events of the added listeners are always called.
     */
    public void addInteractionListener(Interactable interactable) {
        listeners.add(interactable);
    }

    public Collection<Interactable> getListeners() {
        return listeners;
    }

    @Override
    public Size getSize() {
        return size;
    }

    public Size getScaledScreenSize() {
        return screenSize;
    }

    @Override
    public Pos2d getAbsolutePos() {
        return pos;
    }

    @Override
    public Pos2d getPos() {
        return pos;
    }

    @Override
    public List<Widget> getChildren() {
        return children;
    }

    public boolean isInitialised() {
        return initialised;
    }

    @SideOnly(Side.CLIENT)
    public Pos2d getMousePos() {
        return mousePos;
    }
}
