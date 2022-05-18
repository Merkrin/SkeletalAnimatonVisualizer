package ru.hse.engine.loaders;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import ru.hse.engine.animation.AnimatedItem;
import ru.hse.engine.animation.AnimatedFrame;
import ru.hse.engine.animation.Animation;
import ru.hse.engine.animation.structure.Bone;
import ru.hse.engine.animation.structure.Node;
import ru.hse.engine.animation.structure.VertexWeight;
import ru.hse.engine.utils.Utils;
import ru.hse.graphics.model.Material;
import ru.hse.graphics.model.Mesh;

import java.util.*;
import java.util.stream.Collectors;

import static org.lwjgl.assimp.Assimp.*;

/**
 * Loader of an animated mesh.
 */
public class AnimatedMeshLoader extends StaticMeshLoader {
    /**
     * Load animated item.
     *
     * @param meshPath          path to the mesh
     * @param texturesDirectory mesh's texture directory
     * @return loaded mesh
     * @throws Exception an unhandled exception
     */
    public static AnimatedItem loadAnimatedItem(String meshPath, String texturesDirectory) throws Exception {
        return loadAnimatedItem(meshPath, texturesDirectory,
                aiProcess_GenSmoothNormals | aiProcess_JoinIdenticalVertices | aiProcess_Triangulate
                        | aiProcess_FixInfacingNormals | aiProcess_LimitBoneWeights);
    }

    /**
     * Load animated item.
     *
     * @param meshPath          path to the mesh
     * @param texturesDirectory mesh's texture directory
     * @param flags             loading flags
     * @return loaded mesh
     * @throws Exception an unhandled exception
     */
    public static AnimatedItem loadAnimatedItem(String meshPath, String texturesDirectory, int flags)
            throws Exception {
        AIScene aiScene = aiImportFile(meshPath, flags);

        if (aiScene == null)
            throw new Exception("Error loading model");

        int materialsNumber = aiScene.mNumMaterials();

        PointerBuffer aiMaterials = aiScene.mMaterials();

        if (aiMaterials == null)
            throw new Exception("Error loading model");

        List<Material> materials = new ArrayList<>();

        for (int i = 0; i < materialsNumber; i++) {
            AIMaterial aiMaterial = AIMaterial.create(aiMaterials.get(i));

            processMaterial(aiMaterial, materials, texturesDirectory);
        }

        List<Bone> boneList = new ArrayList<>();

        int meshesNumber = aiScene.mNumMeshes();

        PointerBuffer aiMeshes = aiScene.mMeshes();

        if (aiMeshes == null)
            throw new Exception("Error loading model");

        Mesh[] meshes = new Mesh[meshesNumber];

        for (int i = 0; i < meshesNumber; i++) {
            AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));

            Mesh mesh = processMesh(aiMesh, materials, boneList);

