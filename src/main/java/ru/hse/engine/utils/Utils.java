package ru.hse.engine.utils;

import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

import java.io.*;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Utility class.
 */
public class Utils {
    /**
     * Load file from resources.
     *
     * @param fileName file name
     * @return file content
     * @throws Exception an unhandled exception
     */
    public static String loadResource(String fileName) throws Exception {
        String result;

        try {
            try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
                 Scanner scanner = new Scanner(in, java.nio.charset.StandardCharsets.UTF_8.name())) {
                result = scanner.useDelimiter("\\A").next();
            }
        } catch (NullPointerException e) {
            String path = Utils.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            String decodedPath = URLDecoder.decode(path, "UTF-8");
            String separator = FileSystems.getDefault().getSeparator();
            System.out.println(decodedPath.substring(0, decodedPath.lastIndexOf(separator) + 1));

            throw new NullPointerException();
        }

        return result;
    }

    /**
     * Create array of ints of list.
     *
     * @param list list of integers
     * @return array of ints
     */
    public static int[] listIntToArray(List<Integer> list) {
        return list.stream().mapToInt((Integer v) -> v).toArray();
    }

    /**
     * Create list from array
     *
     * @param list list
     * @return array
     */
    public static float[] listToArray(List<Float> list) {
        int size = list != null ? list.size() : 0;
        float[] floatArr = new float[size];
        for (int i = 0; i < size; i++) {
            floatArr[i] = list.get(i);
        }
        return floatArr;
    }

    /**
     * Create ByteBuffer from resource.
     *
     * @param resource   resource
     * @param bufferSize buffer size
     * @return byte buffer
     * @throws IOException an unhandled exception
     */
    public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
        ByteBuffer buffer;

        Path path = Paths.get(resource);

        if (Files.isReadable(path)) {
            try (SeekableByteChannel fc = Files.newByteChannel(path)) {
                buffer = MemoryUtil.memAlloc((int) fc.size() + 1);

                while (fc.read(buffer) != -1) ;
            }
        } else {
            try (InputStream source = new FileInputStream(path.toFile());
                 ReadableByteChannel rbc = Channels.newChannel(source)) {
                buffer = MemoryUtil.memAlloc(bufferSize);

                while (true) {
                    int bytes = rbc.read(buffer);

                    if (bytes == -1)
                        break;

                    if (buffer.remaining() == 0)
                        buffer = resizeBuffer(buffer, buffer.capacity() * 2);
                }
            }
        }

        buffer.flip();

        return buffer;
    }

    /**
     * Resize given buffer.
     *
     * @param buffer      buffer
     * @param newCapacity new size
     * @return resized buffer
     */
    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }
}
