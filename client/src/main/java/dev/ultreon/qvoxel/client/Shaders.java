package dev.ultreon.qvoxel.client;

import dev.ultreon.qvoxel.client.shader.GLShaderType;
import dev.ultreon.qvoxel.client.shader.Reloadable;
import dev.ultreon.qvoxel.client.shader.ShaderPart;
import dev.ultreon.qvoxel.client.shader.ShaderProgram;
import dev.ultreon.qvoxel.resource.ReloadContext;

import java.util.concurrent.CompletableFuture;

import static dev.ultreon.qvoxel.CommonConstants.id;

public class Shaders implements AutoCloseable, Reloadable {
    private ShaderProgram colorChooserSV;
    private ShaderProgram colorChooserHue;
    private boolean init;
    private ShaderProgram worldProgram;
    private ShaderProgram lineProgram;
    private ShaderProgram particlesShaderProgram;
    private ShaderProgram skyboxProgram;
    private ShaderProgram celestialBodyProgram;
    private ShaderProgram itemGuiProgram;

    public ShaderProgram getColorChooserSV() {
        return colorChooserSV;
    }

    public ShaderProgram getColorChooserHue() {
        return colorChooserHue;
    }

    @Override
    public void close() {
        colorChooserSV.delete();
    }

    public CompletableFuture<?> reload(ReloadContext context) {
        context.submitSafe(() -> {
            if (!init) {
                colorChooserSV = new ShaderProgram("Color Chooser SV", id("shaders/gui/color_chooser_sv.vert"), id("shaders/gui/color_chooser_sv.frag"));
                colorChooserHue = new ShaderProgram("Color Chooser Hue", id("shaders/gui/color_chooser_hue.vert"), id("shaders/gui/color_chooser_hue.frag"));

                worldProgram = new ShaderProgram(
                        "World Shader Program",
                        id("shaders/world.vert"),
                        id("shaders/world.frag")
                );
                lineProgram = new ShaderProgram(
                        "Line Shader Program",
                        new ShaderPart(id("shaders/line.vert"), GLShaderType.Vertex),
                        new ShaderPart(id("shaders/line.geom"), GLShaderType.Geometry),
                        new ShaderPart(id("shaders/line.frag"), GLShaderType.Fragment)
                );
                particlesShaderProgram = new ShaderProgram(
                        "Particle Shader Program",
                        id("shaders/world/particle.vert"),
                        id("shaders/world/particle.frag")
                );
                skyboxProgram = new ShaderProgram(
                        "Skybox Shader Program",
                        id("shaders/skybox.vert"),
                        id("shaders/skybox.frag")
                );
                celestialBodyProgram = new ShaderProgram(
                        "Celestial Body Shader Program",
                        id("shaders/simple/texture.vert"),
                        id("shaders/simple/texture.frag")
                );
                itemGuiProgram = new ShaderProgram(
                        "Celestial Body Shader Program",
                        id("shaders/gui/item.vert"),
                        id("shaders/gui/item.frag")
                );
            }
        }).join();

        return CompletableFuture.allOf(
                colorChooserSV.reload(context),
                colorChooserHue.reload(context),
                worldProgram.reload(context),
                lineProgram.reload(context),
                particlesShaderProgram.reload(context),
                skyboxProgram.reload(context),
                celestialBodyProgram.reload(context),
                itemGuiProgram.reload(context)
        );
    }

    public ShaderProgram getWorldProgram() {
        return worldProgram;
    }

    public ShaderProgram getLineProgram() {
        return lineProgram;
    }

    public ShaderProgram getParticlesShaderProgram() {
        return particlesShaderProgram;
    }

    public ShaderProgram getSkyboxProgram() {
        return skyboxProgram;
    }

    public ShaderProgram getCelestialBodyProgram() {
        return celestialBodyProgram;
    }

    public ShaderProgram getItemGuiProgram() {
        return itemGuiProgram;
    }
}
