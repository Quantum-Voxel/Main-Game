// Copyright 2025. Quinten 'Qubix' Jungblut
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
#version 410 core

layout (location = 0) in vec3 Position;
layout (location = 1) in vec3 Normal;
layout (location = 2) in vec2 UV;
layout (location = 3) in vec4 AO;
layout (location = 4) in vec2 LocalUV;
layout (location = 5) in float Light;

uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

uniform ivec3 chunkPos;

uniform vec4 uvScalar;
uniform vec4 cameraPos;
uniform mat4 projectionMatrix;
uniform float time;

out vec4 fragPosition;
out vec4 fragCoord;
out vec3 fragNormal;
out vec3 fragModelNormal;
out vec2 fragUV;
out vec2 fragLocalUV;
out vec4 fragAO;
out vec2 fragWave;
out vec4 fragLight;
out float fragFog;

// === Value noise ===
float hash(vec2 p) {
  return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 p) {
  vec2 i = floor(p);
  vec2 f = fract(p);
  float a = hash(i);
  float b = hash(i + vec2(1.0, 0.0));
  float c = hash(i + vec2(0.0, 1.0));
  float d = hash(i + vec2(1.0, 1.0));
  vec2 u = f * f * (3.0 - 2.0 * f);
  return mix(a, b, u.x) +
  (c - a) * u.y * (1.0 - u.x) +
  (d - b) * u.x * u.y;
}

// === Fractal Brownian Motion ===
float fbm(vec2 p) {
  float value = 0.0;
  float amplitude = 0.5;
  float frequency = 1.0;
  for (int i = 0; i < 4; i++) {
    value += amplitude * noise(p * frequency);
    frequency *= 2.0;
    amplitude *= 0.5;
  }
  return value;
}

// === Wave height function ===
float getWaveHeight(dvec2 worldXZ, float time) {
  float waveStrength = 0.02;
  float waveSpeed    = 1.5;
  float waveFrequency = 2.0;

  float phaseX = float(mod(worldXZ.x * waveFrequency + time * waveSpeed, 300000.0));
  float phaseZ = float(mod(worldXZ.y * waveFrequency + time * waveSpeed, 300000.0));

  float baseWave  = sin(phaseX) + cos(phaseZ);
  float noiseWave = fbm(float(mod(worldXZ, 300000.0)) * 0.2 + vec2(time * 0.1, time * 0.15));

  return (baseWave * 0.5 + noiseWave * 1.2) * waveStrength - waveStrength - 0.125;
}

void main() {
  fragUV = uvScalar.xy + UV * uvScalar.zw;

  vec3 position = Position;
  double worldX = double(chunkPos.x) * 16.0 + position.x;
  double worldZ = double(chunkPos.z) * 16.0 + position.z;
  dvec2 worldXZ = dvec2(worldX, worldZ);

  float waveHeight = getWaveHeight(worldXZ, time);

  // === Apply displacement depending on face type ===
  float blockBottom = 0.0;
  float blockTop    = 1.0;

  if (Normal.y > 0.9) {
    // Top face → fully wave
    position.y = blockTop + waveHeight;
  } else if (abs(Normal.y) < 0.001) {
    // Side face → interpolate displacement between bottom and top
    float t = (position.y - blockBottom) / (blockTop - blockBottom);
    position.y = mix(blockBottom, blockTop + waveHeight, t);
  }
  // Bottom face → unchanged

  vec4 pos = modelMatrix * vec4(position, 1.0);

  vec3 flen = cameraPos.xyz - pos.xyz;
  float fog = dot(flen, flen) * cameraPos.w;
  fragFog = min(fog, 1.0);

  // === Normals ===
  vec3 waveNormal;
  if (Normal.y > 0.9) {
    // Top normals follow wave surface
    float eps = 0.1;
    float hL = getWaveHeight(worldXZ - vec2(eps, 0.0), time);
    float hR = getWaveHeight(worldXZ + vec2(eps, 0.0), time);
    float hD = getWaveHeight(worldXZ - vec2(0.0, eps), time);
    float hU = getWaveHeight(worldXZ + vec2(0.0, eps), time);

    waveNormal = normalize(vec3(hL - hR, 2.0 * eps, hD - hU));
    fragWave = vec2(hL - hR, hD - hU);
  } else {
    // Side & bottom keep original normals
    waveNormal = Normal;
    fragWave = vec2(0.0);
  }

  fragNormal = normalize(waveNormal);
  fragModelNormal = waveNormal;

  gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(Position, 1);
  fragPosition = viewMatrix * modelMatrix * vec4(Position, 1);
  fragCoord = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1);
  fragUV = UV + uvScalar.xy * uvScalar.zw;
  fragLocalUV = LocalUV;
  fragNormal = normalize(waveNormal);
  fragAO = AO;

  int rgbs = floatBitsToInt(Light);
  float r = float((rgbs >> 24) & 0xFF) / 255.0;
  float g = float((rgbs >> 16) & 0xFF) / 255.0;
  float b = float((rgbs >> 8) & 0xFF) / 255.0;
  float sky = float(rgbs & 0xFF) / 15.0;

  fragLight = vec4(r, g, b, sky);
}
