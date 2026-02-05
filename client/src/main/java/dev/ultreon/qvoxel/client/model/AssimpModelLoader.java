package dev.ultreon.qvoxel.client.model;/*
 * AssimpMultiToSingleMeshConverter.java
 *
 * Converts multiple Assimp models into a single MeshData.
 * Respects the order of VertexAttribute[] for interleaved vertex layout.
 * Applies node hierarchy transforms to each vertex.
 */

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.render.GLShape;
import dev.ultreon.qvoxel.client.render.VertexAttribute;
import dev.ultreon.qvoxel.client.render.VertexAttributes;
import dev.ultreon.qvoxel.client.texture.TextureAtlas;
import dev.ultreon.qvoxel.resource.ResourceManager;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.lang.Math;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.UUID;

import static org.lwjgl.assimp.Assimp.*;

public class AssimpModelLoader {

    public static class ImportOptions {
        public int assimpFlags = aiProcess_Triangulate | aiProcess_GenSmoothNormals | aiProcess_FlipUVs | aiProcess_PreTransformVertices;
    }

    public static AIScene load(ResourceManager manager, Identifier path, ImportOptions options) {
        if (options == null) options = new ImportOptions();

        byte[] bytes = manager.getResource(path).readBytes();
        ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        return aiImportFileFromMemory(buffer, options.assimpFlags, path.path());
    }

    public static RawMeshData convert(AIScene scene, TextureAtlas atlas, VertexAttribute[] attributes, RawMeshInfo info, String domain, int light, List<String> embeddedTextures) {
        FloatList vertexList = info.vertexList();
        IntList indexList = info.indexList();
        int vertexOffset = 0;

        processNode(scene.mRootNode(), scene, attributes, vertexList, indexList, vertexOffset, new Matrix4fStack(100), atlas, domain, light, embeddedTextures);

        return new RawMeshData(GLShape.Triangles, vertexList, indexList, attributes);
    }

    public static RawMeshData convert(AIScene scene, TextureAtlas atlas, VertexAttribute[] attributes, int x, int y, int z, String domain, int light, List<String> embeddedTextures) {
        return convert(scene, atlas, attributes, x, y, z, 1.0f, domain, light, embeddedTextures);
    }

    public static RawMeshData convert(AIScene scene, TextureAtlas atlas, VertexAttribute[] attributes, float x, float y, float z, float scale, String domain, int light, List<String> embeddedTextures) {
        FloatList vertexList = new FloatArrayList();
        IntList indexList = new IntArrayList();
        int vertexOffset = 0;

        processNode(scene.mRootNode(), scene, attributes, vertexList, indexList, vertexOffset, (Matrix4fStack) new Matrix4fStack(100).translate(x + 0.5f, y, z + 0.5f).scale(scale), atlas, domain, light, embeddedTextures);

        return new RawMeshData(GLShape.Triangles, vertexList, indexList, attributes);
    }

    public static List<String> collectTextureNames(AIScene scene, String domain, List<String> textures, List<String> embeddedTextures) {
        for (int i = 0; i < scene.mMeshes().limit(); i++) {
            AIMesh safe = AIMesh.createSafe(scene.mMeshes().get(i));
            if (safe != null) {
                int materialIndex = safe.mMaterialIndex();
                long l = scene.mMaterials().get(materialIndex);
                AIMaterial material = AIMaterial.createSafe(l);
                processMaterialTextures(material, scene, domain, textures, embeddedTextures);
            }
        }

        return textures;
    }
    private static void processMaterialTextures(AIMaterial material, AIScene scene, String domain, List<String> textures, List<String> embeddedTexture) {
        int texCount = aiGetMaterialTextureCount(material, aiTextureType_DIFFUSE);
        for (int i = 0; i < texCount; i++) {
            AIString path = AIString.calloc();
            if (aiGetMaterialTexture(material, aiTextureType_DIFFUSE, i, path, (IntBuffer)null, null, null, null, null, null) == aiReturn_SUCCESS) {
                String texName = path.dataString();
                if (texName.startsWith("*")) {
                    int idx = Integer.parseInt(texName.substring(1));
                    AITexture tex = AITexture.create(scene.mTextures().get(idx));
                    ByteBuffer imgData = tex.pcDataCompressed(); // raw image bytes
                    byte[] dataAsPng = new byte[imgData.limit()];
                    imgData.get(dataAsPng);
                    // You need to load imgData into your TextureAtlas here
                    // For now, we can just add a placeholder name

                    String e = "_embedded/" + idx + "_" + UUID.randomUUID().toString().replace("-", "");
                    embeddedTexture.add(e);
                    QuantumClient.get().resourceManager.setFakeResource(new Identifier(domain, "textures/blocks/" + e + ".png"), dataAsPng);
                    QuantumClient.get().resourceManager.setFakeResource(new Identifier(domain, "textures/items/" + e + ".png"), dataAsPng);
                    textures.add(e);
                } else {
                    textures.add(texName);
                }
            }
            path.free();
        }
    }

