#version 330 core

layout (location = 0) in vec3 Position;
layout (location = 1) in vec3 Normal;
layout (location = 2) in vec2 UV;
layout (location = 3) in vec4 Color;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

out vec2 uv;
out vec3 normal;
out vec3 vertexColor;

void main() {
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(Position, 1);
    uv = UV;
    normal = Normal;
}