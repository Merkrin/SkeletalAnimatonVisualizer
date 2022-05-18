package ru.hse.core;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import ru.hse.core.utils.Constants;
import ru.hse.core.utils.Settings;
import ru.hse.engine.Camera;
import ru.hse.engine.MeshedItem;
import ru.hse.engine.animation.AnimatedItem;
import ru.hse.engine.animation.AnimatedFrame;
import ru.hse.engine.shadows.ShadowCascade;
import ru.hse.engine.shadows.ShadowRenderer;
import ru.hse.engine.utils.FrustumCullingFilter;
import ru.hse.engine.utils.Utils;
import ru.hse.engine.utils.Window;
import ru.hse.engine.wrappers.Scene;
import ru.hse.engine.wrappers.SceneLight;
import ru.hse.graphics.ShaderProgram;
import ru.hse.graphics.Transformation;
import ru.hse.graphics.lighting.DirectionalLight;
import ru.hse.graphics.lighting.PointLight;
import ru.hse.graphics.model.Mesh;
import ru.hse.graphics.model.Texture;
import ru.hse.graphics.skybox.Skybox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE2;
import static ru.hse.core.utils.Constants.MAX_POINT_LIGHTS;
import static ru.hse.core.utils.Constants.MAX_SPOT_LIGHTS;

/**
 * Renderer class for graphics presentation.
 */
public class Renderer {
    private static final Settings SETTINGS = Settings.getInstance();

    private final List<MeshedItem> filteredItems;

    private final FrustumCullingFilter frustumCullingFilter;

    private ShaderProgram sceneShaderProgram;
    private ShaderProgram skyBoxShaderProgram;

    private final ShadowRenderer shadowRenderer;

    private final Transformation transformation;

    private final float specularPower;

    /**
     * The class' constructor.
     */
    public Renderer() {
        frustumCullingFilter = new FrustumCullingFilter();

        transformation = new Transformation();

        shadowRenderer = new ShadowRenderer();

        filteredItems = new ArrayList<>();

        specularPower = SETTINGS.getSpecularPower();
    }

    /**
     * Initializing method for main renderer setups.
     *
     * @throws Exception an unhandled exception
     */
    public void init() throws Exception {
        shadowRenderer.init();

        setupSkyboxShader();
        setupSceneShader();
    }

    /**
     * Main rendering method.
     *
     * @param window       active window
     * @param camera       active camera
     * @param scene        scene to render
     * @param sceneChanged changed scene flag
     */
    public void render(Window window, Camera camera, Scene scene, boolean sceneChanged) {
        clear();

        if (window.getOptions().frustumCulling) {
            frustumCullingFilter.updateFrustum(window.getProjectionMatrix(), camera.getViewMatrix());

            frustumCullingFilter.filter(scene.getGameMeshes());
            frustumCullingFilter.filter(scene.getGameInstancedMeshes());
        }

        if (scene.isRenderShadows() && sceneChanged)
            shadowRenderer.render(window, scene, camera, transformation);

        glViewport(0, 0, window.getWidth(), window.getHeight());

        window.updateProjectionMatrix();

        renderScene(window, camera, scene);
        renderSkybox(window, camera, scene);
    }

    /**
     * Method for skybox shader setup.
     *
     * @throws Exception an unhandled exception
     */
    private void setupSkyboxShader() throws Exception {
        skyBoxShaderProgram = new ShaderProgram();

        skyBoxShaderProgram.createVertexShader(Utils.loadResource("shaders/skybox_vertex.glsl"));
        skyBoxShaderProgram.createFragmentShader(Utils.loadResource("shaders/skybox_fragment.glsl"));
        skyBoxShaderProgram.link();

        skyBoxShaderProgram.createUniform("projectionMatrix");
        skyBoxShaderProgram.createUniform("modelViewMatrix");
        skyBoxShaderProgram.createUniform("texture_sampler");
        skyBoxShaderProgram.createUniform("ambientLight");
        skyBoxShaderProgram.createUniform("colour");
        skyBoxShaderProgram.createUniform("hasTexture");
    }

