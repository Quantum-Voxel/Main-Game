#version 330 core

in vec2 uv;

uniform vec4 color;
uniform sampler2D colorTexture;

out vec4 fragColor;

void main() {
    fragColor = color * texture(colorTexture, uv);
}