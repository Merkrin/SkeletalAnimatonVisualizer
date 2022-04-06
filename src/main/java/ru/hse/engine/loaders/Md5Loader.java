package ru.hse.engine.loaders;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import ru.hse.engine.GameItem;
import ru.hse.engine.loaders.md5.Md5JointInfo;
import ru.hse.engine.loaders.md5.Md5Mesh;
import ru.hse.engine.loaders.md5.Md5Model;
import ru.hse.engine.utils.Utils;
import ru.hse.graphics.model.Material;
import ru.hse.graphics.model.Mesh;
import ru.hse.graphics.model.Texture;

import java.util.ArrayList;
import java.util.List;

public class Md5Loader {
    private static final String NORMAL_FILE_SUFFIX = "_normal";

    public static GameItem process(Md5Model md5Model, Vector4f defaultColour) throws Exception {
        List<Md5Mesh> md5MeshList = md5Model.getMeshes();

        List<Mesh> list = new ArrayList<>();
        for (Md5Mesh md5Mesh : md5Model.getMeshes()) {
            Mesh mesh = generateMesh(md5Model, md5Mesh, defaultColour);
            handleTexture(mesh, md5Mesh, defaultColour);
            list.add(mesh);
        }
        Mesh[] meshes = new Mesh[list.size()];
        meshes = list.toArray(meshes);
        GameItem gameItem = new GameItem(meshes);

        return gameItem;
    }

    private static Mesh generateMesh(Md5Model md5Model, Md5Mesh md5Mesh, Vector4f defaultColour) throws Exception {
        List<VertexInfo> vertexInfoList = new ArrayList<>();
        List<Float> textCoords = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        List<Md5Mesh.MD5Vertex> vertices = md5Mesh.getVertices();
        List<Md5Mesh.Md5Weight> weights = md5Mesh.getWeights();
        List<Md5JointInfo.Md5JointData> joints = md5Model.getJointInfo().getJoints();

        for (Md5Mesh.MD5Vertex vertex : vertices) {
            Vector3f vertexPos = new Vector3f();
            Vector2f vertexTextCoords = vertex.getTextCoords();
            textCoords.add(vertexTextCoords.x);
            textCoords.add(vertexTextCoords.y);

            int startWeight = vertex.getStartWeight();
            int numWeights = vertex.getWeightCount();

            for (int i = startWeight; i < startWeight + numWeights; i++) {
                Md5Mesh.Md5Weight weight = weights.get(i);
                Md5JointInfo.Md5JointData joint = joints.get(weight.getJointIndex());
                Vector3f rotatedPos = new Vector3f(weight.getPosition()).rotate(joint.getOrientation());
                Vector3f acumPos = new Vector3f(joint.getPosition()).add(rotatedPos);
                acumPos.mul(weight.getBias());
                vertexPos.add(acumPos);
            }

            vertexInfoList.add(new VertexInfo(vertexPos));
        }

        for (Md5Mesh.MD5Triangle tri : md5Mesh.getTriangles()) {
            indices.add(tri.getVertex0());
            indices.add(tri.getVertex1());
            indices.add(tri.getVertex2());

            // Normals
            VertexInfo v0 = vertexInfoList.get(tri.getVertex0());
            VertexInfo v1 = vertexInfoList.get(tri.getVertex1());
            VertexInfo v2 = vertexInfoList.get(tri.getVertex2());
            Vector3f pos0 = v0.position;
            Vector3f pos1 = v1.position;
            Vector3f pos2 = v2.position;

            Vector3f normal = (new Vector3f(pos2).sub(pos0)).cross(new Vector3f(pos1).sub(pos0));

            v0.normal.add(normal);
            v1.normal.add(normal);
            v2.normal.add(normal);
        }

        // Once the contributions have been added, normalize the result
        for(VertexInfo v : vertexInfoList) {
            v.normal.normalize();
        }

        float[] positionsArr = VertexInfo.toPositionsArr(vertexInfoList);
        float[] textCoordsArr = Utils.listToArray(textCoords);
        float[] normalsArr = VertexInfo.toNormalArr(vertexInfoList);
        int[] indicesArr = indices.stream().mapToInt(i -> i).toArray();
        Mesh mesh = new Mesh(positionsArr, textCoordsArr, normalsArr, indicesArr);

        return mesh;
    }

    private static void handleTexture(Mesh mesh, Md5Mesh md5Mesh, Vector4f defaultColour) throws Exception {
        String texturePath = md5Mesh.getTexture();
        if (texturePath != null && texturePath.length() > 0) {
            Texture texture = new Texture(texturePath);
            Material material = new Material(texture);

            // Handle normal Maps;
            int pos = texturePath.lastIndexOf(".");
            if (pos > 0) {
                String basePath = texturePath.substring(0, pos);
                String extension = texturePath.substring(pos, texturePath.length());
                String normalMapFileName = basePath + NORMAL_FILE_SUFFIX + extension;
                if (Utils.existsResourceFile(normalMapFileName)) {
                    Texture normalMap = new Texture(normalMapFileName);
                    material.setNormalMap(normalMap);
                }
            }
            mesh.setMaterial(material);
        } else {
            mesh.setMaterial(new Material(defaultColour, 1));
        }
    }

    private static class VertexInfo {

        public Vector3f position;

        public Vector3f normal;

        public VertexInfo(Vector3f position) {
            this.position = position;
            normal = new Vector3f();
        }

        public VertexInfo() {
            position = new Vector3f();
            normal = new Vector3f();
        }

        public static float[] toPositionsArr(List<VertexInfo> list) {
            int length = list != null ? list.size() * 3 : 0;
            float[] result = new float[length];
            int i = 0;
            for (VertexInfo v : list) {
                result[i] = v.position.x;
                result[i + 1] = v.position.y;
                result[i + 2] = v.position.z;
                i += 3;
            }
            return result;
        }

        public static float[] toNormalArr(List<VertexInfo> list) {
            int length = list != null ? list.size() * 3 : 0;
            float[] result = new float[length];
            int i = 0;
            for (VertexInfo v : list) {
                result[i] = v.normal.x;
                result[i + 1] = v.normal.y;
                result[i + 2] = v.normal.z;
                i += 3;
            }
            return result;
        }
    }
}
