package ru.hse.engine.loaders;

import org.joml.*;
import ru.hse.engine.GameItem;
import ru.hse.engine.animation.AnimGameItem;
import ru.hse.engine.animation.AnimVertex;
import ru.hse.engine.animation.AnimatedFrame;
import ru.hse.engine.loaders.md5.*;
import ru.hse.engine.utils.Utils;
import ru.hse.graphics.model.Material;
import ru.hse.graphics.model.Mesh;
import ru.hse.graphics.model.Texture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Md5Loader {
    private static final String NORMAL_FILE_SUFFIX = "_normal";

    /**
     * Constructs and AnimGameItem instace based on a MD5 Model an MD5 Animation
     *
     * @param md5Model The MD5 Model
     * @param animModel The MD5 Animation
     * @param defaultColour Default colour to use if there are no textures
     * @return
     * @throws Exception
     */
    public static AnimGameItem process(Md5Model md5Model, Md5AnimModel animModel, Vector4f defaultColour) throws Exception {
        List<Matrix4f> invJointMatrices = calcInJointMatrices(md5Model);
        List<AnimatedFrame> animatedFrames = processAnimationFrames(md5Model, animModel, invJointMatrices);

        List<Mesh> list = new ArrayList<>();
        for (Md5Mesh md5Mesh : md5Model.getMeshes()) {
            Mesh mesh = generateMesh(md5Model, md5Mesh);
            handleTexture(mesh, md5Mesh, defaultColour);
            list.add(mesh);
        }

        Mesh[] meshes = new Mesh[list.size()];
        meshes = list.toArray(meshes);

        AnimGameItem result = new AnimGameItem(meshes, animatedFrames, invJointMatrices);
        return result;
    }

    private static List<Matrix4f> calcInJointMatrices(Md5Model md5Model) {
        List<Matrix4f> result = new ArrayList<>();

        List<Md5JointInfo.Md5JointData> joints = md5Model.getJointInfo().getJoints();
        for (Md5JointInfo.Md5JointData joint : joints) {
            // Calculate translation matrix using joint position
            // Calculates rotation matrix using joint orientation
            // Gets transformation matrix bu multiplying translation matrix by rotation matrix
            // Instead of multiplying we can apply rotation which is optimized internally
            Matrix4f mat = new Matrix4f()
                    .translate(joint.getPosition())
                    .rotate(joint.getOrientation())
                    .invert();
            result.add(mat);
        }
        return result;
    }

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

    private static Mesh generateMesh(Md5Model md5Model, Md5Mesh md5Mesh) {
        List<AnimVertex> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        List<Md5Mesh.MD5Vertex> md5Vertices = md5Mesh.getVertices();
        List<Md5Mesh.Md5Weight> weights = md5Mesh.getWeights();
        List<Md5JointInfo.Md5JointData> joints = md5Model.getJointInfo().getJoints();

        for (Md5Mesh.MD5Vertex md5Vertex : md5Vertices) {
            AnimVertex vertex = new AnimVertex();
            vertices.add(vertex);

            vertex.position = new Vector3f();
            vertex.textCoords = md5Vertex.getTextCoords();

            int startWeight = md5Vertex.getStartWeight();
            int numWeights = md5Vertex.getWeightCount();

            vertex.jointIndices = new int[numWeights];
            Arrays.fill(vertex.jointIndices, -1);
            vertex.weights = new float[numWeights];
            Arrays.fill(vertex.weights, -1);
            for (int i = startWeight; i < startWeight + numWeights; i++) {
                Md5Mesh.Md5Weight weight = weights.get(i);
                Md5JointInfo.Md5JointData joint = joints.get(weight.getJointIndex());
                Vector3f rotatedPos = new Vector3f(weight.getPosition()).rotate(joint.getOrientation());
                Vector3f acumPos = new Vector3f(joint.getPosition()).add(rotatedPos);
                acumPos.mul(weight.getBias());
                vertex.position.add(acumPos);
                vertex.jointIndices[i - startWeight] = weight.getJointIndex();
                vertex.weights[i - startWeight] = weight.getBias();
            }
        }

        for (Md5Mesh.MD5Triangle tri : md5Mesh.getTriangles()) {
            indices.add(tri.getVertex0());
            indices.add(tri.getVertex1());
            indices.add(tri.getVertex2());

            // Normals
            AnimVertex v0 = vertices.get(tri.getVertex0());
            AnimVertex v1 = vertices.get(tri.getVertex1());
            AnimVertex v2 = vertices.get(tri.getVertex2());
            Vector3f pos0 = v0.position;
            Vector3f pos1 = v1.position;
            Vector3f pos2 = v2.position;

            Vector3f normal = (new Vector3f(pos2).sub(pos0)).cross(new Vector3f(pos1).sub(pos0));

            v0.normal.add(normal);
            v1.normal.add(normal);
            v2.normal.add(normal);
        }

        // Once the contributions have been added, normalize the result
        for(AnimVertex v : vertices) {
            v.normal.normalize();
        }

        Mesh mesh = createMesh(vertices, indices);
        return mesh;
    }

    private static List<AnimatedFrame> processAnimationFrames(Md5Model md5Model, Md5AnimModel animModel, List<Matrix4f> invJointMatrices) {
        List<AnimatedFrame> animatedFrames = new ArrayList<>();
        List<Md5Frame> frames = animModel.getFrames();
        for (Md5Frame frame : frames) {
            AnimatedFrame data = processAnimationFrame(md5Model, animModel, frame, invJointMatrices);
            animatedFrames.add(data);
        }
        return animatedFrames;
    }

    private static AnimatedFrame processAnimationFrame(Md5Model md5Model, Md5AnimModel animModel, Md5Frame frame, List<Matrix4f> invJointMatrices) {
        AnimatedFrame result = new AnimatedFrame();

        Md5BaseFrame baseFrame = animModel.getBaseFrame();
        List<Md5Hierarchy.Md5HierarchyData> hierarchyList = animModel.getHierarchy().getHierarchyDataList();

        List<Md5JointInfo.Md5JointData> joints = md5Model.getJointInfo().getJoints();
        int numJoints = joints.size();
        float[] frameData = frame.getFrameData();
        for (int i = 0; i < numJoints; i++) {
            Md5JointInfo.Md5JointData joint = joints.get(i);
            Md5BaseFrame.Md5BaseFrameData baseFrameData = baseFrame.getFrameDataList().get(i);
            Vector3f position = baseFrameData.getPosition();
            Quaternionf orientation = baseFrameData.getOrientation();

            int flags = hierarchyList.get(i).getFlags();
            int startIndex = hierarchyList.get(i).getStartIndex();

            if ((flags & 1) > 0) {
                position.x = frameData[startIndex++];
            }
            if ((flags & 2) > 0) {
                position.y = frameData[startIndex++];
            }
            if ((flags & 4) > 0) {
                position.z = frameData[startIndex++];
            }
            if ((flags & 8) > 0) {
                orientation.x = frameData[startIndex++];
            }
            if ((flags & 16) > 0) {
                orientation.y = frameData[startIndex++];
            }
            if ((flags & 32) > 0) {
                orientation.z = frameData[startIndex++];
            }
            // Update Quaternion's w component
            orientation = Md5Utils.calculateQuaternion(orientation.x, orientation.y, orientation.z);

            // Calculate translation and rotation matrices for this joint
            Matrix4f translateMat = new Matrix4f().translate(position);
            Matrix4f rotationMat = new Matrix4f().rotate(orientation);
            Matrix4f jointMat = translateMat.mul(rotationMat);

            // Joint position is relative to joint's parent index position. Use parent matrices
            // to transform it to model space
            if (joint.getParentIndex() > -1) {
                Matrix4f parentMatrix = result.getLocalJointMatrices()[joint.getParentIndex()];
                jointMat = new Matrix4f(parentMatrix).mul(jointMat);
            }

            result.setMatrix(i, jointMat, invJointMatrices.get(i));
        }

        return result;
    }

    private static Mesh createMesh(List<AnimVertex> vertices, List<Integer> indices) {
        List<Float> positions = new ArrayList<>();
        List<Float> textCoords = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Integer> jointIndices = new ArrayList<>();
        List<Float> weights = new ArrayList<>();

        for (AnimVertex vertex : vertices) {
            positions.add(vertex.position.x);
            positions.add(vertex.position.y);
            positions.add(vertex.position.z);

            textCoords.add(vertex.textCoords.x);
            textCoords.add(vertex.textCoords.y);

            normals.add(vertex.normal.x);
            normals.add(vertex.normal.y);
            normals.add(vertex.normal.z);

            int numWeights = vertex.weights.length;
            for (int i = 0; i < Mesh.MAX_WEIGHTS; i++) {
                if (i < numWeights) {
                    jointIndices.add(vertex.jointIndices[i]);
                    weights.add(vertex.weights[i]);
                } else {
                    jointIndices.add(-1);
                    weights.add(-1.0f);
                }
            }
        }

        float[] positionsArr = Utils.listToArray(positions);
        float[] textCoordsArr = Utils.listToArray(textCoords);
        float[] normalsArr = Utils.listToArray(normals);
        int[] indicesArr = Utils.listIntToArray(indices);
        int[] jointIndicesArr = Utils.listIntToArray(jointIndices);
        float[] weightsArr = Utils.listToArray(weights);

        Mesh result = new Mesh(positionsArr, textCoordsArr, normalsArr, indicesArr, jointIndicesArr, weightsArr);

        return result;
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
