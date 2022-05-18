package ru.hse.engine.shadows;

import ru.hse.core.utils.Constants;
import ru.hse.graphics.model.ArrTexture;

import static org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL30.*;

/**
 * Shadow buffer class.
 */
public class ShadowBuffer {
    public static final int SHADOW_MAP_WIDTH = (int) Math.pow(65, 2);

    public static final int SHADOW_MAP_HEIGHT = SHADOW_MAP_WIDTH;

    private final int depthMapFBO;

    private final ArrTexture depthMap;

    /**
     * The class' constructor.
     *
     * @throws Exception an unhandled exception
     */
    public ShadowBuffer() throws Exception {
        depthMapFBO = glGenFramebuffers();

        depthMap = new ArrTexture(Constants.CASCADES_NUMBER, SHADOW_MAP_WIDTH, SHADOW_MAP_HEIGHT, GL_DEPTH_COMPONENT);

        glBindFramebuffer(GL_FRAMEBUFFER, depthMapFBO);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthMap.getIds()[0], 0);

        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            throw new Exception("Could not create FrameBuffer");

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    /**
     * Depth map texture getter
     *
     * @return depth map texture
     */
    public ArrTexture getDepthMapTexture() {
        return depthMap;
    }

    /**
     * Depth map FBO getter
     *
     * @return Depth map FBO
     */
    public int getDepthMapFBO() {
        return depthMapFBO;
    }

    /**
     * Bind textures.
     *
     * @param start binding start index
     */
    public void bindTextures(int start) {
        for (int i = 0; i < Constants.CASCADES_NUMBER; i++) {
            glActiveTexture(start + i);
            glBindTexture(GL_TEXTURE_2D, depthMap.getIds()[i]);
        }
    }

    /**
     * Cleanup method.
     */
    public void cleanup() {
        glDeleteFramebuffers(depthMapFBO);
        depthMap.cleanup();
    }
}
