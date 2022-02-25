package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.IWidgetParent;
import com.cleanroommc.modularui.api.math.*;
import com.cleanroommc.modularui.common.internal.JsonHelper;
import com.cleanroommc.modularui.common.internal.ModularUI;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * This class depicts a functional element of a ModularUI
 */
public abstract class Widget extends Gui {

    public static final Random RNG = new Random();

    private String name = "";

    private ModularUI gui = null;
    private IWidgetParent parent = null;
    private Size size = Size.zero();
    private Pos2d relativePos = Pos2d.zero();
    private Pos2d pos = null;
    private Pos2d fixedPos = null;
    private boolean fillParent;
    private boolean initialised = false;
    protected boolean enabled = true;
    private int layer = -1;
    private boolean needRebuild = false;

    public Widget() {
    }

    public Widget(Size size) {
        this();
        this.size = size;
    }

    public Widget(Size size, Pos2d pos) {
        this();
        this.size = size;
        this.relativePos = pos;
    }

    protected String createDefaultName() {
        return this.getClass().getName() + ";" + Integer.toHexString(RNG.nextInt() & 0xFFFFFF);
    }

    public void readJson(JsonObject json, String type) {
        if (json.has("name")) {
            this.name = json.get("name").getAsString();
        }
        if (json.has("pos")) {
            setPos(Pos2d.ofJson(json.get("pos")));
        }
        if (json.has("size")) {
            setSize(Size.ofJson(json.get("size")));
        }
        if (json.has("fixedPos")) {
            setPos(Pos2d.ofJson(json.get("fixedPos")));
        }
        fillParent = JsonHelper.getBoolean(json, false, "fillParent");
    }

    /**
     * Only used internally
     */
    public final void initialize(ModularUI modularUI, IWidgetParent parent, int layer) {
        if (modularUI == null || parent == null || initialised) {
            throw new IllegalStateException("Illegal initialise call to widget!! " + toString());
        }
        this.gui = modularUI;
        this.parent = parent;
        this.layer = layer;

        if (fillParent) {
            size = parent.getSize();
        }

        onInit();
        this.initialised = true;

        if (ModularUI.isClient()) {
            this.needRebuild = true;
            rebuildInternal(false);
        }

        if (this instanceof IWidgetParent) {
            int nextLayer = layer + 1;
            for (Widget widget : ((IWidgetParent) this).getChildren()) {
                widget.initialize(this.gui, (IWidgetParent) this, nextLayer);
            }
        }
        if (ModularUI.isClient()) {
            onRebuildPost();
            this.needRebuild = false;
        }
    }

    public final void screenUpdateInternal() {
        if (needRebuild) {
            rebuildInternal(true);
            needRebuild = false;
        }
        onScreenUpdate();
    }

    protected final void rebuildInternal(boolean runForChildren) {
        if (!initialised) {
            return;
        }
        if (isFixed()) {
            setPos(this.fixedPos.subtract(parent.getAbsolutePos()));
            setAbsolutePos(this.fixedPos);
        } else {
            setAbsolutePos(parent.getAbsolutePos().add(getPos()));
        }
        onRebuildPre();
        if (runForChildren && this instanceof IWidgetParent) {
            for (Widget child : ((IWidgetParent) this).getChildren()) {
                child.rebuildInternal(true);
            }
            onRebuildPost();
        }
    }

    /**
     * Called once per tick
     */
    @SideOnly(Side.CLIENT)
    public void onScreenUpdate() {
    }

    /**
     * Called each frame, approximately 60 times per second
     */
    @SideOnly(Side.CLIENT)
    public void onFrameUpdate() {
    }

    public void onRebuildPre() {
    }

    public void onRebuildPost() {
    }

    protected void onInit() {
    }

    public boolean isUnderMouse() {
        return isUnderMouse(gui.getMousePos(), getAbsolutePos(), getSize());
    }

    public ModularUI getGui() {
        return gui;
    }

    public IWidgetParent getParent() {
        return parent;
    }

    public GuiArea getArea() {
        return GuiArea.of(size, pos);
    }

    public Pos2d getPos() {
        return relativePos;
    }

    public Pos2d getAbsolutePos() {
        return pos;
    }

    public Size getSize() {
        return size;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getLayer() {
        return layer;
    }

    public final boolean isInitialised() {
        return initialised;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isFixed() {
        return fixedPos != null;
    }

    public Widget setSize(Size size) {
        checkNeedsRebuild();
        this.size = size;
        return this;
    }

    public Widget setPos(Pos2d relativePos) {
        checkNeedsRebuild();
        this.relativePos = relativePos;
        return this;
    }

    protected void setAbsolutePos(Pos2d relativePos) {
        this.pos = relativePos;
    }

    public Widget setFixedPos(@Nullable Pos2d pos) {
        checkNeedsRebuild();
        this.fixedPos = pos;
        return this;
    }

    public Widget fillParent() {
        this.fillParent = true;
        return this;
    }

    public void checkNeedsRebuild() {
        if (initialised && !needRebuild) {
            this.needRebuild = true;
        }
    }

    public static boolean isUnderMouse(Pos2d mouse, Pos2d areaTopLeft, Size areaSize) {
        return mouse.x >= areaTopLeft.x &&
                mouse.x <= areaTopLeft.x + areaSize.width &&
                mouse.y >= areaTopLeft.y &&
                mouse.y <= areaTopLeft.y + areaSize.height;
    }
}
