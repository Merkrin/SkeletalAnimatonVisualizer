package ru.hse.engine.utils;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Active window representation class.
 */
public class Window {
    public static final float FOV = (float) Math.toRadians(60.0f);

    public static final float Z_NEAR = 0.01f;

    public static final float Z_FAR = 1000.f;

    private final String title;

    private int width;

    private int height;

    private long windowHandle;

    private boolean resized;

    private boolean vSync;

    private final WindowOptions windowOptions;

    private final Matrix4f projectionMatrix;

    /**
     * The class' constructor.
     *
     * @param title         window title
     * @param width         window width
     * @param height        windiw height
     * @param vSync         vSync flag
     * @param windowOptions window options
     */
    public Window(String title, int width, int height, boolean vSync, WindowOptions windowOptions) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.vSync = vSync;
        this.resized = false;
        this.windowOptions = windowOptions;

        projectionMatrix = new Matrix4f();
    }

    /**
     * Initialization method.
     */
    public void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);

        if (windowOptions.compatibleProfile)
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE);
        else {
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        }

        boolean maximized = false;

        if (width == 0 || height == 0) {
            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            width = vidMode.width();
            height = vidMode.height();

            glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);

            maximized = true;
        }

        windowHandle = glfwCreateWindow(width, height, title, NULL, NULL);

        if (windowHandle == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetFramebufferSizeCallback(windowHandle, (window, width, height) -> {
            this.width = width;
            this.height = height;
            this.setResized(true);
        });

        glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
        });

        if (maximized)
            glfwMaximizeWindow(windowHandle);
        else {
            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(windowHandle, (vidMode.width() - width) / 2, (vidMode.height() - height) / 2);
        }

        glfwMakeContextCurrent(windowHandle);

        if (isvSync())
            glfwSwapInterval(1);

        glfwShowWindow(windowHandle);

        GL.createCapabilities();

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_STENCIL_TEST);

        if (windowOptions.showTriangles)
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        if (windowOptions.cullFace) {
            glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK);
        }

        if (windowOptions.antialiasing)
            glfwWindowHint(GLFW_SAMPLES, 4);
    }

    /**
     * Window handle getter.
     *
     * @return window handle
     */
    public long getWindowHandle() {
        return windowHandle;
    }

    /**
     * Projection matrix getter.
     *
     * @return projection matrix
     */
    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    /**
     * Update projection matrix.
     */
    public void updateProjectionMatrix() {
        float aspectRatio = (float) width / (float) height;

        projectionMatrix.setPerspective(FOV, aspectRatio, Z_NEAR, Z_FAR);
    }

    /**
     * Check if given key is pressed.
     *
     * @param keyCode key code
     * @return true if the key is pressed and false otherwise
     */
    public boolean isKeyPressed(int keyCode) {
        return glfwGetKey(windowHandle, keyCode) == GLFW_PRESS;
    }

    /**
     * Check if window should be closed.
     *
     * @return true if the window should be closed and false otherwise
     */
    public boolean windowShouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }

    /**
     * Window width getter.
     *
     * @return window width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Window height getter.
     *
     * @return window height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Resized flag setter.
     *
     * @param resized resized flag
     */
    public void setResized(boolean resized) {
        this.resized = resized;
    }

    /**
     * vSync flag getter.
     *
     * @return vSync flag
     */
    public boolean isvSync() {
        return vSync;
    }

    /**
     * vSync flag setter.
     *
     * @param vSync vSync flag
     */
    public void setvSync(boolean vSync) {
        this.vSync = vSync;
    }

    /**
     * Update window state.
     */
    public void update() {
        glfwSwapBuffers(windowHandle);
        glfwPollEvents();
    }

    /**
     * Window options getter.
     *
     * @return window options
     */
    public WindowOptions getOptions() {
        return windowOptions;
    }

    /**
     * Window options storage.
     */
    public static class WindowOptions {

        public boolean cullFace;

        public boolean showTriangles;

        public boolean compatibleProfile;

        public boolean antialiasing;

        public boolean frustumCulling;
    }
}
