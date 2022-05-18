package ru.hse.engine.utils.screenshots;

/**
 * Enum of screenshot types.
 */
public enum ScreenshotFileType {
    PNG(".png", "PNG"),
    JPG(".jpg", "JPG"),
    BMP(".bmp", "BMP");

    public final String fileExtension;
    public final String fileType;

    /**
     * The enum's constructor.
     *
     * @param fileExtension file extension
     * @param fileType      file type
     */
    ScreenshotFileType(String fileExtension, String fileType) {
        this.fileExtension = fileExtension;
        this.fileType = fileType;
    }
}
