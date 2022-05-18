package ru.hse.engine.animation;

import org.joml.Matrix4f;
import ru.hse.core.utils.Constants;

import java.util.Arrays;

/**
 * Class for animation frame representation.
 */
public class AnimatedFrame {
    private static final Matrix4f IDENTITY_MATRIX = new Matrix4f();

    private final Matrix4f[] jointMatrices;

    /**
     * The class' constructor.
     */
    public AnimatedFrame() {
        jointMatrices = new Matrix4f[Constants.MAXIMAL_JOINTS_AMOUNT];
        Arrays.fill(jointMatrices, IDENTITY_MATRIX);
    }

    /**
     * Joint matrices getter.
     *
     * @return joint matrices
     */
    public Matrix4f[] getJointMatrices() {
        return jointMatrices;
    }

    /**
     * Setter of the given joint matrix.
     *
     * @param position    joint matrix position in the array
     * @param jointMatrix new joint matrix
     */
    public void setJointMatrix(int position, Matrix4f jointMatrix) {
        jointMatrices[position] = jointMatrix;
    }
}
