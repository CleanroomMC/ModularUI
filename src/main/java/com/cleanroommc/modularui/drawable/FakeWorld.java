package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.fakeworld.FBOWorldSceneRenderer;
import com.cleanroommc.modularui.utils.fakeworld.TrackedDummyWorld;
import com.cleanroommc.modularui.utils.fakeworld.WorldSceneRenderer;

import net.minecraft.client.shader.Framebuffer;

public class FakeWorld implements IDrawable {

    private static Framebuffer FBO;
    private final WorldSceneRenderer renderer;

    public FakeWorld(WorldSceneRenderer renderer) {
        this.renderer = renderer;
    }

    public FakeWorld(TrackedDummyWorld world) {
        if (FBO == null) {
            FBO = new Framebuffer(1080, 1080, true);
        }
        this.renderer = new FBOWorldSceneRenderer(world, FBO);
        double pitch = Math.PI / 4;
        double yaw = Math.PI / 4;
        this.renderer.setCameraLookAt(world.getCenter(), 10, pitch, yaw);
        this.renderer.addRenderedBlocks(world.renderedBlocks, null);

        /*this.renderer.setBeforeWorldRender(renderer -> {
           renderer.setCameraLookAt(world.getCenter(), world.getMaxSize() / 2 + 2, pitch, yaw);
        });*/
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        this.renderer.render(x, y, width, height, context.getAbsMouseX(), context.getAbsMouseY());
    }
}
