package ru.hse.core;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import ru.hse.engine.Camera;
import ru.hse.engine.GameItem;
import ru.hse.engine.IGameLogic;
import ru.hse.engine.loaders.Md5Loader;
import ru.hse.engine.loaders.OBJLoader;
import ru.hse.engine.loaders.md5.Md5Model;
import ru.hse.engine.utils.MouseInput;
import ru.hse.engine.utils.Window;
import ru.hse.engine.wrappers.Scene;
import ru.hse.engine.wrappers.SceneLight;
import ru.hse.graphics.lighting.DirectionalLight;
import ru.hse.graphics.lighting.PointLight;
import ru.hse.graphics.model.Material;
import ru.hse.graphics.model.Mesh;
import ru.hse.graphics.model.Texture;
import ru.hse.graphics.skybox.Skybox;
import ru.hse.graphics.utils.GraphicsUtils;

import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public class DummyGame implements IGameLogic {
    private static final float MOUSE_SENSITIVITY = 0.2f;

    private final Vector3f cameraInc;

    private final Renderer renderer;

    private final Camera camera;

    private Scene scene;

    private float angleInc;

    private float lightAngle;

    private static final float CAMERA_POS_STEP = 0.05f;

    public DummyGame() {
        renderer = new Renderer();
        camera = new Camera();
        cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        lightAngle = -90;
        angleInc = 0;
    }

    @Override
    public void init(Window window) throws Exception {
        renderer.init(window);

        scene = new Scene();

        float reflectance = 1f;

        Mesh quadMesh = OBJLoader.loadMesh("/models/plane.obj");
        Material quadMaterial = new Material(new Vector4f(0.0f, 0.0f, 1.0f, 1.0f), reflectance);
        quadMesh.setMaterial(quadMaterial);
        GameItem quadGameItem = new GameItem(quadMesh);
        quadGameItem.setPosition(0, 0, 0);
        quadGameItem.setScale(2.5f);

        // Setup  GameItems
        Md5Model md5Meshodel = Md5Model.parse("/models/monster.md5mesh");
        GameItem monster = Md5Loader.process(md5Meshodel, new Vector4f(1, 1, 1, 1));
        monster.setScale(0.05f);
        monster.setRotation(90, 0, 0);

        scene.setGameItems(new GameItem[] { quadGameItem, monster} );

        // Setup Lights
        setupLights();

        camera.getPosition().x = 0.25f;
        camera.getPosition().y = 6.5f;
        camera.getPosition().z = 6.5f;
        camera.getRotation().x = 25;
        camera.getRotation().y = -1;

//        Mesh mesh = OBJLoader.loadMesh("/models/cube.obj");
//        Texture texture = new Texture("/Users/merkrin/Programming/SkeletalAnimatonVisualizer/src/main/resources/textures/grassblock.png");
//        Material material = new Material(texture, reflectance);
//
//        mesh.setMaterial(material);

    }

    private void setupLights() {
        SceneLight sceneLight = new SceneLight();
        scene.setSceneLight(sceneLight);

        // Ambient Light
        sceneLight.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));
        sceneLight.setSkyboxLight(new Vector3f(1.0f, 1.0f, 1.0f));

        // Directional Light
        float lightIntensity = 1.0f;
        Vector3f lightDirection = new Vector3f(0, 1, 1);
        DirectionalLight directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), lightDirection, lightIntensity);
        directionalLight.setShadowPosMult(5);
        directionalLight.setOrthoCords(-10.0f, 10.0f, -10.0f, 10.0f, -1.0f, 20.0f);
        sceneLight.setDirectionalLight(directionalLight);
    }

    @Override
    public void input(Window window, MouseInput mouseInput) {
        cameraInc.set(0, 0, 0);
        if (window.isKeyPressed(GLFW_KEY_W)) {
            cameraInc.z = -1;
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            cameraInc.z = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            cameraInc.x = -1;
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            cameraInc.x = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_Z)) {
            cameraInc.y = -1;
        } else if (window.isKeyPressed(GLFW_KEY_X)) {
            cameraInc.y = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_LEFT)) {
            angleInc -= 0.05f;
        } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
            angleInc += 0.05f;
        } else {
            angleInc = 0;
        }

        GraphicsUtils.setWireframe(window.isKeyPressed(GLFW_KEY_G));
    }

    @Override
    public void update(float interval, MouseInput mouseInput) {
        // Update camera based on mouse
        if (mouseInput.isRightButtonPressed()) {
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
        }

        // Update camera position
        camera.movePosition(cameraInc.x * CAMERA_POS_STEP, cameraInc.y * CAMERA_POS_STEP,
                cameraInc.z * CAMERA_POS_STEP);

        lightAngle += angleInc;
        if ( lightAngle < 0 ) {
            lightAngle = 0;
        } else if (lightAngle > 180 ) {
            lightAngle = 180;
        }
        float zValue = (float)Math.cos(Math.toRadians(lightAngle));
        float yValue = (float)Math.sin(Math.toRadians(lightAngle));
        Vector3f lightDirection = this.scene.getSceneLight().getDirectionalLight().getDirection();
        lightDirection.x = 0;
        lightDirection.y = yValue;
        lightDirection.z = zValue;
        lightDirection.normalize();
    }

    @Override
    public void render(Window window) {
        renderer.render(window, camera, scene);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();
        Map<Mesh, List<GameItem>> mapMeshes = scene.getGameMeshes();
        for (Mesh mesh : mapMeshes.keySet()) {
            mesh.cleanUp();
        }
    }
}
