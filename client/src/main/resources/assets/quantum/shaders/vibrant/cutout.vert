#version 330 core

layout (location = 0) in vec3 Position;
layout (location = 1) in vec3 Normal;
layout (location = 2) in vec2 UV;
layout (location = 3) in vec4 AO;
layout (location = 4) in vec2 LocalUV;
layout (location = 5) in float Light;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform vec4 uvScalar;// U1, V1, U2, V2

out vec4 fragPosition;
out vec4 fragCoord;
out vec2 fragUV;
out vec2 fragLocalUV;
out vec3 fragNormal;
out vec4 fragAO;
out vec4 fragLight;

void main() {
  gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(Position, 1);
  fragPosition = viewMatrix * modelMatrix * vec4(Position, 1);
  fragCoord = projectionMatrix * viewMatrix * modelMatrix * vec4(Position, 1);
  fragUV = UV + uvScalar.xy * uvScalar.zw;
  fragNormal = Normal;
  fragLocalUV = LocalUV;
  fragAO = AO;

  int rgbs = floatBitsToInt(Light);
  float r = float((rgbs >> 24) & 0xFF) / 255.0;
  float g = float((rgbs >> 16) & 0xFF) / 255.0;
  float b = float((rgbs >> 8) & 0xFF) / 255.0;
  float sky = float(rgbs & 0xFF) / 15.0;

  fragLight = vec4(r, g, b, sky);
}