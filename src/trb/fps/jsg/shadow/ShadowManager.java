package trb.fps.jsg.shadow;

import java.util.ArrayList;
import java.util.List;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import trb.jsg.DepthBuffer;
import trb.jsg.RenderPass;
import trb.jsg.RenderTarget;
import trb.jsg.Shape;
import trb.jsg.Texture;
import trb.jsg.VertexData;
import trb.jsg.View;
import trb.jsg.util.SGUtil;

public class ShadowManager {

	final int w = 256;
	final int h = 256;

	private RenderTarget renderTarget;
	private RenderPass renderPass;
	private List<ShapePair> shapePairs = new ArrayList();

	public void init() {
		// contins depth
		Texture shadowTexture = SGUtil.createTexture(GL30.GL_RG16F, w, h);
		renderTarget = new RenderTarget(w, h, new DepthBuffer(GL11.GL_DEPTH_COMPONENT), false, shadowTexture);

		renderPass = new RenderPass();
		renderPass.setClearMask(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
		renderPass.setView(View.createPerspective((float) Math.PI / 4, 1f, 1f, 100f));
		renderPass.setRenderTarget(renderTarget);
	}

	public void initShapes(RenderPass basePass) {
		shapePairs.clear();
		while (renderPass.getShapeCount() > 0) {
			renderPass.removeShape(renderPass.getShape(renderPass.getShapeCount() - 1));
		}
		for (int i=0; i<basePass.getShapeCount(); i++) {
			addShape(basePass.getShape(i));
		}
	}

	private void addShape(Shape shape) {
		VertexData vertexData = new VertexData();
		vertexData.coordinates = shape.getVertexData().coordinates;
		vertexData.indices = shape.getVertexData().indices;
		Shape shadowShape = new Shape(vertexData);
		shapePairs.add(new ShapePair(shape, shadowShape));
		renderPass.addShape(shadowShape);
	}

	public void updateShapes() {
		for (ShapePair pair : shapePairs) {
			pair.copy();
		}
	}

	class ShapePair {
		final Shape baseShape;
		final Shape shadowShape;

		public ShapePair(Shape baseShape, Shape shadowShape) {
			this.baseShape = baseShape;
			this.shadowShape = shadowShape;
		}

		void copy() {
			shadowShape.setModelMatrix(baseShape.getModelMatrix());
			shadowShape.setVisible(baseShape.isVisible());
		}
	}
}
