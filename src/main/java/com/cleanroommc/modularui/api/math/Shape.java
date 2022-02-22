package com.cleanroommc.modularui.api.math;

import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Shape {

    private float[] vertices;
    private int gl_begin_mode;

    public Shape(int gl_begin_mode, float... vertices) {
        this.vertices = vertices;
        this.gl_begin_mode = gl_begin_mode;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Shape line(Pos2d p1, float thiccness) {
        float angle = Pos2d.zero().angle(p1);
        float length = (float) Pos2d.zero().distance(p1);
        return builder().beginnMode(GL11.GL_QUAD_STRIP).changeBuildMode(BuildMode.INCREMENT)
                .vertex(Pos2d.polar(angle - 90, thiccness / 2))
                .vertex(Pos2d.polar(angle, length))
                .vertex(Pos2d.polar(angle + 90, thiccness))
                .vertex(Pos2d.polar(angle + 180, length))
                .vertex(Pos2d.polar(angle + 270, thiccness / 2))
                .build();
    }

    public static Shape rect(Size size) {
        return builder().beginnMode(GL11.GL_QUADS)
                .vertex(Pos2d.zero())
                .vertexX(size.width)
                .vertexY(size.height)
                .vertexX(0)
                .vertexY(0)
                .build();
    }


    @Deprecated // doesn't work very well
    public static Shape regularPolygon(int vertices, float diameter) {
        //float angle = 360f / triangles;
        //float sideLength = (float) (2 * (diameter / 2 * Math.sin(Math.toRadians(angle / 2))));
        Builder builder = builder().beginnMode(GL11.GL_QUAD_STRIP);
        for (int i = 0; i < vertices; i++) {

            float angle = (float) (2 * Math.PI * i / vertices);
            float x = (float) (diameter / 2 * Math.cos(angle)), y = (float) (diameter / 2 * Math.sin(angle));
            builder.vertex(Pos2d.cartesian(x, y));

            /*builder.changeBuildMode(BuildMode.ABSOLUTE)
                .vertex(Point.ZERO)
                //.changeBuildMode(BuildMode.INCREMENT)
                .vertex(Point.polar(angle, diameter / 2));
            angle += 360f / triangles;*/
            //builder.vertex(Point.polar(angle, sideLength));
        }
        return builder.build();
    }

    public int vertices() {
        checkVertices();
        return vertices.length / 2;
    }

    public void forEachVertex(Consumer<Pos2d> consumer) {
        checkVertices();
        for (int i = 1; i < vertices.length; i += 2) {
            consumer.accept(new Pos2d(vertices[i - 1], vertices[i]));
        }
    }

    public void forEachVertex(BiConsumer<Float, Float> consumer) {
        checkVertices();
        for (int i = 1; i < vertices.length; i += 2) {
            consumer.accept(vertices[i - 1], vertices[i]);
        }
    }

    private void checkVertices() {
        if (vertices.length % 2 != 0) {
            throw new IllegalStateException("Vertices can not have an odd amount off values");
        }
    }

    public Size calculateSize() {
        float x0 = 0, x1 = 0, y0 = 0, y1 = 0;
        checkVertices();
        for (int i = 1; i < vertices.length; i += 2) {
            float x = vertices[i - 1], y = vertices[i];
            x0 = Math.min(x0, x);
            x1 = Math.max(x1, x);
            y0 = Math.min(y0, y);
            y1 = Math.max(y1, y);
        }
        return new Size(x1 - x0, y1 - y0);
    }

    public int getGl_begin_mode() {
        return gl_begin_mode;
    }

    public static class Builder {

        BuildMode buildMode;
        List<Float> vertexList = new ArrayList<>();
        Pos2d lastPos;
        int beginnMode;

        private Builder() {
            this.buildMode = BuildMode.ABSOLUTE;
            this.lastPos = Pos2d.zero();
            beginnMode = GL11.GL_TRIANGLES;
        }

        public Builder beginnMode(int mode) {
            this.beginnMode = mode;
            return this;
        }

        public Builder changeBuildMode(BuildMode buildMode) {
            this.buildMode = buildMode;
            return this;
        }

        public Builder vertexX(float x) {
            float y = buildMode == BuildMode.ABSOLUTE ? lastPos.getY() : 0; // make sure y doesn't move
            return vertex(Pos2d.cartesian(x, y));
        }

        public Builder vertexY(float y) {
            float x = buildMode == BuildMode.ABSOLUTE ? lastPos.getX() : 0; // make sure x doesn't move
            return vertex(Pos2d.cartesian(x, y));
        }

        public Builder vertex(float x, float y) {
            return vertex(new Pos2d(x, y));
        }

        public Builder polarVertex(float angle, float length) {
            return vertex(Pos2d.polar(angle, length));
        }

        public Builder vertex(Pos2d p) {
            Pos2d p1 = buildMode == BuildMode.INCREMENT ? lastPos.add(p) : p;
            vertexList.add(p1.getX());
            vertexList.add(p1.getY());
            this.lastPos = p1;
            return this;
        }

        public Shape build() {
            float[] vertices = new float[vertexList.size()];
            for (int i = 0; i < vertices.length; i++) {
                vertices[i] = vertexList.get(i);
            }
            vertexList = null;
            return new Shape(beginnMode, vertices);
        }
    }

    public enum BuildMode {

        /**
         * The next vertices will refer to 0, 0
         */
        ABSOLUTE,

        /**
         * The next vertices will refer to the last set vertex
         * (0, 0 if it is the first vertex)
         */
        INCREMENT
    }
}
