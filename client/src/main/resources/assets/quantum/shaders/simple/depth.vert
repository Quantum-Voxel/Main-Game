attribute vec3 Position;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

void main() {
    vec4 pos = projectionMatrix * viewMatrix * modelMatrix * vec4(Position, 1.0);

    gl_Position = pos;
}
