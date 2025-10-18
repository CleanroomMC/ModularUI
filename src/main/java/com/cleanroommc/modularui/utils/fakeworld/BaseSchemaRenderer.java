package com.cleanroommc.modularui.utils.fakeworld;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.Icon;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.GuiUtils;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.utils.VectorUtil;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.SchemaWidget;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BaseSchemaRenderer implements IDrawable {

    protected static final FloatBuffer PIXEL_DEPTH_BUFFER = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asFloatBuffer();

    private static final Framebuffer FBO = new Framebuffer(1080, 1080, true);

    private final ISchema schema;
    private final IBlockAccess renderWorld;
    private final Framebuffer framebuffer;
    private final Camera camera = new Camera();
    private RayTraceResult lastRayTrace = null;
    private final int[] viewport = new int[4];

    public BaseSchemaRenderer(ISchema schema, Framebuffer framebuffer) {
        this.schema = schema;
        this.framebuffer = framebuffer;
        this.renderWorld = new RenderWorld(schema);
    }

    public BaseSchemaRenderer(ISchema schema) {
        this(schema, FBO);
    }

    @Nullable
    public RayTraceResult getLastRayTrace() {
        return lastRayTrace;
    }

    public ISchema getSchema() {
        return schema;
    }

    public Camera getCamera() {
        return camera;
    }

    @Override
    public SchemaWidget asWidget() {
        return new SchemaWidget(this);
    }

    @Override
    public Icon asIcon() {
        return IDrawable.super.asIcon().size(50);
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        render(context, x, y, width, height);
    }

    protected void render(GuiContext context, int x, int y, int width, int height) {
        int mx = context.getMouseX();
        int my = context.getMouseY();
        onSetupCamera();
        int lastFbo = bindFBO();
        setupCamera(this.framebuffer.framebufferWidth, this.framebuffer.framebufferHeight);
        renderWorld();
        if (doRayTrace()) {
            RayTraceResult result = null;
            if (Area.isInside(x, y, width, height, mx, my)) {
                result = rayTrace(mx, my, width, height);
            }
            if (result == null) {
                if (this.lastRayTrace != null) {
                    onRayTraceFailed();
                }
            } else {
                onSuccessfulRayTrace(result);
            }
            this.lastRayTrace = result;
        }
        onRendered();
        Platform.setupDrawTex();
        resetCamera();
        unbindFBO(lastFbo);

        // bind FBO as texture
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        lastFbo = GL11.glGetInteger(GL11.GL_TEXTURE_2D);
        GlStateManager.bindTexture(this.framebuffer.framebufferTexture);
        GlStateManager.color(1, 1, 1, 1);

        // render rect with FBO texture
        Platform.startDrawing(Platform.DrawMode.QUADS, Platform.VertexFormat.POS_TEX, bufferBuilder -> {
            bufferBuilder.pos(x + width, y + height, 0).tex(1, 0).endVertex();
            bufferBuilder.pos(x + width, y, 0).tex(1, 1).endVertex();
            bufferBuilder.pos(x, y, 0).tex(0, 1).endVertex();
            bufferBuilder.pos(x, y + height, 0).tex(0, 0).endVertex();
        });
        GlStateManager.bindTexture(lastFbo);
    }

    /**
     * Raytraces at the given mouse pos.
     *
     * @param mouseX A mouse x pos from 0 to width
     * @param mouseY A mouse y pos from 0 to height
     * @param width  Height of the drawn framebuffer
     * @param height Width of the drawn framebuffer
     * @return raytrace result
     */
    protected RayTraceResult rayTrace(int mouseX, int mouseY, int width, int height) {
        // transform mouse pos into relative mouse pos from 0 - 1
        Vector3f levelMouse = screenToWorldPos((float) mouseX / width, (float) mouseY / height);
        Vector3f target = this.camera.getLookVec().mul(20).add(levelMouse);
        return schema.getWorld().rayTraceBlocks(VectorUtil.toVec3d(levelMouse), VectorUtil.toVec3d(target), true);
    }

    /**
     * Converts a relative screen pos to a world pos.
     *
     * @param x X pos from 0 to 1
     * @param y Y pos from 0 to 1
     * @return world pos
     */
    protected Vector3f screenToWorldPos(float x, float y) {
        // read projection and modelview matrix
        Matrix4f transform = GuiUtils.getProjectionMatrix().mul(GuiUtils.getTransformationMatrix());
        // convert pos to framebuffer pos
        int wx = (int) (x * this.viewport[2]);
        int wy = (int) (y * this.viewport[3]);
        wy = viewport[3] - wy; // invert y
        GL11.glReadPixels(wx, wy, 1, 1, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, PIXEL_DEPTH_BUFFER); // read depth under mouse
        PIXEL_DEPTH_BUFFER.rewind();
        float depth = PIXEL_DEPTH_BUFFER.get();
        PIXEL_DEPTH_BUFFER.rewind();
        return transform.unproject(wx, wy, depth, this.viewport, new Vector3f());
    }

    private void renderWorld() {
        Minecraft mc = Minecraft.getMinecraft();
        Platform.setupDrawTex();
        GlStateManager.enableCull();
        GlStateManager.enableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        mc.entityRenderer.disableLightmap();
        mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        BlockRenderLayer oldRenderLayer = MinecraftForgeClient.getRenderLayer();
        GlStateManager.disableLighting();
        Platform.setupDrawGradient(); // needed for ambient occlusion

        List<TileEntity> tesr = null;
        try { // render block in each layer
            for (BlockRenderLayer layer : BlockRenderLayer.values()) {
                if (layer.ordinal() == 0 && isTesrEnabled()) {
                    tesr = renderBlocksInLayer(mc, layer, true);
                } else {
                    renderBlocksInLayer(mc, layer, false);
                }
            }
        } finally {
            ForgeHooksClient.setRenderLayer(oldRenderLayer);
        }

        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableLighting();

        try { // render TESR
            if (tesr != null && !tesr.isEmpty()) {
                renderTesr(tesr, 0);
                if (!tesr.isEmpty()) { // any tesr that don't render in pass 1 or 2 are removed from the list
                    renderTesr(tesr, 1);
                    renderTesr(tesr, 2);
                }
            }
        } finally {
            ForgeHooksClient.setRenderPass(-1);
        }

        Platform.endDrawGradient();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }

    private List<TileEntity> renderBlocksInLayer(Minecraft mc, BlockRenderLayer layer, boolean collectTesr) {
        List<TileEntity> tesr = collectTesr ? new ArrayList<>() : null;
        ForgeHooksClient.setRenderLayer(layer);
        int pass = layer == BlockRenderLayer.TRANSLUCENT ? 1 : 0;
        setDefaultPassRenderState(pass);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        BlockRendererDispatcher blockrendererdispatcher = mc.getBlockRendererDispatcher();
        this.schema.forEach(pair -> {
            BlockPos pos = pair.getKey();
            IBlockState state = pair.getValue().getBlockState();
            if (state.getBlock().isAir(state, this.renderWorld, pos)) return;
            if (collectTesr) {
                TileEntity te = pair.getValue().getTileEntity();
                if (te != null && !te.isInvalid()) {
                    if (!te.getPos().equals(pos)) te.setPos(pos.toImmutable());
                    if (TileEntityRendererDispatcher.instance.getRenderer(te.getClass()) != null) {
                        // only collect tiles to render which actually have a tesr
                        tesr.add(te);
                    }
                }
            }
            if (state.getBlock().canRenderInLayer(state, layer)) {
                blockrendererdispatcher.renderBlock(state, pos, this.renderWorld, buffer);
            }
        });
        Tessellator.getInstance().draw();
        Tessellator.getInstance().getBuffer().setTranslation(0, 0, 0);
        return tesr;
    }

    private static void renderTesr(List<TileEntity> tileEntities, int pass) {
        ForgeHooksClient.setRenderPass(pass);
        GlStateManager.color(1, 1, 1, 1);
        setDefaultPassRenderState(pass);
        for (Iterator<TileEntity> iterator = tileEntities.iterator(); iterator.hasNext(); ) {
            TileEntity tile = iterator.next();
            if (tile == null || tile.isInvalid()) continue;
            if (pass == 0 && (!tile.shouldRenderInPass(1) || !tile.shouldRenderInPass(2))) {
                // remove tiles that don't render in further passes
                iterator.remove();
            }
            if (tile.shouldRenderInPass(pass)) {
                BlockPos pos = tile.getPos();
                TileEntityRendererDispatcher.instance.render(tile, pos.getX(), pos.getY(), pos.getZ(), 0);
            }
        }
    }

    private static void setDefaultPassRenderState(int pass) {
        GlStateManager.color(1, 1, 1, 1);
        if (pass == 0) { // SOLID
            GlStateManager.enableDepth();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
        } else { // TRANSLUCENT
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.depthMask(false);
        }
    }

    protected final void setupCamera(int width, int height) {
        //GlStateManager.pushAttrib();

        Minecraft.getMinecraft().entityRenderer.disableLightmap();
        GlStateManager.disableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();

        // setup viewport and clear GL buffers
        GlStateManager.viewport(0, 0, width, height);
        Color.setGlColor(getClearColor());
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // setup projection matrix to perspective
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();

        float near = isIsometric() ? 1f : 0.1f;
        float far = 10000.0f;
        float fovY = 60.0f; // Field of view in the Y direction
        float aspect = (float) width / height; // width and height are the dimensions of your window
        float top = near * (float) Math.tan(Math.toRadians(fovY) / 2.0);
        float bottom = -top;
        float left = aspect * bottom;
        float right = aspect * top;
        if (isIsometric()) {
            GL11.glOrtho(left, right, bottom, top, near, far);
        } else {
            GL11.glFrustum(left, right, bottom, top, near, far);
        }

        // setup modelview matrix
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        if (isIsometric()) {
            GlStateManager.scale(0.1, 0.1, 0.1);
        }
        var c = this.camera.getPos();
        var lookAt = this.camera.getLookAt();
        GLU.gluLookAt(c.x, c.y, c.z, lookAt.x, lookAt.y, lookAt.z, 0, 1, 0);
    }

    protected final void resetCamera() {
        // reset viewport
        Minecraft minecraft = Minecraft.getMinecraft();
        GlStateManager.viewport(0, 0, minecraft.displayWidth, minecraft.displayHeight);
        // reset projection matrix
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();
        // reset modelview matrix
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();
        GlStateManager.disableBlend();
        GlStateManager.disableDepth();
        // reset attributes
        // GlStateManager.popAttrib();
    }

    private int bindFBO() {
        int lastID = GL11.glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT);
        this.framebuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
        this.framebuffer.framebufferClear();
        this.framebuffer.bindFramebuffer(true);
        this.viewport[0] = 0;
        this.viewport[1] = 0;
        this.viewport[2] = this.framebuffer.framebufferWidth;
        this.viewport[3] = this.framebuffer.framebufferHeight;
        GlStateManager.pushMatrix();
        return lastID;
    }

    private void unbindFBO(int lastID) {
        GlStateManager.popMatrix();
        this.framebuffer.unbindFramebufferTexture();
        OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, lastID);
    }

    @ApiStatus.OverrideOnly
    protected void onSetupCamera() {}

    @ApiStatus.OverrideOnly
    protected void onRendered() {}

    @ApiStatus.OverrideOnly
    protected void onSuccessfulRayTrace(@NotNull RayTraceResult result) {}

    @ApiStatus.OverrideOnly
    protected void onRayTraceFailed() {}

    public boolean doRayTrace() {
        return false;
    }

    public int getClearColor() {
        return Color.withAlpha(0, 0.5f);
    }

    public boolean isIsometric() {
        return false;
    }

    public boolean isTesrEnabled() {
        return true;
    }
}



