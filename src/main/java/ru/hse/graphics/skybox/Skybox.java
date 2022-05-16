package ru.hse.graphics.skybox;

import org.joml.Vector4f;
import ru.hse.engine.MeshedItem;
import ru.hse.engine.loaders.StaticMeshLoader;
import ru.hse.graphics.model.Material;
import ru.hse.graphics.model.Mesh;
import ru.hse.graphics.model.Texture;

public class Skybox extends MeshedItem {
    public Skybox(String objModel, String textureFile) throws Exception {
        super();
        Mesh skyBoxMesh = StaticMeshLoader.load(objModel, "")[0];
        Texture skyBoxtexture = new Texture(textureFile);
        skyBoxMesh.setMaterial(new Material(skyBoxtexture, 0.0f));
        setMesh(skyBoxMesh);
        setPosition(0, 0, 0);
    }

    public Skybox(String objModel, Vector4f colour) throws Exception {
        super();
        Mesh skyBoxMesh = StaticMeshLoader.load(objModel, "", 0)[0];
        Material material = new Material(colour, 0);
        skyBoxMesh.setMaterial(material);
        setMesh(skyBoxMesh);
        setPosition(0, 0, 0);
    }
}
