varying vec3 fragCoord;

void main() {
    float depth = fragCoord.z / fragCoord.w;

    vec3 depthIn3Channels;
    depthIn3Channels.r = mod(depth, 1.0);
    depth -= depthIn3Channels.r;
    depth /= 256.0;

    depthIn3Channels.g = mod(depth, 1.0);
    depth -= depthIn3Channels.g;
    depth /= 256.0;

    depthIn3Channels.b = depth;

    gl_FragColor = vec4(depthIn3Channels, 1.0);
}
