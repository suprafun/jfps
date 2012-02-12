package trb.fps.jsg.shadow;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import javax.vecmath.Color4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import trb.fps.jsg.shader.Blur;
import trb.jsg.DepthBuffer;

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
import trb.jsg.enums.Face;
import trb.jsg.enums.Format;
import trb.jsg.enums.TextureType;
import trb.jsg.enums.Wrap;
import trb.jsg.renderer.Renderer;
import trb.jsg.util.Mat4;
import trb.jsg.util.SGUtil;
import trb.jsg.util.Vec3;
import trb.jsg.util.geometry.VertexDataUtils;

public class VarianceShadowTest {

    public static void main(String[] args) throws Exception {
        Display.setDisplayMode(new DisplayMode(640, 480));
        Display.create();

        Shape baseBox = new Shape(VertexDataUtils.createBox(new Vec3(-1, 1, -1), new Vec3(1, 3, 1)));
        Texture baseTexture = SGUtil.createTexture(GL11.GL_RGBA, 128, 128);
        RenderTarget baseTarget = new RenderTarget(128, 128, new DepthBuffer(GL30.GL_DEPTH24_STENCIL8), false, baseTexture);
        RenderPass basePass = new RenderPass();
        basePass.setClearMask(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
        basePass.setClearColor(new Color4f(0, 1, 1, 0));
        basePass.setView(View.createPerspective((float) Math.PI / 4f, 1, 0.1f, 100f));
        basePass.getView().setCameraMatrix(new Mat4().rotateEulerDeg(-30, 0, 0).translate(0, 0, 10).invert_());
        basePass.setRenderTarget(baseTarget);
        basePass.addShape(new Shape(VertexDataUtils.createBox(new Vec3(-4, -1, -4), new Vec3(4, 0, 4))));
        basePass.addShape(baseBox);

		int w = 256;
		int h = 256;
        ByteBuffer[][] pixels = {{BufferUtils.createByteBuffer(w * h * 4)}};
        Texture shadowTexture = new Texture(TextureType.TEXTURE_2D, GL30.GL_RG32F
                , w, h, 0, Format.RGBA, pixels, false, false);
        shadowTexture.setWrapS(Wrap.CLAMP_TO_EDGE);
        shadowTexture.setWrapT(Wrap.CLAMP_TO_EDGE);
        //shadowTexture.setMinFilter(MinFilter.LINEAR_MIPMAP_LINEAR);
        shadowTexture.setMaxAnisotropy(4f);
        RenderTarget shadowTarget = new RenderTarget(w, h, new DepthBuffer(GL11.GL_DEPTH_COMPONENT), false, shadowTexture);
        RenderPass shadowPass = new RenderPass();
        shadowPass.setClearMask(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
        shadowPass.setClearColor(new Color4f(0, 0, 1, 0));
        shadowPass.setView(View.createPerspective((float) Math.PI / 4f, 1, 0.1f, 100f));
        shadowPass.setRenderTarget(shadowTarget);
        shadowPass.addShape(new Shape(VertexDataUtils.createBox(new Vec3(-4, -1, -4), new Vec3(4, 0, 4))));
        shadowPass.addShape(new Shape(VertexDataUtils.createBox(new Vec3(-1, 1, -1), new Vec3(1, 3, 1))));
        shadowPass.getView().setCameraMatrix(new Mat4().rotateEulerDeg(-90 - 45, 120, 0).translate(0, 0, 10).invert_());

        Shape baseShape = new Shape(VertexDataUtils.createQuad(50, 100 + 256, 256, -256, 0));
        baseShape.getState().setUnit(0, new Unit(baseTexture));
        Shape shadowShape = new Shape(VertexDataUtils.createQuad(350, 100, 256, 256, 0));
        shadowShape.getState().setUnit(0, new Unit(shadowTexture));
        RenderPass finalPass = new RenderPass();
        finalPass.setClearMask(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        finalPass.setClearColor(new Color4f(1, 1, 0, 0));
        finalPass.setView(View.createOrtho(0, 640, 0, 480, -1000, 1000));
        finalPass.addShape(baseShape);
        finalPass.addShape(shadowShape);

        //Texture testTexture = createSampleTexture();
        shadowStuff(basePass, shadowPass, shadowTexture);

        ByteBuffer[][] blurPixels = {{BufferUtils.createByteBuffer(128 * 128 * 4)}};
        Texture blurTexture = new Texture(TextureType.TEXTURE_2D, GL30.GL_RG32F
                , 128, 128, 0, Format.RGBA, blurPixels, false, false);
		blurTexture.setWrapS(Wrap.CLAMP_TO_EDGE);
		blurTexture.setWrapT(Wrap.CLAMP_TO_EDGE);

        SceneGraph finalSceneGraph = new SceneGraph();
        finalSceneGraph.addRenderPass(shadowPass);
		finalSceneGraph.addRenderPass(Blur.createPass(shadowTexture, blurTexture, 1.5f / shadowTexture.getWidth(), 0f));
		finalSceneGraph.addRenderPass(Blur.createPass(blurTexture, shadowTexture, 0, 1.5f / shadowTexture.getWidth()));
        finalSceneGraph.addRenderPass(basePass);
        finalSceneGraph.addRenderPass(finalPass);
        Renderer finalRenderer = new Renderer(finalSceneGraph);

		int angle = 0;
        long startTime = System.currentTimeMillis();
        while (!Display.isCloseRequested()) {
			if (Mouse.isButtonDown(0)) {
				angle += Mouse.getDX();
			}
//			shadowPass.getView().setCameraMatrix(new Mat4().rotateEulerDeg(-90 - 45, angle, 0).translate(0, 0, 14).invert_());
//			basePass.getView().setCameraMatrix(new Mat4().rotateEulerDeg(-30, angle, 0).translate(0, 0, 10).invert_());

            float timeSec = (System.currentTimeMillis() - startTime) / 1000f;
            baseBox.setModelMatrix(new Mat4().rotateEulerDeg(0, timeSec * 45, 0).translate(0, Math.sin(timeSec*1.7f)*2, 0));

            for (int i = 0; i < basePass.getShapeCount(); i++) {
                shadowPass.getShape(i).setModelMatrix(basePass.getShape(i).getModelMatrix());
            }

            //shadowRenderer.render();
            finalRenderer.render();
            Display.update();
        }

        Display.destroy();
    }

    static void shadowStuff(RenderPass basePass, RenderPass lightPass, Texture shadowTexture) {
        Mat4 viewTransform = basePass.getView().getCameraMatrix();
        Mat4 lightTransform = lightPass.getView().getCameraMatrix();

        Mat4 homogenToTexCoord = new Mat4().translate(0.5f, 0.5f, 0.5f).scale(0.5, 0.5, 0.5);

        Mat4 viewToTexture = new Mat4();
        viewToTexture.mul(homogenToTexCoord);
        viewToTexture.mul(lightPass.getView().getProjectionMatrix());
        viewToTexture.mul_(new Mat4(lightTransform));
        viewToTexture.mul_(new Mat4(viewTransform).invert_());

        Mat4 lightToView = new Mat4();
        lightToView.mul_(new Mat4(viewTransform));
        lightToView.mul_(new Mat4(lightTransform).invert_());
        Vec3 lightPosVS = lightToView.transformAsPoint(new Vec3());

        Shader shader = createShader(viewToTexture);
        shader.putUniform(new Uniform("lightPosVS", Uniform.Type.VEC3, lightPosVS.toFloats()));
        for (Shape shape : basePass.getAllShapes()) {
            shape.getState().setShader(shader);
            shape.getState().setUnit(0, new Unit(shadowTexture));
        }

        Shader spotLightShader = new Shader(loadProgram("storeMoments"));
        for (Shape shape : lightPass.getAllShapes()) {
            shape.getState().setShader(spotLightShader);
        }
    }

    static Shader createShader(Mat4 viewToLight) {
        Shader shader = new Shader(loadProgram("shadow2"));
        shader.putUniform(new Uniform("viewToLight", Uniform.Type.MAT4, getTransposedFloats(viewToLight)));
        return shader;
    }

    static ShaderProgram loadProgram(String name) {
        try {
            return ShaderLoader.load(new FileInputStream("data/varianceShadowMapShaders.xml"), name);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    static float[] getTransposedFloats(Mat4 transform) {
        Mat4 m = new Mat4(transform);
        m.transpose();
        return m.toFloats();
    }

    static Texture createSampleTexture() {
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(256 * 256 * 4);
        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 256; x++) {
                byte b = (byte) (x ^ y);
                byteBuffer.put(b).put(b).put(b).put(b);
            }
        }
        ByteBuffer[][] pixels = {{byteBuffer}};
        return new Texture(TextureType.TEXTURE_2D, 4, 256, 256, 0, Format.BGRA, pixels, false, false);
    }
}
