package ru.hse.engine.wrappers;

import org.joml.Vector3f;
import ru.hse.graphics.lighting.DirectionalLight;
import ru.hse.graphics.lighting.PointLight;

public class SceneLight {
    private Vector3f ambientLight;

    private PointLight[] pointLightList;

    private DirectionalLight directionalLight;

    public Vector3f getAmbientLight() {
        return ambientLight;
    }

    public void setAmbientLight(Vector3f ambientLight) {
        this.ambientLight = ambientLight;
    }

    public PointLight[] getPointLightList() {
        return pointLightList;
    }

    public void setPointLightList(PointLight[] pointLightList) {
        this.pointLightList = pointLightList;
    }

    public DirectionalLight getDirectionalLight() {
        return directionalLight;
    }

    public void setDirectionalLight(DirectionalLight directionalLight) {
        this.directionalLight = directionalLight;
    }
}
