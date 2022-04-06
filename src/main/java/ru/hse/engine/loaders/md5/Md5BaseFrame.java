package ru.hse.engine.loaders.md5;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Md5BaseFrame {
    private List<Md5BaseFrameData> frameDataList;

    public List<Md5BaseFrameData> getFrameDataList() {
        return frameDataList;
    }

    public void setFrameDataList(List<Md5BaseFrameData> frameDataList) {
        this.frameDataList = frameDataList;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("base frame [" + System.lineSeparator());
        for (Md5BaseFrameData frameData : frameDataList) {
            str.append(frameData).append(System.lineSeparator());
        }
        str.append("]").append(System.lineSeparator());
        return str.toString();
    }

    public static Md5BaseFrame parse(List<String> blockBody) {
        Md5BaseFrame result = new Md5BaseFrame();

        List<Md5BaseFrameData> frameInfoList = new ArrayList<>();
        result.setFrameDataList(frameInfoList);

        for (String line : blockBody) {
            Md5BaseFrameData frameInfo = Md5BaseFrameData.parseLine(line);
            if (frameInfo != null) {
                frameInfoList.add(frameInfo);
            }
        }

        return result;
    }

    public static class Md5BaseFrameData {

        private static final Pattern PATTERN_BASEFRAME = Pattern.compile("\\s*" + Md5Utils.VECTOR3_REGEXP + "\\s*" + Md5Utils.VECTOR3_REGEXP + ".*");

        private Vector3f position;

        private Quaternionf orientation;

        public Vector3f getPosition() {
            return position;
        }

        public void setPosition(Vector3f position) {
            this.position = position;
        }

        public Quaternionf getOrientation() {
            return orientation;
        }

        public void setOrientation(Vector3f vec) {
            this.orientation = Md5Utils.calculateQuaternion(vec);
        }

        @Override
        public String toString() {
            return "[position: " + position + ", orientation: " + orientation + "]";
        }

        public static Md5BaseFrameData parseLine(String line) {
            Matcher matcher = PATTERN_BASEFRAME.matcher(line);
            Md5BaseFrameData result = null;
            if (matcher.matches()) {
                result = new Md5BaseFrameData();
                float x = Float.parseFloat(matcher.group(1));
                float y = Float.parseFloat(matcher.group(2));
                float z = Float.parseFloat(matcher.group(3));
                result.setPosition(new Vector3f(x, y, z));

                x = Float.parseFloat(matcher.group(4));
                y = Float.parseFloat(matcher.group(5));
                z = Float.parseFloat(matcher.group(6));
                result.setOrientation(new Vector3f(x, y, z));
            }

            return result;
        }
    }
}
