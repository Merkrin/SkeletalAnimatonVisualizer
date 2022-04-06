package ru.hse.engine.loaders.md5;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Md5JointInfo {
    private List<Md5JointData> joints;

    public List<Md5JointData> getJoints() {
        return joints;
    }

    public void setJoints(List<Md5JointData> joints) {
        this.joints = joints;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("joints [" + System.lineSeparator());
        for (Md5JointData joint : joints) {
            str.append(joint).append(System.lineSeparator());
        }
        str.append("]").append(System.lineSeparator());
        return str.toString();
    }

    public static Md5JointInfo parse(List<String> blockBody) {
        Md5JointInfo result = new Md5JointInfo();
        List<Md5JointData> joints = new ArrayList<>();
        for (String line : blockBody) {
            Md5JointData jointData = Md5JointData.parseLine(line);
            if (jointData != null) {
                joints.add(jointData);
            }
        }
        result.setJoints(joints);
        return result;
    }

    public static class Md5JointData {

        private static final String PARENT_INDEX_REGEXP = "([-]?\\d+)";

        private static final String NAME_REGEXP = "\\\"([^\\\"]+)\\\"";

        private static final String JOINT_REGEXP = "\\s*" + NAME_REGEXP + "\\s*" + PARENT_INDEX_REGEXP + "\\s*"
                + Md5Utils.VECTOR3_REGEXP + "\\s*" + Md5Utils.VECTOR3_REGEXP + ".*";

        private static final Pattern PATTERN_JOINT = Pattern.compile(JOINT_REGEXP);

        private String name;

        private int parentIndex;

        private Vector3f position;

        private Quaternionf orientation;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getParentIndex() {
            return parentIndex;
        }

        public void setParentIndex(int parentIndex) {
            this.parentIndex = parentIndex;
        }

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
            return "[name: " + name + ", parentIndex: " + parentIndex + ", position: " + position + ", orientation: " + orientation + "]";
        }

        public static Md5JointData parseLine(String line) {
            Md5JointData result = null;
            Matcher matcher = PATTERN_JOINT.matcher(line);
            if (matcher.matches()) {
                result = new Md5JointData();
                result.setName(matcher.group(1));
                result.setParentIndex(Integer.parseInt(matcher.group(2)));
                float x = Float.parseFloat(matcher.group(3));
                float y = Float.parseFloat(matcher.group(4));
                float z = Float.parseFloat(matcher.group(5));
                result.setPosition(new Vector3f(x, y, z));

                x = Float.parseFloat(matcher.group(6));
                y = Float.parseFloat(matcher.group(7));
                z = Float.parseFloat(matcher.group(8));
                result.setOrientation(new Vector3f(x, y, z));
            }
            return result;
        }
    }
}
