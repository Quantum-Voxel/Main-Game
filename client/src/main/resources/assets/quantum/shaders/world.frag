#version 330 core

in vec2 uv;
in vec3 normal;

uniform sampler2D colorTexture;

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
    fragColor = texture(colorTexture, uv) * gamma(sh_light(normal, groove));
}