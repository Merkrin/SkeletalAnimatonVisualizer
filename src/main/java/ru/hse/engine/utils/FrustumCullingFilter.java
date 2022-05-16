package ru.hse.engine.utils;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import ru.hse.engine.MeshedItem;
import ru.hse.graphics.model.Mesh;

import java.util.List;
import java.util.Map;

public class FrustumCullingFilter {
    private final Matrix4f projectionViewMatrix;

    private final FrustumIntersection frustumIntersection;

    public FrustumCullingFilter() {
        projectionViewMatrix = new Matrix4f();
        frustumIntersection = new FrustumIntersection();
    }

    public void updateFrustum(Matrix4f projectionMatrix, Matrix4f viewMatrix) {
        projectionViewMatrix.set(projectionMatrix);
        projectionViewMatrix.mul(viewMatrix);

        frustumIntersection.set(projectionViewMatrix);
    }

    public void filter(Map<? extends Mesh, List<MeshedItem>> mapMesh) {
        for (Map.Entry<? extends Mesh, List<MeshedItem>> entry : mapMesh.entrySet()) {
            List<MeshedItem> gameItems = entry.getValue();
            filter(gameItems, entry.getKey().getBoundingRadius());
        }
    }

    public void filter(List<MeshedItem> meshedItems, float meshBoundingRadius) {
        float boundingRadius;
        Vector3f position;

        for (MeshedItem gameItem : meshedItems) {
            if (!gameItem.isDisableFrustumCulling()) {
                boundingRadius = gameItem.getScale() * meshBoundingRadius;
                position = gameItem.getPosition();
                gameItem.setInsideFrustum(insideFrustum(position.x, position.y, position.z, boundingRadius));
            }
        }
    }

    public boolean insideFrustum(float x0, float y0, float z0, float boundingRadius) {
        return frustumIntersection.testSphere(x0, y0, z0, boundingRadius);
    }
}
