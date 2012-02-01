package trb.fps.editor;

import trb.jsg.Shader;
import trb.jsg.ShaderProgram;
import trb.jsg.Shape;
import trb.jsg.State.StencilFuncParams;
import trb.jsg.State.StencilOpParams;
import trb.jsg.TreeNode;
import trb.jsg.enums.BlendDstFunc;
import trb.jsg.enums.BlendSrcFunc;
import trb.jsg.enums.DepthFunc;
import trb.jsg.enums.PolygonMode;
import trb.jsg.enums.StencilAction;
import trb.jsg.enums.StencilFunc;
import trb.jsg.util.Vec3;
import trb.jsg.util.geometry.VertexDataUtils;

public class SelectionViualisation {

    private static final String vertexShader =
            ""
            + "void main(void) {"
            + "    gl_Position = ftransform();"
            + "}";
    private static final String fragmentShader =
            ""
            + "\nvoid main(void) {"
            + "\n    gl_FragData[0] = vec4(0.5, 0.4, 0.8, 1.0);"
            + "\n}";

    public final Shape selectionShape = new Shape(VertexDataUtils.createBox(new Vec3(-2, -2, -2), new Vec3(2, 2, 2)));
    public final TreeNode treeNode = new TreeNode(selectionShape);

    public SelectionViualisation() {
        selectionShape.getState().setBlendEnabled(true);
        selectionShape.getState().setBlendSrcFunc(BlendSrcFunc.SRC_COLOR);
        selectionShape.getState().setBlendDstFunc(BlendDstFunc.DST_COLOR);
        selectionShape.getState().setDepthFunc(DepthFunc.LEQUAL);
        selectionShape.getState().setCullEnabled(true);
        selectionShape.getState().setPolygonMode(PolygonMode.LINE);
        selectionShape.getState().setLineWidth(3);
        selectionShape.getState().setLineSmooth(true);
        selectionShape.getState().setShader(new Shader(new ShaderProgram(vertexShader, fragmentShader)));
        selectionShape.getState().setStencilTestEnabled(true);
        selectionShape.getState().setStencilFunc(new StencilFuncParams(StencilFunc.ALWAYS, 1, 1));
        selectionShape.getState().setStencilOp(new StencilOpParams(StencilAction.KEEP, StencilAction.KEEP, StencilAction.REPLACE));
    }
}
