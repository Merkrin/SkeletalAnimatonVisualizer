package ru.hse.engine.utils;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import ru.hse.engine.MeshedItem;
import ru.hse.graphics.model.Mesh;

import java.util.List;
import java.util.Map;

public class FrustumCullingFilter {
    private final Matrix4f prjViewMatrix;

    private final FrustumIntersection frustumInt;

    public FrustumCullingFilter() {
        prjViewMatrix = new Matrix4f();
        frustumInt = new FrustumIntersection();
    }

    public void updateFrustum(Matrix4f projMatrix, Matrix4f viewMatrix) {
        // Calculate projection view matrix
        prjViewMatrix.set(projMatrix);
        prjViewMatrix.mul(viewMatrix);
        // Update frustum intersection class
        frustumInt.set(prjViewMatrix);
    }

    public void filter(Map<? extends Mesh, List<MeshedItem>> mapMesh) {
        for (Map.Entry<? extends Mesh, List<MeshedItem>> entry : mapMesh.entrySet()) {
            List<MeshedItem> gameItems = entry.getValue();
            filter(gameItems, entry.getKey().getBoundingRadius());
        }
    }

    public void filter(List<MeshedItem> gameItems, float meshBoundingRadius) {
        float boundingRadius;
        Vector3f pos;
        for (MeshedItem gameItem : gameItems) {
            if (!gameItem.isDisableFrustumCulling()) {
                boundingRadius = gameItem.getScale() * meshBoundingRadius;
                pos = gameItem.getPosition();
                gameItem.setInsideFrustum(insideFrustum(pos.x, pos.y, pos.z, boundingRadius));
            }
        }
    }

    public boolean insideFrustum(float x0, float y0, float z0, float boundingRadius) {
        return frustumInt.testSphere(x0, y0, z0, boundingRadius);
    }
}
