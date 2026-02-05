#version 330 core

in vec2 uv;

out vec4 fragColor;

vec3 hsv2rgb(vec3 c) {
    float h = mod(c.x, 1.0) * 6.0;
    float f = fract(h);
    float p = c.z * (1.0 - c.y);
    float q = c.z * (1.0 - c.y * f);
    float t = c.z * (1.0 - c.y * (1.0 - f));
    if (h < 1.0) return vec3(c.z, t, p);
    if (h < 2.0) return vec3(q, c.z, p);
    if (h < 3.0) return vec3(p, c.z, t);
    if (h < 4.0) return vec3(p, q, c.z);
    if (h < 5.0) return vec3(t, p, c.z);
    return vec3(c.z, p, q);
}

void main() {
    float hue = uv.y; // hue along the bar
    fragColor = vec4(hsv2rgb(vec3(hue, 1.0, 1.0)), 1.0);
}
