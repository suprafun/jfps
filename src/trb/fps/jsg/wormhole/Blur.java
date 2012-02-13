package trb.fps.jsg.wormhole;

import java.nio.ByteBuffer;
import java.util.Random;
import javax.vecmath.Color4f;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import trb.jsg.RenderPass;
import trb.jsg.RenderTarget;
import trb.jsg.SceneGraph;
import trb.jsg.Shader;
import trb.jsg.ShaderProgram;
import trb.jsg.Shape;
import trb.jsg.Texture;
import trb.jsg.Uniform;
import trb.jsg.Unit;
import trb.jsg.View;
import trb.jsg.enums.MagFilter;
import trb.jsg.enums.MinFilter;
import trb.jsg.renderer.Renderer;
import trb.jsg.util.SGUtil;
import trb.jsg.util.geometry.VertexDataUtils;

public class Blur {
	private static String vertexShader =
			"void main(void) {"
			+ "    gl_TexCoord[0] = gl_MultiTexCoord0;"
			+ "    gl_Position    = ftransform();"
			+ "}";

	private static String fragmentShader =
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

	private static String vertexShader2 =
			"void main(void) {"
			+ "    gl_TexCoord[0] = gl_MultiTexCoord0;"
			+ "    gl_Position    = ftransform();"
			+ "}";
	private static String fragmentShader2 =
			"uniform sampler2D source;"
			+ "uniform float time;"
			+ "void main(void) {"
			+ "    vec2 tc = vec2(gl_TexCoord[0].s, time);"
			+ "    float rayLength = texture2D(source, tc).x;"
//			+ "    vec4 c = vec4(1.0 - (gl_TexCoord[0].t * rayLength));"
			+ "    vec4 c = vec4(1.0);\n"
			+ "    float a = gl_TexCoord[0].t;\n"
			+ "    if (rayLength < a) {\n"
			+ "       c = vec4(0.0);"
			+ "    }\n"
			+ "    gl_FragColor = c;\n"
			+ "}";


	public static void main(String[] args) throws Exception {
		Display.setDisplayMode(new DisplayMode(640, 480));
		Display.create();

		int w = 256;
		int h = 256;
		Texture from = SGUtil.createTexture(4, w, h);
		fillRandom(from);
		from.setMagFilter(MagFilter.LINEAR);
		from.setMinFilter(MinFilter.LINEAR);
		Texture to = SGUtil.createTexture(4, w, h);
		to.setMagFilter(MagFilter.LINEAR);
		to.setMinFilter(MinFilter.LINEAR);
		blur(from, to);

		ShaderProgram shaderProgram = new ShaderProgram(vertexShader2, fragmentShader2);
		Shader vertShader = new Shader(shaderProgram);
		vertShader.putUniform(new Uniform("time", Uniform.Type.FLOAT, 0f));
		Shape vertShape = new Shape(VertexDataUtils.createQuad(0, 0, w, h, 0));
		vertShape.getState().setUnit(0, new Unit(from));
		vertShape.getState().setShader(vertShader);
		RenderTarget vertTarget = new RenderTarget(w, h, null, false, to);
		RenderPass vertPass = new RenderPass();
		vertPass.setView(View.createOrtho(0, w, 0, h, -1000, 1000));
		vertPass.addShape(vertShape);
		vertPass.setRenderTarget(vertTarget);

		Shape shape = new Shape(VertexDataUtils.createQuad(100, 100, 300, 300, 0));
		shape.getState().setUnit(0, new Unit(to));

		RenderPass renderPass = new RenderPass();
		renderPass.setClearMask(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		renderPass.setClearColor(new Color4f(1, 0, 0, 1));
		renderPass.setView(View.createOrtho(0, 640, 0, 480, -1000, 1000));
		renderPass.getRootNode().addShape(shape);
		SceneGraph sceneGraph = new SceneGraph();
		sceneGraph.addRenderPass(vertPass);
		sceneGraph.addRenderPass(renderPass);
		Renderer renderer = new Renderer(sceneGraph);

		long startTime = System.currentTimeMillis();
		while (!Display.isCloseRequested()) {
			float time = (System.currentTimeMillis() - startTime) / (1000f * 256);
			Display.setTitle("" + time);
			vertShader.putUniform(new Uniform("time", Uniform.Type.FLOAT, time));
			renderer.render();
			Display.update();
			Display.sync(60);
		}

		Display.destroy();
		System.exit(0);
	}

	public static void fillRandom(Texture from) {
		Random random = new Random();
		ByteBuffer byteBuffer = from.getPixels()[0][0];
		for (int i = 0; i < byteBuffer.limit(); i++) {
			byteBuffer.put(i, (byte) random.nextInt());
		}
	}

	public static void blur(Texture from, Texture to) {
		int w = from.getWidth();
		int h = from.getHeight();
		float offset = 1.5f / w;
		ShaderProgram shaderProgram = new ShaderProgram(vertexShader, fragmentShader);

//		float[] coef| = {5f / 21f, 6f / 21f, 5f / 21f};
		float[] coef = {6f / 21f, 7f / 21f, 6f / 21f};

		Shader horShader = new Shader(shaderProgram);
		horShader.putUniform(new Uniform("coefficients", Uniform.Type.FLOAT, coef));
		horShader.putUniform(new Uniform("offset", Uniform.Type.VEC2, offset, 0f));
		Shape horShape = new Shape(VertexDataUtils.createQuad(0, 0, w, h, 0));
		horShape.getState().setUnit(0, new Unit(from));
		horShape.getState().setShader(horShader);
		RenderTarget horTarget = new RenderTarget(w, h, null, false, to);
		RenderPass horPass = new RenderPass();
		horPass.setView(View.createOrtho(0, w, 0, h, -1000, 1000));
		horPass.addShape(horShape);
		horPass.setRenderTarget(horTarget);

		Shader vertShader = new Shader(shaderProgram);
		vertShader.putUniform(new Uniform("coefficients", Uniform.Type.FLOAT, coef));
		vertShader.putUniform(new Uniform("offset", Uniform.Type.VEC2, 0f, offset));
		Shape vertShape = new Shape(VertexDataUtils.createQuad(0, 0, w, h, 0));
		vertShape.getState().setUnit(0, new Unit(to));
		vertShape.getState().setShader(vertShader);
		RenderTarget vertTarget = new RenderTarget(w, h, null, false, from);
		RenderPass vertPass = new RenderPass();
		vertPass.setView(View.createOrtho(0, w, 0, h, -1000, 1000));
		vertPass.addShape(vertShape);
		vertPass.setRenderTarget(vertTarget);

		SceneGraph sceneGraph = new SceneGraph(horPass);
		sceneGraph.addRenderPass(vertPass);
		Renderer renderer = new Renderer(sceneGraph);
		for (int i = 0; i < 5; i++) {
			renderer.render();
		}
	}
}
