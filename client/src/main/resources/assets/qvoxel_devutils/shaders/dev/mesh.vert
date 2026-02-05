#version 330 core

layout (location = 0) in vec3 Position;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;

out vec3 vPositionWorld;

void main() {
    vPositionWorld = (model * vec4(Position, 1.0)).xyz;
    gl_Position = projection * view * model * vec4(Position, 1);
}