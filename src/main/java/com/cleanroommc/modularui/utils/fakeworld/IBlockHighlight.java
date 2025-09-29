package com.cleanroommc.modularui.utils.fakeworld;

import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.Platform;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import org.jetbrains.annotations.Nullable;

public interface IBlockHighlight {


    void renderHighlight(BlockPos pos, EnumFacing side);

    default void renderHighlight(@Nullable RayTraceResult rayTraceResult) {
        if (rayTraceResult == null || rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK) return;
        renderHighlight(rayTraceResult.getBlockPos(), rayTraceResult.sideHit);
    }

    class FullHighlight implements IBlockHighlight {

        // this is magic
        private static final float[][] vertices = new float[6][12];

        static {
            int[][] intVertices = {
                    {1, 0, 0, 1, 0, 1, 0, 0, 1, 0, 0, 0},
                    {0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0},
                    {0, 1, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0},
                    {0, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1},
                    {0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0},
                    {1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 0},
                    //{1, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 0}
            };
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 12; j++) {
                    int v = intVertices[i][j];
                    vertices[i][j] = v == 1 ? 1.005f : -0.005f;
                }
            }
        }

        private int color = Color.withAlpha(Color.GREEN.main, 0.7f);
        private boolean allSides = true;

        @Override
        public void renderHighlight(BlockPos pos, EnumFacing side) {
            Platform.setupDrawColor();
            Color.setGlColor(this.color);
            Platform.startDrawing(Platform.DrawMode.QUADS, Platform.VertexFormat.POS, builder -> {
                if (this.allSides) {
                    for (int i = 0; i < 6; i++) {
                        buildFace(builder, vertices[i], pos);
                    }
                } else {
                    buildFace(builder, vertices[side.getIndex()], pos);
                }
            });
        }
    }

    static void buildFace(BufferBuilder builder, float[] vertices, BlockPos pos) {
        buildVertex(builder, vertices, pos, 0);
        buildVertex(builder, vertices, pos, 3);
        buildVertex(builder, vertices, pos, 6);
        buildVertex(builder, vertices, pos, 9);
    }

    static void buildVertex(BufferBuilder builder, float[] vertices, BlockPos pos, int i) {
        float x = vertices[i] + pos.getX();
        float y = vertices[i + 1] + pos.getY();
        float z = vertices[i + 2] + pos.getZ();
        builder.pos(x, y, z).endVertex();
    }
}
