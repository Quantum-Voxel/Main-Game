package dev.ultreon.qvoxel.client.render;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Objects;

public class MeshData {
    private final GLShape shape;
    private final FloatBuffer vertexBuffer;
    private final IntBuffer indexBuffer;
    private final VertexAttribute[] attributes;

    public MeshData(GLShape shape, float[] vertexBuffer, int[] indexBuffer, VertexAttribute... attributes) {
        this.shape = shape;
        this.vertexBuffer = BufferUtils.createFloatBuffer(vertexBuffer.length);
        this.indexBuffer = BufferUtils.createIntBuffer(indexBuffer.length);
        this.vertexBuffer.put(vertexBuffer);
        this.indexBuffer.put(indexBuffer);
        this.attributes = attributes;
    }

    public MeshData(GLShape shape, FloatBuffer vertexBuffer, IntBuffer indexBuffer, VertexAttribute... attributes) {
        this.shape = shape;
        if (!vertexBuffer.isDirect())
            throw new GLException("Indirect vertex buffer");
        if (!indexBuffer.isDirect())
            throw new GLException("Indirect index buffer");

        this.vertexBuffer = vertexBuffer;
        this.indexBuffer = indexBuffer;
        this.vertexBuffer.put(vertexBuffer);
        this.indexBuffer.put(indexBuffer);
        this.attributes = attributes;
    }

    public MeshData optimize() {
        vertexBuffer.flip();
        indexBuffer.flip();
//
//        int vertexStrideFloats = getVertexStrideFloats();
//        int vertexStrideBytes = vertexStrideFloats * Float.BYTES;
//
//        int indexCount = indexBuffer.capacity();
//        int vertexCount = vertexBuffer.capacity() / vertexStrideFloats;
//
//        ByteBuffer vertexBytes = BufferUtils.createByteBuffer(vertexBuffer.capacity() * Float.BYTES);
//        vertexBytes.asFloatBuffer().put(vertexBuffer);
//        vertexBytes.limit(vertexBuffer.capacity() * Float.BYTES);
//        vertexBytes.position(0);
//
//        IntBuffer remap = BufferUtils.createIntBuffer(vertexCount);
//
//        long newVertexCount = MeshOptimizer.meshopt_generateVertexRemap(
//                remap,
//                indexBuffer,
//                indexCount,
//                vertexBytes,
//                vertexCount,
//                vertexStrideBytes
//        );
//
//        IntBuffer newIndexInt = BufferUtils.createIntBuffer(indexCount);
//        ByteBuffer newVertexBytes = BufferUtils.createByteBuffer((int)newVertexCount * vertexStrideBytes);
//
//        MeshOptimizer.meshopt_remapIndexBuffer(newIndexInt, indexBuffer, indexCount, remap);
//        MeshOptimizer.meshopt_remapVertexBuffer(newVertexBytes, vertexBytes, vertexCount, vertexStrideBytes, remap);
//
////        MeshOptimizer.meshopt_optimizeVertexCache(newIndexInt, newIndexInt, indexCount);
////
////        FloatBuffer posFloat = newVertexBytes.asFloatBuffer();
////        MeshOptimizer.meshopt_optimizeOverdraw(
////                newIndexInt,
////                newIndexInt,
////                posFloat,
////                newVertexCount,
////                vertexStrideFloats,
////                1.05f
////        );
////
////        MeshOptimizer.meshopt_optimizeVertexFetch(
////                newVertexBytes,
////                newIndexInt,
////                newVertexBytes,
////                newVertexCount,
////                vertexStrideBytes
////        );

//        vertexBuffer.clear();
//        vertexBuffer.put(newVertexBytes.asFloatBuffer());
//        vertexBuffer.flip();
//
//        indexBuffer.clear();
//        indexBuffer.flip();

        return this;
    }


    private int getVertexStrideFloats() {
        int sum = 0;
        for (VertexAttribute a : attributes)
            sum += a.size();
        return sum;
    }

    private int getVertexStrideBytes() {
        return getVertexStrideFloats() * Float.BYTES;
    }

    public GLShape shape() {
        return shape;
    }

    public FloatBuffer vertexBuffer() {
        return vertexBuffer;
    }

    public IntBuffer indexBuffer() {
        return indexBuffer;
    }

    public VertexAttribute[] attributes() {
        return attributes;
    }

    public MeshData add(MeshData data) {
        if (!Arrays.equals(attributes, data.attributes))
            throw new IllegalArgumentException("Attributes don't match!");
        if (!Objects.equals(shape, data.shape))
            throw new IllegalArgumentException("Attributes don't match!");

        FloatBuffer newVertices = BufferUtils.createFloatBuffer(vertexBuffer.limit() + data.vertexBuffer.limit());
        IntBuffer newIndices = BufferUtils.createIntBuffer(indexBuffer.limit() + data.indexBuffer.limit());

        newVertices.put(vertexBuffer);
        newVertices.put(data.vertexBuffer);

        newIndices.put(indexBuffer);
        newIndices.put(data.indexBuffer);

        return new MeshData(shape, vertexBuffer, indexBuffer, attributes);
    }
}
