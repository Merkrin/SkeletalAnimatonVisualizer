package ru.hse.engine.utils;

import ru.hse.core.utils.Constants;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CmdSaver {
    public static void saveCmd(String cmd) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(Constants.SCREENSHOT_FILENAME_PATTERN);
        LocalDateTime now = LocalDateTime.now();
        File file = new File(dtf.format(now) + ".sav");

        try {
            Files.write(Paths.get(file.getPath()), cmd.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
