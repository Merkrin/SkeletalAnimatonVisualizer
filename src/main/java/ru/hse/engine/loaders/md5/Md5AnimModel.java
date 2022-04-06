package ru.hse.engine.loaders.md5;

import ru.hse.engine.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Md5AnimModel {
    private Md5AnimHeader header;

    private Md5Hierarchy hierarchy;

    private Md5BoundInfo boundInfo;

    private Md5BaseFrame baseFrame;

    private List<Md5Frame> frames;

    public Md5AnimModel() {
        frames = new ArrayList<>();
    }

    public Md5AnimHeader getHeader() {
        return header;
    }

    public void setHeader(Md5AnimHeader header) {
        this.header = header;
    }

    public Md5Hierarchy getHierarchy() {
        return hierarchy;
    }

    public void setHierarchy(Md5Hierarchy hierarchy) {
        this.hierarchy = hierarchy;
    }

    public Md5BoundInfo getBoundInfo() {
        return boundInfo;
    }

    public void setBoundInfo(Md5BoundInfo boundInfo) {
        this.boundInfo = boundInfo;
    }

    public Md5BaseFrame getBaseFrame() {
        return baseFrame;
    }

    public void setBaseFrame(Md5BaseFrame baseFrame) {
        this.baseFrame = baseFrame;
    }

    public List<Md5Frame> getFrames() {
        return frames;
    }

    public void setFrames(List<Md5Frame> frames) {
        this.frames = frames;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("MD5AnimModel: " + System.lineSeparator());
        str.append(getHeader()).append(System.lineSeparator());
        str.append(getHierarchy()).append(System.lineSeparator());
        str.append(getBoundInfo()).append(System.lineSeparator());
        str.append(getBaseFrame()).append(System.lineSeparator());

        for (Md5Frame frame : frames) {
            str.append(frame).append(System.lineSeparator());
        }
        return str.toString();
    }

    public static Md5AnimModel parse(String animFile) throws Exception {
        List<String> lines = Utils.readAllLines(animFile);

        Md5AnimModel result = new Md5AnimModel();

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
        Md5AnimHeader header = Md5AnimHeader.parse(headerBlock);
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

    private static void parseBlock(Md5AnimModel model, String blockId, List<String> blockBody) throws Exception {
        switch (blockId) {
            case "hierarchy":
                Md5Hierarchy hierarchy = Md5Hierarchy.parse(blockBody);
                model.setHierarchy(hierarchy);
                break;
            case "bounds":
                Md5BoundInfo boundInfo = Md5BoundInfo.parse(blockBody);
                model.setBoundInfo(boundInfo);
                break;
            case "baseframe":
                Md5BaseFrame baseFrame = Md5BaseFrame.parse(blockBody);
                model.setBaseFrame(baseFrame);
                break;
            default:
                if (blockId.startsWith("frame ")) {
                    Md5Frame frame = Md5Frame.parse(blockId, blockBody);
                    model.getFrames().add(frame);
                }
                break;
        }
    }
}
