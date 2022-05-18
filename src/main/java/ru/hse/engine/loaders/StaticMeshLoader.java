package ru.hse.engine.loaders;

import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import ru.hse.engine.utils.TextureCache;
import ru.hse.engine.utils.Utils;
import ru.hse.graphics.model.Material;
import ru.hse.graphics.model.Mesh;
import ru.hse.graphics.model.Texture;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.assimp.Assimp.*;

/**
 * Static mesh loader.
 */
public class StaticMeshLoader {
    /**
     * Load static mesh.
     *
     * @param meshPath          mesh path
     * @param texturesDirectory texture directory
     * @return loaded meshes
     * @throws Exception an unhandled exception
     */
    public static Mesh[] load(String meshPath, String texturesDirectory) throws Exception {
        return load(meshPath, texturesDirectory,
                aiProcess_GenSmoothNormals | aiProcess_JoinIdenticalVertices | aiProcess_Triangulate
                        | aiProcess_FixInfacingNormals | aiProcess_PreTransformVertices);
    }

    /**
     * Load static mesh.
     *
     * @param meshPath          mesh path
     * @param texturesDirectory texture directory
     * @param flags             loading flags
     * @return loaded meshes
     * @throws Exception an unhandled exception
     */
    public static Mesh[] load(String meshPath, String texturesDirectory, int flags) throws Exception {
        AIScene aiScene = aiImportFile(meshPath, flags);

        if (aiScene == null)
            throw new Exception("Error loading model");

        int materialsAmount = aiScene.mNumMaterials();

        PointerBuffer aiMaterials = aiScene.mMaterials();

        if (aiMaterials == null)
            throw new Exception("Error loading model");

        List<Material> materials = new ArrayList<>();

        for (int i = 0; i < materialsAmount; i++) {
            AIMaterial aiMaterial = AIMaterial.create(aiMaterials.get(i));

            processMaterial(aiMaterial, materials, texturesDirectory);
        }

        int meshesAmount = aiScene.mNumMeshes();

        PointerBuffer aiMeshes = aiScene.mMeshes();

        if (aiMeshes == null)
            throw new Exception("Error loading model");

        Mesh[] meshes = new Mesh[meshesAmount];

        for (int i = 0; i < meshesAmount; i++) {
            AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));

            Mesh mesh = processMesh(aiMesh, materials);

            meshes[i] = mesh;
        }

        return meshes;
    }

    /**
     * Load mesh's indices.
     *
     * @param aiMesh  loaded mesh
     * @param indices indices
     */
    protected static void processIndices(AIMesh aiMesh, List<Integer> indices) {
        int facesNumber = aiMesh.mNumFaces();

        AIFace.Buffer aiFaces = aiMesh.mFaces();

        for (int i = 0; i < facesNumber; i++) {
            AIFace aiFace = aiFaces.get(i);

            IntBuffer buffer = aiFace.mIndices();

            while (buffer.remaining() > 0)
                indices.add(buffer.get());
        }
    }

    /**
     * Load material.
     *
     * @param aiMaterial        loaded material
     * @param materials         materials list
     * @param texturesDirectory texture directory
     * @throws Exception an unhandled exception
     */
    protected static void processMaterial(AIMaterial aiMaterial, List<Material> materials,
                                          String texturesDirectory) throws Exception {
        AIColor4D color = AIColor4D.create();

        AIString path = AIString.calloc();

        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_DIFFUSE, 0, path, (IntBuffer) null,
                null, null, null, null, null);

        String texturePath = path.dataString();

        Texture texture = null;

        if (texturePath != null && texturePath.length() > 0) {
            TextureCache textureCache = TextureCache.getInstance();

            String textureFile = "";

            if (texturesDirectory != null && texturesDirectory.length() > 0)
                textureFile += texturesDirectory + "/";

            textureFile += texturePath;
            textureFile = textureFile.replace("//", "/");

            texture = textureCache.getTexture(textureFile);
        }

        Vector4f ambient = Material.DEFAULT_COLOUR;

        int result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_AMBIENT, aiTextureType_NONE, 0, color);

        if (result == 0)
            ambient = new Vector4f(color.r(), color.g(), color.b(), color.a());

        Vector4f diffuse = Material.DEFAULT_COLOUR;

        result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0, color);

        if (result == 0)
            diffuse = new Vector4f(color.r(), color.g(), color.b(), color.a());

        Vector4f specular = Material.DEFAULT_COLOUR;

        result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_SPECULAR, aiTextureType_NONE, 0, color);

        if (result == 0)
            specular = new Vector4f(color.r(), color.g(), color.b(), color.a());

        Material material = new Material(ambient, diffuse, specular, 1.0f);

        material.setTexture(texture);
        materials.add(material);
    }

    /**
     * Load mesh.
     *
     * @param aiMesh    loaded mesh
     * @param materials materials list
     * @return loaded mesh
     */
    private static Mesh processMesh(AIMesh aiMesh, List<Material> materials) {
        List<Float> vertices = new ArrayList<>();
        List<Float> textures = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        processVertices(aiMesh, vertices);
        processNormals(aiMesh, normals);
        processTextureCoordinates(aiMesh, textures);
        processIndices(aiMesh, indices);

        if (textures.size() == 0) {
            int numElements = (vertices.size() / 3) * 2;

            for (int i = 0; i < numElements; i++)
                textures.add(0.0f);
        }

        Mesh mesh = new Mesh(Utils.listToArray(vertices),
                Utils.listToArray(textures),
                Utils.listToArray(normals),
                Utils.listIntToArray(indices));

        Material material;

        int materialIndex = aiMesh.mMaterialIndex();

        if (materialIndex >= 0 && materialIndex < materials.size())
            material = materials.get(materialIndex);
        else
            material = new Material();

        mesh.setMaterial(material);

        return mesh;
    }

    /**
     * Load normals.
     *
     * @param aiMesh  loaded mesh
     * @param normals normals list
     */
    protected static void processNormals(AIMesh aiMesh, List<Float> normals) {
        AIVector3D.Buffer aiNormals = aiMesh.mNormals();

        while (aiNormals != null && aiNormals.remaining() > 0) {
            AIVector3D aiNormal = aiNormals.get();

            normals.add(aiNormal.x());
            normals.add(aiNormal.y());
            normals.add(aiNormal.z());
        }
    }

    /**
     * Loade texture coordinates.
     *
     * @param aiMesh   loaded mesh
     * @param textures textures list
     */
    protected static void processTextureCoordinates(AIMesh aiMesh, List<Float> textures) {
        AIVector3D.Buffer textureCoordinates = aiMesh.mTextureCoords(0);

        int textureCoordinatesAmount = textureCoordinates != null ? textureCoordinates.remaining() : 0;

        for (int i = 0; i < textureCoordinatesAmount; i++) {
            AIVector3D textureCoordinate = textureCoordinates.get();

            textures.add(textureCoordinate.x());
            textures.add(1 - textureCoordinate.y());
        }
    }

    /**
     * Load vertices.
     *
     * @param aiMesh   loaded mesh
     * @param vertices vertices list
     */
    protected static void processVertices(AIMesh aiMesh, List<Float> vertices) {
        AIVector3D.Buffer aiVertices = aiMesh.mVertices();

        while (aiVertices.remaining() > 0) {
            AIVector3D aiVertex = aiVertices.get();

            vertices.add(aiVertex.x());
            vertices.add(aiVertex.y());
            vertices.add(aiVertex.z());
        }
    }
}
