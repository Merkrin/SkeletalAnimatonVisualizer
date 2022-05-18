package ru.hse.engine.utils.screenshots;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import ru.hse.core.utils.Constants;
import ru.hse.core.utils.Settings;
import ru.hse.engine.utils.Window;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static ru.hse.engine.utils.screenshots.ScreenshotFileType.*;

/**
 * Class for screenshots capture.
 */
public class ScreenCapture implements Runnable {
    private static final Settings SETTINGS = Settings.getInstance();

    private int windowWidth;
    private int windowHeight;

    /**
     * Initialization method.
     *
     * @param window active window
     */
    public void initialize(Window window) {
        windowWidth = window.getWidth();
        windowHeight = window.getHeight();
    }

    /**
     * Overridden run method.
     */
    @Override
    public void run() {
        saveImage();
    }

    /**
     * Method for image saving.
     */
    private void saveImage() {
        GL11.glReadBuffer(GL11.GL_FRONT);

        int bpp = 4;
        ByteBuffer buffer = BufferUtils.createByteBuffer(windowWidth * windowHeight * bpp);
        GL11.glReadPixels(0, 0, windowWidth, windowHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(Constants.SCREENSHOT_FILENAME_PATTERN);
        LocalDateTime now = LocalDateTime.now();

        ScreenshotFileType fileType = SETTINGS.getScreenshotType() == 1 ? PNG :
                (SETTINGS.getScreenshotType() == 2 ? JPG : BMP);

        File file = new File(dtf.format(now) + fileType.fileExtension);
        String format = fileType.fileType;
        BufferedImage image = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < windowWidth; x++) {
            for (int y = 0; y < windowHeight; y++) {
                int i = (x + (windowWidth * y)) * bpp;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                image.setRGB(x, windowHeight - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
        }

        try {
            ImageIO.write(image, format, file);
        } catch (IOException e) {
            System.out.println("Unable to save screenshot.");
            e.printStackTrace();
        }
    }
}
