package trb.fps.jsg.shader;

import java.nio.ByteBuffer;
import javax.vecmath.Color4f;
import org.lwjgl.BufferUtils;
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
import trb.jsg.enums.Format;
import trb.jsg.enums.TextureType;
import trb.jsg.renderer.Renderer;
import trb.jsg.util.geometry.VertexDataUtils;

public class Blur {
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


	Renderer horisontalRenderer;
	Renderer verticalRenderer;

	public Blur(Texture ping, Texture pong) {
		horisontalRenderer = new Renderer(new SceneGraph(createPass(ping, pong, 1.5f/ping.getWidth(), 0f)));
		verticalRenderer = new Renderer(new SceneGraph(createPass(pong, ping, 0, 1.5f/ping.getHeight())));
	}

	private RenderPass createPass(Texture ping, Texture pong, float offsetx, float offsety) {
		Shader shader = new Shader(new ShaderProgram(vertexShader, fragmentShader));
		shader.putUniform(new Uniform("coefficients", Uniform.Type.FLOAT, 5f/15f, 5f/15f, 5f/15f));
		shader.putUniform(new Uniform("offset", Uniform.Type.VEC2, offsetx, offsety));
		Shape shape = new Shape(VertexDataUtils.createQuad(0, 0, 1, 1, 0));
		shape.getState().setDepthTestEnabled(false);
		shape.getState().setUnit(0, new Unit(ping));
		shape.getState().setShader(shader);
		RenderPass pass = new RenderPass();
		pass.setView(View.createOrtho(0, 1, 1, 0, -1000, 1000));
		pass.setRenderTarget(new RenderTarget(ping.getWidth(), ping.getHeight(), null, false, pong));
		pass.addShape(shape);
		return pass;
	}

	public void blur() {
		horisontalRenderer.render();
		verticalRenderer.render();
	}

	public static void main(String[] args) throws Exception {
		Display.setDisplayMode(new DisplayMode(640, 480));
		Display.create();

		RenderPass renderPass = new RenderPass();
		renderPass.setClearMask(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		renderPass.setClearColor(new Color4f(0.5f, 0.4f, 0.3f, 1f));
		renderPass.setView(View.createOrtho(0, 640, 0, 480, -1000, 1000));

		int w = 256;
		int h = 256;
		ByteBuffer byteBuffer = BufferUtils.createByteBuffer(w * h * 4);
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				byte b = (byte) (((x<<3) ^ (y<<3)));
				byteBuffer.put(b).put(b).put(b).put(b);
			}
		}
		ByteBuffer[][] pixels = {{byteBuffer}};
		ByteBuffer[][] pongPixels =  {{BufferUtils.createByteBuffer(w * h * 4)}};
		Texture ping = new Texture(TextureType.TEXTURE_2D, 4, w, h, 0, Format.BGRA, pixels, false, false);
		Texture pong = new Texture(TextureType.TEXTURE_2D, 4, w, h, 0, Format.BGRA, pongPixels, false, false);

		Blur blur = new Blur(ping, pong);
		for (int i=0; i<2; i++) {
			blur.blur();
		}

		Shape shape = new Shape(VertexDataUtils.createQuad(100, 100, 356, 356, 0));
		shape.getState().setUnit(0, new Unit(ping));

		renderPass.getRootNode().addShape(shape);
		Renderer renderer = new Renderer(new SceneGraph(renderPass));

		while (!Display.isCloseRequested()) {
			renderer.render();
			Display.update();
		}

		Display.destroy();
		System.exit(0);
	}
}
