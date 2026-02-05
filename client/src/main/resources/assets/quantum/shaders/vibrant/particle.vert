#version 330 core

layout (location=0) in vec3 Position;
layout (location=1) in vec2 UV;
layout (location=2) in vec3 Normal;

out vec2 outTexCoord;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;
uniform vec4 uvTransform; // u, v, u2, v2

void main()
{
    gl_Position = projectionMatrix * modelViewMatrix * vec4(Position, 1.0);
    outTexCoord = UV * uvTransform.zw + uvTransform.xy;
}