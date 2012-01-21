package trb.fps.jsg.shader;

import javax.vecmath.Color4f;
import javax.vecmath.Matrix4f;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import trb.jsg.LightState.Light;

import trb.jsg.RenderPass;
import trb.jsg.SceneGraph;
import trb.jsg.Shader;
import trb.jsg.Shape;
import trb.jsg.TreeNode;
import trb.jsg.VertexData;
import trb.jsg.View;
import trb.jsg.renderer.Renderer;
import trb.jsg.util.ShaderUtils;
import trb.jsg.util.Vec3;

/**
 * Renders a single triangle in ortho mode.
 *
 * @author tombr
 *
 */
public class JsgShaderTest {

    public static void main(String[] args) throws Exception {
        // Use LWJGL to create a frame
        int windowWidth = 640;
        int windowHeight = 480;
        Display.setDisplayMode(new DisplayMode(windowWidth, windowHeight));
        Display.create();

        // ortho mode with a 1:1 mapping to the screen
        View view = new View();
        view.ortho(0, windowWidth, 0, windowHeight, -1000, 1000);

        Light light1 = new Light();
        light1.specular.set(0, 0, 0);
        light1.diffuse.set(0.5f, 0.5f, 0.5f);
        light1.setPointLight(new Vec3(200, 200, 200), 1, 0, 0);

        // create a renderpass that renders to the screen
        RenderPass renderPass = new RenderPass();
        renderPass.setClearMask(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        renderPass.setClearColor(new Color4f(0, 0, 0.4f, 0));
        renderPass.setView(view);
        renderPass.getLightState().lights.set(light1, 0);

        // create the shader
        Shader shader = ShaderUtils.loadFromResource(
                "/trb/fps/jsg/shader/pointLightVertex.shader"
                , "/trb/fps/jsg/shader/pointLightFragment.shader");

        // a simple triangle
        VertexData vertexData = new VertexData();
        vertexData.setCoordinates(
                new float[]{100, 100, 0, 100, 400, 0, 400, 400, 0,  400, 100, 0} // coordinates
                , new float[] {0, 0, 1,  0, 0, 1,  0, 0, 1,  0, 0, 1}
                , null, 0, null, new int[]{0, 1, 2, 2, 3, 0} // indices
                );

        // shape has vertex data, state and matrix
        Shape shape = new Shape();
        shape.getState().setShader(shader);
        shape.setVertexData(vertexData);

        // add shape to the renderpass tree
        TreeNode root = renderPass.getRootNode();
        root.addShape(shape);

        // add renderpass to scene graph
        SceneGraph sceneGraph = new SceneGraph();
        sceneGraph.insertRenderPass(renderPass, 0);

        // create a renderer that renders the scenegraph
        Renderer renderer = new Renderer(sceneGraph);

        // main game loop
        while (!Display.isCloseRequested()) {
            // render the scene graph
            renderer.render();

            // flip backbuffer
            Display.update();
        }

        // destroy frame when we're done
        Display.destroy();
    }
}
