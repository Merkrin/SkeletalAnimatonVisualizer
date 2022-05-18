package ru.hse.engine.animation.structure;

/**
 * Skeleton's vertex weight representation class.
 */
public class VertexWeight {
    private final int boneId;

    private final int vertexId;

    private float weight;

    /**
     * The class' constructor.
     *
     * @param boneId   id of the related bone
     * @param vertexId id of the related vertex
     * @param weight   weight of the vertex
     */
    public VertexWeight(int boneId, int vertexId, float weight) {
        this.boneId = boneId;
        this.vertexId = vertexId;
        this.weight = weight;
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
     * Vertex id getter.
     *
     * @return vertex id
     */
    public int getVertexId() {
        return vertexId;
    }

    /**
     * Weight getter.
     *
     * @return weight
     */
    public float getWeight() {
        return weight;
    }

    /**
     * Weight setter.
     *
     * @param weight new weight
     */
    public void setWeight(float weight) {
        this.weight = weight;
    }
}
