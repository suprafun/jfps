package trb.fps.jsg.shadow;

import javax.vecmath.Color4f;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import trb.jsg.DepthBuffer;

import trb.jsg.RenderPass;
import trb.jsg.RenderTarget;
import trb.jsg.SceneGraph;
import trb.jsg.Shape;
import trb.jsg.Texture;
import trb.jsg.Unit;
import trb.jsg.View;
import trb.jsg.renderer.Renderer;
import trb.jsg.util.Mat4;
import trb.jsg.util.SGUtil;
import trb.jsg.util.Vec3;
import trb.jsg.util.geometry.VertexDataUtils;

public class ShadowTest {

	public static void main(String[] args) throws Exception {
		Display.setDisplayMode(new DisplayMode(640, 480));
		Display.create();

		Shape baseBox = new Shape(VertexDataUtils.createBox(new Vec3(-1, 1, -1), new Vec3(1, 3, 1)));
		Texture baseTexture = SGUtil.createTexture(GL11.GL_RGBA, 128, 128);
		RenderTarget baseTarget = new RenderTarget(128, 128, new DepthBuffer(GL30.GL_DEPTH24_STENCIL8), false, baseTexture);
		RenderPass basePass = new RenderPass();
		basePass.setClearMask(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
		basePass.setClearColor(new Color4f(0, 1, 1, 0));
		basePass.setView(View.createPerspective((float)Math.PI / 4f, 1, 0.1f, 100f));
		basePass.getView().setCameraMatrix(new Mat4().rotateEulerDeg(-30, 10, 0).translate(0, 0, 10).invert_());
		basePass.setRenderTarget(baseTarget);
		basePass.addShape(new Shape(VertexDataUtils.createBox(new Vec3(-4, -1, -4), new Vec3(4, 0, 4))));
		basePass.addShape(baseBox);

		Texture shadowTexture = SGUtil.createTexture(GL11.GL_RGBA, 128, 128);
		RenderTarget shadowTarget = new RenderTarget(128, 128, new DepthBuffer(GL30.GL_DEPTH24_STENCIL8), false, shadowTexture);
		RenderPass shadowPass = new RenderPass();
		shadowPass.setClearMask(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
		shadowPass.setClearColor(new Color4f(0, 0, 1, 0));
		shadowPass.setView(View.createPerspective((float) Math.PI / 4f, 1, 0.1f, 100f));
		shadowPass.setRenderTarget(shadowTarget);
		shadowPass.addShape(new Shape(VertexDataUtils.createBox(new Vec3(-4, -1, -4), new Vec3(4, 0, 4))));
		shadowPass.addShape(new Shape(VertexDataUtils.createBox(new Vec3(-1, 1, -1), new Vec3(1, 3, 1))));
		shadowPass.getView().setCameraMatrix(new Mat4().rotateEulerDeg(-50, 0, 0).translate(0, 0, 10).invert_());
		Renderer shadowRenderer = new Renderer(new SceneGraph(shadowPass));

		Shape baseShape = new Shape(VertexDataUtils.createQuad(100, 100, 128, 128, 0));
		baseShape.getState().setUnit(0, new Unit(baseTexture));
		Shape shadowShape = new Shape(VertexDataUtils.createQuad(300, 100, 128, 128, 0));
		shadowShape.getState().setUnit(0, new Unit(shadowTexture));
		RenderPass finalPass = new RenderPass();
		finalPass.setClearMask(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		finalPass.setClearColor(new Color4f(1, 1, 0, 0));
		finalPass.setView(View.createOrtho(0, 640, 0, 480, -1000, 1000));
		finalPass.addShape(baseShape);
		finalPass.addShape(shadowShape);

		SceneGraph finalSceneGraph = new SceneGraph();
		finalSceneGraph.addRenderPass(basePass);
//		finalSceneGraph.addRenderPass(shadowPass);
		finalSceneGraph.addRenderPass(finalPass);
		Renderer finalRenderer = new Renderer(finalSceneGraph);

		long startTime = System.currentTimeMillis();
		while (!Display.isCloseRequested()) {
			float timeSec = (System.currentTimeMillis() - startTime) / 1000f;
			baseBox.setModelMatrix(new Mat4().rotateEulerDeg(0, timeSec * 45, 0));
			shadowRenderer.render();
			finalRenderer.render();
			Display.update();
		}

		Display.destroy();
	}
}
