package trb.fps.jsg.shader;

import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import trb.jsg.DepthBuffer;
import trb.jsg.RenderPass;
import trb.jsg.RenderTarget;
import trb.jsg.Shader;
import trb.jsg.ShaderProgram;
import trb.jsg.Shape;
import trb.jsg.Texture;
import trb.jsg.Uniform;
import trb.jsg.Unit;
import trb.jsg.View;
import trb.jsg.enums.Format;
import trb.jsg.enums.TextureType;
import trb.jsg.util.geometry.VertexDataUtils;

public class Bloom {
    
    public Texture sourceTexture;
    public Texture bloomTexture;
    public RenderPass renderPass = new RenderPass();

    private String vertexShader =
            "void main(void) {"
            + "    gl_TexCoord[0] = gl_MultiTexCoord0;"
            + "    gl_Position    = ftransform();"
            + "}";

    private String fragmentShader =
            "uniform sampler2D source;"
            + "uniform float coefficients[3];"
            + "uniform vec2 offset;"
            + "void main(void) {"
            + "    vec4 c;"
            + "    vec2 tc = gl_TexCoord[0].st;"
            + ""
            + "    c  = coefficients[0] * texture2D(source, tc - offset);"
            + "    c += coefficients[1] * texture2D(source, tc);"
            + "    c += coefficients[2] * texture2D(source, tc + offset);"
            + ""
            + "    gl_FragColor = c;"
            + "}";

    public Bloom(Texture texture) {
        this.sourceTexture = texture;

        int w = texture.getWidth();
        int h = texture.getHeight();
        int w2 = w >> 1;
        int h2 = h >> 1;
        float offset = 1.5f / w;

        Shader shader = new Shader(new ShaderProgram(vertexShader, fragmentShader));
        shader.putUniform(new Uniform("coefficients", Uniform.Type.FLOAT, 5f/21f, 6f/21f, 5f/21f));
        shader.putUniform(new Uniform("offset", Uniform.Type.VEC2, offset, 0f));

        ByteBuffer[][] pixels = {{BufferUtils.createByteBuffer(w2 * h2 * 4)}};
        bloomTexture = new Texture(TextureType.TEXTURE_2D, GL30.GL_RGBA16F, w2, h2, 0, Format.RGBA, pixels, false, false);

        Shape shape = new Shape();
        shape.setVertexData(VertexDataUtils.createQuad(0, 0, w2, h2, 0));
        shape.getState().setUnit(0, new Unit(sourceTexture));
        shape.getState().setShader(shader);

        View view = View.createOrtho(0, w2, h2, 0, -1000, 1000);

        RenderTarget renderTarget = new RenderTarget(w2, h2, null, false, bloomTexture);

        renderPass.setClearMask(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        renderPass.setView(view);
        renderPass.getRootNode().addShape(shape);
        renderPass.setRenderTarget(renderTarget);
    }
}

