package trb.fps.jsg;

import javax.vecmath.Color4f;
import javax.vecmath.Matrix4f;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import trb.fps.FpsRenderer;
import trb.fps.FpsServer;
import trb.fps.Level;
import trb.fps.LevelGenerator;
import trb.fps.model.BulletData;
import trb.fps.model.LevelData;
import trb.fps.model.PlayerData;
import trb.jsg.LightState.Light;
import trb.jsg.RenderPass;
import trb.jsg.SceneGraph;
import trb.jsg.Shader;
import trb.jsg.Shape;
import trb.jsg.State.Material;
import trb.jsg.TreeNode;
import trb.jsg.VertexData;
import trb.jsg.View;
import trb.jsg.renderer.Renderer;
import trb.jsg.util.Mat4;
import trb.jsg.util.ShaderUtils;
import trb.jsg.util.Vec3;

public class JsgRenderer implements FpsRenderer {

    public boolean useTopView = false;

    private View view = new View();
    private Renderer renderer;
    private long startTimeMillis = System.currentTimeMillis();

    private TreeNode[] playerModels;
    private TreeNode[] bulletModels;

    private JsgHud hud;

    private Level level;

    private Shader shader;


    public void init(Level level) {
        this.level = level;

        view.setCameraMatrix(new Mat4());
        float fovy = (float) Math.toRadians(60);
        float aspect = Display.getDisplayMode().getWidth() / (float) Display.getDisplayMode().getHeight();
        view.perspective(fovy, aspect, 0.1f, 10000f);

        shader = ShaderUtils.loadFromResource(
                "/trb/fps/jsg/shader/pointLightVertex.shader"
                , "/trb/fps/jsg/shader/pointLightFragment.shader");

        Light light1 = new Light();
        light1.specular.set(0, 0, 0);
        light1.diffuse.set(1f, 1f, 1f);
        light1.setPointLight(new Vec3(5, 15, 0), 1f, 0.001f, 0);
//        light1.setDirectionalLight(new Vec3(1, -1, -1).normalize_());
        Light light2 = new Light();
        light2.specular.set(0, 0, 0);
        light2.diffuse.set(0.5f, 0.5f, 0.5f);
        light2.setDirectionalLight(new Vec3(-1, -0.5f, 0).normalize_());
        Light light3 = new Light();
        light3.specular.set(0, 0, 0);
        light3.diffuse.set(0.5f, 1f, 0.5f);
        light3.setDirectionalLight(new Vec3(0,  1, 0).normalize_());

        // create a renderpass that renders to the screen
        RenderPass renderPass = new RenderPass();
        renderPass.setClearMask(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        renderPass.setClearColor(new Color4f(0, 0, 0.4f, 0));
        renderPass.setView(view);
        renderPass.getLightState().lights.set(light1, 0);
        renderPass.getLightState().lights.set(light2, 1);
        renderPass.getLightState().lights.set(light3, 2);

        // add shape to the renderpass tree
        for (Shape shape : new LevelGenerator().get()) {
            shape.getState().setShader(shader);
            renderPass.getRootNode().addShape(shape);
            level.physicsLevel.addAsConvexHull(shape, false);
        }

        playerModels = createModels(LevelData.MAX_PLAYERS, renderPass.getRootNode()
                , JsgBox.createFromPosSize(new Vec3(0, 1, 0), new Vec3(0.5f, 2f, 0.08f)));
        bulletModels = createModels(LevelData.MAX_BULLETS, renderPass.getRootNode()
                , JsgBox.createFromPosSize(new Vec3(0, 0, 0), new Vec3(0.5f, 0.5f, 0.5f)));

        // add renderpass to scene graph
        SceneGraph sceneGraph = new SceneGraph();
        sceneGraph.insertRenderPass(renderPass, 0);

        hud = new JsgHud();
        sceneGraph.insertRenderPass(hud.renderPass, 1);

        // create a renderer that renders the scenegraph
        renderer = new Renderer(sceneGraph);
    }


    private TreeNode[] createModels(int cnt, TreeNode root, VertexData vertexData) {
        TreeNode[] nodes = new TreeNode[cnt];
        for (int i = 0; i < cnt; i++) {
            Shape shape = new Shape();
            shape.getState().setCullEnabled(true);
            shape.getState().setMaterial(new Material());
            shape.getState().setShader(shader);
            //playerShape.setVisible(false);
            shape.setVertexData(vertexData);

            TreeNode node = new TreeNode();
            root.addChild(node);
            node.addShape(shape);
            nodes[i] = node;
            //node.setTranform(new Mat4().getMatrix4f());
        }
        return nodes;
    }

    public void render(Level l, int localPlayerIdx) {
        LevelData level = l.levelData;

        float timeSec = (System.currentTimeMillis() - startTimeMillis) / 1000f;

        if (localPlayerIdx >= 0 && !useTopView) {
            //PlayerData player = level.players[localPlayerIdx];
            PlayerData player = l.predictedState.getCurrentState();
            view.setCameraMatrix(player.getViewTransform());
        } else {
            float angle = level.serverTimeMillis / 3000f;
            view.setCameraMatrix(new Mat4()
                    .lookAt(new Vec3(Math.cos(angle)*50, 15, Math.sin(angle)*50), new Vec3(0, 2, 0), new Vec3(0, 1, 0))
                    );
        }

        renderPlayers(l, localPlayerIdx);
        renderBullets(level);

        hud.render(level, localPlayerIdx);

        // render the scene graph
        renderer.render();

        // flip backbuffer
        Display.update();
    }

    private void renderPlayers(Level level, int localPlayerIdx) {
        for (int i = 0; i < playerModels.length; i++) {
            PlayerData player = level.interpolatedState.get(i).getCurrentState();
            boolean isLocal = (i == localPlayerIdx);
            boolean visible = player.isConnected() && (!isLocal || useTopView);
            for (Shape shape : playerModels[i].getAllShapesInTree()) {
                shape.setVisible(visible);
            }
            if (visible) {
                Vec3 pos = new Vec3(player.getPosition());
                if (player.getHealth() > 0) {
                    playerModels[i].setTransform(player.getModelTransform());
                } else {
                    playerModels[i].setTransform(new Mat4()
                            .setTranslation_(new Vec3(pos).add_(new Vec3(0, 1, 0)))
                            .setEuler(new Vec3(0, 0, Math.PI / 2)));
                }
            } else {
                // some bug workaround
                playerModels[i].setTransform(new Mat4());
            }
        }
    }

    private void renderBullets(LevelData level) {
        for (int i = 0; i < level.bullets.length; i++) {
            BulletData bullet = level.bullets[i];
            for (Shape shape : bulletModels[i].getAllShapesInTree()) {
                shape.setVisible(bullet.alive);
            }
            if (bullet.alive) {
                Vec3 bulletPos = FpsServer.getPositionAtTime(bullet, level.serverTimeMillis);
                bulletModels[i].setTransform(new Mat4().setTranslation_(bulletPos));
            }
        }
    }
}
