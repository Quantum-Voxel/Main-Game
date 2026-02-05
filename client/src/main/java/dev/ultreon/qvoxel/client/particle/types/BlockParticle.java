/*
 * Copyright 2025. Quinten 'Qubix' Jungblut
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.ultreon.qvoxel.client.particle.types;

import dev.ultreon.qvoxel.client.render.Mesh;
import dev.ultreon.qvoxel.client.shader.ShaderProgram;
import dev.ultreon.qvoxel.client.texture.TextureAtlas;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class BlockParticle extends Particle {
    private TextureAtlas.AtlasRegion atlasRegion = null;
    private Vector4f uvTransform;

    public BlockParticle(long baseTtl, Mesh mesh, Vector3f speed) {
        super(baseTtl, mesh, speed);
    }

    public BlockParticle(BlockParticle baseParticle) {
        super(baseParticle);
        atlasRegion = baseParticle.atlasRegion;
    }

    public TextureAtlas.AtlasRegion getAtlasRegion() {
        return atlasRegion;
    }

    public void setAtlasRegion(TextureAtlas.AtlasRegion atlasRegion) {
        this.atlasRegion = atlasRegion;
    }

    @Override
    public void draw(Mesh mesh, ShaderProgram particlesShaderProgram) {
        if (uvTransform == null) {
            uvTransform = new Vector4f(atlasRegion.getU(), atlasRegion.getV(), atlasRegion.getU2() - atlasRegion.getU(), atlasRegion.getV2() - atlasRegion.getV());
            float uOffset = (float) Math.random();
            float vOffset = (float) Math.random();

            final float uvCut = 0.3f;

            // Select a region within the UV transform
            // And make it offset by uvOffset but within the region of the uvTransform
            // Coords are: x, y, width, height
            // Width and height needs to be the same as uvCut within the region.
            uvTransform.x += uOffset * (uvTransform.z * uvCut);
            uvTransform.y += vOffset * (uvTransform.w * uvCut);
            uvTransform.z *= uvCut;
            uvTransform.w *= uvCut;
        }
        particlesShaderProgram.setUniform("uvTransform", uvTransform);
        super.draw(mesh, particlesShaderProgram);
    }
}
