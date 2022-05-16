package ru.hse.engine.shadows;

import org.joml.Matrix4f;
import ru.hse.core.Renderer;
import ru.hse.core.utils.Constants;
import ru.hse.engine.Camera;
import ru.hse.engine.MeshedItem;
import ru.hse.engine.animation.AnimatedItem;
import ru.hse.engine.animation.AnimatedFrame;
import ru.hse.engine.utils.Utils;
import ru.hse.engine.utils.Window;
import ru.hse.engine.wrappers.Scene;
import ru.hse.engine.wrappers.SceneLight;
import ru.hse.graphics.ShaderProgram;
import ru.hse.graphics.Transformation;
import ru.hse.graphics.lighting.DirectionalLight;
import ru.hse.graphics.model.InstancedMesh;
import ru.hse.graphics.model.Mesh;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL30.*;

public class ShadowRenderer {
    public static final float[] CASCADE_SPLITS = new float[]{Window.Z_FAR / 20.0f, Window.Z_FAR / 10.0f, Window.Z_FAR};

    private ShaderProgram depthShaderProgram;

    private List<ShadowCascade> shadowCascades;

    private ShadowBuffer shadowBuffer;

    private final List<MeshedItem> filteredItems;

    public ShadowRenderer() {
        filteredItems = new ArrayList<>();
    }

    public void init() throws Exception {
        shadowBuffer = new ShadowBuffer();
        shadowCascades = new ArrayList<>();

        setupDepthShader();

        float zNear = Window.Z_NEAR;

        for (int i = 0; i < Constants.CASCADES_NUMBER; i++) {
            ShadowCascade shadowCascade = new ShadowCascade(zNear, CASCADE_SPLITS[i]);
            shadowCascades.add(shadowCascade);
            zNear = CASCADE_SPLITS[i];
        }
    }

    public List<ShadowCascade> getShadowCascades() {
        return shadowCascades;
    }

    public void bindTextures(int start) {
        this.shadowBuffer.bindTextures(start);
    }

    private void setupDepthShader() throws Exception {
        depthShaderProgram = new ShaderProgram();
        depthShaderProgram.createVertexShader(Utils.loadResource("/shaders/depth_vertex.glsl"));
        depthShaderProgram.createFragmentShader(Utils.loadResource("/shaders/depth_fragment.glsl"));
        depthShaderProgram.link();

        depthShaderProgram.createUniform("isInstanced");
        depthShaderProgram.createUniform("modelNonInstancedMatrix");
        depthShaderProgram.createUniform("lightViewMatrix");
        depthShaderProgram.createUniform("jointsMatrix");
        depthShaderProgram.createUniform("orthoProjectionMatrix");
    }

    private void update(Window window, Matrix4f viewMatrix, Scene scene) {
        SceneLight sceneLight = scene.getSceneLight();
        DirectionalLight directionalLight = sceneLight != null ? sceneLight.getDirectionalLight() : null;
        for (int i = 0; i < Constants.CASCADES_NUMBER; i++) {
            ShadowCascade shadowCascade = shadowCascades.get(i);
            shadowCascade.update(window, viewMatrix, directionalLight);
        }
    }

    public void render(Window window, Scene scene, Camera camera, Transformation transformation, Renderer renderer) {
        update(window, camera.getViewMatrix(), scene);

        glBindFramebuffer(GL_FRAMEBUFFER, shadowBuffer.getDepthMapFBO());
        glViewport(0, 0, ShadowBuffer.SHADOW_MAP_WIDTH, ShadowBuffer.SHADOW_MAP_HEIGHT);
        glClear(GL_DEPTH_BUFFER_BIT);

        depthShaderProgram.bind();

        for (int i = 0; i < Constants.CASCADES_NUMBER; i++) {
            ShadowCascade shadowCascade = shadowCascades.get(i);

            depthShaderProgram.setUniform("orthoProjectionMatrix", shadowCascade.getOrthogonalProjectionMatrix());
            depthShaderProgram.setUniform("lightViewMatrix", shadowCascade.getLightViewMatrix());

            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, shadowBuffer.getDepthMapTexture().getIds()[i], 0);
            glClear(GL_DEPTH_BUFFER_BIT);

            renderNonInstancedMeshes(scene, transformation);

            renderInstancedMeshes(scene, transformation);
        }

        depthShaderProgram.unbind();
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private void renderNonInstancedMeshes(Scene scene, Transformation transformation) {
        depthShaderProgram.setUniform("isInstanced", 0);

        Map<Mesh, List<MeshedItem>> mapMeshes = scene.getGameMeshes();
        for (Mesh mesh : mapMeshes.keySet()) {
            mesh.renderList(mapMeshes.get(mesh), (MeshedItem gameItem) -> {
                        Matrix4f modelMatrix = transformation.buildModelMatrix(gameItem);
                        depthShaderProgram.setUniform("modelNonInstancedMatrix", modelMatrix);
                        if (gameItem instanceof AnimatedItem) {
                            AnimatedItem animatedItem = (AnimatedItem) gameItem;
                            AnimatedFrame frame = animatedItem.getCurrentAnimation().getCurrentFrame();
                            depthShaderProgram.setUniform("jointsMatrix", frame.getJointMatrices());
                        }
                    }
            );
        }
    }

    private void renderInstancedMeshes(Scene scene, Transformation transformation) {
        depthShaderProgram.setUniform("isInstanced", 1);

        Map<InstancedMesh, List<MeshedItem>> mapMeshes = scene.getGameInstancedMeshes();
        for (InstancedMesh mesh : mapMeshes.keySet()) {
            filteredItems.clear();

            for (MeshedItem gameItem : mapMeshes.get(mesh))
                if (gameItem.isInsideFrustum())
                    filteredItems.add(gameItem);

            bindTextures(GL_TEXTURE2);

            mesh.renderListInstanced(filteredItems, transformation, null);
        }
    }

    public void cleanup() {
        if (shadowBuffer != null)
            shadowBuffer.cleanup();

        if (depthShaderProgram != null)
            depthShaderProgram.cleanup();
    }
}
