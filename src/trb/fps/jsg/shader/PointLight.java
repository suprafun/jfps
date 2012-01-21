package trb.fps.jsg.shader;

import trb.jsg.RenderPass;
import trb.jsg.Shader;
import trb.jsg.ShaderProgram;
import trb.jsg.Shape;
import trb.jsg.State.StencilFuncParams;
import trb.jsg.Texture;
import trb.jsg.Uniform;
import trb.jsg.Unit;
import trb.jsg.VertexData;
import trb.jsg.View;
import trb.jsg.enums.BlendDstFunc;
import trb.jsg.enums.BlendSrcFunc;
import trb.jsg.enums.Face;
import trb.jsg.enums.StencilFunc;
import trb.jsg.util.Mat4;
import trb.jsg.util.Vec3;
import trb.jsg.util.geometry.Box;

public class PointLight {

    public final Vec3 positionWorld = new Vec3();
    public float radius;
    private final Shader shader;
    private final Uniform colorUniform;
    private final Uniform positionViewSpaceUniform;
    private final Uniform farClipDistanceUniform;
    private Shape boxShape;
    private Shape quadShape;

    public PointLight(Vec3 color, Vec3 positionWorld, float radius
            , Texture texture, ShaderProgram shaderProgram, VertexData fullScreenVertexData) {
        this.positionWorld.set(positionWorld);
        this.radius = radius;
        this.shader = new Shader(shaderProgram);

        shader.putUniform(new Uniform("radius", Uniform.Type.FLOAT, radius));
        colorUniform = new Uniform("color", Uniform.Type.VEC3, color.toFloats());
        farClipDistanceUniform = new Uniform("farClipDistance", Uniform.Type.FLOAT, 1f);
        positionViewSpaceUniform = new Uniform("position", Uniform.Type.VEC3, 0f, 0f, 0f);
        shader.putUniform(new Uniform("bufferSize", Uniform.Type.VEC2, (float) texture.getWidth(), (float) texture.getHeight()));

        shader.putUniform(colorUniform);
        shader.putUniform(farClipDistanceUniform);
        shader.putUniform(positionViewSpaceUniform);

        boxShape = new Box(new Vec3(-radius, -radius, -radius), new Vec3(radius, radius, radius));
        boxShape.setModelMatrix(new Mat4().translate(positionWorld));
        boxShape.getState().setCullEnabled(true);
        boxShape.getState().setCullFace(Face.BACK);
        boxShape.getState().setUnit(0, new Unit(texture));
        boxShape.getState().setShader(shader);
        boxShape.getState().setBlendEnabled(true);
        boxShape.getState().setBlendDstFunc(BlendDstFunc.ONE);
        boxShape.getState().setBlendSrcFunc(BlendSrcFunc.ONE);
        boxShape.getState().setDepthTestEnabled(true);
        boxShape.getState().setDepthWriteEnabled(false);
        boxShape.getState().setStencilTestEnabled(true);
        boxShape.getState().setStencilFunc(new StencilFuncParams(StencilFunc.EQUAL, 1, 1));

        quadShape = new Shape(fullScreenVertexData);
        quadShape.getState().setUnit(0, new Unit(texture));
        quadShape.getState().setShader(shader);
        quadShape.getState().setBlendEnabled(true);
        quadShape.getState().setBlendDstFunc(BlendDstFunc.ONE);
        quadShape.getState().setBlendSrcFunc(BlendSrcFunc.ONE);
        quadShape.getState().setDepthTestEnabled(false);
        quadShape.getState().setDepthWriteEnabled(false);
        quadShape.getState().setStencilTestEnabled(true);
        quadShape.getState().setStencilFunc(new StencilFuncParams(StencilFunc.EQUAL, 1, 1));
        quadShape.setVisible(false);
    }

    public void setColor(Vec3 color) {
        colorUniform.setFloats(color.toFloats());
    }

    public void updateUniforms(View view) {
        boolean intersectsView = intersectsView(view);
        quadShape.setVisible(intersectsView);
        quadShape.setModelMatrix(new Mat4(view.getCameraMatrix()).invert_());
        boxShape.setVisible(!intersectsView);
        boxShape.setModelMatrix(new Mat4().translate(positionWorld));

        Vec3 vs = new Mat4(view.getCameraMatrix()).transformAsPoint(new Vec3(positionWorld));
        positionViewSpaceUniform.setFloats(vs.toFloats());
        farClipDistanceUniform.setFloats(view.getFar());
    }

    private boolean intersectsView(View view) {
        Vec3 viewPos = new Mat4(view.getCameraMatrix()).invert_().getTranslation();
        float dist = viewPos.distance(positionWorld) ;
        float r = (float) (radius * 1.74 + view.getNear() * 2);
        boolean intersects =  dist < r;
//        System.out.println("intersects " + intersects + " " + dist + " < " + r + " "
//                + viewPos + " " + positionWorld);
        return intersects;
    }

    public void addShapes(RenderPass renderPass) {
        renderPass.getRootNode().addShape(boxShape);
        renderPass.getRootNode().addShape(quadShape);
    }
}
