package trb.fps.jsg.shader;

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
import trb.jsg.enums.StencilFunc;
import trb.jsg.util.Mat4;
import trb.jsg.util.Vec3;

public class HemisphereLight {

    private final Vec3 directionWorld = new Vec3();
    private final Shader shader;
    private final Uniform skyColorUniform;
    private final Uniform groundColorUniform;
    private final Uniform directionUniform;
    private Shape shape;

    public HemisphereLight(Vec3 skyColor, Vec3 groundColor, Vec3 directionWorld
            , Texture texture, ShaderProgram shaderProgram, VertexData fullScreenVertexData) {
        this.directionWorld.set(directionWorld);
        this.directionWorld.normalize();
        this.shader = new Shader(shaderProgram);

        skyColorUniform = new Uniform("skyColor", Uniform.Type.VEC3, skyColor.toFloats());
        groundColorUniform = new Uniform("groundColor", Uniform.Type.VEC3, groundColor.toFloats());
        directionUniform = new Uniform("direction", Uniform.Type.VEC3, directionWorld.toFloats());

        shader.putUniform(skyColorUniform);
        shader.putUniform(groundColorUniform);
        shader.putUniform(directionUniform);
        shader.putUniform(new Uniform("bufferSize", Uniform.Type.VEC2, (float) texture.getWidth(), (float) texture.getHeight()));

        shape = new Shape();
        shape.setVertexData(fullScreenVertexData);
        shape.getState().setUnit(0, new Unit(texture));
        shape.getState().setShader(shader);
        shape.getState().setBlendEnabled(true);
        shape.getState().setBlendDstFunc(BlendDstFunc.ONE);
        shape.getState().setBlendSrcFunc(BlendSrcFunc.ONE);
        shape.getState().setDepthTestEnabled(false);
        shape.getState().setDepthWriteEnabled(false);
        shape.getState().setStencilTestEnabled(true);
        shape.getState().setStencilFunc(new StencilFuncParams(StencilFunc.EQUAL, 1, 1));
    }

    public void updateUniforms(View view) {
        shape.setModelMatrix(new Mat4(view.getCameraMatrix()).invert_());
        directionUniform.setFloats(new Mat4(view.getCameraMatrix()).transformAsVector(new Vec3(directionWorld)).toFloats());
    }

    public Shape getShape() {
        return shape;
    }
}
