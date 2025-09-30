package com.cleanroommc.modularui.utils.fakeworld;

import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.math.RayTraceResult;

import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

public class SchemaRenderer extends BaseSchemaRenderer {

    private DoubleSupplier scale;
    private BooleanSupplier disableTESR;
    private Consumer<SchemaRenderer> afterRender;
    private BiConsumer<Camera, ISchema> cameraFunc;
    private boolean isometric = false;
    private boolean rayTracing = false;
    private BlockHighlight highlight;

    public SchemaRenderer(ISchema schema, Framebuffer framebuffer) {
        super(schema, framebuffer);
    }

    public SchemaRenderer(ISchema schema) {
        super(schema);
    }

    public SchemaRenderer cameraFunc(BiConsumer<Camera, ISchema> camera) {
        this.cameraFunc = camera;
        return this;
    }

    public SchemaRenderer afterRender(Consumer<SchemaRenderer> consumer) {
        this.afterRender = consumer;
        return this;
    }

    public SchemaRenderer isometric(boolean isometric) {
        this.isometric = isometric;
        return this;
    }

    public SchemaRenderer scale(double scale) {
        return scale(() -> scale);
    }

    public SchemaRenderer scale(DoubleSupplier scale) {
        this.scale = scale;
        return this;
    }

    public SchemaRenderer disableTESR(boolean disable) {
        return disableTESR(() -> disable);
    }

    public SchemaRenderer disableTESR(BooleanSupplier disable) {
        this.disableTESR = disable;
        return this;
    }

    public SchemaRenderer rayTracing(boolean rayTracing) {
        this.rayTracing = rayTracing;
        return this;
    }

    public SchemaRenderer highlightRenderer(BlockHighlight highlight) {
        this.highlight = highlight;
        return rayTracing(true);
    }

    @Override
    protected void onSetupCamera() {
        if (this.scale != null) {
            getCamera().scaleDistanceKeepLookAt((float) this.scale.getAsDouble());
        }
        if (this.cameraFunc != null) {
            this.cameraFunc.accept(getCamera(), getSchema());
        }
    }

    @Override
    protected void onRendered() {
        if (this.afterRender != null) {
            this.afterRender.accept(this);
        }
    }

    @Override
    protected void onSuccessfulRayTrace(@NotNull RayTraceResult result) {
        if (this.highlight != null) {
            this.highlight.renderHighlight(result.getBlockPos(), result.sideHit, getCamera().getPos());
        }
    }

    @Override
    public boolean doRayTrace() {
        return this.rayTracing;
    }

    @Override
    public boolean isTesrEnabled() {
        return this.disableTESR == null || !this.disableTESR.getAsBoolean();
    }

    @Override
    public boolean isIsometric() {
        return isometric;
    }
}
