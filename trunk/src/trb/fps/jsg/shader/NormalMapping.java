package trb.fps.jsg.shader;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import trb.jsg.Shader;
import trb.jsg.ShaderProgram;
import trb.jsg.Shape;
import trb.jsg.Texture;
import trb.jsg.Uniform;
import trb.jsg.Uniform.Type;
import trb.jsg.Unit;
import trb.jsg.VertexData;
import trb.jsg.VertexData.AttributeData;
import trb.jsg.enums.MagFilter;
import trb.jsg.enums.MinFilter;
import trb.jsg.examples.NormalMap;
import trb.jsg.util.ShaderUtils;

/**
 * Deferred normal mapping.
 */
public class NormalMapping {
    private static final String vertexShader =
            "attribute vec4 tangentIn;"
            + "varying vec3 tangent;"
            + "varying vec3 bitangent;"
            + "varying vec3 normal;"
            + "varying vec3 posv;"
            + "void main(void) {"
            + "    tangent = gl_NormalMatrix * tangentIn.xyz;"
            + "    normal = (gl_NormalMatrix * gl_Normal);"
            + "    bitangent = cross(normal, tangent) * tangentIn.w;"
            + "    posv = (gl_ModelViewMatrix * gl_Vertex).xyz;"
            + "    gl_TexCoord[0] = gl_MultiTexCoord0;"
            + "    gl_TexCoord[1] = gl_MultiTexCoord1;"
            + "    gl_Position = ftransform();"
            + "}";
    private static final String fragmentShader =
              "\nuniform sampler2D texturemap;"
            + "\nuniform sampler2D normalmap;"
            + "\nvarying vec3 tangent;"
            + "\nvarying vec3 bitangent;"
            + "\nvarying vec3 normal;"
            + "\nvarying vec3 posv;"
            + "\nuniform float farClipDistance;"
            + "\nvoid main(void) {"
            + "\n    vec3 n = normal;"
            + "\n    vec3 t = tangent;"
            + "\n    vec3 b = bitangent;"
            + "\n    mat3 base = mat3(t, b, n);"
			+ "\n    n = texture2D(normalmap, gl_TexCoord[0].xy).xyz * 2.0 - 1.0;"
			+ "\n    vec3 nVS = base * n;"
            + "\n    gl_FragData[0] = vec4( normalize( nVS ) * 0.5 + 0.5, -posv.z / farClipDistance );"
            + "\n    gl_FragData[1] = texture2D(texturemap, gl_TexCoord[0].xy).xyzw;"
            + "\n}";

    public static final Shader shader;
    private static final Texture texturemap;
    private static final Texture normalmap;

    static {
        shader = new Shader(new ShaderProgram(vertexShader, fragmentShader, "tangentIn"));
        shader.putUniform(new Uniform("texturemap", Type.INT, new int[] {0}));
        shader.putUniform(new Uniform("normalmap", Type.INT, new int[]{1}));
        BufferedImage textureImage = null;
        BufferedImage normalImage = null;
        try {
            textureImage = ImageIO.read(NormalMapping.class.getResource("texturemap.jpg"));
            normalImage = ImageIO.read(NormalMapping.class.getResource("normalmap.jpg"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        texturemap = NormalMap.createNormalMapTexture(textureImage);
        texturemap.setMagFilter(MagFilter.LINEAR);
        texturemap.setMinFilter(MinFilter.LINEAR_MIPMAP_NEAREST);
        normalmap = NormalMap.createNormalMapTexture(normalImage);
        normalmap.setMagFilter(MagFilter.LINEAR);
        normalmap.setMinFilter(MinFilter.LINEAR_MIPMAP_NEAREST);
    }


    public static void apply(Shape shape) {
        VertexData vertexData = shape.getVertexData();
        float[] tangent = ShaderUtils.calculateTangent(vertexData, 0);
        vertexData.attributes.set(new AttributeData(tangent, 4), 0);
        vertexData.texCoords.set(vertexData.texCoords.get(0), 1);
        vertexData.changed();
        shape.setVertexData(vertexData);
        shape.getState().setShader(shader);
        shape.getState().setUnit(0, new Unit(texturemap));
        shape.getState().setUnit(1, new Unit(normalmap));
    }
}
