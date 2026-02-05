#version 330 core

layout(location = 0) in vec2 Position;// [-1,1] screen quad positions
layout(location = 1) in vec2 UV;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

out vec2 vUV;

void main() {
  vUV = UV;

  gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(Position, 0.0, 1.0);
}
