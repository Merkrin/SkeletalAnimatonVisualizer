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

    private Vector3f cameraPosition = new Vector3f(-20, 20, -20);
    private Vector3f cameraRotation = new Vector3f(20, 140, 0);

    private int TARGET_FPS = 75;

    private int TARGET_UPS = 30;

    private float specularPower = 10;

    private boolean isVSyncEnabled = true;

    private int screenshotType = 1;

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

    public Vector3f getCameraPosition() {
        return cameraPosition;
    }

    public void setCameraPosition(Vector3f cameraPosition) {
        this.cameraPosition = cameraPosition;
    }

    public Vector3f getCameraRotation() {
        return cameraRotation;
    }

    public void setCameraRotation(Vector3f cameraRotation) {
        this.cameraRotation = cameraRotation;
    }

    public int getTARGET_FPS() {
        return TARGET_FPS;
    }

    public void setTARGET_FPS(int TARGET_FPS) {
        this.TARGET_FPS = TARGET_FPS;
    }

    public int getTARGET_UPS() {
        return TARGET_UPS;
    }

    public void setTARGET_UPS(int TARGET_UPS) {
        this.TARGET_UPS = TARGET_UPS;
    }

    public float getSpecularPower() {
        return specularPower;
    }

    public void setSpecularPower(float specularPower) {
        this.specularPower = specularPower;
    }

    public boolean isVSyncEnabled() {
        return isVSyncEnabled;
    }

    public void setVSyncEnabled(boolean VSyncEnabled) {
        isVSyncEnabled = VSyncEnabled;
    }

    public int getScreenshotType() {
        return screenshotType;
    }

    public void setScreenshotType(int screenshotType) {
        this.screenshotType = screenshotType;
    }
}
