/*
 * Copyright 2025. Quinten 'Qubix' Jungblut
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.ultreon.qvoxel.client.world;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.render.GLShape;
import dev.ultreon.qvoxel.client.render.GraphicsException;
import dev.ultreon.qvoxel.client.render.VertexAttribute;
import dev.ultreon.qvoxel.client.render.VertexAttributes;
import dev.ultreon.qvoxel.client.shader.ShaderProgram;
import dev.ultreon.qvoxel.resource.ReloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class RenderType {
    private static final List<RenderType> VALUES = new ArrayList<>();

    public static final RenderType OUTLINE = RenderType.builder("outline")
            .attributes(VertexAttributes.POSITION, VertexAttributes.UV, VertexAttributes.COLOR)
            .shaderProgram(CommonConstants.id("gui/color"))
            .shape(GLShape.Lines)
            .prepare(() -> {
                QuantumClient client = QuantumClient.get();
                client.blockTextureAtlas.use();
            })
            .build();
    public static final RenderType SOLID = RenderType.builder("solid")
            .attributes(VertexAttributes.POSITION, VertexAttributes.NORMAL, VertexAttributes.UV, VertexAttributes.COLOR, VertexAttributes.AO, VertexAttributes.LOCAL_UV, VertexAttributes.LIGHT)
            .shaderProgram(CommonConstants.id("world/solid"))
            .shape(GLShape.Triangles)
            .prepare(() -> {
                QuantumClient client = QuantumClient.get();
                client.blockTextureAtlas.use();
            })
            .build();
    public static final RenderType TRANSPARENT = RenderType.builder("transparent")
            .attributes(VertexAttributes.POSITION, VertexAttributes.NORMAL, VertexAttributes.UV, VertexAttributes.COLOR, VertexAttributes.AO, VertexAttributes.LOCAL_UV, VertexAttributes.LIGHT)
            .shaderProgram(CommonConstants.id("world/transparent"))
            .shape(GLShape.Triangles)
            .prepare(() -> {
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                QuantumClient client = QuantumClient.get();
                client.blockTextureAtlas.use();
            })
            .finish(() -> GL11.glDisable(GL11.GL_BLEND))
            .build();
    public static final RenderType PARTICLE = RenderType.builder("particle")
            .attributes(VertexAttributes.POSITION, VertexAttributes.UV, VertexAttributes.NORMAL)
            .shaderProgram(CommonConstants.id("world/particle"))
            .shape(GLShape.Triangles)
            .prepare(() -> {
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            })
            .finish(() -> GL11.glDisable(GL11.GL_BLEND))
            .build();
    public static final RenderType WATER = RenderType.builder("water")
            .attributes(VertexAttributes.POSITION, VertexAttributes.NORMAL, VertexAttributes.UV, VertexAttributes.COLOR, VertexAttributes.AO, VertexAttributes.LOCAL_UV, VertexAttributes.LIGHT)
            .shaderProgram(CommonConstants.id("world/water"))
            .shape(GLShape.Triangles)
            .prepare(() -> {
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glDisable(GL11.GL_CULL_FACE);
            })
            .finish(() -> {
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glEnable(GL11.GL_CULL_FACE);
            })
            .build();
    public static final RenderType CUTOUT = RenderType.builder("cutout")
            .attributes(VertexAttributes.POSITION, VertexAttributes.NORMAL, VertexAttributes.UV, VertexAttributes.COLOR, VertexAttributes.AO, VertexAttributes.LOCAL_UV, VertexAttributes.LIGHT)
            .shaderProgram(CommonConstants.id("world/cutout"))
            .shape(GLShape.Triangles)
            .build();
    public static final RenderType CUTOUT_NO_CULL = RenderType.builder("cutout")
            .attributes(VertexAttributes.POSITION, VertexAttributes.NORMAL, VertexAttributes.UV, VertexAttributes.COLOR, VertexAttributes.AO, VertexAttributes.LOCAL_UV, VertexAttributes.LIGHT)
            .shaderProgram(CommonConstants.id("world/cutout"))
            .shape(GLShape.Triangles)
            .prepare(() -> GL11.glDisable(GL11.GL_CULL_FACE))
            .finish(() -> GL11.glEnable(GL11.GL_CULL_FACE))
            .build();
    public static final RenderType ENTITY_CUTOUT = RenderType.builder("entity_cutout")
            .attributes(VertexAttributes.POSITION, VertexAttributes.NORMAL, VertexAttributes.UV)
            .shaderProgram(CommonConstants.id("world/entity_cutout"))
            .shape(GLShape.Triangles)
            .prepare(() -> GL11.glFrontFace(GL11.GL_CCW))
            .finish(() -> GL11.glFrontFace(GL11.GL_CW))
            .build();
    public static final RenderType LEAVES = RenderType.builder("leaves")
            .attributes(VertexAttributes.POSITION, VertexAttributes.NORMAL, VertexAttributes.UV, VertexAttributes.COLOR, VertexAttributes.AO, VertexAttributes.LOCAL_UV, VertexAttributes.LIGHT)
            .shaderProgram(CommonConstants.id("world/cutout"))
            .shape(GLShape.Triangles)
            .build();

    private final String name;
    private final ShaderProgram shaderProgram;
    private final VertexAttribute[] attributes;
    private final GLShape shape;
    private final Runnable preparations;
    private final Runnable finish;
    private ShaderProgram shaderProgramOverride;

    private RenderType(Builder builder) {
        name = builder.name;
        shaderProgram = builder.shaderProgram;
        attributes = builder.attributes;
        shape = builder.shape;
        preparations = builder.preparations;
        finish = builder.finish;
        if (shape == null) throw new GraphicsException("Render type " + name + " has no shape");
        if (attributes == null) throw new GraphicsException("Render type " + name + " has no attributes");

        VALUES.add(this);
    }

    public static RenderType byName(String renderPass) {
        for (RenderType renderType : VALUES) {
            if (renderType.name.equals(renderPass)) {
                return renderType;
            }
        }
        throw new GraphicsException("Unknown render pass: " + renderPass);
    }

    public static CompletableFuture<?> reloadAll(ReloadContext context) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (RenderType value : VALUES) {
            futures.add(value.reload(context));
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture<?>[]::new));
    }

    public CompletableFuture<?> reload(ReloadContext context) {
        return shaderProgram.reload(context);
    }

    public GLShape shape() {
        return shape;
    }

    public @Nullable ShaderProgram shaderProgram() {
        if (shaderProgramOverride != null) {
            return shaderProgramOverride;
        }
        return shaderProgram;
    }

    public VertexAttribute[] attributes() {
        return attributes;
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public boolean doesMerging() {
        return true;
    }

    public void prepare() {
        preparations.run();
    }

    public void finish() {
        finish.run();
    }

    public @NotNull String getName() {
        return name;
    }

    public void setShaderProgram(ShaderProgram solidShader) {
        shaderProgramOverride = solidShader;
    }

    public static class Builder {
        private final String name;
        private Runnable preparations = () -> {
        };
        private VertexAttribute[] attributes;
        private ShaderProgram shaderProgram;
        private GLShape shape;
        private Runnable finish = () -> {
        };

        public Builder(String name) {
            this.name = name;
        }

        public Builder attributes(VertexAttribute... attributes) {
            this.attributes = attributes;
            return this;
        }

        public Builder shaderProgram(ShaderProgram shaderProgram) {
            this.shaderProgram = shaderProgram;
            return this;
        }

        public Builder shaderProgram(Identifier id) {
            return shaderProgram(new ShaderProgram("Render Type '" + name + "' Shader", id.mapPath(path -> "shaders/" + path + ".vert"), id.mapPath(path -> "shaders/" + path + ".frag")));
        }

        public Builder shape(GLShape shape) {
            this.shape = shape;
            return this;
        }

        public Builder prepare(Runnable preparations) {
            this.preparations = preparations;
            return this;
        }

        public RenderType build() {
            return new RenderType(this);
        }

        public Builder finish(Runnable finish) {
            this.finish = finish;
            return this;
        }
    }
}
