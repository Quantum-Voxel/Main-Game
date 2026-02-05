#version 330 core

in vec4 fColor;
in float edgeDist;

out vec4 fragColor;

void main() {
    float alpha = 1.0 - smoothstep(0.9, 1.0, edgeDist);
    fragColor = vec4(fColor.rgb, alpha * fColor.a);

    if (fragColor.a < 0.01)
    discard;
}
