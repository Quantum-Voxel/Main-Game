#version 330 core

uniform sampler2D colorBuffer;
uniform sampler2D depthMap;
uniform sampler2D depthBlur;

// Physical Depth of Field uniforms
uniform float uFocalLength;      // meters (e.g., 0.050 for 50mm)
uniform float uFNumber;          // f-stop (e.g., 1.8)
uniform float uFocusDistance;    // meters to plane in focus
uniform float uSensorHeight;     // meters (e.g., 0.024 for 35mm full-frame height)
uniform float uNearPlane;        // near plane distance (same unit as depth), optional
uniform float uFarPlane;         // far plane distance (same unit as depth), optional
uniform float uMaxBlurPixels;    // optional clamp for blur radius in pixels

out vec4 fragOut;

void blurredTexture(sampler2D colorTexture, int size, float separation, out vec4 fragColor) {
  vec2 texSize  = textureSize(colorTexture, 0).xy;
  vec2 texCoord = gl_FragCoord.xy / texSize;
  texCoord.y = 1 - texCoord.y;

  fragColor = texture(colorTexture, texCoord);

  if (size <= 0) {
    return;
  }

  separation = max(separation, 1);

  fragColor.rgb = vec3(0);

  float count = 0.0;

  for (int i = -size; i <= size; ++i) {
    for (int j = -size; j <= size; ++j) {
      vec2 sepUV = (gl_FragCoord.xy + (vec2(i, j) * separation)) / texSize;
      sepUV.y = 1.0 - sepUV.y;
      vec4 color = texture(colorTexture, sepUV);
      fragColor.rgb += color.rgb;

      count += 1.0;
    }
  }

  fragColor.a = 1.0;

  fragColor.rgb /= count;
}

// Depth linearization helper: if near/far provided, treat depthMap as non-linear and linearize; otherwise return depth as-is.
float getLinearDepth(float depth)
{
  if (uFarPlane > uNearPlane && uNearPlane > 0.0) {
    // OpenGL depth range [0,1]
    float z = depth * 2.0 - 1.0;
    float n = uNearPlane;
    float f = uFarPlane;
    float linear = (2.0 * n * f) / (f + n - z * (f - n));
    return linear;
  }
  return depth;
}

void main() {
  vec2 uv = gl_FragCoord.xy / textureSize(depthMap, 0).xy;
  uv.y = 1 - uv.y;

  // Read optional gating alpha from depthBlur
  float alpha = texture(depthBlur, vec2(0.5, 0.5)).a;

  // Depth at current pixel (linearized if near/far provided)
  float depthUV = getLinearDepth(texture(depthMap, uv).r);

  // Physical DoF parameters path (enabled when all required uniforms are valid)
  bool hasPhysicalParams = (uFocalLength > 0.0 && uFNumber > 0.0 && uFocusDistance > 0.0 && uSensorHeight > 0.0);

  float radius;
  if (hasPhysicalParams) {
    // Compute CoC on the sensor using thin lens approximation
    float f = uFocalLength;
    float N = uFNumber;
    float s = uFocusDistance;
    float z = max(depthUV, 1e-6);

    // c_sensor = | (f^2 / (N * (s - f))) * ((z - s) / z) |
    float c_sensor = abs((f * f) / (N * max(s - f, 1e-6)) * ((z - s) / z));

    // Convert sensor-space CoC to pixel radius using screen height and sensor height
    float screenHeight = float(textureSize(colorBuffer, 0).y);
    float c_pixels = c_sensor * (screenHeight / uSensorHeight);

    float maxBlur = (uMaxBlurPixels > 0.0) ? uMaxBlurPixels : 32.0;
    radius = clamp(c_pixels, 0.0, maxBlur);
  } else {
    // Fallback to legacy heuristic using provided depthBlur focus value
    float depthFocus = texture(depthBlur, vec2(0.5, 0.5)).r;
    float radiusLegacy = pow(abs(depthFocus - depthUV), 1.0 / 2.6);
    radius = min(radiusLegacy, 32.0);
  }

  // Preserve previous gating behavior
  if (alpha == 0.0) radius = (uMaxBlurPixels > 0.0) ? min(radius, uMaxBlurPixels) : 32.0;

  blurredTexture(colorBuffer, int(radius), 2, fragOut);
}
