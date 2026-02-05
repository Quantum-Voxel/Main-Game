#version 330 core

layout(triangles) in;
layout(triangle_strip, max_vertices = 3) out;

in vec3 vPositionWorld[]; // from vertex shader
out vec3 normal;

void main() {
    // Compute face normal from the three input vertices
    vec3 p0 = vPositionWorld[0];
    vec3 p1 = vPositionWorld[1];
    vec3 p2 = vPositionWorld[2];

    vec3 faceNormal = normalize(cross(p1 - p0, p2 - p0));

    // Emit the triangle, passing same normal for all 3 vertices
    for (int i = 0; i < 3; i++) {
        normal = faceNormal;
        gl_Position = gl_in[i].gl_Position;
        EmitVertex();
    }
    EndPrimitive();
}
