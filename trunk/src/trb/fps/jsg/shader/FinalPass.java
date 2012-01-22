package trb.fps.jsg.shader;

import trb.jsg.DepthBuffer;
import trb.jsg.RenderPass;
import trb.jsg.RenderTarget;
import trb.jsg.Shader;
import trb.jsg.ShaderProgram;
import trb.jsg.Shape;
import trb.jsg.State.StencilFuncParams;
import trb.jsg.State.StencilOpParams;
import trb.jsg.Texture;
import trb.jsg.Uniform;
import trb.jsg.Unit;
import trb.jsg.VertexData;
import trb.jsg.View;
import trb.jsg.enums.StencilAction;
import trb.jsg.enums.StencilFunc;
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

	public static RenderPass mixPass;
	public static RenderPass transparentPass;
	public static RenderPass toScreenPass;

    public static void createFinalPass(Texture light, Texture texture
			, Texture mixTexture, DepthBuffer depthBuffer,int w, int h, View persView) {
        VertexData vertexData = VertexDataUtils.createQuad(0, 0, w, h, 0);

        Shape shape = new Shape(vertexData);
		shape.getState().setDepthTestEnabled(false);
		shape.getState().setStencilFunc(new StencilFuncParams(StencilFunc.EQUAL, 1, 1));
		shape.getState().setStencilOp(new StencilOpParams(StencilAction.KEEP, StencilAction.KEEP, StencilAction.KEEP));
        applyShader(shape, light, texture, w, h);

		RenderTarget mixRenderTarget = new RenderTarget(
				light.getWidth(), light.getHeight(), null, false, mixTexture);

        // create a renderpass that renders to the screen
        mixPass = new RenderPass();
        mixPass.setClearMask(0);
        mixPass.setView(View.createOrtho(0, w, 0, h, -1000, 1000));
        mixPass.getRootNode().addShape(shape);
		mixPass.setRenderTarget(mixRenderTarget);

		RenderTarget transparentRenderTarget = new RenderTarget(
				light.getWidth(), light.getHeight(), depthBuffer, false, mixTexture);
		transparentPass = new RenderPass();
		transparentPass.setClearMask(0);
		transparentPass.setView(persView);
		transparentPass.setRenderTarget(transparentRenderTarget);

		Shape toScreenShape = new Shape(vertexData);
		toScreenShape.getState().setDepthTestEnabled(false);
		toScreenShape.getState().setUnit(0, new Unit(mixTexture));
		toScreenShape.getState().setMaterial(null);
		toScreenPass = new RenderPass();
		toScreenPass.setClearMask(0);
		toScreenPass.setView(View.createOrtho(0, w, h, 0, -1000, 1000));
		toScreenPass.getRootNode().addShape(toScreenShape);
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
