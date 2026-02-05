#version 330 core

layout (location = 0) in vec3 Position;
layout (location = 1) in vec2 UV;
layout (location = 2) in vec3 Normal;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;
uniform vec4 uvOffset;

out vec2 uv;
out vec3 normal;

void main() {
    gl_Position = projection * view * model * vec4(Position, 1);
    uv = UV * uvOffset.zw + uvOffset.xy;
    normal = Normal;
}