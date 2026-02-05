#version 330 core

layout(location = 0) in vec3 Position;
layout(location = 1) in vec3 Normal;
layout(location = 2) in vec4 Color;

out VS_OUT {
    vec3 start;
    vec3 end;
    vec4 color;
} vs_out;

void main() {
    vs_out.start = Position;
    vs_out.end = Normal;
    vs_out.color = Color;
}
