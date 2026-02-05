#version 330 core

layout (location = 0) in vec3 Position;
layout (location = 1) in vec2 UV;
layout (location = 2) in vec3 Normal;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform vec4 uvScalar; // U1, V1, U2, V2

out vec2 uv;
out vec3 normal;

void main() {
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(Position, 1);
    uv = UV + uvScalar.xy * uvScalar.zw;
    normal = Normal;
}