            meshes[i] = mesh;
        }

        Node rootNode = buildNodesTree(Objects.requireNonNull(aiScene.mRootNode()), null);

        Matrix4f globalInverseTransformation = toMatrix(Objects.requireNonNull(aiScene.mRootNode())
                .mTransformation())
                .invert();

        Map<String, Animation> animations = processAnimations(aiScene, boneList, rootNode, globalInverseTransformation);

        return new AnimatedItem(meshes, animations);
    }

    /**
     * Build tree of nodes.
     *
     * @param aiNode     loaded node
     * @param parentNode parent node
     * @return root node
     * @throws Exception an unhandled exception
     */
    private static Node buildNodesTree(AINode aiNode, Node parentNode) throws Exception {
        String nodeName = aiNode.mName().dataString();

        Node node = new Node(nodeName, parentNode, toMatrix(aiNode.mTransformation()));

        int childrenAmount = aiNode.mNumChildren();

        PointerBuffer aiChildren = aiNode.mChildren();

        for (int i = 0; i < childrenAmount; i++) {
            AINode aiChildNode = AINode.create(aiChildren.get(i));
            Node childNode = buildNodesTree(aiChildNode, node);
            node.addChild(childNode);
        }

        return node;
    }

    /**
     * Load animations.
     *
     * @param aiScene                     loaded scene
     * @param boneList                    list of bones
     * @param rootNode                    root node
     * @param globalInverseTransformation global inverse transformation
     * @return loaded animations
     * @throws Exception an unhandled exception
     */
    private static Map<String, Animation> processAnimations(AIScene aiScene, List<Bone> boneList,
                                                            Node rootNode, Matrix4f globalInverseTransformation)
            throws Exception {
        Map<String, Animation> animations = new HashMap<>();

        int animationsAmount = aiScene.mNumAnimations();

        PointerBuffer aiAnimations = aiScene.mAnimations();

        if (aiAnimations == null)
            throw new Exception("Error loading model");

        for (int i = 0; i < animationsAmount; i++) {
            AIAnimation aiAnimation = AIAnimation.create(aiAnimations.get(i));

            int maxFrames = calculateAnimationMaximalFramesAmount(aiAnimation);

            List<AnimatedFrame> frames = new ArrayList<>();

            Animation animation = new Animation(aiAnimation.mName().dataString(), frames, aiAnimation.mDuration());

            animations.put(animation.getName(), animation);

            for (int j = 0; j < maxFrames; j++) {
                AnimatedFrame animatedFrame = new AnimatedFrame();

                buildFrameMatrices(aiAnimation, boneList, animatedFrame, j, rootNode,
                        rootNode.getNodeTransformation(), globalInverseTransformation);

                frames.add(animatedFrame);
            }
        }

        return animations;
    }

    /**
     * Build matrices for each frame.
     *
     * @param aiAnimation                 loaded animation
     * @param boneList                    bones list
     * @param animatedFrame               animation frame
     * @param frame                       current frame
     * @param node                        current node
     * @param parentTransformation        parent transformation
     * @param globalInverseTransformation global inverse transformation
     * @throws Exception an unhandled exception
     */
    private static void buildFrameMatrices(AIAnimation aiAnimation, List<Bone> boneList, AnimatedFrame animatedFrame,
                                           int frame, Node node, Matrix4f parentTransformation,
                                           Matrix4f globalInverseTransformation) throws Exception {
        String nodeName = node.getName();

        AINodeAnim aiNodeAnim = findAIAnimNode(aiAnimation, nodeName);

        Matrix4f nodeTransformation = node.getNodeTransformation();

        if (aiNodeAnim != null)
            nodeTransformation = buildNodeTransformationMatrix(aiNodeAnim, frame);

        Matrix4f nodeGlobalTransformation = new Matrix4f(parentTransformation).mul(nodeTransformation);

        List<Bone> affectedBones = boneList.stream().filter(b -> b.getBoneName()
                        .equals(nodeName))
                .collect(Collectors.toList());

        for (Bone bone : affectedBones) {
            Matrix4f boneTransform = new Matrix4f(globalInverseTransformation)
                    .mul(nodeGlobalTransformation)
                    .mul(bone.getOffsetMatrix());

            animatedFrame.setJointMatrix(bone.getBoneId(), boneTransform);
        }

        for (Node childNode : node.getChildren())
            buildFrameMatrices(aiAnimation, boneList, animatedFrame, frame, childNode,
                    nodeGlobalTransformation, globalInverseTransformation);
    }

    /**
     * Build transformation matrix for the node.
     *
     * @param aiNodeAnim loaded node animation
     * @param frame      current frame
     * @return node transformation matrix
     * @throws Exception an unhandled exception
     */
    private static Matrix4f buildNodeTransformationMatrix(AINodeAnim aiNodeAnim, int frame) throws Exception {
        AIVectorKey.Buffer positionKeys = aiNodeAnim.mPositionKeys();

        if (positionKeys == null)
            throw new Exception("Error loading model");

        AIVectorKey.Buffer scalingKeys = aiNodeAnim.mScalingKeys();

        if (scalingKeys == null)
            throw new Exception("Error loading model");

        AIQuatKey.Buffer rotationKeys = aiNodeAnim.mRotationKeys();

        if (rotationKeys == null)
            throw new Exception("Error loading model");

        AIVectorKey aiVectorKey;
        AIVector3D aiVector3D;

        Matrix4f nodeTransform = new Matrix4f();

        int positionsAmount = aiNodeAnim.mNumPositionKeys();

        if (positionsAmount > 0) {
            aiVectorKey = positionKeys.get(Math.min(positionsAmount - 1, frame));
            aiVector3D = aiVectorKey.mValue();

            nodeTransform.translate(aiVector3D.x(), aiVector3D.y(), aiVector3D.z());
        }

        int rotationsAmount = aiNodeAnim.mNumRotationKeys();

        if (rotationsAmount > 0) {
            AIQuatKey aiQuatKey = rotationKeys.get(Math.min(rotationsAmount - 1, frame));
            AIQuaternion aiQuaternion = aiQuatKey.mValue();

            Quaternionf quaternion = new Quaternionf(aiQuaternion.x(),
                    aiQuaternion.y(),
                    aiQuaternion.z(),
                    aiQuaternion.w());

            nodeTransform.rotate(quaternion);
        }

        int scalingKeysAmount = aiNodeAnim.mNumScalingKeys();

        if (scalingKeysAmount > 0) {
            aiVectorKey = scalingKeys.get(Math.min(scalingKeysAmount - 1, frame));
            aiVector3D = aiVectorKey.mValue();

            nodeTransform.scale(aiVector3D.x(), aiVector3D.y(), aiVector3D.z());
        }

        return nodeTransform;
    }

    /**
     * Find animation node.
     *
     * @param aiAnimation loded animation
     * @param nodeName    node name
     * @return animation node
     * @throws Exception an unhandled exception
     */
    private static AINodeAnim findAIAnimNode(AIAnimation aiAnimation, String nodeName) throws Exception {
        AINodeAnim result = null;

        int animNodesAmount = aiAnimation.mNumChannels();

        PointerBuffer aiChannels = aiAnimation.mChannels();

        if (aiChannels == null)
            throw new Exception("Error loading model");

        for (int i = 0; i < animNodesAmount; i++) {
            AINodeAnim aiNodeAnim = AINodeAnim.create(aiChannels.get(i));

            if (nodeName.equals(aiNodeAnim.mNodeName().dataString())) {
                result = aiNodeAnim;
                break;
            }
        }
        return result;
    }

    /**
     * Calculate animation maximal frames amount.
     *
     * @param aiAnimation loaded animation
     * @return frames amount
     * @throws Exception an unhandled exception
     */
    private static int calculateAnimationMaximalFramesAmount(AIAnimation aiAnimation) throws Exception {
        int maximalFramesAmount = 0;

        int nodeAnimationsAmount = aiAnimation.mNumChannels();

        PointerBuffer aiChannels = aiAnimation.mChannels();

        if (aiChannels == null)
            throw new Exception("Error loading model");

        for (int i = 0; i < nodeAnimationsAmount; i++) {
            AINodeAnim aiNodeAnim = AINodeAnim.create(aiChannels.get(i));

            int numFrames = Math.max(Math.max(aiNodeAnim.mNumPositionKeys(),
                            aiNodeAnim.mNumScalingKeys()),
                    aiNodeAnim.mNumRotationKeys());

            maximalFramesAmount = Math.max(maximalFramesAmount, numFrames);
        }

        return maximalFramesAmount;
    }

    /**
     * Load bones.
     *
     * @param aiMesh   loaded mesh
     * @param boneList bones list
     * @param boneIds  bones ids
     * @param weights  weights
     * @throws Exception an unhandled exception
     */
    private static void processBones(AIMesh aiMesh, List<Bone> boneList, List<Integer> boneIds, List<Float> weights)
            throws Exception {
        Map<Integer, List<VertexWeight>> weightSet = new HashMap<>();

        int bonesAmount = aiMesh.mNumBones();

        PointerBuffer aiBones = aiMesh.mBones();

        if (aiBones == null)
            throw new Exception("Error loading model");

        for (int i = 0; i < bonesAmount; i++) {
            AIBone aiBone = AIBone.create(aiBones.get(i));

            int id = boneList.size();

            Bone bone = new Bone(id, aiBone.mName().dataString(), toMatrix(aiBone.mOffsetMatrix()));

            boneList.add(bone);

            int weightsAmount = aiBone.mNumWeights();

            AIVertexWeight.Buffer aiWeights = aiBone.mWeights();

            for (int j = 0; j < weightsAmount; j++) {
                AIVertexWeight aiVertexWeight = aiWeights.get(j);

                VertexWeight vertexWeight = new VertexWeight(bone.getBoneId(),
                        aiVertexWeight.mVertexId(), aiVertexWeight.mWeight());

                List<VertexWeight> vertexWeightList = weightSet.computeIfAbsent(vertexWeight.getVertexId(), k -> new ArrayList<>());

                vertexWeightList.add(vertexWeight);
            }
        }

        int verticesAmount = aiMesh.mNumVertices();

        for (int i = 0; i < verticesAmount; i++) {
            List<VertexWeight> vertexWeightList = weightSet.get(i);

            int size = vertexWeightList != null ? vertexWeightList.size() : 0;

            for (int j = 0; j < Mesh.MAX_WEIGHTS; j++) {
                if (j < size) {
                    VertexWeight vertexWeight = vertexWeightList.get(j);

                    weights.add(vertexWeight.getWeight());
                    boneIds.add(vertexWeight.getBoneId());
                } else {
                    weights.add(0.0f);
                    boneIds.add(0);
                }
            }
        }
    }

    /**
     * Load mesh.
     *
     * @param aiMesh    loaded mesh
     * @param materials materials
     * @param boneList  bones list
     * @return mesh
     * @throws Exception an unhandled exception
     */
    private static Mesh processMesh(AIMesh aiMesh, List<Material> materials, List<Bone> boneList) throws Exception {
        List<Float> vertices = new ArrayList<>();
        List<Float> textures = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        List<Integer> boneIds = new ArrayList<>();
        List<Float> weights = new ArrayList<>();

        processVertices(aiMesh, vertices);
        processNormals(aiMesh, normals);
        processTextureCoordinates(aiMesh, textures);
        processIndices(aiMesh, indices);
        processBones(aiMesh, boneList, boneIds, weights);

        if (textures.size() == 0) {
            int elementsAmount = (vertices.size() / 3) * 2;

            for (int i = 0; i < elementsAmount; i++)
                textures.add(0.0f);
        }

        Mesh mesh = new Mesh(Utils.listToArray(vertices), Utils.listToArray(textures),
                Utils.listToArray(normals), Utils.listIntToArray(indices),
                Utils.listIntToArray(boneIds), Utils.listToArray(weights));

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
     * Utility method for creating Matrix4f.
     *
     * @param aiMatrix4x4 loaded matrix
     * @return Matrix4f
     */
    private static Matrix4f toMatrix(AIMatrix4x4 aiMatrix4x4) {
        Matrix4f result = new Matrix4f();

        result.m00(aiMatrix4x4.a1());
        result.m10(aiMatrix4x4.a2());
        result.m20(aiMatrix4x4.a3());
        result.m30(aiMatrix4x4.a4());
        result.m01(aiMatrix4x4.b1());
        result.m11(aiMatrix4x4.b2());
        result.m21(aiMatrix4x4.b3());
        result.m31(aiMatrix4x4.b4());
        result.m02(aiMatrix4x4.c1());
        result.m12(aiMatrix4x4.c2());
        result.m22(aiMatrix4x4.c3());
        result.m32(aiMatrix4x4.c4());
        result.m03(aiMatrix4x4.d1());
        result.m13(aiMatrix4x4.d2());
        result.m23(aiMatrix4x4.d3());
        result.m33(aiMatrix4x4.d4());

        return result;
    }
}
