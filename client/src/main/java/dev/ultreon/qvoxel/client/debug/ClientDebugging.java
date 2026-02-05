package dev.ultreon.qvoxel.client.debug;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.shader.GLShaderType;
import dev.ultreon.qvoxel.client.shader.ShaderPart;
import dev.ultreon.qvoxel.client.shader.ShaderProgram;
import dev.ultreon.qvoxel.client.texture.Texture;

public class ClientDebugging {
    public static ShaderProgram meshShader;
    private static Texture pasteTexture;

    static {
        QuantumClient.invoke(ClientDebugging::init);
        QuantumClient.onClose(ClientDebugging::destroy);
    }

    public static void init() {
        meshShader = new ShaderProgram(
                "Debug Mesh Shader",
                new ShaderPart(new Identifier("qvoxel_devutils", "shaders/dev/mesh.vert"), GLShaderType.Vertex),
                new ShaderPart(new Identifier("qvoxel_devutils", "shaders/dev/mesh.geom"), GLShaderType.Geometry),
                new ShaderPart(new Identifier("qvoxel_devutils", "shaders/dev/mesh.frag"), GLShaderType.Fragment)
        );
    }

    public static void destroy() {
        meshShader.delete();
    }

    public static void setPasteTexture(Texture texture) {
        if (pasteTexture != null) {
            pasteTexture.delete();
        }

        pasteTexture = texture;
    }

    public static Texture getPasteTexture() {
        return pasteTexture;
    }
}
