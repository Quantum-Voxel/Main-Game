package dev.ultreon.qvoxel.client.render;

import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.render.pipeline.*;
import dev.ultreon.qvoxel.client.shader.ShaderProgram;
import dev.ultreon.qvoxel.client.texture.TextureFormat;
import dev.ultreon.qvoxel.client.world.WorldRenderer;
import dev.ultreon.qvoxel.world.HitResult;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import static dev.ultreon.qvoxel.CommonConstants.id;

public class GraphicsMode {
    public static final Vector2f PI_SHADER_INPUT = new Vector2f((float) Math.PI, (float) (Math.PI / 180.0f));
    public static final Vector2f GAMMA_SHADER_INPUT = new Vector2f(2.2f, 1.0f / 2.2f);
    private static final Matrix4f tmp = new Matrix4f();
    private static final Vector3d tmpPos = new Vector3d();
    public static final GraphicsMode VIBRANT = new GraphicsMode(
            "vibrant", (WorldRenderer worldRenderer, GuiRenderer renderer, int width, int height) -> {
        // --- Pipeline ---
        RenderPipeline pipeline = new RenderPipeline(renderer, width, height);

        // --- Input Node ---
        RenderNode input = worldRenderer.createNode();
        pipeline.addNode("Input Node", input);
        worldRenderer.setSolidShader(new ShaderProgram(
                "Vibrant Solid Shader",
                id("shaders/vibrant/solid.vert"),
                id("shaders/vibrant/solid.frag")
        ));
        worldRenderer.setTransparentShader(new ShaderProgram(
                "Vibrant Transparent Shader",
                id("shaders/vibrant/transparent.vert"),
                id("shaders/vibrant/transparent.frag")
        ));
        worldRenderer.setCutoutShader(new ShaderProgram(
                "Vibrant Cutout Shader",
                id("shaders/vibrant/cutout.vert"),
                id("shaders/vibrant/cutout.frag")
        ));
        worldRenderer.setWaterShader(new ShaderProgram(
                "Vibrant Water Shader",
                id("shaders/vibrant/water.vert"),
                id("shaders/vibrant/water.frag")
        ));
        input.setBlending(true);
        input.setShaderOutput("color", new ColorTextureTarget(TextureFormat.RGBA32F));

        // --- Input Node ---
        var dataInput = worldRenderer.createDepthlessNode();
        pipeline.addNode("Input Data Node", dataInput);
        dataInput.setBlending(false);
        dataInput.setShaderOutput("color", new ColorTextureTarget(TextureFormat.RGBA32F));
        dataInput.setShaderOutput("uv", new ColorTextureTarget(TextureFormat.RGBA32F));
        dataInput.setShaderOutput("normal", new ColorTextureTarget(TextureFormat.RGBA32F));
        dataInput.setShaderOutput("position", new ColorTextureTarget(TextureFormat.RGBA32F));
        dataInput.setShaderOutput("refractionMask", new ColorTextureTarget(TextureFormat.RGBA32F));
        dataInput.setShaderOutput("reflectionMask", new ColorTextureTarget(TextureFormat.RGBA32F));
        dataInput.setShaderOutput("depth", new DepthTextureTarget(DepthTextureFormat.DEPTH24_STENCIL8));

        // -- Nodes ---
        var ssrReflection = pipeline.createNode("SSR Reflection Node");
        pipeline.addNode("SSR Reflection Node", ssrReflection);
        ssrReflection.setShader(new ShaderProgram(
                "SSR Reflection Shader",
                id("shaders/postprocess/ssr_reflection.vert"),
                id("shaders/postprocess/ssr_reflection.frag")
        ));
        ssrReflection.setShaderInput("gNormal", dataInput, "normal");
        ssrReflection.setShaderInput("colorBuffer", input, "color");
        ssrReflection.setShaderInput("depthMap", dataInput, "depth");
        ssrReflection.setShaderInput("gReflection", dataInput, "reflectionMask");
        ssrReflection.setShaderInput("SCR_WIDTH", (player, program, uniformName) -> program.setUniform(uniformName, (float) input.getWidth()));
        ssrReflection.setShaderInput("SCR_HEIGHT", (player, program, uniformName) -> program.setUniform(uniformName, (float) input.getHeight()));
        ssrReflection.setShaderInput("invProjection", (player, program, uniformName) -> program.setUniform(uniformName, tmp.set(worldRenderer.getCamera().getProjectionMatrix()).invert()));
        ssrReflection.setShaderInput("invViewMatrix", (player, program, uniformName) -> program.setUniform(uniformName, tmp.set(worldRenderer.getCamera().getViewMatrix()).invert()));
        ssrReflection.setShaderInput("projection", (player, program, uniformName) -> program.setUniform(uniformName, worldRenderer.getCamera().getProjectionMatrix()));
        ssrReflection.setShaderInput("topColor", (player, program, uniformName) -> program.setUniform(uniformName, worldRenderer.skyboxRenderer.topColor));
        ssrReflection.setShaderInput("midColor", (player, program, uniformName) -> program.setUniform(uniformName, worldRenderer.skyboxRenderer.midColor));
        ssrReflection.setShaderInput("bottomColor", (player, program, uniformName) -> program.setUniform(uniformName, worldRenderer.skyboxRenderer.bottomColor));
        ssrReflection.setShaderOutput("reflectionBuffer", new ColorTextureTarget(TextureFormat.RGBA32F));

        var output = pipeline.createNode("Output Node");
        output.setShader(new ShaderProgram(
                "Output Shader",
                id("shaders/simple/texture.vert"),
                id("shaders/postprocess/output.frag")
        ));
        output.setShaderInput("colorTexture", input, "color");
        output.setShaderInput("refTexture", ssrReflection, "reflectionBuffer");
//        output.setShaderInput("maskTexture", dataInput, "refractionMask");
        output.setShaderOutput("output", new ColorTextureTarget(TextureFormat.RGBA32F));

        // --- Depth Blur Node ---
        var depthBlur = pipeline.createNode("Depth of Field Node");
        depthBlur.setShader(new ShaderProgram(
                "Depth of Field Shader",
                id("shaders/simple/texture.vert"),
                id("shaders/postprocess/box_blur.frag")
        ));
        depthBlur.setShaderInput("colorTexture", dataInput, "depth");
        depthBlur.setShaderInput("parameters", new Vector2f(5, 2));
        depthBlur.setShaderOutput("depth", new ColorTextureTarget(TextureFormat.RGBA32F));

        // --- Depth of Field Node ---
        var depthOfField = pipeline.createNode("Depth of Field Node");
        depthOfField.setShader(new ShaderProgram(
                "Depth of Field Shader",
                id("shaders/simple/texture.vert"),
                id("shaders/postprocess/depth_of_field.frag")
        ));
        depthOfField.setShaderInput("colorBuffer", output, "output");
        depthOfField.setShaderInput("depthBlur", depthBlur, "depth");
        depthOfField.setShaderInput("depthMap", dataInput, "depth");
        depthOfField.setShaderInput("uFocalLength", 0.05f);
        depthOfField.setShaderInput("uFNumber", 1.8f);
        depthOfField.setShaderInput("uFocusDistance", (player, program, uniformName) -> {
            QuantumClient client = QuantumClient.get();
            var cam = worldRenderer.getCamera();
            if (player != null && cam != null) {
                HitResult hitResult = player.castRay(cam.getFarPlane());
                float focusDistance;
                if (hitResult != null) {
                    Vector3d hitAbs = hitResult.hitPos();
                    Vector3d playerAbs = tmpPos.set(player.position).add(cam.position);

                    // Convert hit to camera-relative space (same as rendering)
                    float rx = (float)(hitAbs.x - playerAbs.x);
                    float ry = (float)(hitAbs.y - playerAbs.y);
                    float rz = (float)(hitAbs.z - playerAbs.z);

                    // Project onto camera forward (optical axis)
                    Vector3f camFwd = new Vector3f(cam.getDirection()).normalize();
                    float proj = rx * camFwd.x + ry * camFwd.y + rz * camFwd.z;

                    focusDistance = Math.max(0.0f, proj);
                    focusDistance = Math.min(Math.max(focusDistance, cam.getNearPlane() + 1e-3f), cam.getFarPlane() - 1e-3f);
                } else {
                    focusDistance = cam.getFarPlane() * 0.9f;
                }
                program.setUniform(uniformName, focusDistance);
            } else {
                program.setUniform(uniformName, worldRenderer.getCamera().getFarPlane() * 0.9f);
            }
        });
        depthOfField.setShaderInput("uSensorHeight", 0.024f);
        depthOfField.setShaderInput("uNearPlane", (_, program, uniformName) -> program.setUniform(uniformName, worldRenderer.getCamera().getNearPlane()));
        depthOfField.setShaderInput("uFarPlane", (_, program, uniformName) -> program.setUniform(uniformName, worldRenderer.getCamera().getFarPlane()));
        depthOfField.setShaderInput("uMaxBlurPixels", 16f);
        depthOfField.setShaderOutput("pipelineOut", new ColorTextureTarget(TextureFormat.RGBA32F));

        input.finish();
        dataInput.finish();
        ssrReflection.finish();
        depthBlur.finish();
        depthOfField.finish();
        output.finish();
        // finalCombine.finish();
        // bloom.finish();
        // dof.finish();
        // motionBlur.finish();

        // Output node
        pipeline.setOutputNode(output);
        return pipeline;
    }
    );

