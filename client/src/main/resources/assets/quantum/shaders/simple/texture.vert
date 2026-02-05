#version 330 core

layout (location = 0) in vec3 Position;
layout (location = 1) in vec2 UV;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

out vec2 uv;

void main() {
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(Position, 1);
    uv = UV;
}