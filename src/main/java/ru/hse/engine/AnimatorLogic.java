package ru.hse.engine;

import org.joml.Vector2f;
import org.joml.Vector3f;
import ru.hse.core.Renderer;
import ru.hse.core.utils.ArgumentsParser;
import ru.hse.core.utils.Constants;
import ru.hse.core.utils.Settings;
import ru.hse.engine.animation.AnimatedItem;
import ru.hse.engine.animation.Animation;
import ru.hse.engine.loaders.AnimatedMeshLoader;
import ru.hse.engine.loaders.StaticMeshLoader;
import ru.hse.engine.utils.AnimationTimer;
import ru.hse.engine.utils.MouseInput;
import ru.hse.engine.utils.screenshots.ScreenCapture;
import ru.hse.engine.utils.Window;
import ru.hse.engine.wrappers.Scene;
import ru.hse.engine.wrappers.SceneLight;
import ru.hse.graphics.lighting.DirectionalLight;
import ru.hse.graphics.model.Mesh;
import ru.hse.graphics.skybox.Skybox;
import ru.hse.graphics.utils.GraphicsUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public class AnimatorLogic implements Logic {
    private static final Settings SETTINGS = Settings.getInstance();

    private final AnimationTimer timer;

    private final Camera camera;

    private final Renderer renderer;

    private final ScreenCapture screenCapture;

    private final Vector3f cameraPositionIncrement;

    // seconds for one animation frame
    private final double timePerAnimationFrame;

    private Animation animation;

    private Scene scene;

    private float lightAngleIncrement;
    private float currentLightAngle;

    private boolean isFirstTime;
    private boolean sceneChanged;

    public AnimatorLogic() {
        cameraPositionIncrement = new Vector3f(0.0f, 0.0f, 0.0f);

        camera = new Camera();

        renderer = new Renderer();

        screenCapture = new ScreenCapture();

        lightAngleIncrement = 0;
        currentLightAngle = SETTINGS.getCurrentLightAngle();

        isFirstTime = true;

        timePerAnimationFrame = 1.0 / SETTINGS.getAnimationFramesPerSecond();

        timer = new AnimationTimer();
    }

    @Override
    public void init(Window window) throws Exception {
        renderer.init();

        scene = new Scene();

        MeshedItem item;

        if (SETTINGS.isModelAnimated()) {
            item = AnimatedMeshLoader.loadAnimatedItem(SETTINGS.getPathToModel(), SETTINGS.getPathToTexture());

            animation = ((AnimatedItem) item).getCurrentAnimation();
        } else {
            item = new MeshedItem(StaticMeshLoader.load(SETTINGS.getPathToModel(), SETTINGS.getPathToTexture()));
        }

        item.setScale(SETTINGS.getScale());

        scene.setGameItems(new MeshedItem[]{item});

        scene.setRenderShadows(true);

        Skybox skyBox = new Skybox(SETTINGS.getSkyboxPath(), SETTINGS.getSkyboxColor());
        skyBox.setScale(SETTINGS.getSkyboxScale());
        scene.setSkyBox(skyBox);

        setupLights();

        camera.setPosition(SETTINGS.getCameraPosition());
        camera.setRotation(SETTINGS.getCameraRotation());

        screenCapture.initialize(window);

        timer.init();
    }

    private void setupLights() {
        SceneLight sceneLight = new SceneLight();

        scene.setSceneLight(sceneLight);

        // Ambient Light
        sceneLight.setAmbientLight(SETTINGS.getAmbientLight());
        sceneLight.setSkyBoxLight(SETTINGS.getSkyboxLight());

        // Directional Light
        sceneLight.setDirectionalLight(new DirectionalLight(SETTINGS.getLightColor(),
                SETTINGS.getLightDirection(),
                SETTINGS.getLightIntensity()));

        // Point lights
        sceneLight.setPointLightList(SETTINGS.getPointLights());
    }

    @Override
    public void input(Window window, MouseInput mouseInput) {
        sceneChanged = false;

        cameraPositionIncrement.set(0, 0, 0);

        if (window.isKeyPressed(GLFW_KEY_W)) {
            sceneChanged = true;

            cameraPositionIncrement.z = -1;
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            sceneChanged = true;

            cameraPositionIncrement.z = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            sceneChanged = true;

            cameraPositionIncrement.x = -1;
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            sceneChanged = true;

            cameraPositionIncrement.x = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_Z)) {
            sceneChanged = true;

            cameraPositionIncrement.y = -1;
        } else if (window.isKeyPressed(GLFW_KEY_X)) {
            sceneChanged = true;

            cameraPositionIncrement.y = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_LEFT)) {
            sceneChanged = true;

            lightAngleIncrement -= 0.05f;
        } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
            sceneChanged = true;

            lightAngleIncrement += 0.05f;
        } else {
            sceneChanged = true;

            lightAngleIncrement = 0;
        }
        if (window.isKeyPressed(GLFW_KEY_SPACE)) {
            if (SETTINGS.isModelAnimated()) {
                sceneChanged = true;

                float elapsedTime = timer.getElapsedTime();

                if (animation != null && timePerAnimationFrame > 0 && elapsedTime >= timePerAnimationFrame) {
                    timer.updateLastLoopTime();

                    animation.nextFrame();
                } else if (animation != null)
                    animation.nextFrame();
            }
        }
        if (window.isKeyPressed(GLFW_KEY_P)) {
            screenCapture.run();
        }
        if (window.isKeyPressed(GLFW_KEY_C)) {
            try {
                saveSettings();
            } catch (IOException e) {
                System.out.println("Unable to save file.");
            }
        }
        if (window.isKeyPressed(GLFW_KEY_H)) {
            showHelp();
        }
        if (window.isKeyPressed(GLFW_KEY_J)) {
            showAbout();
        }

        GraphicsUtils.setWireframe(window.isKeyPressed(GLFW_KEY_G));
    }

    @Override
    public void update(float interval, MouseInput mouseInput) {
        if (mouseInput.isRightButtonPressed()) {
            // Update camera based on mouse
            Vector2f rotationVector = mouseInput.getDisplacementVector();

            camera.moveRotation(rotationVector.x * Constants.MOUSE_SENSITIVITY,
                    rotationVector.y * Constants.MOUSE_SENSITIVITY,
                    0);

            sceneChanged = true;
        }

        // Update camera position based on keyboard
        camera.movePosition(cameraPositionIncrement.x * Constants.CAMERA_POSITION_STEP,
                cameraPositionIncrement.y * Constants.CAMERA_POSITION_STEP,
                cameraPositionIncrement.z * Constants.CAMERA_POSITION_STEP);

        currentLightAngle += lightAngleIncrement;

        if (currentLightAngle < 0)
            currentLightAngle = 0;
        else if (currentLightAngle > 180)
            currentLightAngle = 180;

        float zValue = (float) Math.cos(Math.toRadians(currentLightAngle));
        float yValue = (float) Math.sin(Math.toRadians(currentLightAngle));

        Vector3f lightDirection = this.scene.getSceneLight().getDirectionalLight().getDirection();

        lightDirection.x = 0;
        lightDirection.y = yValue;
        lightDirection.z = zValue;
        lightDirection.normalize();

        // Update view matrix
        camera.updateViewMatrix();
    }

    private void saveSettings() throws IOException {
        String cli = ArgumentsParser.createCommandLine();

        File file = getFileToSave(".sav");

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(cli);

        writer.close();
    }

    private static File getFileToSave(String fileFormat) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
        LocalDateTime now = LocalDateTime.now();

        return new File(dtf.format(now) + fileFormat);
    }

    @Override
    public void render(Window window) {
        if (isFirstTime) {
            sceneChanged = true;

            isFirstTime = false;
        }

        renderer.render(window, camera, scene, sceneChanged);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();

        Map<Mesh, List<MeshedItem>> mapMeshes = scene.getGameMeshes();

        for (Mesh mesh : mapMeshes.keySet())
            mesh.cleanUp();
    }

    private void showHelp() {
        System.out.println("Use 'W', 'S', 'A' and 'D' to move camera.\n" +
                "Use 'X' and 'Z' to move camera up and down.\n" +
                "Use left and right arrows to change light position.\n" +
                "Use space bar to move model.\n" +
                "Use 'P' to save screenshot.\n" +
                "Use 'G' to show wireframe.");
    }

    private void showAbout() {
        System.out.println("HSE University Moscow, Barciuc Irina, 2022");
    }
}