    public static final GraphicsMode DEFAULT = new GraphicsMode(
            "default", (WorldRenderer worldRenderer, GuiRenderer renderer, int width, int height) -> {
        RenderPipeline pipeline = new RenderPipeline(renderer, width, height);

        RenderNode input = worldRenderer.createNode();
        pipeline.addNode("Singleton Node", input);
        input.setShaderOutput("pipelineOut", new ColorTextureTarget(TextureFormat.RGBA32F));
        input.finish();

        pipeline.setOutputNode(input);
        return pipeline;
    }
    );

    private final String name;
    private final PipelineBuilder pipelineBuilder;

    public GraphicsMode(String name, PipelineBuilder pipelineBuilder) {
        this.name = name;
        this.pipelineBuilder = pipelineBuilder;
    }

    public static GraphicsMode[] values() {
        return new GraphicsMode[]{DEFAULT, VIBRANT};
    }

    public RenderPipeline createPipeline(WorldRenderer worldRenderer, GuiRenderer renderer, int width, int height) {
        return pipelineBuilder.build(worldRenderer, renderer, width, height);
    }

    public int ordinal() {
        GraphicsMode[] values = values();
        for (int i = 0; i < values.length; i++) {
            if (values[i] == this) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        return name;
    }

    @FunctionalInterface
    public interface PipelineBuilder {
        RenderPipeline build(WorldRenderer worldRenderer, GuiRenderer renderer, int width, int height);
    }
}
