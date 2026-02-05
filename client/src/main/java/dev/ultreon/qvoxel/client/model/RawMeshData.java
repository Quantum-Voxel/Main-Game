package dev.ultreon.qvoxel.client.model;

import dev.ultreon.qvoxel.client.render.GLShape;
import dev.ultreon.qvoxel.client.render.MeshData;
import dev.ultreon.qvoxel.client.render.VertexAttribute;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public record RawMeshData(GLShape shape, FloatList vertexList, IntList indexList, VertexAttribute[] attributes) implements RawMeshInfo {
    public MeshData build() {
        FloatBuffer vBuf = MemoryUtil.memAllocFloat(vertexList.size());
        vBuf.put(vertexList.toFloatArray());
        vBuf.flip();

        IntBuffer iBuf = MemoryUtil.memAllocInt(indexList.size());
        iBuf.put(indexList.toIntArray());
        iBuf.flip();

        return new MeshData(GLShape.Triangles, vBuf, iBuf, attributes);
    }
}
