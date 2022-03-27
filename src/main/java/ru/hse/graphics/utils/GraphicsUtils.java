package ru.hse.graphics.utils;

import org.lwjgl.opengl.GL11;

public class GraphicsUtils {
    private static boolean inWireframe = false;

    /**
     * Method for wireframe setting.
     *
     * @param enable flag if the setting has to be enabled or not
     */
    public static void setWireframe(boolean enable) {
        if (enable && !inWireframe) {
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

            inWireframe = true;
        } else if (!enable && inWireframe) {
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);

            inWireframe = false;
        }
    }
}
