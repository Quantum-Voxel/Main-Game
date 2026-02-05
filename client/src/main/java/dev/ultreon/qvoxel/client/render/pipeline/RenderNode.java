package dev.ultreon.qvoxel.client.render.pipeline;

import dev.ultreon.qvoxel.client.debug.DebugRenderer;
import dev.ultreon.qvoxel.client.debug.ImGuiOverlay;
import dev.ultreon.qvoxel.client.framebuffer.DepthAttachment;
import dev.ultreon.qvoxel.client.framebuffer.Framebuffer;
import dev.ultreon.qvoxel.client.render.*;
import dev.ultreon.qvoxel.client.shader.ShaderProgram;
import dev.ultreon.qvoxel.client.world.ClientPlayerEntity;
import imgui.ImGui;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.lwjgl.opengl.GL11;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The RenderNode class represents a rendering node in a graphical pipeline. It extends the functionality
 * of a Framebuffer and offers support for shader programs, input/output bindings, and full-screen quad rendering.
 * It is designed for operations involving rendering graphics to a texture or directly to the screen within
 * a composable rendering pipeline.
 * <p>
 * The class enables the configuration of shader inputs and outputs, as well as rendering with custom
 * shader programs. It also allows blending capabilities to be enabled or disabled if required.
 *
 * @see RenderPipeline
 */
@DebugRenderer(RenderNode.DebugRenderer.class)
public class RenderNode extends Framebuffer {
    private final Map<String, ShaderInput> shaderInputs = new LinkedHashMap<>();
    private final Map<String, TextureSource> input = new LinkedHashMap<>();
    private final Map<String, TextureTarget> output = new LinkedHashMap<>();
    private ShaderProgram shaderProgram = null;
    private boolean rendered = false;
    private Mesh fullscreenQuad;
    private Boolean blendEnabled;

    /// Constructs a new RenderNode instance with the specified dimensions and optional depth buffer.
    /// This constructor initializes the RenderNode and attaches a depth buffer if required.
    /// Additionally, it configures OpenGL blending behavior if the blending state is specified.
    /// Finally, a fullscreen quad is created for rendering purposes.
    ///
    /// @param width    The width of the RenderNode. Must be greater than 0.
    /// @param height   The height of the RenderNode. Must be greater than 0.
    /// @param hasDepth Indicates whether the RenderNode should include a depth buffer. If true, a depth buffer is attached to the framebuffer.
    protected RenderNode(int width, int height, boolean hasDepth) {
        super(width, height);

        if (hasDepth) {
            start0();
            if (blendEnabled != null) {
                if (blendEnabled) {
                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                } else {
                    GL11.glDisable(GL11.GL_BLEND);
                }
            }
            attach(new DepthAttachment());
            end();
        }

        createQuad(width, height);
    }

    /// Sets the shader program to be used by this RenderNode.
    ///
    /// @param shader The ShaderProgram to assign. This controls the rendering behavior of the RenderNode.
    public void setShader(ShaderProgram shader) {
        shaderProgram = shader;
    }

    /// Sets a shader input using a specified texture source. The input is defined by its uniform name
    /// in the shader and is mapped to the provided [TextureSource].
    ///
    /// @param name   The name of the shader uniform to bind the texture source to. Cannot be null or empty.
    /// @param source The texture source to set as the input. Cannot be null.
    /// @throws IllegalArgumentException If the provided texture source is null.
    public void setShaderInput(String name, TextureSource source) {
        if (source == null) {
            throw new IllegalArgumentException("Texture source cannot be null.");
        }
        input.put(name, source);
    }

    /// Sets a shader input using a specified render node's output as the input source.
    /// The input is defined by its uniform name in the shader and is mapped to the texture source
    /// provided by the specified output of the render node.
    ///
    /// @param name       The name of the shader uniform to bind the texture source to. Cannot be null or empty.
    /// @param node       The render node from which the shader output is retrieved. Cannot be null.
    /// @param outputName The name of the render node's output texture source to use as input. Cannot be null or empty.
    /// @throws IllegalArgumentException If the specified output texture source is not found in the render node.
    public void setShaderInput(String name, RenderNode node, String outputName) {
        TextureSource source = node.getShaderOutput(outputName);
        if (source == null) {
            throw new IllegalArgumentException("Texture source not found in the specified render node: " + outputName);
        }
        setShaderInput(name, source);
    }

