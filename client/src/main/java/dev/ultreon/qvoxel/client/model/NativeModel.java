package dev.ultreon.qvoxel.client.model;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.render.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class NativeModel {
    public static @Nullable EntityModel.Node load(Identifier id, InputStream stream) throws AssimpException {
        byte[] bytes;
        try {
            bytes = stream.readAllBytes();
        } catch (IOException e) {
            throw new AssimpException("Failed to read IO stream: " + e.getMessage());
        }
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(bytes.length);
        byteBuffer.put(bytes);
        byteBuffer.flip();
        try (AIScene aiScene = Assimp.aiImportFileFromMemory(byteBuffer, Assimp.aiProcess_Triangulate | Assimp.aiProcess_FlipUVs, id.path())) {
            String error = Assimp.aiGetErrorString();
            if (error != null && !error.isBlank())
                throw new AssimpException(error);

            if (aiScene == null) {
                CommonConstants.LOGGER.warn("Assimp scene is empty for model: {}", id);
                return null;
            }
            PointerBuffer meshes = aiScene.mMeshes();
            if (meshes == null) {
                CommonConstants.LOGGER.warn("Model is empty: {}", id);
                return null;
            }

            int meshCount = meshes.limit();
            if (meshCount == 0)
                CommonConstants.LOGGER.warn("Model has no meshes: {}", id);

            Mesh[] resultMeshes = new Mesh[meshCount];
            for (int i = 0; i < meshCount; i++) {
                try (AIMesh aiMesh = AIMesh.create(meshes.get(i))) {
                    Mesh mesh = processMesh(aiMesh);
                    resultMeshes[i] = mesh;
                }
            }

            PointerBuffer textures = aiScene.mTextures();
            int numTextures = textures != null ? textures.limit() : 0;
            Identifier[] paths = new Identifier[numTextures];
            for (int i = 0; i < numTextures; i++) {
                AITexture aiTexture = AITexture.createSafe(textures.get(i));
                if (aiTexture == null) {
                    continue;
                }
                try (AIString aiString = aiTexture.mFilename()) {
                    paths[i] = id.withPath("textures/entity/" + aiString.dataString());
                }
            }

            AINode aiNode = aiScene.mRootNode();
            if (aiNode == null) {
                CommonConstants.LOGGER.warn("Model has no root node: {}", id);
                return null;
            }
            return createNode(aiNode, resultMeshes);
        }
    }

    private static EntityModel.Node createNode(AINode aiNode, Mesh[] meshes) {
        PointerBuffer subNodeBuffer = aiNode.mChildren();
        AIMatrix4x4 aiMatrix4x4 = aiNode.mTransformation();
        Matrix4f transformation = new Matrix4f(
                aiMatrix4x4.a1(), aiMatrix4x4.a2(), aiMatrix4x4.a3(), aiMatrix4x4.a4(),
                aiMatrix4x4.b1(), aiMatrix4x4.b2(), aiMatrix4x4.b3(), aiMatrix4x4.b4(),
                aiMatrix4x4.c1(), aiMatrix4x4.c2(), aiMatrix4x4.c3(), aiMatrix4x4.c4(),
                aiMatrix4x4.d1(), aiMatrix4x4.d2(), aiMatrix4x4.d3(), aiMatrix4x4.d4()
        ).transpose();
        int numNodes = aiNode.mNumChildren();
        EntityModel.Node[] subNodes = new EntityModel.Node[numNodes];
        if (subNodeBuffer != null) {
            for (int i = 0; i < aiNode.mNumChildren(); i++) {
                subNodes[i] = createNode(AINode.create(subNodeBuffer.get(i)), meshes);
            }
        }
        IntBuffer intBuffer = aiNode.mMeshes();
        if (aiNode.mNumMeshes() == 0 || intBuffer == null)
            return new EntityModel.Node(aiNode.mName().dataString(), subNodes, transformation);
        return new EntityModel.Node(aiNode.mName().dataString(), subNodes, transformation, meshes[intBuffer.get(0)]);
    }

    private static Mesh processMesh(AIMesh mesh) {
        Tessellator builder = Tessellator.getInstance();
        AIFace.Buffer faces = mesh.mFaces();
        AIVector3D.Buffer positionSet = mesh.mVertices();
        AIVector3D.Buffer normalSet = mesh.mNormals();
        AIColor4D.Buffer colorSet = mesh.mColors(0);
        AIVector3D.Buffer textureCoordsSet = mesh.mTextureCoords(0);
        builder.begin(switch (mesh.mPrimitiveTypes() & 0b01111) {
            case Assimp.aiPrimitiveType_TRIANGLE -> GLShape.Triangles;
            case Assimp.aiPrimitiveType_LINE -> GLShape.Lines;
            case Assimp.aiPrimitiveType_POINT -> GLShape.Points;
            case Assimp.aiPrimitiveType_POLYGON -> GLShape.Polygon;
            default -> throw new GLException("Can convert Assimp primitive type " + mesh.mPrimitiveTypes() + " to OpenGL");
        }, VertexAttributes.POS_NORMAL_UV);

        for (int i = 0; i < faces.limit(); i++) {
            AIFace face = faces.get(i);
            IntBuffer indices = face.mIndices();

            for (int j = 0; j < indices.limit(); j++) {
                int idx = indices.get(j);

                AIVector3D pos = positionSet.get(idx);
                AIVector3D normal = normalSet != null ? normalSet.get(idx) : null;
                AIColor4D color = colorSet != null ? colorSet.get(idx) : null;
                AIVector3D uv = textureCoordsSet != null ? textureCoordsSet.get(idx) : null;

                Tessellator vertex = builder.addVertex(pos.x(), pos.y(), pos.z());
                if (uv != null) vertex.setUV(uv.x(), uv.y());
                if (normal != null) vertex.setNormal(normal.x(), normal.y(), normal.z());
                else vertex.setNormal(0, 0, 0);

                builder.endVertex();
            }
        }

        return builder.end();
    }
}
