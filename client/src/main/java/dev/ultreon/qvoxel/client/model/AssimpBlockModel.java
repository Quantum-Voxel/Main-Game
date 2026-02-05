package dev.ultreon.qvoxel.client.model;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.block.BoundingBox;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.texture.TextureAtlas;
import dev.ultreon.qvoxel.client.world.MeshBuilder;
import dev.ultreon.qvoxel.client.world.RenderType;
import dev.ultreon.qvoxel.resource.ResourceManager;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.assimp.AIScene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class AssimpBlockModel implements BlockModel {
    private final Identifier resourceId;
    private RawMeshData data;
    private List<Identifier> textures = new ArrayList<>();
    private AIScene scene;
    private final List<String> embeddedTextures = new ArrayList<>();
    private float scale;

    public AssimpBlockModel(Identifier resourceId) {
        this(resourceId, 1.0f);
    }

    public AssimpBlockModel(Identifier resourceId, float scale) {
        this.resourceId = resourceId;
        this.scale = scale;
    }

    @Override
    public void bakeInto(BoundingBox bounds, OpaqueFaces opaqueFaces, MeshBuilder builder, float x, float y, float z, int cull, AOArray ao, int[][] light) {
        RawMeshData rawMeshData = QuantumClient.invokeAndWait((Supplier<RawMeshData>) () ->
            AssimpModelLoader.convert(scene, QuantumClient.get().blockTextureAtlas, builder.attributes(), x, y, z, scale, resourceId.location(), Light.ofAverage(light), embeddedTextures));
        builder.addMesh(rawMeshData);
    }

    @Override
    public @Nullable BlockState getBlock() {
        return null;
    }

    @Override
    public boolean hasAO() {
        return false;
    }

    @Override
    public RenderType getRenderPass() {
        return RenderType.CUTOUT;
    }

    @Override
    public Identifier getBuriedTexture() {
        return null;
    }

    @Override
    public TextureAtlas.AtlasRegion getParticle() {
        return null;
    }

    @Override
    public void load(QuantumClient client) {

    }

    @Override
    public void preload(QuantumClient client) {
        if (data != null) {
            return;
        }

        ResourceManager resourceManager = client.resourceManager;
        List<String> textures;
        MeshBuilder info;
        scene = AssimpModelLoader.load(resourceManager, resourceId, null);
        textures = new ArrayList<>();
        info = new MeshBuilder();
        embeddedTextures.clear();
        AssimpModelLoader.collectTextureNames(scene, resourceId.location(), textures, embeddedTextures);
        info.discard();
        this.textures = textures.stream().map(s -> new Identifier(resourceId.location(), "blocks/" + s.split("\\.")[0])).toList();
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
    public void close() throws Exception {
        scene.free();
        scene = null;
        data = null;
    }
}
