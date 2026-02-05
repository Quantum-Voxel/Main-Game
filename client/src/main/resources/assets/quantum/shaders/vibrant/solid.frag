#version 330 core

in vec4 fragPosition;
in vec4 fragCoord;
in vec2 fragUV;
in vec2 fragLocalUV;
in vec3 fragNormal;
in vec4 fragAO;
in vec4 fragLight;

uniform sampler2D colorTexture;
uniform sampler2D baseTexture;
uniform float SkyLight;
uniform float MinLight;
uniform bool dataOutput;

layout (location = 0) out vec4 gColor;
layout (location = 1) out vec4 gUV;
layout (location = 2) out vec4 gNormal;
layout (location = 3) out vec4 gPosition;
layout (location = 4) out vec4 gRefractionMask;
layout (location = 5) out vec4 gReflectionMask;
layout (location = 6) out vec4 gDepth;

struct SHC {
  float L00, L1m1, L10, L11, L2m2, L2m1, L20, L21, L22;
};

SHC groove = SHC(
0.3783264,
0.2887813,
0.0379030,
-0.1033028,
-0.0621750,
0.0077820,
-0.0935561,
-0.0572703,
0.0203348
);

SHC beach = SHC(
0.6841148,
0.3173355,
-0.1747193,
-0.4496467,
-0.1690202,
-0.0837808,
-0.0319670,
0.1641816,
0.3697189
);

SHC tomb = SHC(
1.0351604,
0.4442150,
-0.2247797,
0.7110400,
0.6430452,
-0.1150112,
-0.3742487,
-0.1694954,
0.5515260
);

float sh_light(vec3 normal, SHC l) {
  float x = normal.x;
  float y = normal.y;
  float z = normal.z;

  const float C1 = 0.429043;
  const float C2 = 0.511664;
  const float C3 = 0.743125;
  const float C4 = 0.886227;
  const float C5 = 0.247708;

  return (
  C1 * l.L22 * (x * x - y * y) +
  C3 * l.L20 * z * z +
  C4 * l.L00 -
  C5 * l.L20 +
  2.0 * C1 * l.L2m2 * x * y +
  2.0 * C1 * l.L21 * x * z +
  2.0 * C1 * l.L2m1 * y * z +
  2.0 * C2 * l.L11 * x +
  2.0 * C2 * l.L1m1 * y +
  2.0 * C2 * l.L10 * z
  );
}

float gamma(float color) {
  return pow(color, 1.0 / 2.0);
}

void main() {
  // Calculate ambient occlusion.
  float ao0 = mix(fragAO.x, fragAO.z, fragLocalUV.x);
  float ao1 = mix(fragAO.y, fragAO.w, fragLocalUV.x);
  float ao = mix(ao0, ao1, fragLocalUV.y);

  // Add center effect.
  float centerBias = 0.75;
  float centerFactor = 4.0 * (fragLocalUV.x * (1.0 - fragLocalUV.x)) * (fragLocalUV.y * (1.0 - fragLocalUV.y));
  ao = ao + (1.0 - ao) * centerFactor * centerBias;

  // Extract colored block light and sky light from vertex color.
  vec3 color = fragLight.rgb * 1.1;// Slightly brighter to show light even a little bit during daytime.
  float sky = fragLight.a;

  // Make sure sky light is never lower than minimum light.
  float skyLight = mix(MinLight, SkyLight, sky);

  // Calculate lighting.
  vec3 light = vec3(mix(MinLight, 1.0, SkyLight), mix(MinLight, 1.0, SkyLight), mix(MinLight, 1.0, SkyLight));
  light *= mix(MinLight, 1.0, sky) * 2 - 0.4;
  light += (color.rgb - (light * color.rgb));

  // Get texture color.
  vec3 tex = texture(colorTexture, fragUV).rgb;

  // SH lighting.
  float sh = gamma(sh_light(fragNormal, groove));

  // Calculate final color.
  vec3 lit = tex;
  lit = lit * light;
  lit = lit * ao * sh;

  gColor = vec4(lit, 1.0);
  gUV = vec4(fragUV, 1.0, 1.0);
  gNormal = vec4(fragNormal, 1.0);
  gPosition = fragPosition;
  float depth = fragPosition.w / fragPosition.z;

  gDepth = vec4(depth, 0, 0, 1);
  gRefractionMask = vec4(0.0);
  gReflectionMask = vec4(0.0);
}