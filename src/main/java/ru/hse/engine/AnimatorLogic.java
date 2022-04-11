package ru.hse.engine;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import ru.hse.core.Renderer;
import ru.hse.core.utils.Constants;
import ru.hse.core.utils.Settings;
import ru.hse.engine.animation.AnimGameItem;
import ru.hse.engine.animation.Animation;
import ru.hse.engine.loaders.AnimMeshesLoader;
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
        renderer.init(window);

        scene = new Scene();

        AnimGameItem animatedItem = AnimMeshesLoader.loadAnimGameItem("/Users/merkrin/Programming/SkeletalAnimatonVisualizer/src/main/resources/models/boy.dae",
                "/Users/merkrin/Programming/SkeletalAnimatonVisualizer/src/main/resources/textures");
        animatedItem.setScale(0.05f);
        animation = animatedItem.getCurrentAnimation();

        // Mesh[] houseMesh = StaticMeshesLoader.load("/Users/merkrin/Programming/SkeletalAnimatonVisualizer/src/main/resources/models/house/house.obj",
        //                "/Users/merkrin/Programming/SkeletalAnimatonVisualizer/src/main/resources/textures/house/");
        //        GameItem house = new GameItem(houseMesh);

        scene.setGameItems(new MeshedItem[]{animatedItem});

        // Shadows
        scene.setRenderShadows(true);

        // Setup  SkyBox
        float skyBoxScale = 100.0f;
        Skybox skyBox = new Skybox("/Users/merkrin/Programming/SkeletalAnimatonVisualizer/src/main/resources/models/skybox.obj",
                new Vector4f(0.65f, 0.65f, 0.65f, 1.0f));
        skyBox.setScale(skyBoxScale);
        scene.setSkyBox(skyBox);

        // Setup Lights
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
            sceneChanged = true;

            float elapsedTime = timer.getElapsedTime();

            if (animation != null && elapsedTime >= timePerAnimationFrame) {
                timer.updateLastLoopTime();

                animation.nextFrame();
            }
        }
        if(window.isKeyPressed(GLFW_KEY_P)){
            screenCapture.run();
        }

        GraphicsUtils.setWireframe(window.isKeyPressed(GLFW_KEY_G));
    }

    @Override
    public void update(float interval, MouseInput mouseInput) {
        if (mouseInput.isRightButtonPressed()) {
            // Update camera based on mouse
            Vector2f rotationVector = mouseInput.getDisplVec();

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
}