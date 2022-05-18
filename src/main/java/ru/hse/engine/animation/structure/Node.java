package ru.hse.engine.animation.structure;

import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Skeleton's node representation class.
 */
public class Node {
    private final List<Node> children;

    private final String name;

    private final Node parent;

    private final Matrix4f nodeTransformation;

    /**
     * The class' constructor.
     *
     * @param name               name of the node
     * @param parent             parent of the node
     * @param nodeTransformation transformation of the node
     */
    public Node(String name, Node parent, Matrix4f nodeTransformation) {
        this.name = name;
        this.parent = parent;
        this.nodeTransformation = nodeTransformation;
        this.children = new ArrayList<>();
    }

    /**
     * Node transformation getter.
     *
     * @return node transformation
     */
    public Matrix4f getNodeTransformation() {
        return nodeTransformation;
    }

    /**
     * Method to add child to the node.
     *
     * @param node child node
     */
    public void addChild(Node node) {
        this.children.add(node);
    }

    /**
     * Node children getter.
     *
     * @return children of the node
     */
    public List<Node> getChildren() {
        return children;
    }

    /**
     * Node name getter.
     *
     * @return node name
     */
    public String getName() {
        return name;
    }

    /**
     * Node parent getter.
     *
     * @return node parent
     */
    public Node getParent() {
        return parent;
    }
}
