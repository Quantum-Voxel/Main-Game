package dev.ultreon.qvoxel.client.model.entity;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.client.model.EntityModel;
import dev.ultreon.qvoxel.client.model.ModelException;
import dev.ultreon.qvoxel.client.model.NativeModel;
import dev.ultreon.qvoxel.client.shader.ShaderProgram;
import dev.ultreon.qvoxel.client.world.Camera;
import dev.ultreon.qvoxel.client.world.RenderBuffer;
import dev.ultreon.qvoxel.client.world.RenderType;
import dev.ultreon.qvoxel.entity.Entity;
import dev.ultreon.qvoxel.resource.Resource;
import dev.ultreon.qvoxel.resource.ResourceManager;
import dev.ultreon.qvoxel.util.ResourceNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fStack;
import org.joml.Vector3d;

import java.io.IOException;
import java.io.InputStream;

public class ResourceEntityModel<T extends Entity> implements EntityModel<T> {
    private @Nullable Node root = null;
    private final Identifier modelId;

    public ResourceEntityModel(Identifier modelId) {
        this.modelId = modelId;
    }

    @Override
    public void render(Camera view, Matrix4fStack transform, Vector3d globalPos, RenderBuffer buffer, float partialTicks) {
        transform.pushMatrix();
        RenderType renderType = buffer.getRenderType();
        ShaderProgram shaderProgram = renderType.shaderProgram();
        if (shaderProgram != null) {
            shaderProgram.use();
            shaderProgram.setUniform("projectionMatrix", view.getProjectionMatrix());
            shaderProgram.setUniform("viewMatrix", view.getViewMatrix());
            if (root != null) {
                root.draw(transform, shaderProgram);
            }
        }

        transform.popMatrix();
    }
    
    public final Node getNode(String name) {
        Node rootNode = root;
        if (rootNode != null) {
            return rootNode.getNode(name);
        }

        return null;
    }


    @Override
    public final void load(ResourceManager resources) throws ModelException {
        try {
            Resource resource = resources.getResource(modelId);
            if (resource == null) {
                throw new ResourceNotFoundException(modelId);
            }
            try (InputStream stream = resource.openStream()) {
                root = NativeModel.load(modelId, stream);
            }
            if (root != null) {
                onLoaded(root);
            }
        } catch (IOException e) {
            throw new ResourceNotFoundException(modelId);
        }
    }

    public void onLoaded(@NotNull EntityModel.Node root) {

    }

    @Override
    public void delete() {
        if (root != null) {
            root.delete();
            root = null;
        }
    }
}
