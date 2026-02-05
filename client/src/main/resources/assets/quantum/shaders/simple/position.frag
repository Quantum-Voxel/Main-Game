#version 330 core

in vec4 vertexPosition;

out vec4 fragColor;

void main() {
  fragColor = vertexPosition.xzyw;
}