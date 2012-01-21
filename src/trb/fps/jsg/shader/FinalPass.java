package trb.fps.jsg.shader;

import org.lwjgl.opengl.GL11;
import trb.jsg.DepthBuffer;
import trb.jsg.RenderPass;
import trb.jsg.Shader;
import trb.jsg.ShaderProgram;
import trb.jsg.Shape;
import trb.jsg.Texture;
import trb.jsg.Uniform;
import trb.jsg.Unit;
import trb.jsg.VertexData;
import trb.jsg.View;
import trb.jsg.util.geometry.VertexDataUtils;

public class FinalPass {

    private static final String vertexShader =
              "void main(void) {"
            + "    gl_Position = ftransform();"
            + "}";

    private static final String fragmentShader =
            "\nuniform sampler2D light;"
            + "\nuniform sampler2D texture;"
            + "\nuniform vec2 bufferSize;"
            + "\nvoid main(void) {"
            + "\n    vec2 texCoord = gl_FragCoord.xy / bufferSize;"
            + "\n    gl_FragData[0] = texture2D(light, texCoord) * texture2D(texture, texCoord);"
            + "\n}";

    private static final String noTextureFragmentShader =
            "\nuniform sampler2D light;"
            + "\nuniform vec2 bufferSize;"
            + "\nvoid main(void) {"
            + "\n    vec2 texCoord = gl_FragCoord.xy / bufferSize;"
            + "\n    gl_FragData[0] = texture2D(light, texCoord);"
            + "\n}";

    private static ShaderProgram textureProgram;
    private static ShaderProgram noTextureProgram;

    static {
        textureProgram = new ShaderProgram(vertexShader, fragmentShader);
        noTextureProgram = new ShaderProgram(vertexShader, noTextureFragmentShader);
    }

    public static RenderPass createFinalPass(Texture light, Texture texture
            ,int w, int h) {
        VertexData vertexData = VertexDataUtils.createQuad(0, 0, w, h, 0);

        Shape shape = new Shape();
        shape.setVertexData(vertexData);
        shape.getState().setDepthTestEnabled(false);

        applyShader(shape, light, texture, w, h);

        View view = new View();
        view.ortho(0, w, 0, h, -1000, 1000);

        // create a renderpass that renders to the screen
        RenderPass renderPass = new RenderPass();
        renderPass.setClearMask(0);
        renderPass.setView(view);
        renderPass.getRootNode().addShape(shape);
        return renderPass;
    }

    private static void applyShader(Shape shape, Texture light, Texture texture, int w, int h) {
        ShaderProgram program = texture != null ? textureProgram : noTextureProgram;
        Shader shader = new Shader(program);
        shader.putUniform(new Uniform("bufferSize", Uniform.Type.VEC2, (float) w, (float) h));
        shader.putUniform(new Uniform("light", Uniform.Type.INT, new int[]{0}));
        shape.getState().setUnit(0, new Unit(light));

        if (texture != null) {
            shader.putUniform(new Uniform("texture", Uniform.Type.INT, new int[]{1}));
            shape.getState().setUnit(1, new Unit(texture));
        }

        shape.getState().setShader(shader);
    }
}
