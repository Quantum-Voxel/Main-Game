#version 330 core

layout(lines) in;
layout(triangle_strip, max_vertices = 4) out;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;
uniform float thickness;

in VS_OUT {
    vec3 start;
    vec3 end;
    vec4 color;
} gs_in[];

out vec4 fColor;
out float edgeDist;

void main() {
    vec4 p0 = view * model * vec4(gs_in[0].start, 1.0);
    vec4 p1 = view * model * vec4(gs_in[0].end, 1.0);

    vec3 dir = normalize(p1.xyz - p0.xyz);
    vec3 right = normalize(cross(dir, vec3(0.0, 0.0, 1.0)));
    vec3 offset = right * (thickness * 0.5);

    fColor = gs_in[0].color;

    gl_Position = projection * vec4(p0.xyz + offset, 1.0);
    edgeDist = 0.0;
    EmitVertex();

    gl_Position = projection * vec4(p0.xyz - offset, 1.0);
    edgeDist = 1.0;
    EmitVertex();

    gl_Position = projection * vec4(p1.xyz + offset, 1.0);
    edgeDist = 0.0;
    EmitVertex();

    gl_Position = projection * vec4(p1.xyz - offset, 1.0);
    edgeDist = 1.0;
    EmitVertex();

    EndPrimitive();
}
