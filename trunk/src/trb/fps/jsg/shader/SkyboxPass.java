package trb.fps.jsg.shader;

import trb.fps.jsg.skybox.Skybox;
import trb.jsg.DepthBuffer;
import trb.jsg.RenderPass;
import trb.jsg.RenderTarget;
import trb.jsg.State.StencilFuncParams;
import trb.jsg.State.StencilOpParams;
import trb.jsg.Texture;
import trb.jsg.View;
import trb.jsg.enums.StencilAction;
import trb.jsg.enums.StencilFunc;
import trb.jsg.util.Mat4;
import trb.jsg.util.Vec3;

public class SkyboxPass {

    public RenderPass renderPass = new RenderPass();
    private View skyboxView;
    private View view;
    private Skybox skybox;

    public SkyboxPass(View view, Texture texture, DepthBuffer baseDepth) {
        this.view = view;
        skyboxView = new View(view);

        RenderTarget renderTarget = new RenderTarget(
                texture.getWidth(), texture.getHeight(), baseDepth, false, texture);

        skybox = new Skybox();
        skybox.shape.getState().setDepthTestEnabled(false);
        skybox.shape.getState().setDepthWriteEnabled(false);
		skybox.shape.getState().setStencilTestEnabled(true);
		skybox.shape.getState().setStencilFunc(new StencilFuncParams(StencilFunc.EQUAL, 0, 1));
		skybox.shape.getState().setStencilOp(new StencilOpParams(StencilAction.KEEP, StencilAction.KEEP, StencilAction.KEEP));

        renderPass.setView(skyboxView);
        renderPass.setClearMask(0);
        renderPass.setRenderTarget(renderTarget);
        renderPass.addShape(skybox.shape);
    }

    public void update() {
        skyboxView.setCameraMatrix(new Mat4(view.getCameraMatrix()).setTranslation_(new Vec3()));
    }
}