    /**
     * Method for scene shader setup.
     *
     * @throws Exception an unhandled exception
     */
    private void setupSceneShader() throws Exception {
        sceneShaderProgram = new ShaderProgram();

        sceneShaderProgram.createVertexShader(Utils.loadResource("shaders/scene_vertex.glsl"));
        sceneShaderProgram.createFragmentShader(Utils.loadResource("shaders/scene_fragment.glsl"));
        sceneShaderProgram.link();

        sceneShaderProgram.createUniform("viewMatrix");
        sceneShaderProgram.createUniform("projectionMatrix");
        sceneShaderProgram.createUniform("texture_sampler");
        sceneShaderProgram.createUniform("normalMap");
        sceneShaderProgram.createMaterialUniform("material");

        sceneShaderProgram.createUniform("specularPower");
        sceneShaderProgram.createUniform("ambientLight");
        sceneShaderProgram.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
        sceneShaderProgram.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);
        sceneShaderProgram.createDirectionalLightUniform("directionalLight");

        for (int i = 0; i < Constants.CASCADES_NUMBER; i++)
            sceneShaderProgram.createUniform("shadowMap_" + i);

        sceneShaderProgram.createUniform("orthoProjectionMatrix", Constants.CASCADES_NUMBER);
        sceneShaderProgram.createUniform("modelNonInstancedMatrix");
        sceneShaderProgram.createUniform("lightViewMatrix", Constants.CASCADES_NUMBER);
        sceneShaderProgram.createUniform("cascadeFarPlanes", Constants.CASCADES_NUMBER);
        sceneShaderProgram.createUniform("renderShadow");

        sceneShaderProgram.createUniform("jointsMatrix");

        sceneShaderProgram.createUniform("isInstanced");
        sceneShaderProgram.createUniform("numCols");
        sceneShaderProgram.createUniform("numRows");

