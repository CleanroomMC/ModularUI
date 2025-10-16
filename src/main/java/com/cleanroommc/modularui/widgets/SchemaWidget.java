package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.MathUtils;
import com.cleanroommc.modularui.utils.fakeworld.BaseSchemaRenderer;
import com.cleanroommc.modularui.utils.fakeworld.ISchema;
import com.cleanroommc.modularui.widget.Widget;

import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class SchemaWidget extends Widget<SchemaWidget> implements Interactable {

    public static final float PI = (float) Math.PI;
    public static final float PI2 = 2f * PI;
    public static final float PI_HALF = PI / 2f;
    public static final float PI_QUART = PI / 4f;

    private final BaseSchemaRenderer schema;
    private boolean enableRotation = true;
    private boolean enableTranslation = true;
    private boolean enableScaling = true;
    private int lastMouseX;
    private int lastMouseY;
    private float scale = 10;
    private float pitch = PI_QUART;
    private float yaw = 0;
    private final Vector3f offset = new Vector3f();

    public SchemaWidget(ISchema schema) {
        this(new BaseSchemaRenderer(schema));
    }

    public SchemaWidget(BaseSchemaRenderer schema) {
        this.schema = schema;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        Vec3d f = this.schema.getSchema().getFocus();
        this.schema.getCamera().setLookAtAndAngle((float) (f.x + this.offset.x), (float) (f.y + this.offset.y), (float) (f.z + this.offset.z), scale, yaw, pitch);
        this.schema.drawAtZero(context, getArea(), widgetTheme.getTheme());
    }

    @Override
    public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
        if (this.enableScaling) {
            incrementScale(-scrollDirection.modifier * amount / 120.0f);
            return true;
        }
        return false;
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        this.lastMouseX = getContext().getAbsMouseX();
        this.lastMouseY = getContext().getAbsMouseY();
        return Result.SUCCESS;
    }

    @Override
    public void onMouseDrag(int mouseButton, long timeSinceClick) {
        int mouseX = getContext().getAbsMouseX();
        int mouseY = getContext().getAbsMouseY();
        int dx = mouseX - lastMouseX;
        int dy = mouseY - lastMouseY;
        if (mouseButton == 0 && this.enableRotation) {
            float moveScale = 0.03f;
            yaw(this.yaw + dx * moveScale);
            pitch(this.pitch + dy * moveScale);
        } else if (mouseButton == 2 && this.enableTranslation) {
            float moveScale = 0.09f;
            Vector3f look = this.schema.getCamera().getLookVec().normalize(); // direction camera is looking
            Vector3f right = look.cross(0, 1, 0, new Vector3f()).normalize(); // right relative to screen
            Vector3f up = right.cross(look, new Vector3f()); // up relative to screen
            this.offset.add(right.mul(-dx * moveScale)).add(up.mul(dy * moveScale));
        }
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
    }

    public void incrementScale(float amount) {
        this.scale += amount;
    }

    public SchemaWidget scale(float scale) {
        this.scale = scale;
        return this;
    }

    public SchemaWidget pitch(float pitch) {
        this.pitch = MathUtils.clamp(pitch, -PI_HALF + 0.001f, PI_HALF - 0.001f);
        return this;
    }

    public SchemaWidget yaw(float yaw) {
        this.yaw = (yaw + PI2) % PI2;
        return this;
    }

    public SchemaWidget offset(float x, float y, float z) {
        this.offset.set(x, y, z);
        return this;
    }

    public SchemaWidget enableDragRotation(boolean enable) {
        this.enableRotation = enable;
        return this;
    }

    public SchemaWidget enableDragTranslation(boolean enable) {
        this.enableTranslation = enable;
        return this;
    }

    public SchemaWidget enableScrollScaling(boolean enable) {
        this.enableScaling = enable;
        return this;
    }

    public SchemaWidget enableInteraction(boolean rotation, boolean translation, boolean scaling) {
        return enableDragRotation(rotation)
                .enableDragTranslation(translation)
                .enableScrollScaling(scaling);
    }

    public SchemaWidget enableAllInteraction(boolean enable) {
        return enableInteraction(enable, enable, enable);
    }

    public RayTraceResult getBlockUnderMouse() {
        return schema.getLastRayTrace();
    }

    public static class LayerButton extends ButtonWidget<LayerButton> {

        private final int minLayer;
        private final int maxLayer;
        private int currentLayer = Integer.MIN_VALUE;

        public LayerButton(ISchema schema, int minLayer, int maxLayer) {
            this.minLayer = minLayer;
            this.maxLayer = maxLayer;
            overlay(IKey.dynamic(() -> currentLayer > Integer.MIN_VALUE ? Integer.toString(currentLayer) : "ALL").scale(0.5f));

            onMousePressed(mouseButton -> {
                if (mouseButton == 0 || mouseButton == 1) {
                    if (mouseButton == 0) {
                        if (currentLayer == Integer.MIN_VALUE) {
                            currentLayer = minLayer;
                        } else {
                            currentLayer++;
                        }
                    } else {
                        if (currentLayer == Integer.MIN_VALUE) {
                            currentLayer = maxLayer;
                        } else {
                            currentLayer--;
                        }
                    }
                    if (currentLayer > maxLayer || currentLayer < minLayer) {
                        currentLayer = Integer.MIN_VALUE;
                    }
                    return true;
                }
                return false;
            });
            schema.setRenderFilter((blockPos, blockInfo) -> currentLayer == Integer.MIN_VALUE || currentLayer >= blockPos.getY());
        }

        public LayerButton startLayer(int start) {
            this.currentLayer = start;
            return this;
        }
    }
}
