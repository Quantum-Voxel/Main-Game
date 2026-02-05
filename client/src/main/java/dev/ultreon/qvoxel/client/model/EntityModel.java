package dev.ultreon.qvoxel.client.model;

import dev.ultreon.qvoxel.client.render.Mesh;
import dev.ultreon.qvoxel.client.shader.ShaderProgram;
import dev.ultreon.qvoxel.client.world.Renderable;
import dev.ultreon.qvoxel.entity.Entity;
import dev.ultreon.qvoxel.resource.ResourceManager;
import dev.ultreon.qvoxel.util.Deletable;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface EntityModel<T extends Entity> extends Renderable, Deletable {
    void load(ResourceManager resources) throws ModelException;

    class Node {
        public static final Node[] EMPTY_NODE_ARRAY = new Node[0];

        // Local transformation info, immutable and model specific.
        private final Vector3f localPos = new Vector3f();
        private final Quaternionf localRot = new Quaternionf();
        private final Vector3f localScale = new Vector3f(1);

        // Transformation info, mutable and used for stuff like head rotation and animations.
        public final Vector3f pos = new Vector3f();
        public final Quaternionf rot = new Quaternionf(0, 0, 0, 1);
        public final Vector3f scale = new Vector3f(1);

        private String name;
        // Sub-nodes for recursive rendering, and mesh for drawing the shape.
        public final Node[] subNodes;
        private final Matrix4f transformation;
        public final @Nullable Mesh mesh;

        public Node(String name, Node[] subNodes, Matrix4f transformation) {
            this(name, subNodes, transformation, null);
        }

        public Node(String name, Matrix4f transformation, @Nullable Mesh mesh) {
            this(name, EMPTY_NODE_ARRAY, transformation, mesh);
        }

        public Node(String name, Node[] subNodes, Matrix4f transformation, @Nullable Mesh mesh) {
            this.name = name;
            this.subNodes = subNodes;
            this.transformation = transformation;
            this.mesh = mesh;
        }

        /**
         * Draws the model with the current shader program.
         *
         * @param transform the transformation of the {@linkplain #mesh mesh}.
         * @param program the shader that draw the {@linkplain #mesh mesh}.
         */
        public void draw(Matrix4fStack transform, ShaderProgram program) {
            transform.pushMatrix();
            try {
                transform.mul(transformation);
                transform.translate(pos)
                        .rotate(rot)
                        .scale(scale);

                if (mesh != null) {
                    program.setUniform("modelMatrix", transform);
                    mesh.render(program);
                }

                for (Node subNode : subNodes) {
                    if (subNode != null) {
                        subNode.draw(transform, program);
                    }
                }
            } finally {
                transform.popMatrix();
            }
        }

        public void delete() {
            if (mesh != null) mesh.delete();
            for (Node subNode : subNodes) {
                subNode.delete();
            }
        }

        public String getName() {
            return name;
        }

        public Node getNode(String name) {
            if (this.name.equals(name)) {
                return this;
            }

            for (Node subNode : subNodes) {
                if (subNode == null) continue;

                Node node = subNode.getNode(name);
                if (node != null) {
                    return node;
                }
            }

            return null;
        }
    }
}