    /// Sets a shader input for the specified uniform name in the shader program.
    /// If the shader program contains a uniform with the provided name, the input is
    /// added or updated in the internal mapping of shader inputs.
    ///
    /// @param name  The name of the shader uniform to associate with the input. Must not be null or empty.
    /// @param input The shader input to be applied. Must not be null.
    public void setShaderInput(String name, ShaderInput input) {
        shaderProgram.use();
        if (shaderProgram.hasUniform(name))
            shaderInputs.put(name, input);
    }

    /// Sets a shader input for the specified uniform name in the shader program.
    /// If the shader program contains a uniform with the provided name, the value
    /// is assigned to that uniform.
    ///
    /// @param name  The name of the shader uniform to set. Must not be null or empty.
    /// @param value The integer value to assign to the uniform. This defines the input for the associated shader.
    public void setShaderInput(String name, int value) {
        shaderProgram.use();
        if (shaderProgram.hasUniform(name)) shaderProgram.setUniform(name, value);
    }

    /// Sets a shader input for the specified uniform name in the shader program.
    /// If the shader program contains a uniform with the provided name, the value
    /// is assigned to that uniform.
    ///
    /// @param name  The name of the shader uniform to set. Must not be null or empty.
    /// @param value The float value to assign to the uniform. This defines the input for the associated shader.
    public void setShaderInput(String name, float value) {
        shaderProgram.use();
        if (shaderProgram.hasUniform(name)) shaderProgram.setUniform(name, value);
    }

    /// Sets a shader input for the specified uniform name in the shader program.
    /// If the shader program contains a uniform with the provided name, the value
    /// is assigned to that uniform.
    ///
    /// @param name  The name of the shader uniform to set. Must not be null or empty.
    /// @param value The boolean value to assign to the uniform. This defines the input for the associated shader.
    public void setShaderInput(String name, boolean value) {
        shaderProgram.use();
        if (shaderProgram.hasUniform(name)) shaderProgram.setUniform(name, value);
    }

    /// Sets a shader input for the specified uniform name in the shader program.
    /// If the shader program contains a uniform with the provided name, the value
    /// is assigned to that uniform.
    ///
    /// @param name   The name of the shader uniform to set. Must not be null or empty.
    /// @param values The float values to assign to the uniform. This defines the input for the associated shader.
    public void setShaderInput(String name, float[] values) {
        shaderProgram.use();
        if (shaderProgram.hasUniform(name)) shaderProgram.setUniform(name, values);
    }

    /// Sets a shader input for the specified uniform name in the shader program.
    /// If the shader program contains a uniform with the provided name, the value
    /// is assigned to that uniform.
    ///
    /// @param name   The name of the shader uniform to set. Must not be null or empty.
    /// @param values The integer values to assign to the uniform. This defines the input for the associated shader.
    public void setShaderInput(String name, int[] values) {
        shaderProgram.use();
        if (shaderProgram.hasUniform(name)) shaderProgram.setUniform(name, values);
    }

    /// Sets a shader input for the specified uniform name in the shader program.
    /// If the shader program contains a uniform with the provided name, the value
    /// is assigned to that uniform.
    ///
    /// @param name  The name of the shader uniform to set. Must not be null or empty.
    /// @param value The 2D float vector value to assign to the uniform. This defines the input for the associated shader.
    public void setShaderInput(String name, Vector2f value) {
        shaderProgram.use();
        if (shaderProgram.hasUniform(name)) shaderProgram.setUniform(name, value);
    }

    /// Sets a shader input for the specified uniform name in the shader program.
    /// If the shader program contains a uniform with the provided name, the value
    /// is assigned to that uniform.
    ///
    /// @param name   The name of the shader uniform to set. Must not be null or empty.
    /// @param values The 2D float vector values to assign to the uniform. This defines the input for the associated shader.
    public void setShaderInput(String name, Vector2f[] values) {
        shaderProgram.use();
        if (shaderProgram.hasUniform(name)) shaderProgram.setUniform(name, values);
    }

    /// Sets a shader input for the specified uniform name in the shader program.
    /// If the shader program contains a uniform with the provided name, the value
    /// is assigned to that uniform.
    ///
    /// @param name  The name of the shader uniform to set. Must not be null or empty.
    /// @param value The 3D float vector value to assign to the uniform. This defines the input for the associated shader.
    public void setShaderInput(String name, Vector3f value) {
        shaderProgram.use();
        if (shaderProgram.hasUniform(name)) shaderProgram.setUniform(name, value);
    }

    /// Sets a shader input for the specified uniform name in the shader program.
    /// If the shader program contains a uniform with the provided name, the value
    /// is assigned to that uniform.
    ///
    /// @param name   The name of the shader uniform to set. Must not be null or empty.
    /// @param values The 3D float vector values to assign to the uniform. This defines the input for the associated shader.
    public void setShaderInput(String name, Vector3f[] values) {
        shaderProgram.use();
        if (shaderProgram.hasUniform(name)) shaderProgram.setUniform(name, values);
    }

