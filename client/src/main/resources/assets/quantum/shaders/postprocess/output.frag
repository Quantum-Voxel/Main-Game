#version 330 core

out vec4 FragColor;

in vec2 uv;

uniform sampler2D colorTexture;
uniform sampler2D refTexture;
uniform sampler2D maskTexture;

void main(){
  vec2 uv = gl_FragCoord.xy / textureSize(colorTexture, 0).xy;
  uv.y = 1 - uv.y;
  vec3 baseColor = texture(colorTexture, uv).rgb;
  //vec3 ks = texture(specularTexture, v_texCoords).rgb;
  // reflection factor from material
  float reflectionStrength = texture(maskTexture, uv.xy).g;
  vec4 reflectionColor = texture(refTexture, uv.xy);
  if (reflectionColor.a < 1.0){
    FragColor = vec4(baseColor, 1);
    return;
  }

  vec3 finalColor = mix(baseColor, reflectionColor.rgb, 0.6);
  FragColor = vec4(finalColor, 1);
}