    private static String processMaterialTexture(AIMaterial material) {
        int[] count = new int[1];

        // Diffuse textures
        count[0] = aiGetMaterialTextureCount(material, aiTextureType_DIFFUSE);
        for (int i = 0; i < count[0]; i++) {
            AIString path = AIString.calloc();
            if (aiGetMaterialTexture(material, aiTextureType_DIFFUSE, i, path, (IntBuffer)null, null, null, null, null, null) == aiReturn_SUCCESS) {
                return path.dataString();
            }
            path.free();
        }
        return null;
    }

    private static int processNode(AINode node, AIScene scene, VertexAttribute[] attributes,
                                   FloatList vertexList, IntList indexList, int vertexOffset,
                                   Matrix4fStack parentTransform, TextureAtlas atlas, String domain, int light, List<String> embeddedTextures) {
        parentTransform.pushMatrix();
        try {
            Matrix4fStack nodeTransform = (Matrix4fStack) parentTransform.mul(
                    new Matrix4f().set(aiMatrix4x4ToFloatArray(node.mTransformation()))
            );

            // Process all meshes in this node
            int meshCount = node.mNumMeshes();
            if (meshCount > 0) {
                IntBuffer meshesPB = node.mMeshes();
                for (int i = 0; i < meshCount; i++) {
                    parentTransform.pushMatrix();
                    int meshIndex = meshesPB.get(i);
                    AIMesh mesh = AIMesh.create(scene.mMeshes().get(meshIndex));
                    processMesh(mesh, attributes, vertexList, indexList, vertexOffset, nodeTransform, atlas, domain, scene, light, embeddedTextures);
                    // Update offset after mesh
                    vertexOffset += mesh.mNumVertices();
                    parentTransform.popMatrix();
                }
            }

            // Recurse into children with current offset
            int childCount = node.mNumChildren();
            PointerBuffer children = node.mChildren();
            for (int i = 0; i < childCount; i++) {
                parentTransform.pushMatrix();
                AINode child = AINode.create(children.get(i));
                vertexOffset = processNode(child, scene, attributes, vertexList, indexList, vertexOffset, nodeTransform, atlas, domain, light, embeddedTextures);
                parentTransform.popMatrix();
            }
        } finally {
            parentTransform.popMatrix();
        }

        return vertexOffset; // pass updated offset back to parent
    }

    private static void processMesh(AIMesh mesh, VertexAttribute[] attributes,
                                    FloatList vertexList, IntList indexList, int vertexOffset, Matrix4f transform, TextureAtlas atlas, String domain, AIScene scene, int light, List<String> embeddedTextures) {
        int vertexCount = mesh.mNumVertices();
        AIVector3D.Buffer verts = mesh.mVertices();
        AIVector3D.Buffer normals = mesh.mNormals();
        AIVector3D.Buffer texcoords = mesh.mTextureCoords(0);
        AIColor4D.Buffer colors = mesh.mColors(0);

        Matrix3f normalMatrix = transform.get3x3(new Matrix3f());

        for (int i = 0; i < vertexCount; i++) {
            for (VertexAttribute attr : attributes) {
                if (attr == VertexAttributes.POSITION) {
                    AIVector3D v = verts.get(i);
                    Vector4f p = transform.transform(new Vector4f(0 + v.x(), 0 + v.y(), 0 + v.z(), 1));
                    vertexList.add(0 + p.x);
                    vertexList.add(0 + p.y);
                    vertexList.add(0 + p.z);
                } else if (attr == VertexAttributes.NORMAL) {
                    if (normals != null) {
                        AIVector3D n = normals.get(i);
                        Vector3f p = normalMatrix.transform(new Vector3f(0 + n.x(), 0 + n.y(), 0 + n.z()));
                        vertexList.add(0 + p.x);
                        vertexList.add(0 + p.y);
                        vertexList.add(0 + p.z);
                    } else {
                        vertexList.add(0f);
                        vertexList.add(0f);
                        vertexList.add(0f);
                    }
                } else if (attr == VertexAttributes.UV) {
                    if (texcoords != null) {
                        AIVector3D t = texcoords.get(i);
                        int material = mesh.mMaterialIndex();
                        long l = scene.mMaterials().get(material);
                        AIMaterial safe = AIMaterial.createSafe(l);
                        String name = processMaterialTexture(safe);
                        TextureAtlas.AtlasRegion region;
                        if (safe != null && name != null) {
                            if (name.startsWith("*")) {
                                int index = Integer.parseInt(name.substring(1));
                                name = embeddedTextures.get(index) + ".png";
                            }
                            region = atlas.getRegion(new Identifier(domain, "textures/blocks/" + name));
                        } else {
                            region = atlas.getDefaultRegion();
                        }

                        float tx = t.x();
                        float ty = t.y();

                        vertexList.add(org.joml.Math.lerp(region.getU(), region.getU2(), tx));
                        vertexList.add(org.joml.Math.lerp(region.getV(), region.getV2(), ty));
                    } else {
                        vertexList.add(0f);
                        vertexList.add(0f);
                    }
                } else if (attr == VertexAttributes.LOCAL_UV) {
                    if (texcoords != null) {
                        AIVector3D t = texcoords.get(i);
                        float tx = t.x();
                        float ty = t.y();

                        vertexList.add(tx);
                        vertexList.add(ty);
                    } else {
                        vertexList.add(0f);
                        vertexList.add(0f);
                    }
                } else if (attr == VertexAttributes.COLOR) {
                    if (colors != null) {
                        AIColor4D t = colors.get(i);
                        vertexList.add(t.r());
                        vertexList.add(t.g());
                        vertexList.add(t.b());
                        vertexList.add(t.a());
                    } else {
                        vertexList.add(1f);
                        vertexList.add(1f);
                        vertexList.add(1f);
                        vertexList.add(1f);
                    }
                } else if (attr == VertexAttributes.LIGHT) {
                    vertexList.add(Float.intBitsToFloat(light));
                } else if (attr == VertexAttributes.AO) {
                    vertexList.add(1f);
                    vertexList.add(1f);
                    vertexList.add(1f);
                    vertexList.add(1f);
                } else {
                    for (int j = 0; j < attr.size(); j++) vertexList.add(0f);
                }
            }
        }

        int faceCount = mesh.mNumFaces();
        AIFace.Buffer faces = mesh.mFaces();
        for (int f = 0; f < faceCount; f++) {
            AIFace face = faces.get(f);
            for (int k = face.mNumIndices() - 1; k >= 0; k--) {
                indexList.add(vertexOffset + face.mIndices().get(k));
            }
        }
    }

