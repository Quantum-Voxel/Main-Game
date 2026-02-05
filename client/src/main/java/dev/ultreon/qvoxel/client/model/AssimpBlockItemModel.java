package dev.ultreon.qvoxel.client.model;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.model.json.BakedModel;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.client.render.VertexAttributes;
import dev.ultreon.qvoxel.client.world.MeshBuilder;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static dev.ultreon.qvoxel.client.model.json.JsonModel.SCALE;

public class AssimpBlockItemModel implements ItemModel {
    private final Identifier resourceId;
    private BakedModel model;
    private List<Identifier> textures = new ArrayList<>();

    public AssimpBlockItemModel(AssimpBlockModel model) {
        this.resourceId = model.resourceId();
    }

    @Override
    public void load(QuantumClient client) {

    }

    @Override
    public Identifier resourceId() {
        return resourceId;
    }

    @Override
    public boolean isCustom() {
        return false;
    }

    @Override
    public Collection<Identifier> getAllTextures() {
        return textures;
    }

    @Override
    public BakedModel getModel() {
        return model;
    }

    @Override
    public Vector3f getItemScale() {
        return SCALE;
    }

    @Override
    public void renderItem(GuiRenderer renderer, int x, int y) {
        if (model == null) {
            throw new IllegalStateException("Model not loaded: " + resourceId());
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        Matrix4f modelMatrix = renderer.getModelMatrix();
        modelMatrix.identity();
        modelMatrix.translate(x + 15, y - 10, 0);
        modelMatrix.scale(SCALE.x, SCALE.y, SCALE.z);
        modelMatrix.rotateX(Math.toRadians(-25));
        modelMatrix.rotateY(Math.toRadians(45));

        renderer.renderModel(model);
    }

    @Override
    public void preload(QuantumClient client) {
        MeshBuilder builder = new MeshBuilder(VertexAttributes.POS_UV_NORMAL);
        List<String> textures = new ArrayList<>();
        AIScene load = AssimpModelLoader.load(client.resourceManager, resourceId, null);
        List<String> embeddedTextures = new ArrayList<>();
        AssimpModelLoader.collectTextureNames(load, resourceId.location(), textures, embeddedTextures);
        this.textures = textures.stream().map(s -> new Identifier(resourceId.location(), "blocks/" + s.split("\\.")[0])).toList();
        AssimpModelLoader.convert(load, client.itemTextureAtlas, VertexAttributes.POS_UV_NORMAL, builder, resourceId.location(), 0x00000000f, embeddedTextures);
        model = new BakedModel(builder.buildData().optimize());
    }

    @Override
    public void close() throws Exception {
        model.close();
    }
}
