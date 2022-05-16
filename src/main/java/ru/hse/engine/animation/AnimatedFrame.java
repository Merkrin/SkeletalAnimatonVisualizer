package ru.hse.engine.animation;

import org.joml.Matrix4f;
import ru.hse.core.utils.Constants;

import java.util.Arrays;

public class AnimatedFrame {
    private static final Matrix4f IDENTITY_MATRIX = new Matrix4f();

    private final Matrix4f[] jointMatrices;

    public AnimatedFrame() {
        jointMatrices = new Matrix4f[Constants.MAXIMAL_JOINTS_AMOUNT];
        Arrays.fill(jointMatrices, IDENTITY_MATRIX);
    }

    public Matrix4f[] getJointMatrices() {
        return jointMatrices;
    }

    public void setMatrix(int pos, Matrix4f jointMatrix) {
        jointMatrices[pos] = jointMatrix;
    }
}
