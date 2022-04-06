package ru.hse.engine.loaders.md5;

import ru.hse.engine.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Md5Model {
    private Md5JointInfo jointInfo;

    private Md5ModelHeader header;

    private List<Md5Mesh> meshes;

    public Md5Model() {
        meshes = new ArrayList<>();
    }

    public Md5JointInfo getJointInfo() {
        return jointInfo;
    }

    public void setJointInfo(Md5JointInfo jointInfo) {
        this.jointInfo = jointInfo;
    }

    public Md5ModelHeader getHeader() {
        return header;
    }

    public void setHeader(Md5ModelHeader header) {
        this.header = header;
    }

    public List<Md5Mesh> getMeshes() {
        return meshes;
    }

    public void setMeshes(List<Md5Mesh> meshes) {
        this.meshes = meshes;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("MD5MeshModel: " + System.lineSeparator());
        str.append(getHeader()).append(System.lineSeparator());
        str.append(getJointInfo()).append(System.lineSeparator());

        for (Md5Mesh mesh : meshes) {
            str.append(mesh).append(System.lineSeparator());
        }
        return str.toString();
    }

    public static Md5Model parse(String meshModelFile) throws Exception {
        List<String> lines = Utils.readAllLines(meshModelFile);

        Md5Model result = new Md5Model();

        int numLines = lines != null ? lines.size() : 0;
        if (numLines == 0) {
            throw new Exception("Cannot parse empty file");
        }

        // Parse Header
        boolean headerEnd = false;
        int start = 0;
        for (int i = 0; i < numLines && !headerEnd; i++) {
            String line = lines.get(i);
            headerEnd = line.trim().endsWith("{");
            start = i;
        }
        if (!headerEnd) {
            throw new Exception("Cannot find header");
        }
        List<String> headerBlock = lines.subList(0, start);
        Md5ModelHeader header = Md5ModelHeader.parse(headerBlock);
        result.setHeader(header);

        // Parse the rest of block
        int blockStart = 0;
        boolean inBlock = false;
        String blockId = "";
        for (int i = start; i < numLines; i++) {
            String line = lines.get(i);
            if (line.endsWith("{")) {
                blockStart = i;
                blockId = line.substring(0, line.lastIndexOf(" "));
                inBlock = true;
            } else if (inBlock && line.endsWith("}")) {
                List<String> blockBody = lines.subList(blockStart + 1, i);
                parseBlock(result, blockId, blockBody);
                inBlock = false;
            }
        }

        return result;
    }

    private static void parseBlock(Md5Model model, String blockId, List<String> blockBody) throws Exception {
        switch (blockId) {
            case "joints":
                Md5JointInfo jointInfo = Md5JointInfo.parse(blockBody);
                model.setJointInfo(jointInfo);
                break;
            case "mesh":
                Md5Mesh md5Mesh = Md5Mesh.parse(blockBody);
                model.getMeshes().add(md5Mesh);
                break;
            default:
                break;
        }
    }
}
