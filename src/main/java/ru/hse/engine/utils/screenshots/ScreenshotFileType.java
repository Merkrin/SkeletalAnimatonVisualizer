package ru.hse.engine.utils.screenshots;

public enum ScreenshotFileType {
    PNG(".png", "PNG"),
    JPG(".jpg", "JPG"),
    BMP(".bmp", "BMP");

    public final String fileExtension;
    public final String fileType;

    ScreenshotFileType(String fileExtension, String fileType) {
        this.fileExtension = fileExtension;
        this.fileType = fileType;
    }
}