    /// Sets a shader input for the specified uniform name in the shader program.
    /// If the shader program contains a uniform with the provided name, the value
    /// is assigned to that uniform.
    ///
    /// @param name  The name of the shader uniform to set. Must not be null or empty.
    /// @param value The 4D float vector value to assign to the uniform. This defines the input for the associated shader.
    public void setShaderInput(String name, Vector4f value) {
        shaderProgram.use();
        if (shaderProgram.hasUniform(name)) shaderProgram.setUniform(name, value);
    }

    /// Sets a shader input for the specified uniform name in the shader program.
    /// If the shader program contains a uniform with the provided name, the value
    /// is assigned to that uniform.
    ///
    /// @param name   The name of the shader uniform to set. Must not be null or empty.
    /// @param values The 4D float vector values to assign to the uniform. This defines the input for the associated shader.
    public void setShaderInput(String name, Vector4f[] values) {
        shaderProgram.use();
        if (shaderProgram.hasUniform(name)) shaderProgram.setUniform(name, values);
    }

    /// Sets a shader input for the specified uniform name in the shader program.
    /// If the shader program contains a uniform with the provided name, the value
    /// is assigned to that uniform.
    ///
    /// @param name  The name of the shader uniform to set. Must not be null or empty.
    /// @param value The 3x3 float matrix value to assign to the uniform. This defines the input for the associated shader.
    public void setShaderInput(String name, Matrix3f value) {
        shaderProgram.use();
        if (shaderProgram.hasUniform(name)) shaderProgram.setUniform(name, value);
    }

    /// Sets a shader input for the specified uniform name in the shader program.
    /// If the shader program contains a uniform with the provided name, the value
    /// is assigned to that uniform.
    ///
    /// @param name   The name of the shader uniform to set. Must not be null or empty.
    /// @param values The 3x3 float matrix values to assign to the uniform. This defines the input for the associated shader.
    public void setShaderInput(String name, Matrix3f[] values) {
        shaderProgram.use();
        if (shaderProgram.hasUniform(name)) shaderProgram.setUniform(name, values);
    }

    /// Sets a shader input for the specified uniform name in the shader program.
    /// If the shader program contains a uniform with the provided name, the value
    /// is assigned to that uniform.
    ///
    /// @param name  The name of the shader uniform to set. Must not be null or empty.
    /// @param value The 4x4 float matrix value to assign to the uniform. This defines the input for the associated shader.
    public void setShaderInput(String name, Matrix4f value) {
        shaderProgram.use();
        if (shaderProgram.hasUniform(name)) shaderProgram.setUniform(name, value);
    }

    /// Sets a shader input for the specified uniform name in the shader program.
    /// If the shader program contains a uniform with the provided name, the value
    /// is assigned to that uniform.
    ///
    /// @param name   The name of the shader uniform to set. Must not be null or empty.
    /// @param values The 4x4 float matrix values to assign to the uniform. This defines the input for the associated shader.
    public void setShaderInput(String name, Matrix4f[] values) {
        shaderProgram.use();
        if (shaderProgram.hasUniform(name)) shaderProgram.setUniform(name, values);
    }

    /// Sets a shader input for the specified uniform name in the shader program.
    /// If the shader program contains a uniform with the provided name, the value
    /// is assigned to that uniform.
    ///
    /// @param name  The name of the shader uniform to set. Must not be null or empty.
    /// @param value The color value to assign to the uniform. This defines the input for the associated shader.
    public void setShaderInput(String name, Color value) {
        shaderProgram.use();
        if (shaderProgram.hasUniform(name)) shaderProgram.setUniform(name, value);
    }

    /// Sets a shader input for the specified uniform name in the shader program.
    /// If the shader program contains a uniform with the provided name, the value
    /// is assigned to that uniform.
    ///
    /// @param name   The name of the shader uniform to set. Must not be null or empty.
    /// @param target The texture target to assign to the uniform. This defines the input for the associated shader.
    public void setShaderOutput(String name, TextureTarget target) {
        withinContext(() -> {
            output.put(name, target);
            attach(target);
        });
    }

    /// Retrieves the shader output texture source associated with the given output name.
    ///
    /// @param outputName the name of the shader output to retrieve
    /// @return the TextureSource associated with the specified output name, or null if no match is found
    public TextureSource getShaderOutput(String outputName) {
        return output.get(outputName);
    }

