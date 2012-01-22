package trb.fps.jsg.shader;

import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import trb.jsg.RenderPass;
import trb.jsg.RenderTarget;
import trb.jsg.SceneGraph;
import trb.jsg.Shape;
import trb.jsg.Texture;
import trb.jsg.Unit;
import trb.jsg.VertexData;
import trb.jsg.View;
import trb.jsg.enums.Format;
import trb.jsg.enums.TextureType;
import trb.jsg.renderer.Renderer;
import trb.jsg.util.SGUtil;
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


	RenderPass renderPass = new RenderPass();

	public Blur(Texture ping, Texture pong) {
		RenderTarget renderTarget = new RenderTarget(ping.getWidth(), ping.getHeight()
				, null, false, pong);

		VertexData vertexData = VertexDataUtils.createQuad(0, 0, ping.getWidth(), ping.getHeight(), 0);

		Shape shape = new Shape(vertexData);
		shape.getState().setDepthTestEnabled(false);
		shape.getState().setUnit(0, new Unit(ping));

		renderPass.setView(View.createOrtho(0, ping.getWidth(), 0, ping.getHeight(), -1000, 1000));
		renderPass.setRenderTarget(renderTarget);
		renderPass.getRootNode().addShape(shape);

//		Renderer renderer = new Renderer(new SceneGraph(renderPass));
//		renderer.render();
	}


	public static void main(String[] args) throws Exception {
		Display.setDisplayMode(new DisplayMode(640, 480));
		Display.create();

		RenderPass renderPass = new RenderPass();
		renderPass.setClearMask(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		renderPass.setView(View.createOrtho(0, 640, 0, 480, -1000, 1000));

		int w = 32;
		int h = 32;
		ByteBuffer byteBuffer = BufferUtils.createByteBuffer(w * h * 4);
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				byte b = (byte) ((x ^ y)  << 7);
				byteBuffer.put(b).put(b).put(b).put(b);
			}
		}
		ByteBuffer[][] pixels = {{byteBuffer}};
		Texture ping = new Texture(TextureType.TEXTURE_2D, 4, 256, 256, 0, Format.BGRA, pixels, false);
		Texture pong = SGUtil.createTexture(4, 256, 256);

		Blur blur = new Blur(ping, pong);

		Shape shape = new Shape(VertexDataUtils.createQuad(100, 100, 356, 356, 0));
		shape.getState().setUnit(0, new Unit(ping));

		renderPass.getRootNode().addShape(shape);
		SceneGraph scenegraph = new SceneGraph();
		scenegraph.addRenderPass(blur.renderPass);
		scenegraph.addRenderPass(renderPass);
		Renderer renderer = new Renderer(scenegraph);


		while (!Display.isCloseRequested()) {
			renderer.render();
			Display.update();
		}

		Display.destroy();
		System.exit(0);
	}
}
