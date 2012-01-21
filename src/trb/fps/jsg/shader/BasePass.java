package trb.fps.jsg.shader;

import javax.vecmath.Color4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import trb.jsg.DepthBuffer;
import trb.jsg.RenderPass;
import trb.jsg.RenderTarget;
import trb.jsg.ShaderProgram;
import trb.jsg.Texture;
import trb.jsg.View;
import trb.jsg.util.SGUtil;

public class BasePass {
    
    private static final String vertexShader =
              "varying vec3 normal;"
            + "varying vec3 posv;"
            + "void main(void) {"
            + "    normal = (gl_NormalMatrix * gl_Normal);"
            + "    posv = (gl_ModelViewMatrix * gl_Vertex).xyz;"
            + "    gl_Position = ftransform();"
            + "}";
    private static final String fragmentShader =
              "\nvarying vec3 normal;"
            + "\nvarying vec3 posv;"
            + "\nuniform float farClipDistance;"
            + "\nvoid main(void) {"
            + "\n    gl_FragData[0] = vec4( normalize( normal ) * 0.5 + 0.5, -posv.z / farClipDistance );"
            + "\n}";

    public static ShaderProgram baseProgram = new ShaderProgram(vertexShader, fragmentShader);

    public static RenderPass createBasePass(View view, int basew, int baseh) {
        Texture baseTexture = SGUtil.createTexture(GL30.GL_RGBA16F, basew, baseh);
        Texture rgbaTexture = SGUtil.createTexture(GL11.GL_RGBA, basew, baseh);
        RenderTarget renderTarget = new RenderTarget(
                baseTexture.getWidth(), baseTexture.getHeight(), new DepthBuffer(GL30.GL_DEPTH24_STENCIL8), false, baseTexture, rgbaTexture);

        RenderPass renderPass = new RenderPass();
        renderPass.setClearMask(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
        renderPass.setClearColor(new Color4f(0, 1, 0, 0));
        renderPass.setClearStencil(0);
        renderPass.setView(view);
        renderPass.setRenderTarget(renderTarget);
        return renderPass;
    }
}
