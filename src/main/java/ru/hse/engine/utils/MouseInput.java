package ru.hse.engine.utils;

import org.joml.Vector2d;
import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Class for mouse input handling.
 */
public class MouseInput {
    private final Vector2d previousPosition;

    private final Vector2d currentPosition;

    private final Vector2f displacementVector;

    private boolean inWindow = false;

    private boolean leftButtonPressed = false;

    private boolean rightButtonPressed = false;

    /**
     * The class' constructor.
     */
    public MouseInput() {
        previousPosition = new Vector2d(-1, -1);
        currentPosition = new Vector2d(0, 0);
        displacementVector = new Vector2f();
    }

    /**
     * Initialization method.
     *
     * @param window current window
     */
    public void init(Window window) {
        glfwSetCursorPosCallback(window.getWindowHandle(), (windowHandle, xpos, ypos) -> {
            currentPosition.x = xpos;
            currentPosition.y = ypos;
        });

        glfwSetCursorEnterCallback(window.getWindowHandle(), (windowHandle, entered) -> inWindow = entered);

        glfwSetMouseButtonCallback(window.getWindowHandle(), (windowHandle, button, action, mode) -> {
            leftButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS;
            rightButtonPressed = button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS;
        });
    }

    /**
     * Displacement vector getter.
     *
     * @return displacement vector
     */
    public Vector2f getDisplacementVector() {
        return displacementVector;
    }

    /**
     * Get mouse input.
     */
    public void input() {
        displacementVector.x = 0;
        displacementVector.y = 0;

        if (previousPosition.x > 0 && previousPosition.y > 0 && inWindow) {
            double deltax = currentPosition.x - previousPosition.x;
            double deltay = currentPosition.y - previousPosition.y;

            boolean rotateX = deltax != 0;
            boolean rotateY = deltay != 0;

            if (rotateX)
                displacementVector.y = (float) deltax;

            if (rotateY)
                displacementVector.x = (float) deltay;
        }

        previousPosition.x = currentPosition.x;
        previousPosition.y = currentPosition.y;
    }

    /**
     * Check if right mouse button is pressed.
     *
     * @return true if right button is pressed and false otherwise
     */
    public boolean isRightButtonPressed() {
        return rightButtonPressed;
    }
}
