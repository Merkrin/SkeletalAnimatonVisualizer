package ru.hse.engine.animation.structure;

import org.joml.Matrix4f;

/**
 * Skeleton's bone representation class.
 */
public class Bone {
    private final int boneId;

    private final String boneName;

    private final Matrix4f offsetMatrix;

    /**
     * The class' constructor.
     *
     * @param boneId       id of the bone
     * @param boneName     name of the bone
     * @param offsetMatrix offset matrix of the bone
     */
    public Bone(int boneId, String boneName, Matrix4f offsetMatrix) {
        this.boneId = boneId;
        this.boneName = boneName;
        this.offsetMatrix = offsetMatrix;
    }

    /**
     * Bone id getter.
     *
     * @return bone id
     */
    public int getBoneId() {
        return boneId;
    }

    /**
     * Bone name getter.
     *
     * @return bone name
     */
    public String getBoneName() {
        return boneName;
    }

    /**
     * Bone offset matrix getter.
     *
     * @return bone offset matrix
     */
    public Matrix4f getOffsetMatrix() {
        return offsetMatrix;
    }
}
