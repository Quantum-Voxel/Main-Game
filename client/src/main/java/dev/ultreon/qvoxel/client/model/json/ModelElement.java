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

package dev.ultreon.qvoxel.client.model.json;

import com.google.common.base.Preconditions;
import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.block.BoundingBox;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.model.AOArray;
import dev.ultreon.qvoxel.client.model.FaceCull;
import dev.ultreon.qvoxel.client.model.Light;
import dev.ultreon.qvoxel.client.model.OpaqueFaces;
import dev.ultreon.qvoxel.client.render.Mesh;
import dev.ultreon.qvoxel.client.render.VertexAttributes;
import dev.ultreon.qvoxel.client.texture.TextureAtlas;
import dev.ultreon.qvoxel.client.world.MeshBuilder;
import dev.ultreon.qvoxel.client.world.Vertex;
import dev.ultreon.qvoxel.util.Direction;
import dev.ultreon.qvoxel.world.Axis;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Map;
import java.util.Objects;

public record ModelElement(Map<Direction, FaceElement> blockFaceFaceElementMap, boolean shade, ElementRotation rotation,
                           Vector3f from, Vector3f to) {
    public ModelElement {
        Preconditions.checkNotNull(blockFaceFaceElementMap);
        Preconditions.checkNotNull(rotation);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
    }

    @SuppressWarnings("D")
    public void bakeInto(BoundingBox bounds, OpaqueFaces opaqueFaces, MeshBuilder builder, Map<String, Identifier> textureElements, float x, float y, float z, int cull, AOArray ao, int[][] light, TextureAtlas textureAtlas) {
        final var from = from();
        final var to = to();

        Vector3f normal = new Vector3f();
        for (var entry : blockFaceFaceElementMap.entrySet()) {
            final var direction = entry.getKey();
            final var faceElement = entry.getValue();
            if (FaceCull.culls(faceElement.cullface(), cull)) {
                opaqueFaces.add((int) x, (int) y, (int) z, direction);
                continue;
            }
            final var texRef = faceElement.texture();
            @Nullable Identifier texture = Objects.equals(texRef, "#missing")
                    ? Identifier.parse("blocks/error")
                    : texRef.startsWith("#")
                    ? textureElements.get(texRef.substring(1))
                    : Identifier.parse(texRef).mapPath(path -> path);

            texture = texture == null
                    ? Identifier.parse("textures/blocks/error.png")
                    : texture.mapPath(path -> "textures/" + path + ".png");

            final var v00 = builder.vertex();
            final var v01 = builder.vertex();
            final var v10 = builder.vertex();
            final var v11 = builder.vertex();

            AOArray.AO sAo = ao.aoForSide(direction);
            int[] sLight = Light.getCorners(light, direction);

            if (direction.getAxis() == Axis.Y) {
                if (direction.isNegative()) {
                    v00.setAO(sAo.corner00(), sAo.corner01(), sAo.corner10(), sAo.corner11());
                    v01.setAO(sAo.corner00(), sAo.corner01(), sAo.corner10(), sAo.corner11());
                    v10.setAO(sAo.corner00(), sAo.corner01(), sAo.corner10(), sAo.corner11());
                    v11.setAO(sAo.corner00(), sAo.corner01(), sAo.corner10(), sAo.corner11());
                    // Set per-vertex lighting for DOWN face
                    setVertexLight(v00, sLight[0]);
                    setVertexLight(v01, sLight[1]);
                    setVertexLight(v10, sLight[2]);
                    setVertexLight(v11, sLight[3]);
                } else {
                    v00.setAO(sAo.corner11(), sAo.corner10(), sAo.corner01(), sAo.corner00());
                    v01.setAO(sAo.corner11(), sAo.corner10(), sAo.corner01(), sAo.corner00());
                    v10.setAO(sAo.corner11(), sAo.corner10(), sAo.corner01(), sAo.corner00());
                    v11.setAO(sAo.corner11(), sAo.corner10(), sAo.corner01(), sAo.corner00());
                    // Set per-vertex lighting for UP face (flipped)
                    setVertexLight(v00, sLight[2]);
                    setVertexLight(v01, sLight[0]);
                    setVertexLight(v10, sLight[3]);
                    setVertexLight(v11, sLight[1]);
                }
            } else if (direction.getAxis() == Axis.X) {
                v00.setAO(sAo.corner10(), sAo.corner11(), sAo.corner00(), sAo.corner01());
                v01.setAO(sAo.corner10(), sAo.corner11(), sAo.corner00(), sAo.corner01());
                v10.setAO(sAo.corner10(), sAo.corner11(), sAo.corner00(), sAo.corner01());
                v11.setAO(sAo.corner10(), sAo.corner11(), sAo.corner00(), sAo.corner01());
                // Set per-vertex lighting for X axis faces
                if (direction.isNegative()) {
                    setVertexLight(v00, sLight[0]);
                    setVertexLight(v01, sLight[1]);
                    setVertexLight(v10, sLight[2]);
                    setVertexLight(v11, sLight[3]);
                } else {
                    setVertexLight(v00, sLight[2]);
                    setVertexLight(v01, sLight[3]);
                    setVertexLight(v10, sLight[0]);
                    setVertexLight(v11, sLight[1]);
                }
            } else if (direction.getAxis() == Axis.Z) {
                v00.setAO(sAo.corner00(), sAo.corner01(), sAo.corner10(), sAo.corner11());
                v01.setAO(sAo.corner00(), sAo.corner01(), sAo.corner10(), sAo.corner11());
                v10.setAO(sAo.corner00(), sAo.corner01(), sAo.corner10(), sAo.corner11());
                v11.setAO(sAo.corner00(), sAo.corner01(), sAo.corner10(), sAo.corner11());
                // Set per-vertex lighting for Z axis faces
                if (direction.isNegative()) {
                    setVertexLight(v00, sLight[2]);
                    setVertexLight(v01, sLight[3]);
                    setVertexLight(v10, sLight[0]);
                    setVertexLight(v11, sLight[1]);
                } else {
                    setVertexLight(v00, sLight[2]);
                    setVertexLight(v01, sLight[3]);
                    setVertexLight(v10, sLight[0]);
                    setVertexLight(v11, sLight[1]);
                }
            }

            Vector3f nor = direction.getNormal(normal);
            v00.setNormal(nor);
            v01.setNormal(nor);
            v10.setNormal(nor);
            v11.setNormal(nor);

            var region = textureAtlas.getRegion(Objects.requireNonNull(texture));
            if (region == null) {
                region = textureAtlas.getDefaultRegion();

                if (region == null) throw new IllegalArgumentException("Undefined error texture! " + texture);

                v00.setUV(region.getU(), region.getV2());
                v01.setUV(region.getU(), region.getV());
                v10.setUV(region.getU2(), region.getV2());
                v11.setUV(region.getU2(), region.getV());
            } else {
                float u0 = region.getU() + faceElement.uvs().x1 * (region.getU2() - region.getU());
                float v0 = region.getV() + faceElement.uvs().y2 * (region.getV2() - region.getV());

                float u1 = region.getU() + faceElement.uvs().x1 * (region.getU2() - region.getU());
                float v1 = region.getV() + faceElement.uvs().y1 * (region.getV2() - region.getV());

                float u2 = region.getU() + faceElement.uvs().x2 * (region.getU2() - region.getU());
                float v2 = region.getV() + faceElement.uvs().y2 * (region.getV2() - region.getV());

                float u3 = region.getU() + faceElement.uvs().x2 * (region.getU2() - region.getU());
                float v3 = region.getV() + faceElement.uvs().y1 * (region.getV2() - region.getV());

                v00.setUV(u0, v0);
                v01.setUV(u1, v1);
                v10.setUV(u2, v2);
                v11.setUV(u3, v3);
            }

            v00.setLocalUV(0, 0);
            v01.setLocalUV(0, 1);
            v10.setLocalUV(1, 0);
            v11.setLocalUV(1, 1);

            switch (direction) {
                case UP:
                    v01.setPosition(from.x / 16, to.y / 16, from.z / 16);
                    v00.setPosition(from.x / 16, to.y / 16, to.z / 16);
                    v11.setPosition(to.x / 16, to.y / 16, from.z / 16);
                    v10.setPosition(to.x / 16, to.y / 16, to.z / 16);
                    break;
                case DOWN:
                    v00.setPosition(from.x / 16, from.y / 16, from.z / 16);
                    v01.setPosition(from.x / 16, from.y / 16, to.z / 16);
                    v10.setPosition(to.x / 16, from.y / 16, from.z / 16);
                    v11.setPosition(to.x / 16, from.y / 16, to.z / 16);
                    break;
                case WEST:
                    v00.setPosition(from.x / 16, from.y / 16, from.z / 16);
                    v01.setPosition(from.x / 16, to.y / 16, from.z / 16);
                    v10.setPosition(from.x / 16, from.y / 16, to.z / 16);
                    v11.setPosition(from.x / 16, to.y / 16, to.z / 16);
                    break;
                case EAST:
                    v10.setPosition(to.x / 16, from.y / 16, from.z / 16);
                    v11.setPosition(to.x / 16, to.y / 16, from.z / 16);
                    v00.setPosition(to.x / 16, from.y / 16, to.z / 16);
                    v01.setPosition(to.x / 16, to.y / 16, to.z / 16);
                    break;
                case NORTH:
                    v00.setPosition(to.x / 16, from.y / 16, from.z / 16);
                    v01.setPosition(to.x / 16, to.y / 16, from.z / 16);
                    v10.setPosition(from.x / 16, from.y / 16, from.z / 16);
                    v11.setPosition(from.x / 16, to.y / 16, from.z / 16);
                    break;
                case SOUTH:
                    v10.setPosition(to.x / 16, from.y / 16, to.z / 16);
                    v11.setPosition(to.x / 16, to.y / 16, to.z / 16);
                    v00.setPosition(from.x / 16, from.y / 16, to.z / 16);
                    v01.setPosition(from.x / 16, to.y / 16, to.z / 16);
                    break;
            }

            bounds.ext(v00.position);
            bounds.ext(v01.position);
            bounds.ext(v10.position);
            bounds.ext(v11.position);

            rotate(v00, v01, v10, v11, rotation);

            v00.position.add(x, y, z);
            v01.position.add(x, y, z);
            v10.position.add(x, y, z);
            v11.position.add(x, y, z);

            builder.face(v00, v10, v11, v01);
        }
    }

    public Mesh bake(int idx, Map<String, Identifier> textureElements) {
        MeshBuilder builder = new MeshBuilder(VertexAttributes.POS_UV_NORMAL);
        bakeInto(new BoundingBox(), new OpaqueFaces(), builder, textureElements, -0.5f, -0.5f, -0.5f, FaceCull.of(
                false, false, false, false, false, false
        ), AOArray.of(
                AOArray.AO.of(1f, 1f, 1f, 1f),
                AOArray.AO.of(1f, 1f, 1f, 1f),
                AOArray.AO.of(1f, 1f, 1f, 1f),
                AOArray.AO.of(1f, 1f, 1f, 1f),
                AOArray.AO.of(1f, 1f, 1f, 1f),
                AOArray.AO.of(1f, 1f, 1f, 1f)
        ), Light.of(0xffffffFF, 0xffffffFF, 0xffffffFF, 0xffffffFF, 0xffffffFF, 0xffffffFF), QuantumClient.get().itemTextureAtlas);
        return builder.build();
    }

    private void setVertexLight(Vertex vertex, int lightValue) {
        float red = (lightValue >> 24 & 0xFF) / 255f;
        float green = (lightValue >> 16 & 0xFF) / 255f;
        float blue = (lightValue >> 8 & 0xFF) / 255f;
        float sky = (lightValue & 0x0F) / 15f;
        vertex.setLight(red, green, blue, sky);
    }

    private void rotate(
            Vertex v00,
            Vertex v01,
            Vertex v10,
            Vertex v11,
            ElementRotation rotation
    ) {
        final var originVec = rotation.originVec();
        final var axis = rotation.axis();
        final var angle = rotation.angle();
        final var rescale = rotation.rescale(); // TODO: implement

        // Rotate the vertices
        Vector3f p0 = v00.getPosition();
        Vector3f p1 = v01.getPosition();
        Vector3f p2 = v10.getPosition();
        Vector3f p3 = v11.getPosition();
        rotate(p0, originVec, axis, angle, p0);
        rotate(p1, originVec, axis, angle, p1);
        rotate(p2, originVec, axis, angle, p2);
        rotate(p3, originVec, axis, angle, p3);

    }

    public Vector3f rotate(Vector3f position, Vector3f originVec, Axis axis, float degrees, Vector3f out) {
        return rotateVector(position, originVec, degrees, axis.getVector(), out);
    }

    private Vector3f rotateVector(
            Vector3f vector,
            Vector3f origin,
            float angleDegrees,
            Vector3f axis,
            Vector3f result
    ) {
        Vector3f tmp1 = new Vector3f();
        Vector3f tmp2 = new Vector3f();
        Vector4f tmp3 = new Vector4f();
        Matrix4f rotationMatrix = new Matrix4f();

        // Step 1: Translate the vector to the origin
        tmp1.set(vector).sub(tmp2.set(origin).mul(1 / 16f));

        // Step 2: Create a rotation matrix
        rotationMatrix.rotation(Math.toRadians(angleDegrees), axis);

        // Step 3: Apply the rotation matrix to the translated vector
        tmp3.set(tmp1, 1f);
        tmp3.mul(rotationMatrix);
        tmp1.set(tmp3.x, tmp3.y, tmp3.z);

        // Step 4: Translate the vector back
        result.set(tmp1).add(tmp2);

        return result;
    }

    @Override
    public String toString() {
        return "ModelElement[" +
                "blockFaceFaceElementMap=" + blockFaceFaceElementMap + ", " +
                "shade=" + shade + ", " +
                "rotation=" + rotation + ", " +
                "from=" + from + ", " +
                "to=" + to + ']';
    }

}
