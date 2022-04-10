package ru.hse.core.utils;

import org.joml.Vector3f;

public class Settings {
    private static final Settings INSTANCE = new Settings();

    private Settings() {
    }

    public static Settings getInstance() {
        return INSTANCE;
    }

    private Vector3f ambientLight = new Vector3f(0.3f, 0.3f, 0.3f);
    private Vector3f skyboxLight = new Vector3f(1.0f, 1.0f, 1.0f);

    private Vector3f lightDirection = new Vector3f(0, 1, 1);
    private Vector3f lightColor = new Vector3f(1, 1, 1);

    private float lightIntensity = 1.0f;

    private float currentLightAngle = 90f;

    private int animationFramesPerSecond = 2;

    public Vector3f getAmbientLight() {
        return ambientLight;
    }

    public void setAmbientLight(Vector3f ambientLight) {
        this.ambientLight = ambientLight;
    }

    public Vector3f getSkyboxLight() {
        return skyboxLight;
    }

    public void setSkyboxLight(Vector3f skyboxLight) {
        this.skyboxLight = skyboxLight;
    }

    public float getLightIntensity() {
        return lightIntensity;
    }

    public void setLightIntensity(float lightIntensity) {
        this.lightIntensity = lightIntensity;
    }

    public Vector3f getLightDirection() {
        return lightDirection;
    }

    public void setLightDirection(Vector3f lightDirection) {
        this.lightDirection = lightDirection;
    }

    public Vector3f getLightColor() {
        return lightColor;
    }

    public void setLightColor(Vector3f lightColor) {
        this.lightColor = lightColor;
    }

    public float getCurrentLightAngle() {
        return currentLightAngle;
    }

    public void setCurrentLightAngle(float currentLightAngle) {
        this.currentLightAngle = currentLightAngle;
    }

    public int getAnimationFramesPerSecond() {
        return animationFramesPerSecond;
    }

    public void setAnimationFramesPerSecond(int animationFramesPerSecond) {
        this.animationFramesPerSecond = animationFramesPerSecond;
    }
}