    /// Completes the current operation and marks the rendered state as false.
    /// This method calls the superclass's finish method to perform any necessary
    /// cleanup or finalization steps and updates the internal state to indicate
    /// that rendering has been completed.
    public void finish() {
        super.finish();
        rendered = false;
    }

    /// Renders the graphical output using the specified `GuiRenderer`. The method sets up shader inputs,
    /// binds necessary framebuffers, clears buffers, applies shaders, and renders a full-screen quad. If the rendering
    /// process is already completed, this method will return early.
    ///
    /// @param player
    /// @param renderer the `GuiRenderer` instance used for rendering operations
    /// @throws IllegalStateException if one or more shader inputs are not set
    public final void render(ClientPlayerEntity player, GuiRenderer renderer) {
        if (rendered) {
            return;
        }

        rendered = true;

        for (TextureSource source : input.values()) {
            if (source == null) {
                throw new IllegalStateException("One or more shader inputs are not set.");
            }

            source.render(player, renderer);
        }

        // Bind framebuffer
        start();
        GL11.glClearColor(0f, 0f, 0f, 0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

        // Use shader program
        if (shaderProgram == null) {
            doRender(player, renderer);
            end();
            return;
        }

        shaderProgram.use();

        // Use inputs
        int unit = 0;
        for (Map.Entry<String, TextureSource> source : input.entrySet()) {
            shaderProgram.setUniform(source.getKey(), unit);
            source.getValue().use(unit++);
        }
        for (Map.Entry<String, ShaderInput> entry : shaderInputs.entrySet()) {
            if (!shaderProgram.hasUniform(entry.getKey())) {
                continue;
            }
            entry.getValue().apply(player, shaderProgram, entry.getKey());
        }

        // Render a full-screen quad
        doRender(player, renderer);

        // Unbind framebuffer
        end();
    }

    protected void doRender(ClientPlayerEntity player, GuiRenderer renderer) {
        Matrix4f modelMatrix = renderer.getModelMatrix();
        Matrix4f viewMatrix = renderer.getViewMatrix();
        Matrix4f projectionMatrix = renderer.getProjectionMatrix();

        modelMatrix.identity();
        modelMatrix.translate(0, 0, 0);
        modelMatrix.scale(getWidth(), getHeight(), 1);
        if (shaderProgram.hasUniform("modelMatrix"))
            shaderProgram.setUniform("modelMatrix", modelMatrix);
        if (shaderProgram.hasUniform("viewMatrix"))
            shaderProgram.setUniform("viewMatrix", viewMatrix);
        if (shaderProgram.hasUniform("projectionMatrix"))
            shaderProgram.setUniform("projectionMatrix", projectionMatrix);
        fullscreenQuad.render(shaderProgram);
    }

    @Override
    public void delete() {
        super.delete();

        if (shaderProgram != null) {
            shaderProgram.delete();
            shaderProgram = null;
        }
    }

    public void setBlending(Boolean enabled) {
        blendEnabled = enabled;
    }

    public static class DebugRenderer extends Framebuffer.DebugRenderer {
        @Override
        public void render(Framebuffer object, @Nullable Consumer<Framebuffer> setter) {
            RenderNode renderNode = (RenderNode) object;
            if (ImGui.treeNode(renderNode.getName())) {
                ImGui.pushID(renderNode.getName());
                ImGui.text("Inputs:");
                for (Map.Entry<String, TextureSource> entry : renderNode.input.entrySet()) {
                    if (ImGui.treeNode(entry.getKey())) {
                        ImGui.pushID("Input:" + entry.getKey());
                        ImGuiOverlay.renderObject(entry.getValue());
                        ImGui.popID();
                        ImGui.treePop();
                    }
                }

                ImGui.text("Outputs:");
                for (Map.Entry<String, TextureTarget> entry : renderNode.output.entrySet()) {
                    if (ImGui.treeNode(entry.getKey())) {
                        ImGui.pushID("Output:" + entry.getKey());
                        ImGuiOverlay.renderObject(entry.getValue());
                        ImGui.popID();
                        ImGui.treePop();
                    }
                }

                ImGui.popID();
                ImGui.treePop();
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        if (fullscreenQuad != null) fullscreenQuad.delete();
        createQuad(width, height);
    }

    private void createQuad(int width, int height) {
        fullscreenQuad = new Mesh(
                GLShape.Triangles,
                new float[]{
                        0, 0, 0,
                        0, 1, 0,
                        1, 1, 0,
                        1, 0, 0
                },
                new int[]{
                        0, 1, 2,
                        0, 2, 3
                },
                VertexAttributes.POSITION
        );
    }
}
