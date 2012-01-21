// deferred shading VERTEX (LIGHTING: POINT)
varying vec3 posv;

void main( void ){
    posv = ( gl_ModelViewMatrix * gl_Vertex ).xyz;
    gl_Position = ftransform();
}