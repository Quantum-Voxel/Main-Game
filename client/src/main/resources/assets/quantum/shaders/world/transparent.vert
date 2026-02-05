#version 330 core

layout (location = 0) in vec3 Position;
layout (location = 1) in vec3 Normal;
layout (location = 2) in vec2 UV;
layout (location = 3) in vec4 AO;
layout (location = 4) in vec2 LocalUV;
layout (location = 5) in float Light;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform vec4 uvScalar; // U1, V1, U2, V2

out vec2 uv;
out vec2 localUV;
out vec3 normal;
out vec4 vertexAO;
out vec4 vertexColor;

void main() {
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(Position, 1);
    uv = UV + uvScalar.xy * uvScalar.zw;
    localUV = LocalUV;
    normal = Normal;
    vertexAO = AO;

    int rgbs = floatBitsToInt(Light);
    float r = float((rgbs >> 24) & 0xFF) / 255.0;
    float g = float((rgbs >> 16) & 0xFF) / 255.0;
    float b = float((rgbs >> 8) & 0xFF) / 255.0;
    float sky = float(rgbs & 0xFF) / 15.0;

    vertexColor = vec4(r, g, b, sky);
}