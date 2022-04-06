package ru.hse.engine.loaders.md5;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Md5BoundInfo {
    private List<Md5Bound> bounds;

    public List<Md5Bound> getBounds() {
        return bounds;
    }

    public void setBounds(List<Md5Bound> bounds) {
        this.bounds = bounds;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("bounds [" + System.lineSeparator());
        for (Md5Bound bound : bounds) {
            str.append(bound).append(System.lineSeparator());
        }
        str.append("]").append(System.lineSeparator());
        return str.toString();
    }

    public static Md5BoundInfo parse(List<String> blockBody) {
        Md5BoundInfo result = new Md5BoundInfo();
        List<Md5Bound> bounds = new ArrayList<>();
        for (String line : blockBody) {
            Md5Bound bound = Md5Bound.parseLine(line);
            if (bound != null) {
                bounds.add(bound);
            }
        }
        result.setBounds(bounds);
        return result;
    }

    private static class Md5Bound {

        private static final Pattern PATTERN_BOUND = Pattern.compile("\\s*" + Md5Utils.VECTOR3_REGEXP + "\\s*" + Md5Utils.VECTOR3_REGEXP + ".*");

        private Vector3f minBound;

        private Vector3f maxBound;

        public Vector3f getMinBound() {
            return minBound;
        }

        public void setMinBound(Vector3f minBound) {
            this.minBound = minBound;
        }

        public Vector3f getMaxBound() {
            return maxBound;
        }

        public void setMaxBound(Vector3f maxBound) {
            this.maxBound = maxBound;
        }

        @Override
        public String toString() {
            return "[minBound: " + minBound + ", maxBound: " + maxBound + "]";
        }

        public static Md5Bound parseLine(String line) {
            Md5Bound result = null;
            Matcher matcher = PATTERN_BOUND.matcher(line);
            if (matcher.matches()) {
                result = new Md5Bound();
                float x = Float.parseFloat(matcher.group(1));
                float y = Float.parseFloat(matcher.group(2));
                float z = Float.parseFloat(matcher.group(3));
                result.setMinBound(new Vector3f(x, y, z));

                x = Float.parseFloat(matcher.group(4));
                y = Float.parseFloat(matcher.group(5));
                z = Float.parseFloat(matcher.group(6));
                result.setMaxBound(new Vector3f(x, y, z));
            }
            return result;
        }

    }
}
