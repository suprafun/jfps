// deferred shading FRAGMENT (LIGHTING: POINT)
varying vec3 posv;
uniform float farClipDistance;
uniform sampler2D geotexture;
uniform float radius;
uniform vec3 position;
uniform vec2 bufferSize;
uniform vec3 color;

void main( void ){
    vec3 viewRay = vec3(posv.xy * (-farClipDistance / posv.z), -farClipDistance);
    vec2 texCoord = gl_FragCoord.xy / bufferSize;
    vec4 normalAndNormalisedDepth = texture2D(geotexture, texCoord);
    vec3 positionVS = viewRay * normalAndNormalisedDepth.a;
    vec3 lightPosVS = position;
    float distanceLight = length(lightPosVS - positionVS);
    if (distanceLight > radius) {
       gl_FragData[0] = vec4(0);
    } else {
      vec3 lightDir = normalize(positionVS - lightPosVS);
      vec3 n = normalAndNormalisedDepth.xyz * 2.0 - 1.0;
      float NdotL = dot(n, lightDir);
      vec3 halfVector = normalize(lightPosVS*0.5-positionVS);
      float factor = 1.0 - distanceLight/radius;
      float specular = pow(max(dot(n, halfVector),0.0), 64.0) * factor;
      if (NdotL > 0.0) {
        gl_FragData[0] = vec4(specular);
      } else {
        gl_FragData[0] = vec4(-NdotL * color * factor, 1) + (specular * factor);
      }
    }
}
