#version 330 core

in vec2 uv;
in vec3 normal;

uniform vec4 topColor;
uniform vec4 midColor;
uniform vec4 bottomColor;

out vec4 fragColor;

void main() {
    // Normalize the position to getConfig values between 0 and 1
    float normalizedPosition = uv.y - 0.5;
    if (normal.y < -0.01) normalizedPosition = -1;
    if (normal.y > 0.01) normalizedPosition = 2;
    normalizedPosition *= 7.0;
    normalizedPosition += 0.5;
    normalizedPosition = clamp(normalizedPosition, 0.0, 1.0);

    // Calculate the gradient
    vec3 gradient = mix(bottomColor.rgb, midColor.rgb, normalizedPosition);
    gradient = mix(gradient, topColor.rgb, normalizedPosition * normalizedPosition);

    // Output the color
    fragColor = vec4(gradient, 1.0);
}