package trb.fps.jsg;
import java.util.Random;
import javax.vecmath.Point2f;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import trb.fps.FpsRenderer;
import trb.fps.FpsServer;
import trb.fps.Level;
import trb.fps.LevelGenerator;
import trb.fps.jsg.shader.BasePass;
import trb.fps.jsg.shader.FinalPass;
import trb.fps.jsg.shader.LightManager;
import trb.fps.jsg.shader.NormalMapping;
import trb.fps.jsg.shader.SkyboxPass;
import trb.fps.model.BulletData;
import trb.fps.model.LevelData;
import trb.fps.model.PlayerData;
import trb.jsg.DepthBuffer;
import trb.jsg.RenderPass;
import trb.jsg.SceneGraph;
import trb.jsg.Shader;
import trb.jsg.Shape;
import trb.jsg.State.Material;
import trb.jsg.State.StencilFuncParams;
import trb.jsg.State.StencilOpParams;
import trb.jsg.Texture;
import trb.jsg.TreeNode;
import trb.jsg.Uniform;
import trb.jsg.VertexData;
import trb.jsg.View;
import trb.jsg.enums.BlendDstFunc;
import trb.jsg.enums.BlendSrcFunc;
import trb.jsg.enums.SortOrder;
import trb.jsg.enums.StencilAction;
import trb.jsg.enums.StencilFunc;
import trb.jsg.renderer.Renderer;
import trb.jsg.util.Mat4;
import trb.jsg.util.SGUtil;
import trb.jsg.util.Vec3;

public class JsgDeferredRenderer implements FpsRenderer {

    public boolean useTopView = false;
    private View view = new View();
    private Renderer renderer;
    private long startTimeMillis = System.currentTimeMillis();
    private RenderPass basePass;
    private TreeNode replaceableNode = new TreeNode();
    private TreeNode[] playerModels;
    private TreeNode[] bulletModels;
    private JsgHud hud;
    private Level level;
    private final Shader shader = new Shader(BasePass.baseProgram);
    private LightManager lightManager;
    private LevelGenerator levelGenerator;
	private SkyboxPass skyboxPass;

    private final float far = 200f;

    public JsgDeferredRenderer(LevelGenerator levelGenerator) {
        this.levelGenerator = levelGenerator;
    }

