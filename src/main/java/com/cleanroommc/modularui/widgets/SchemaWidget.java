package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.utils.fakeworld.BlockInfo;
import com.cleanroommc.modularui.utils.fakeworld.SchemaRenderer;
import com.cleanroommc.modularui.widget.Widget;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.vector.Vector3f;

import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;

public class SchemaWidget extends Widget<SchemaWidget> implements Interactable {

    public static final float PI = (float) Math.PI;
    public static final float PI2 = 2 * PI;

    private final SchemaRenderer schema;
    private boolean invertMouseScrollScaleAction = false;
    private double scale = 10;

    private int lastMouseX;
    private int lastMouseY;
    private float pitch = (float) (Math.PI / 4f);
    private float yaw = (float) (Math.PI / 4f);
    private final Vector3f offset = new Vector3f();

    public SchemaWidget(SchemaRenderer schema) {
        this.schema = schema;
        schema.cameraFunc((camera, $schema) -> {
            camera.setLookAt($schema.getOriginF().translate(offset.x, offset.y, offset.z), scale, yaw, pitch);
        });
    }

    @Override
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        modifyScale(-scrollDirection.modifier * amount / 120.0);
        return true;
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
        if (mouseButton == 0) {
            float moveScale = 0.025f;
            yaw = (yaw + dx * moveScale + PI2) % PI2;
            pitch = MathHelper.clamp(pitch + dy * moveScale, -PI2 / 4 + 0.001f, PI2 / 4 - 0.001f);
        } else if (mouseButton == 2) {
            // the idea is to construct a vector which points upwards from the camerae pov (y-axis on screen)
            // but force dy we force x = y = 0
            float y = (float) Math.cos(pitch);
            float moveScale = 0.06f;
            // with this the offset can be moved by dy
            offset.translate(0, dy * y * moveScale, 0);
            // to respect dx we need a new vector which is perpendicular on the previous vector (x-axis on screen)
            // y = 0 => mouse movement in x does not move y
            float phi = (yaw + PI / 2) % PI2;
            float x = (float) Math.cos(phi);
            float z = (float) Math.sin(phi);
            offset.translate(dx * x * moveScale, 0, dx * z * moveScale);
        }
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
    }

    public SchemaWidget modifyScale(double scale) {
        this.scale += scale;
        return this;
    }

    public SchemaWidget invertMouseScrollScaleAction(boolean invert) {
        invertMouseScrollScaleAction = invert;
        return this;
    }

    @Override
    public @Nullable IDrawable getOverlay() {
        return schema;
    }

    public static class DisableTESR extends ButtonWidget<DisableTESR> {

        private boolean disable = false;

        public DisableTESR() {
            background(GuiTextures.MC_BACKGROUND);
            overlay(IKey.str("TESR").scale(0.5f));
            onMousePressed(mouseButton -> {
                if (mouseButton == 0) {
                    disable = !disable;
                    return true;
                }

                return false;
            });
        }

        public BooleanSupplier makeSuppler() {
            return () -> disable;
        }
    }

    public static class LayerUpDown extends ButtonWidget<LayerUpDown> {

        private final int minLayer;
        private final int maxLayer;
        private int currentLayer = Integer.MIN_VALUE;

        public LayerUpDown(int minLayer, int maxLayer) {
            this.minLayer = minLayer;
            this.maxLayer = maxLayer;
            background(GuiTextures.MC_BACKGROUND);
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
        }

        public BiPredicate<BlockPos, BlockInfo> makeSchemaFilter() {
            return (blockPos, blockInfo) -> currentLayer == Integer.MIN_VALUE || currentLayer >= blockPos.getY();
        }

        public LayerUpDown startLayer(int start) {
            this.currentLayer = start;
            return this;
        }
    }
}
