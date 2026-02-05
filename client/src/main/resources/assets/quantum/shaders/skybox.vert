#version 330 core

in vec3 Position;
in vec2 UV;
in vec3 Normal;
out vec2 uv;
out vec3 normal;
uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;

void main() {
    gl_Position = projection * view * model * vec4(Position, 1.0);
    uv = UV;
    normal = Normal;
}
