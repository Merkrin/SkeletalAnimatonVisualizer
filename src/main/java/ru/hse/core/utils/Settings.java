package ru.hse.core.utils;

import org.joml.Vector3f;
import org.joml.Vector4f;
import ru.hse.core.utils.exceptions.InvalidSettingException;
import ru.hse.graphics.lighting.PointLight;

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

    private int animationFramesPerSecond = -1;

    private Vector3f cameraPosition = new Vector3f(-20, 20, -20);
    private Vector3f cameraRotation = new Vector3f(20, 140, 0);

    private float specularPower = 10;

    private boolean isVSyncEnabled = true;

    private int screenshotType = 1;

    private boolean isModelAnimated = true;

    private String pathToModel;
    private String pathToTexture;

    private float scale = 1.0f;

    private float skyboxScale = 100.0f;

    private Vector4f skyboxColor = new Vector4f(0.65f, 0.65f, 0.65f, 1.0f);

    private String skyboxPath = "";

    private PointLight[] pointLights = {};

    public Vector3f getAmbientLight() {
        return ambientLight;
    }

    public String getAmbientLightAsString() {
        return ambientLight.x + "," + ambientLight.y + "," + ambientLight.z;
    }

    public void setAmbientLight(Vector3f ambientLight) {
        ambientLight.normalize();
        this.ambientLight = ambientLight;
    }

    public Vector3f getSkyboxLight() {
        return skyboxLight;
    }

    public String getSkyboxLightAsString() {
        return skyboxLight.x + "," + skyboxLight.y + "," + skyboxLight.z;
    }

    public void setSkyboxLight(Vector3f skyboxLight) {
        skyboxLight.normalize();
        this.skyboxLight = skyboxLight;
    }

    public float getLightIntensity() {
        return lightIntensity;
    }

    public void setLightIntensity(float lightIntensity) throws InvalidSettingException {
        if (lightIntensity > 1.0f || lightIntensity < 0.0f)
            throw new InvalidSettingException("Invalid light intensity.");

        this.lightIntensity = lightIntensity;
    }

    public Vector3f getLightDirection() {
        return lightDirection;
    }

    public String getLightDirectionAsString() {
        return lightDirection.x + "," + lightDirection.y + "," + lightDirection.z;
    }

    public void setLightDirection(Vector3f lightDirection) {
        lightDirection.normalize();
        this.lightDirection = lightDirection;
    }

    public Vector3f getLightColor() {
        return lightColor;
    }

    public String getLightColorAsString() {
        return lightColor.x + "," + lightColor.y + "," + lightColor.z;
    }

    public void setLightColor(Vector3f lightColor) throws InvalidSettingException {
        if (lightColor.x > 1.0f || lightColor.x < 0.0f ||
                lightColor.y > 1.0f || lightColor.y < 0.0f ||
                lightColor.z > 1.0f || lightColor.z < 0.0f)
            throw new InvalidSettingException("Invalid color value");

        this.lightColor = lightColor;
    }

    public float getCurrentLightAngle() {
        return currentLightAngle;
    }

    public void setCurrentLightAngle(float currentLightAngle) throws InvalidSettingException {
        if (currentLightAngle < 0.0f || currentLightAngle > 180.0f)
            throw new InvalidSettingException("Invalid light angle.");

        this.currentLightAngle = currentLightAngle;
    }

    public int getAnimationFramesPerSecond() {
        return animationFramesPerSecond;
    }

    public void setAnimationFramesPerSecond(int animationFramesPerSecond) throws InvalidSettingException {
        if (animationFramesPerSecond < 1 && animationFramesPerSecond != -1)
            throw new InvalidSettingException("Invalid animations frames per second value.");

        this.animationFramesPerSecond = animationFramesPerSecond;
    }

    public Vector3f getCameraPosition() {
        return cameraPosition;
    }

    public String getCameraPositionAsString() {
        return cameraPosition.x + "," + cameraPosition.y + "," + cameraPosition.z;
    }

    public void setCameraPosition(Vector3f cameraPosition) {
        this.cameraPosition = cameraPosition;
    }

    public Vector3f getCameraRotation() {
        return cameraRotation;
    }

    public String getCameraRotationAsString() {
        return cameraRotation.x + "," + cameraRotation.y + "," + cameraRotation.z;
    }

    public void setCameraRotation(Vector3f cameraRotation) {
        this.cameraRotation = cameraRotation;
    }

    public float getSpecularPower() {
        return specularPower;
    }

    public void setSpecularPower(float specularPower) throws InvalidSettingException {
        if(specularPower < 0 || specularPower > 100)
            throw new InvalidSettingException("Invalid specular power value.");

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

    public void setScreenshotType(int screenshotType) throws InvalidSettingException {
        if (screenshotType < 1 || screenshotType > 3)
            throw new InvalidSettingException("Invalid screenshot type value.");

        this.screenshotType = screenshotType;
    }

    public boolean isModelAnimated() {
        return isModelAnimated;
    }

    public void setModelAnimated(boolean modelAnimated) {
        isModelAnimated = modelAnimated;
    }

    public String getPathToModel() {
        return pathToModel;
    }

    public void setPathToModel(String pathToModel) {
        this.pathToModel = pathToModel;
    }

    public String getPathToTexture() {
        return pathToTexture;
    }

    public void setPathToTexture(String pathToTexture) {
        this.pathToTexture = pathToTexture;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) throws InvalidSettingException {
        if (scale <= 0.0f)
            throw new InvalidSettingException("Invalid scale value.");

        this.scale = scale;
    }

    public float getSkyboxScale() {
        return skyboxScale;
    }

    public void setSkyboxScale(float skyboxScale) throws InvalidSettingException {
        if (skyboxScale <= 0.0f)
            throw new InvalidSettingException("Invalid skybox scale value.");

        this.skyboxScale = skyboxScale;
    }

    public String getSkyboxPath() {
        return skyboxPath;
    }

    public void setSkyboxPath(String skyboxPath) {
        this.skyboxPath = skyboxPath;
    }

    public PointLight[] getPointLights() {
        return pointLights;
    }

    public String getPointLightPositions() {
        String positions = "";

        for (int i = 0; i < pointLights.length; i++) {
            positions += pointLights[i].getPosition().x + "," +
                    pointLights[i].getPosition().y + "," +
                    pointLights[i].getPosition().z;

            if (i != pointLights.length - 1)
                positions += ",";
        }

        return positions;
    }

    public String getPointLightColors() {
        String colors = "";

        for (int i = 0; i < pointLights.length; i++) {
            colors += pointLights[i].getColor().x + "," +
                    pointLights[i].getColor().y + "," +
                    pointLights[i].getColor().z;

            if (i != pointLights.length - 1)
                colors += ",";
        }

        return colors;
    }

    public void setPointLights(PointLight[] pointLights) {
        this.pointLights = pointLights;
    }

    public Vector4f getSkyboxColor() {
        return skyboxColor;
    }

    public void setSkyboxColor(Vector4f skyboxColor) throws InvalidSettingException {
        if (skyboxColor.x > 1.0f || skyboxColor.x < 0.0f ||
                skyboxColor.y > 1.0f || skyboxColor.y < 0.0f ||
                skyboxColor.z > 1.0f || skyboxColor.z < 0.0f ||
                skyboxColor.w > 1.0f || skyboxColor.w < 0.0f)
            throw new InvalidSettingException("Invalid color value");

        this.skyboxColor = skyboxColor;
    }
}
