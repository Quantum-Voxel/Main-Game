#version 330 core

in vec2 uv;
in vec2 localUV;
in vec3 normal;
in vec4 vertexAO;
in vec4 vertexColor;

uniform sampler2D colorTexture;
uniform float SkyLight;
uniform float MinLight;

out vec4 fragColor;

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
    float ao0 = mix(vertexAO.x, vertexAO.z, localUV.x);
    float ao1 = mix(vertexAO.y, vertexAO.w, localUV.x);
    float ao = mix(ao0, ao1, localUV.y);

    // Add center effect.
    float centerBias = 0.75;
    float centerFactor = 4.0 * (localUV.x * (1.0 - localUV.x)) * (localUV.y * (1.0 - localUV.y));
    ao = ao + (1.0 - ao) * centerFactor * centerBias;

    // Extract colored block light and sky light from vertex color.
    vec3 color = vertexColor.rgb * 1.1; // Slightly brighter to show light even a little bit during daytime.
    float sky = vertexColor.a / 15;

    // Make sure sky light is never lower than minimum light.
    float skyLight = mix(MinLight, SkyLight, sky);

    // Calculate lighting.
    vec3 light = vec3(mix(MinLight, 1.0, skyLight));
    light += (color.rgb - (light * color.rgb));

    // Get texture color.
    vec3 tex = texture(colorTexture, uv).rgb;

    // SH lighting.
    float sh = gamma(sh_light(normal, groove));

    // Calculate final color.
    vec3 lit = tex;
    lit = lit * light;
    lit = lit * ao * sh;

    fragColor = vec4(lit, 1.0);
}