/*
 * HALDEN VR PLATFORM
 *
 * RADIATION MODULE
 *
 * $RCSfile: $
 *
 * Author :
 * Date   :
 * Version: $Revision: $ ($Date: $)
 *
 * (c) 2000-2011 Halden Virtual Reality Centre <http://www.ife.no/vr/>,
 * Institutt for energiteknikk. All rights reserved.
 *
 * This code is the property of Halden VR Centre <vr-info@hrp.no> and may
 * only be used in accordance with the terms of the license agreement
 * granted.
 */

package trb.fps.jsg.shader;

import javax.vecmath.Color4f;
import org.lwjgl.opengl.GL11;
import trb.fps.jsg.skybox.Skybox;
import trb.jsg.DepthBuffer;
import trb.jsg.RenderPass;
import trb.jsg.RenderTarget;
import trb.jsg.Texture;
import trb.jsg.View;
import trb.jsg.util.Mat4;
import trb.jsg.util.Vec3;

/**
 *
 * @author tomrbryn
 */
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

        renderPass.setView(skyboxView);
        renderPass.setClearMask(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        renderPass.setClearColor(new Color4f(1, 1, 1, 1));
        renderPass.setRenderTarget(renderTarget);
        renderPass.addShape(skybox.shape);
    }

    public void update() {
        skyboxView.setCameraMatrix(new Mat4(view.getCameraMatrix()).setTranslation_(new Vec3()));
    }
}
