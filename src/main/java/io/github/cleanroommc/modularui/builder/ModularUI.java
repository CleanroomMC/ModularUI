package io.github.cleanroommc.modularui.builder;

import com.google.common.collect.ImmutableBiMap;
import io.github.cleanroommc.modularui.api.ISyncedWidget;
import io.github.cleanroommc.modularui.api.IWidgetParent;
import io.github.cleanroommc.modularui.api.Interactable;
import io.github.cleanroommc.modularui.api.math.GuiArea;
import io.github.cleanroommc.modularui.api.math.Pos2d;
import io.github.cleanroommc.modularui.internal.ModularGui;
import io.github.cleanroommc.modularui.widget.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class ModularUI implements IWidgetParent {

    @SideOnly(Side.CLIENT)
    public static final Minecraft MC = Minecraft.getMinecraft();

    public final List<Widget> children;
    public final ImmutableBiMap<Integer, ISyncedWidget> syncedWidgets;
    public final List<Interactable> interactables = new ArrayList<>();
    public final Set<Interactable> listeners = new HashSet<>();
    public final GuiArea area;
    private boolean initialised = false;
    public final EntityPlayer player;

    private ModularGui guiScreen;

    protected ModularUI(GuiArea area, List<Widget> children, EntityPlayer player) {
        if (area == null || children == null || player == null) {
            throw new NullPointerException("Illegal ModularUI creation");
        }
        this.area = area;
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

    public void initialise(ModularGui gui) {
        if (initialised) {
            throw new IllegalStateException("Can't initialise ModularUI twice!!");
        }
        this.guiScreen = gui;

        for (Widget widget : children) {
            widget.initialize(this, this, 0);
        }
        // put widgets with higher layers first
        interactables.sort(Comparator.comparingInt(interactable -> ((Widget) interactable).getLayer()).reversed());

        initialised = true;
    }

    @SideOnly(Side.CLIENT)
    public Pos2d getMousePos() {
        float x = Mouse.getEventX() * guiScreen.width / (float) MC.displayWidth;
        float y = guiScreen.height - Mouse.getEventY() * guiScreen.height / (float) MC.displayHeight - 1;
        return new Pos2d(x, y);
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
    public GuiArea getArea() {
        return area;
    }

    @Override
    public List<Widget> getChildren() {
        return children;
    }

    public boolean isInitialised() {
        return initialised;
    }
}
