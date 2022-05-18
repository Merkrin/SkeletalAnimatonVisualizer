package ru.hse.engine.utils;

import ru.hse.graphics.model.Texture;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache for texture.
 */
public class TextureCache {
    private static TextureCache INSTANCE;

    private final Map<String, Texture> texturesMap;

    /**
     * The class' constructor.
     */
    private TextureCache() {
        texturesMap = new HashMap<>();
    }

    /**
     * Class instance getter.
     *
     * @return instance of the class
     */
    public static synchronized TextureCache getInstance() {
        if (INSTANCE == null)
            INSTANCE = new TextureCache();

        return INSTANCE;
    }

    /**
     * Texture getter.
     *
     * @param path path to texture
     * @return texture
     * @throws Exception an unhandled exception
     */
    public Texture getTexture(String path) throws Exception {
        Texture texture = texturesMap.get(path);

        if (texture == null) {
            texture = new Texture(path);

            texturesMap.put(path, texture);
        }

        return texture;
    }
}
