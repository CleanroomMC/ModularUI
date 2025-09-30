package com.cleanroommc.modularui;

import com.cleanroommc.modularui.utils.Vector3f;
import com.cleanroommc.modularui.utils.fakeworld.Camera;

import org.junit.jupiter.api.AssertionFailureBuilder;
import org.junit.jupiter.api.Test;

public class CameraTest {

    static final float PI = (float) Math.PI;
    static final float PI2 = 2f * PI;
    static final float PI_HALF = PI / 2f;
    static final float PI_QUART = PI / 4f;
    static final Vector3f temp = new Vector3f();

    @Test
    void testCamera() {
        Camera a = new Camera();
        Camera b = new Camera();
        a.setAngleKeepLookAt(1, PI_HALF, 0);
        assertPos(a, 0, 0, 1);
        a.setAngleKeepLookAt(1, PI, 0);
        assertPos(a, -1, 0, 0);
        a.setAngleKeepLookAt(1, PI + PI_HALF, 0);
        assertPos(a, 0, 0, -1);
        a.setAngleKeepLookAt(1, PI2, 0);
        assertPos(a, 1, 0, 0);
    }

    void assertPos(Camera c, float x, float y, float z) {
        temp.set(x, y, z);
        Vector3f v = c.getPos();
        if (!areEqual(v, temp)) {
            AssertionFailureBuilder.assertionFailure()
                    .message("Camera pos does not match")
                    .actual(v)
                    .expected(temp)
                    .buildAndThrow();
        }
    }

    void assertLookAt(Camera c, float x, float y, float z) {
        temp.set(x, y, z);
        Vector3f v = c.getLookAt();
        if (!areEqual(v, temp)) {
            AssertionFailureBuilder.assertionFailure()
                    .message("Camera look at does not match")
                    .actual(v)
                    .expected(temp)
                    .buildAndThrow();
        }
    }

    boolean areEqual(Vector3f a, Vector3f b) {
        return areFloatEqual(a.x, b.x) && areFloatEqual(a.y, b.y) && areFloatEqual(a.z, b.z);
    }

    boolean areFloatEqual(float a, float b) {
        return Math.abs(a - b) < 0.00000001f;
    }
}
