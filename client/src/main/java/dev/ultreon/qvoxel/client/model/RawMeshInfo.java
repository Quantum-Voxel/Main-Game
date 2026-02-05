package dev.ultreon.qvoxel.client.model;

import dev.ultreon.qvoxel.client.render.GLShape;
import dev.ultreon.qvoxel.client.render.VertexAttribute;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.IntList;

public interface RawMeshInfo {
    FloatList vertexList();
    IntList indexList();
    VertexAttribute[] attributes();
    GLShape shape();
}