    private static void collectTextureNames(AIMesh mesh, List<String> textures) {
        AIString aiString = AIString.create(mesh.mTextureCoordsNames().get(0));
        String s = aiString.dataString();
        textures.add(s);
    }

    public static int getStride(VertexAttribute[] attributes) {
        int sum = 0;
        for (VertexAttribute a : attributes) sum += a.size();
        return sum;
    }

    private static float[] aiMatrix4x4ToFloatArray(AIMatrix4x4 m) {
        return new float[]{
                m.a1(), m.b1(), m.c1(), m.d1(),
                m.a2(), m.b2(), m.c2(), m.d2(),
                m.a3(), m.b3(), m.c3(), m.d3(),
                m.a4(), m.b4(), m.c4(), m.d4()
        };
    }

    private static float[] multiplyMat4(float[] a, float[] b) {
        float[] r = new float[16];
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                float sum = 0f;
                for (int k = 0; k < 4; k++) {
                    sum += a[k * 4 + row] * b[col * 4 + k];
                }
                r[col * 4 + row] = sum;
            }
        }
        return r;
    }

    private static float[] transformVec3(float[] m, float x, float y, float z) {
        if (m.length == 9) {
            float tx = m[0] * x + m[3] * y + m[6] * z;
            float ty = m[1] * x + m[4] * y + m[7] * z;
            float tz = m[2] * x + m[5] * y + m[8] * z;
            return new float[]{tx, ty, tz};
        }
        float tx = m[0] * x + m[4] * y + m[8] * z + m[12];
        float ty = m[1] * x + m[5] * y + m[9] * z + m[13];
        float tz = m[2] * x + m[6] * y + m[10] * z + m[14];
        return new float[]{tx, ty, tz};
    }

    private static float[] upperLeft3x3InverseTranspose(float[] m) {
        float a00 = m[0], a01 = m[4], a02 = m[8];
        float a10 = m[1], a11 = m[5], a12 = m[9];
        float a20 = m[2], a21 = m[6], a22 = m[10];

        float det = a00 * (a11 * a22 - a12 * a21) - a01 * (a10 * a22 - a12 * a20) + a02 * (a10 * a21 - a11 * a20);
        if (Math.abs(det) < 1e-9f) return new float[]{1, 0, 0, 0, 1, 0, 0, 0, 1};
        float invDet = 1f / det;
        float b00 = (a11 * a22 - a12 * a21) * invDet;
        float b01 = (a02 * a21 - a01 * a22) * invDet;
        float b02 = (a01 * a12 - a02 * a11) * invDet;
        float b10 = (a12 * a20 - a10 * a22) * invDet;
        float b11 = (a00 * a22 - a02 * a20) * invDet;
        float b12 = (a02 * a10 - a00 * a12) * invDet;
        float b20 = (a10 * a21 - a11 * a20) * invDet;
        float b21 = (a01 * a20 - a00 * a21) * invDet;
        float b22 = (a00 * a11 - a01 * a10) * invDet;
        return new float[]{b00, b10, b20, b01, b11, b21, b02, b12, b22};
    }

    private static float[] identityMatrix() {
        return new float[]{
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        };
    }
}