        sceneShaderProgram.createUniform("selectedNonInstanced");
    }

    /**
     * Window clearing method.
     */
    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }

    /**
     * Skybox rendering method.
     *
     * @param window active window
     * @param camera active camera
     * @param scene  scene to render
     */
    private void renderSkybox(Window window, Camera camera, Scene scene) {
        Skybox skybox = scene.getSkyBox();

        if (skybox != null) {
            skyBoxShaderProgram.bind();
            skyBoxShaderProgram.setUniform("texture_sampler", 0);

            Matrix4f projectionMatrix = window.getProjectionMatrix();

            skyBoxShaderProgram.setUniform("projectionMatrix", projectionMatrix);

            Matrix4f viewMatrix = camera.getViewMatrix();

            float m30 = viewMatrix.m30();
            viewMatrix.m30(0);
            float m31 = viewMatrix.m31();
            viewMatrix.m31(0);
            float m32 = viewMatrix.m32();
            viewMatrix.m32(0);

            Mesh mesh = skybox.getMesh();

            Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(skybox, viewMatrix);

            skyBoxShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            skyBoxShaderProgram.setUniform("ambientLight", scene.getSceneLight().getSkyBoxLight());
            skyBoxShaderProgram.setUniform("colour", mesh.getMaterial().getAmbientColour());
            skyBoxShaderProgram.setUniform("hasTexture", mesh.getMaterial().isTextured() ? 1 : 0);

            mesh.render();

            viewMatrix.m30(m30);
            viewMatrix.m31(m31);
            viewMatrix.m32(m32);

            skyBoxShaderProgram.unbind();
        }
    }

    /**
     * Skybox rendering method.
     *
     * @param window active window
     * @param camera active camera
     * @param scene  scene to render
     */
    public void renderScene(Window window, Camera camera, Scene scene) {
        sceneShaderProgram.bind();

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = window.getProjectionMatrix();

        sceneShaderProgram.setUniform("viewMatrix", viewMatrix);
        sceneShaderProgram.setUniform("projectionMatrix", projectionMatrix);

        List<ShadowCascade> shadowCascades = shadowRenderer.getShadowCascades();

        for (int i = 0; i < Constants.CASCADES_NUMBER; i++) {
            ShadowCascade shadowCascade = shadowCascades.get(i);

            sceneShaderProgram.setUniform("orthoProjectionMatrix", shadowCascade.getOrthogonalProjectionMatrix(), i);
            sceneShaderProgram.setUniform("cascadeFarPlanes", ShadowRenderer.CASCADE_SPLITS[i], i);
            sceneShaderProgram.setUniform("lightViewMatrix", shadowCascade.getLightViewMatrix(), i);
        }

        SceneLight sceneLight = scene.getSceneLight();

        renderLights(viewMatrix, sceneLight);

        sceneShaderProgram.setUniform("texture_sampler", 0);
        sceneShaderProgram.setUniform("normalMap", 1);

        int start = 2;

        for (int i = 0; i < Constants.CASCADES_NUMBER; i++)
            sceneShaderProgram.setUniform("shadowMap_" + i, start + i);

        sceneShaderProgram.setUniform("renderShadow", scene.isRenderShadows() ? 1 : 0);

        renderMeshes(scene);

        sceneShaderProgram.unbind();
    }

    /**
     * Method for rendering scene meshes.
     *
     * @param scene scene to render
     */
    private void renderMeshes(Scene scene) {
        sceneShaderProgram.setUniform("isInstanced", 0);

        Map<Mesh, List<MeshedItem>> mapMeshes = scene.getGameMeshes();

        for (Mesh mesh : mapMeshes.keySet()) {
            sceneShaderProgram.setUniform("material", mesh.getMaterial());

            Texture texture = mesh.getMaterial().getTexture();

            if (texture != null) {
                sceneShaderProgram.setUniform("numCols", texture.getNumCols());
                sceneShaderProgram.setUniform("numRows", texture.getNumRows());
            }

            shadowRenderer.bindTextures(GL_TEXTURE2);

            mesh.renderList(mapMeshes.get(mesh), (MeshedItem gameItem) -> {
                sceneShaderProgram.setUniform("selectedNonInstanced", gameItem.isSelected() ? 1.0f : 0.0f);

                Matrix4f modelMatrix = transformation.buildModelMatrix(gameItem);
                sceneShaderProgram.setUniform("modelNonInstancedMatrix", modelMatrix);

                if (gameItem instanceof AnimatedItem) {
                    AnimatedItem animatedItem = (AnimatedItem) gameItem;
                    AnimatedFrame frame = animatedItem.getCurrentAnimation().getCurrentFrame();
                    sceneShaderProgram.setUniform("jointsMatrix", frame.getJointMatrices());
                }
            });
        }
    }

    /**
     * Method for lights rendering.
     *
     * @param viewMatrix camera's view matrix
     * @param sceneLight lights on the scene
     */
    private void renderLights(Matrix4f viewMatrix, SceneLight sceneLight) {
        sceneShaderProgram.setUniform("ambientLight", sceneLight.getAmbientLight());
        sceneShaderProgram.setUniform("specularPower", specularPower);

        PointLight[] pointLightList = sceneLight.getPointLightList();

        int lightsNumber = pointLightList != null ? pointLightList.length : 0;

        for (int i = 0; i < lightsNumber; i++) {
            PointLight currentPointLight = new PointLight(pointLightList[i]);
            Vector3f lightPosition = currentPointLight.getPosition();
            Vector4f auxiliary = new Vector4f(lightPosition, 1);

            auxiliary.mul(viewMatrix);

            lightPosition.x = auxiliary.x;
            lightPosition.y = auxiliary.y;
            lightPosition.z = auxiliary.z;

            sceneShaderProgram.setUniform("pointLights", currentPointLight, i);
        }

        DirectionalLight currentDirectionalLight = new DirectionalLight(sceneLight.getDirectionalLight());
        Vector4f direction = new Vector4f(currentDirectionalLight.getDirection(), 0);

        direction.mul(viewMatrix);

        currentDirectionalLight.setDirection(new Vector3f(direction.x, direction.y, direction.z));

        sceneShaderProgram.setUniform("directionalLight", currentDirectionalLight);
    }

    /**
     * Cleanup method.
     */
    public void cleanup() {
        if (shadowRenderer != null) {
            shadowRenderer.cleanup();
        }
        if (skyBoxShaderProgram != null) {
            skyBoxShaderProgram.cleanup();
        }
        if (sceneShaderProgram != null) {
            sceneShaderProgram.cleanup();
        }
    }
}
