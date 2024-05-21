package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.keys.StringKey;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.utils.fakeworld.BlockInfo;
import com.cleanroommc.modularui.utils.fakeworld.SchemaRenderer;
import com.cleanroommc.modularui.widget.Widget;

import net.minecraft.util.math.BlockPos;

import net.minecraft.util.math.MathHelper;

import org.jetbrains.annotations.Nullable;

import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;

public class SchemaWidget extends Widget<SchemaWidget> implements Interactable {

    private final SchemaRenderer schema;
    private boolean invertMouseScrollScaleAction = false;
    private double scale = 10;


    public SchemaWidget(SchemaRenderer schema) {
        this.schema = schema;
        schema.cameraFunc((camera, $schema) -> {
            camera.setLookAt($schema.getOrigin(), scale, Math.toRadians(yaw), Math.toRadians(pitch));
        });
    }

    @Override
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        switch (scrollDirection) {
            case UP -> {
                modifyScale(-((double) amount / 120));
                return true;
            }
            case DOWN -> {
                modifyScale(((double) amount / 120));
                return true;
            }
            default -> {
                return false;
            }
        }
    }


    public SchemaWidget modifyScale(double scale) {
        this.scale += scale;
        return this;
    }

    public SchemaWidget invertMouseScrollScaleAction(boolean invert) {
        invertMouseScrollScaleAction = invert;
        return this;
    }


    private int lastMouseX;
    private int lastMouseY;

    private float pitch;
    private float yaw;

    @Override
    public void onMouseDrag(int mouseButton, long timeSinceClick) {
        int mouseX = getContext().getAbsMouseX();
        int mouseY = getContext().getAbsMouseY();

        //timeSinceClick is sometimes greater than 0 even on the first click
        if (timeSinceClick == 0) {
            this.lastMouseX = mouseX;
            this.lastMouseY = mouseY;
            return;
        }

        if (mouseButton == 0) {
            yaw += mouseX - lastMouseX + 360;
            yaw = yaw % 360;
            pitch = (float) MathHelper.clamp(pitch + (mouseY - lastMouseY), -89.9, 89.9);

            this.lastMouseX = mouseX;
            this.lastMouseY = mouseY;
        }

    }

    @Override
    public @Nullable IDrawable getOverlay() {
        return schema;
    }


    public static class DisableTESR extends ButtonWidget<DisableTESR> {

        private boolean disable = false;

        public DisableTESR() {
            this.background(GuiTextures.MC_BACKGROUND);
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

        private int minLayer = Integer.MIN_VALUE;
        private int maxLayer = Integer.MAX_VALUE;
        private int currentLayer = Integer.MIN_VALUE;

        public LayerUpDown() {
            this.background(GuiTextures.MC_BACKGROUND);

            onMousePressed(mouseButton -> {
                if (mouseButton == 0) {
                    currentLayer = Math.max(currentLayer + 1, maxLayer);
                }

                if (mouseButton == 1) {
                    currentLayer = Math.max(currentLayer - 1, maxLayer);
                }

                if (mouseButton == 0 || mouseButton == 1) {
                    overlay(new StringKey(Integer.toString(currentLayer)));
                    return true;
                }

                return false;
            });
        }

        public BiPredicate<BlockPos, BlockInfo> makeSchemaFilter() {
            return (blockPos, blockInfo) -> {
                maxLayer = Math.min(maxLayer, blockPos.getY());
                minLayer = Math.max(minLayer, blockPos.getY());
                if (currentLayer > blockPos.getY()) return false;
                return true;
            };
        }

        public LayerUpDown startLayer(int start) {
            this.currentLayer = start;
            return this;
        }
    }

}
