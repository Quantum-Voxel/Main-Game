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

package dev.ultreon.qvoxel.client.render;

public class VertexAttributes {
    public static final VertexAttribute POSITION = new VertexAttribute("Position", 3, GLPrimitive.FLOAT, false);
    public static final VertexAttribute NORMAL = new VertexAttribute("Normal", 3, GLPrimitive.FLOAT, false);
    public static final VertexAttribute COLOR = new VertexAttribute("Color", 4, GLPrimitive.FLOAT, false);
    public static final VertexAttribute UV = new VertexAttribute("UV", 2, GLPrimitive.FLOAT, false);
    public static final VertexAttribute LOCAL_UV = new VertexAttribute("LocalUV", 2, GLPrimitive.FLOAT, false);
    public static final VertexAttribute LIGHT = new VertexAttribute("Light", 1, GLPrimitive.FLOAT, false);
    public static final VertexAttribute STATE = new VertexAttribute("State", 1, GLPrimitive.UNSIGNED_BYTE, false);
    public static final VertexAttribute AO = new VertexAttribute("AO", 4, GLPrimitive.FLOAT, false);
    public static final VertexAttribute[] POS_UV_AO = new VertexAttribute[]{POSITION, UV, AO};
    public static final VertexAttribute[] POS_UV_COLOR = new VertexAttribute[]{POSITION, UV, COLOR};
    public static final VertexAttribute[] POS_UV_NORMAL = new VertexAttribute[]{POSITION, UV, NORMAL};
    public static final VertexAttribute[] POS_UV_NORMAL_COLOR = new VertexAttribute[]{POSITION, UV, NORMAL, COLOR};
    public static final VertexAttribute[] POS_UV = new VertexAttribute[]{POSITION, UV};
    public static final VertexAttribute[] POS_NORMAL_UV = new VertexAttribute[]{POSITION, NORMAL, UV};
    public static final VertexAttribute[] PoS_NORMAL_COLOR = new VertexAttribute[]{POSITION, NORMAL, COLOR};
}
