package ru.hse.engine.shadows;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import ru.hse.engine.utils.Window;
import ru.hse.graphics.Transformation;
import ru.hse.graphics.lighting.DirectionalLight;

/**
 * Shadow cascade class.
 */
public class ShadowCascade {
    private static final int FRUSTUM_CORNERS = 8;

    private final Matrix4f projectionViewMatrix;

    private final Matrix4f orthogonalProjectionMatrix;

    private final Matrix4f lightViewMatrix;

    private final Vector3f centroid;

    private final Vector3f[] frustumCorners;

    private final float zNear;

    private final float zFar;

    private final Vector4f temporalVector;

    /**
     * The class' constructor.
     *
     * @param zNear z-coordinate of the near plane
     * @param zFar  z-coordinate of the far plane
     */
    public ShadowCascade(float zNear, float zFar) {
        this.zNear = zNear;
        this.zFar = zFar;
        this.projectionViewMatrix = new Matrix4f();
        this.orthogonalProjectionMatrix = new Matrix4f();
        this.centroid = new Vector3f();
        this.lightViewMatrix = new Matrix4f();
        this.frustumCorners = new Vector3f[FRUSTUM_CORNERS];

        for (int i = 0; i < FRUSTUM_CORNERS; i++)
            frustumCorners[i] = new Vector3f();

        temporalVector = new Vector4f();
    }

    /**
     * Light view matrix getter
     *
     * @return light view matrix
     */
    public Matrix4f getLightViewMatrix() {
        return lightViewMatrix;
    }

    /**
     * Orthogonal projection matrix getter.
     *
     * @return Orthogonal projection matrix
     */
    public Matrix4f getOrthogonalProjectionMatrix() {
        return orthogonalProjectionMatrix;
    }

    /**
     * Update shadows.
     *
     * @param window     current window
     * @param viewMatrix view matrix
     * @param light      active light
     */
    public void update(Window window, Matrix4f viewMatrix, DirectionalLight light) {
        float aspectRatio = (float) window.getWidth() / (float) window.getHeight();

        projectionViewMatrix.setPerspective(Window.FOV, aspectRatio, zNear, zFar);
        projectionViewMatrix.mul(viewMatrix);

        float maxZ = Float.MIN_VALUE;
        float minZ = Float.MAX_VALUE;

        for (int i = 0; i < FRUSTUM_CORNERS; i++) {
            Vector3f corner = frustumCorners[i];

            corner.set(0, 0, 0);

            projectionViewMatrix.frustumCorner(i, corner);

            centroid.add(corner);
            centroid.div(8.0f);

            minZ = Math.min(minZ, corner.z);
            maxZ = Math.max(maxZ, corner.z);
        }

        Vector3f lightDirection = light.getDirection();
        Vector3f lightPositionIncrement = new Vector3f().set(lightDirection);

        float distance = maxZ - minZ;

        lightPositionIncrement.mul(distance);

        Vector3f lightPosition = new Vector3f();

        lightPosition.set(centroid);
        lightPosition.add(lightPositionIncrement);

        updateLightViewMatrix(lightDirection, lightPosition);

        updateLightProjectionMatrix();
    }

    /**
     * Update light view matrix.
     *
     * @param lightDirection light direction
     * @param lightPosition  light position
     */
    private void updateLightViewMatrix(Vector3f lightDirection, Vector3f lightPosition) {
        float lightAngleX = (float) Math.toDegrees(Math.acos(lightDirection.z));
        float lightAngleY = (float) Math.toDegrees(Math.asin(lightDirection.x));
        float lightAngleZ = 0;

        Transformation.updateGenericViewMatrix(lightPosition,
                new Vector3f(lightAngleX, lightAngleY, lightAngleZ),
                lightViewMatrix);
    }

    /**
     * Update light projection matrix.
     */
    private void updateLightProjectionMatrix() {
        float minX = Float.MAX_VALUE;
        float maxX = -Float.MIN_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = -Float.MIN_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxZ = -Float.MIN_VALUE;

        for (int i = 0; i < FRUSTUM_CORNERS; i++) {
            Vector3f corner = frustumCorners[i];

            temporalVector.set(corner, 1);
            temporalVector.mul(lightViewMatrix);

            minX = Math.min(temporalVector.x, minX);
            maxX = Math.max(temporalVector.x, maxX);
            minY = Math.min(temporalVector.y, minY);
            maxY = Math.max(temporalVector.y, maxY);
            minZ = Math.min(temporalVector.z, minZ);
            maxZ = Math.max(temporalVector.z, maxZ);
        }

        float zDistance = maxZ - minZ;

        orthogonalProjectionMatrix.setOrtho(minX, maxX, minY, maxY, 0, zDistance);
    }

}
