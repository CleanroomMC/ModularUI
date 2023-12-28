package com.cleanroommc.modularui.utils.fakeworld;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.sizer.Area;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;

import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector3f;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SchemaRenderer implements IDrawable {

    private static final Framebuffer FBO = new Framebuffer(1080, 1080, true);

    private final ISchema schema;
    private final Framebuffer framebuffer;
    private final Camera camera = new Camera(new Vector3f(), new Vector3f());
    private boolean cameraSetup = false;
    private Consumer<IRayTracer> onRayTrace;
    private Consumer<Projection> afterRender;
    private BiConsumer<Camera, ISchema> cameraFunc;
    private int clearColor = 0;

    public SchemaRenderer(ISchema schema, Framebuffer framebuffer) {
        this.schema = schema;
        this.framebuffer = framebuffer;
    }

    public SchemaRenderer(ISchema schema) {
        this(schema, FBO);
    }

    public SchemaRenderer cameraFunc(BiConsumer<Camera, ISchema> camera) {
        this.cameraFunc = camera;
        return this;
    }

    public SchemaRenderer onRayTrace(Consumer<IRayTracer> consumer) {
        this.onRayTrace = consumer;
        return this;
    }

    public SchemaRenderer afterRender(Consumer<Projection> consumer) {
        this.afterRender = consumer;
        return this;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        render(x, y, width, height, context.getMouseX(), context.getMouseY());
    }

    public void render(int x, int y, int width, int height, int mouseX, int mouseY) {
        if (this.cameraFunc != null) {
            this.cameraFunc.accept(this.camera, this.schema);
        }
        int lastFbo = bindFBO();
        setupCamera(this.framebuffer.framebufferWidth, this.framebuffer.framebufferHeight);
        renderWorld();
        if (this.onRayTrace != null && Area.isInside(x, y, width, height, mouseX, mouseY)) {
            this.onRayTrace.accept(new IRayTracer() {
                @Override
                public RayTraceResult rayTrace(int screenX, int screenY) {
                    return SchemaRenderer.this.rayTrace(Projection.INSTANCE.unProject(screenX, screenY));
                }

                @Override
                public RayTraceResult rayTraceMousePos() {
                    return rayTrace(mouseX, mouseY);
                }
            });
        }
        resetCamera();
        unbindFBO(lastFbo);

        // bind FBO as texture
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        lastFbo = GL11.glGetInteger(GL11.GL_TEXTURE_2D);
        GlStateManager.bindTexture(this.framebuffer.framebufferTexture);
        GlStateManager.color(1, 1, 1, 1);

        // render rect with FBO texture
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);

        bufferbuilder.pos(x + width, y + height, 0).tex(1, 0).endVertex();
        bufferbuilder.pos(x + width, y, 0).tex(1, 1).endVertex();
        bufferbuilder.pos(x, y, 0).tex(0, 1).endVertex();
        bufferbuilder.pos(x, y + height, 0).tex(0, 0).endVertex();
        tessellator.draw();

        GlStateManager.bindTexture(lastFbo);
    }

    private void renderWorld() {
        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.enableCull();
        GlStateManager.enableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        mc.entityRenderer.disableLightmap();
        mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        BlockRenderLayer oldRenderLayer = MinecraftForgeClient.getRenderLayer();
        GlStateManager.disableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();

        try { // render block in each layer
            for (BlockRenderLayer layer : BlockRenderLayer.values()) {
                ForgeHooksClient.setRenderLayer(layer);
                int pass = layer == BlockRenderLayer.TRANSLUCENT ? 1 : 0;
                setDefaultPassRenderState(pass);
                BufferBuilder buffer = Tessellator.getInstance().getBuffer();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
                BlockRendererDispatcher blockrendererdispatcher = mc.getBlockRendererDispatcher();
                this.schema.forEach(pair -> {
                    BlockPos pos = pair.getKey();
                    IBlockState state = pair.getValue().getBlockState();
                    if (!state.getBlock().isAir(state, this.schema.getWorld(), pos) && state.getBlock().canRenderInLayer(state, layer)) {
                        blockrendererdispatcher.renderBlock(state, pos, this.schema.getWorld(), buffer);
                    }
                });
                Tessellator.getInstance().draw();
                Tessellator.getInstance().getBuffer().setTranslation(0, 0, 0);
            }
        } finally {
            ForgeHooksClient.setRenderLayer(oldRenderLayer);
        }

        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableLighting();

        // render TESR
        for (int pass = 0; pass < 2; pass++) {
            ForgeHooksClient.setRenderPass(pass);
            int finalPass = pass;
            GlStateManager.color(1, 1, 1, 1);
            setDefaultPassRenderState(pass);
            this.schema.forEach(pair -> {
                BlockPos pos = pair.getKey();
                TileEntity tile = pair.getValue().getTileEntity();
                if (tile != null && tile.shouldRenderInPass(finalPass)) {
                    TileEntityRendererDispatcher.instance.render(tile, pos.getX(), pos.getY(), pos.getZ(), 0);
                }
            });
        }
        ForgeHooksClient.setRenderPass(-1);
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        if (this.afterRender != null) {
            this.afterRender.accept(Projection.INSTANCE);
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

    protected void setupCamera(int width, int height) {
        //GlStateManager.pushAttrib();

        Minecraft.getMinecraft().entityRenderer.disableLightmap();
        GlStateManager.disableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();

        // setup viewport and clear GL buffers
        GlStateManager.viewport(0, 0, width, height);
        Color.setGlColor(clearColor);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // setup projection matrix to perspective
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();

        float aspectRatio = width / (height * 1.0f);
        GLU.gluPerspective(60.0f, aspectRatio, 0.1f, 10000.0f);

        // setup modelview matrix
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        var c = this.camera.getPos();
        var lookAt = this.camera.getLookAt();
        GLU.gluLookAt(c.x, c.y, c.z, lookAt.x, lookAt.y, lookAt.z, 0, 1, 0);
        this.cameraSetup = true;
    }

    protected void resetCamera() {
        this.cameraSetup = false;
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
        GlStateManager.pushMatrix();
        return lastID;
    }

    private void unbindFBO(int lastID) {
        GlStateManager.popMatrix();
        this.framebuffer.unbindFramebufferTexture();
        OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, lastID);
    }

    private RayTraceResult rayTrace(Vector3f hitPos) {
        Vec3d startPos = new Vec3d(this.camera.getPos().x, this.camera.getPos().y, this.camera.getPos().z);
        hitPos.scale(2); // Double view range to ensure pos can be seen.
        Vec3d endPos = new Vec3d((hitPos.x - startPos.x), (hitPos.y - startPos.y), (hitPos.z - startPos.z));
        return this.schema.getWorld().rayTraceBlocks(startPos, endPos);
    }

    public boolean isCameraSetup() {
        return cameraSetup;
    }

    public interface IRayTracer {

        RayTraceResult rayTrace(int screenX, int screenY);

        RayTraceResult rayTraceMousePos();
    }

    public interface ICamera {

        void setupCamera(Vector3f cameraPos, Vector3f lookAt);
    }
}
