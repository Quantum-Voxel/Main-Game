package dev.ultreon.qvoxel.devutils;

import dev.ultreon.qvoxel.client.render.Mesh;
import dev.ultreon.qvoxel.client.shader.ShaderProgram;
import dev.ultreon.qvoxel.client.texture.TextureAtlas;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.HashSet;
import java.util.Set;

public class DebugRegistries {
    public static final Set<ShaderProgram> SHADER_PROGRAMS = new HashSet<>();
    public static final Set<TextureAtlas> TEXTURE_ATLASES = new HashSet<>();
    public static final IntSet TEXTURES = new IntArraySet();
    public static final Set<Mesh> MESHES = new HashSet<>();
}