    public void init(Level level) {
        this.level = level;

        int basew = Display.getDisplayMode().getWidth();
        int baseh = Display.getDisplayMode().getHeight();
        float fovy = (float) Math.toRadians(60);
        float aspect = basew / (float) baseh;
        view.perspective(fovy, aspect, 0.1f, far);

        basePass = BasePass.createBasePass(view, basew, baseh);

        // create the shader
        shader.putUniform(new Uniform("farClipDistance", Uniform.Type.FLOAT, far));
        NormalMapping.shader.putUniform(new Uniform("farClipDistance", Uniform.Type.FLOAT, far));

        // add shape to the renderpass tree
        for (Shape shape : levelGenerator.get()) {
            NormalMapping.apply(shape);
            //shape.getState().setShader(shader);
            replaceableNode.addShape(shape);
            level.physicsLevel.addAsConvexHull(shape, false);
        }
        basePass.getRootNode().addChild(replaceableNode);

        playerModels = createModels(LevelData.MAX_PLAYERS, basePass.getRootNode(), JsgBox.createFromPosSize(new Vec3(0, 1, 0), new Vec3(0.5f, 2f, 0.08f)));

        // add renderpass to scene graph
        SceneGraph sceneGraph = new SceneGraph(basePass);

		Texture lightTexture = SGUtil.createTexture(GL30.GL_RGBA16F, basew, baseh);
		Texture mixedTexture = SGUtil.createTexture(GL11.GL_RGB, basew, baseh);

        Texture baseTexture = basePass.getRenderTarget().getColorAttachments()[0];
        Texture rgbaTexture = basePass.getRenderTarget().getColorAttachments()[1];
        DepthBuffer baseDepth = basePass.getRenderTarget().getDepthBuffer();
        lightManager = new LightManager(
                baseTexture, baseDepth, lightTexture, view, new Point2f(basew, baseh));
        lightManager.createPointLight(new Vec3(1, 0, 1), new Vec3(14, 5, 0), 10);
        lightManager.createPointLight(new Vec3(0, 1, 0), new Vec3(-14, 5, 0), 10);
        Random rand = new Random(988231);
        for (float y = -100; y < 100; y += 20) {
            for (float x = -100; x < 100; x += 20) {
                Vec3 pos = new Vec3(x, 3, y);
                Vec3 color = new Vec3(rand.nextFloat(), rand.nextFloat(), rand.nextFloat())
                        .scale_(0.8f).add(0.2, 0.2, 0.2);
                lightManager.createPointLight(color, pos, 15);
            }
        }
        lightManager.createHemisphereLight(new Vec3(0.35, 0.3, 0.4), new Vec3(), new Vec3(-1, 0.25f, 0));

		FinalPass.createFinalPass(lightTexture, rgbaTexture, mixedTexture, baseDepth, basew, baseh, view);
		skyboxPass = new SkyboxPass(view, mixedTexture, baseDepth);

        sceneGraph.addRenderPass(lightManager.renderPass);
        sceneGraph.addRenderPass(FinalPass.mixPass);
		sceneGraph.addRenderPass(FinalPass.transparentPass);
		sceneGraph.addRenderPass(skyboxPass.renderPass);
		sceneGraph.addRenderPass(FinalPass.toScreenPass);

        hud = new JsgHud();
        sceneGraph.addRenderPass(hud.renderPass);

		bulletModels = createModels(LevelData.MAX_BULLETS, FinalPass.transparentPass.getRootNode(), JsgBox.createFromPosSize(new Vec3(0, 0, 0), new Vec3(0.5f, 0.5f, 0.5f)));
		for (TreeNode bulletModel : bulletModels) {
			for (Shape bulletShape : bulletModel.getAllShapesInTree()) {
				bulletShape.setSortOrder(SortOrder.BACK_TO_FRONT);
				bulletShape.getState().setMaterial(null);
				bulletShape.getState().setShader(null);
				bulletShape.getState().setBlendEnabled(false);
				bulletShape.getState().setBlendSrcFunc(BlendSrcFunc.ONE);
				bulletShape.getState().setBlendDstFunc(BlendDstFunc.ONE);
				bulletShape.getState().setDepthWriteEnabled(false);

				System.out.println("AAAAAAAAAA change bullet");
			}
		}


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
            shape.getState().setStencilTestEnabled(true);
            shape.getState().setStencilFunc(new StencilFuncParams(StencilFunc.ALWAYS, 1, 1));
            shape.getState().setStencilOp(new StencilOpParams(StencilAction.REPLACE, StencilAction.REPLACE, StencilAction.REPLACE));
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
        if (levelGenerator.shapesChanged) {
            basePass.getRootNode().removeChild(replaceableNode);
            replaceableNode = new TreeNode();

            for (Shape shape : levelGenerator.get()) {
                shape.getState().setShader(shader);
                replaceableNode.addShape(shape);
            }
            basePass.getRootNode().addChild(replaceableNode);
            basePass.getRootNode().updateTree(true);
        }

        LevelData level = l.levelData;

        float timeSec = (System.currentTimeMillis() - startTimeMillis) / 1000f;

        if (localPlayerIdx >= 0 && !useTopView) {
            //PlayerData player = level.players[localPlayerIdx];
            PlayerData player = l.predictedState.getCurrentState();
            view.setCameraMatrix(player.getViewTransform());
        } else {
            float angle = level.serverTimeMillis / 3000f;
            view.setCameraMatrix(new Mat4().lookAt(new Vec3(Math.cos(angle) * 50, 15, Math.sin(angle) * 50), new Vec3(0, 2, 0), new Vec3(0, 1, 0)));
        }

        renderPlayers(l, localPlayerIdx);
        renderBullets(l);

        hud.render(level, localPlayerIdx);

        lightManager.update(view);

		skyboxPass.update();

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
                    playerModels[i].setTransform(new Mat4().setTranslation_(new Vec3(pos).add_(new Vec3(0, 1, 0))).setEuler(new Vec3(0, 0, Math.PI / 2)));
                }
            } else {
                // some bug workaround
                playerModels[i].setTransform(new Mat4());
            }
        }
    }

    private void renderBullets(Level level) {
        LevelData levelData = level.levelData;
        for (int i = 0; i < levelData.bullets.length; i++) {
            BulletData bullet = levelData.bullets[i];
            for (Shape shape : bulletModels[i].getAllShapesInTree()) {
                shape.setVisible(bullet.alive);
            }
            if (bullet.alive) {
                long time = level.interpolatedServerState.getCurrentState().serverTime;
                Vec3 bulletPos = FpsServer.getPositionAtTime(bullet, time);
                bulletModels[i].setTransform(new Mat4().setTranslation_(bulletPos));
            }
        }
    }
}
