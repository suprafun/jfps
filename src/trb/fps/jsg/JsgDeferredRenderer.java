package trb.fps.jsg;
import javax.vecmath.Point2f;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import trb.fps.client.FpsRenderer;
import trb.fps.client.Level;
import trb.fps.entity.DeferredSystem;
import trb.fps.jsg.shader.BasePass;
import trb.fps.jsg.shader.FinalPass;
import trb.fps.jsg.shader.LightManager;
import trb.fps.jsg.shader.NormalMapping;
import trb.fps.jsg.shader.SkyboxPass;
import trb.fps.net.BulletPacket;
import trb.fps.net.LevelPacket;
import trb.fps.net.PlayerPacket;
import trb.fps.server.GameLogic;
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
    public RenderPass basePass;
    private TreeNode[] playerModels;
    private TreeNode[] bulletModels;
    private JsgHud hud;
    private Level level;
    private final Shader shader = new Shader(BasePass.baseProgram);
    public LightManager lightManager;
	private SkyboxPass skyboxPass;
    private final float far = 200f;
    public DeferredSystem deferredSystem = new DeferredSystem(this);

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

        playerModels = createModels(LevelPacket.MAX_PLAYERS, basePass.getRootNode(), JsgBox.createFromPosSize(new Vec3(0, 1, 0), new Vec3(0.5f, 2f, 0.08f)));

        // add renderpass to scene graph
        SceneGraph sceneGraph = new SceneGraph(basePass);

		Texture lightTexture = SGUtil.createTexture(GL30.GL_RGBA16F, basew, baseh);
		Texture mixedTexture = SGUtil.createTexture(GL11.GL_RGB, basew, baseh);

        Texture baseTexture = basePass.getRenderTarget().getColorAttachments()[0];
        Texture rgbaTexture = basePass.getRenderTarget().getColorAttachments()[1];
        DepthBuffer baseDepth = basePass.getRenderTarget().getDepthBuffer();
        lightManager = new LightManager(
                baseTexture, baseDepth, lightTexture, view, new Point2f(basew, baseh));

		FinalPass.createFinalPass(lightTexture, rgbaTexture, mixedTexture, baseDepth, basew, baseh, view);
		skyboxPass = new SkyboxPass(view, mixedTexture, baseDepth);

        sceneGraph.addRenderPass(lightManager.renderPass);
        sceneGraph.addRenderPass(FinalPass.mixPass);
		sceneGraph.addRenderPass(FinalPass.transparentPass);
		sceneGraph.addRenderPass(skyboxPass.renderPass);
		sceneGraph.addRenderPass(FinalPass.toScreenPass);

        hud = new JsgHud();
        sceneGraph.addRenderPass(hud.renderPass);

		bulletModels = createModels(LevelPacket.MAX_BULLETS, FinalPass.transparentPass.getRootNode(), JsgBox.createFromPosSize(new Vec3(0, 0, 0), new Vec3(0.5f, 0.5f, 0.5f)));
		for (TreeNode bulletModel : bulletModels) {
			for (Shape bulletShape : bulletModel.getAllShapesInTree()) {
				bulletShape.setSortOrder(SortOrder.BACK_TO_FRONT);
				bulletShape.getState().setMaterial(null);
				bulletShape.getState().setShader(null);
				bulletShape.getState().setBlendEnabled(false);
				bulletShape.getState().setBlendSrcFunc(BlendSrcFunc.ONE);
				bulletShape.getState().setBlendDstFunc(BlendDstFunc.ONE);
				bulletShape.getState().setDepthWriteEnabled(false);
				//System.out.println("AAAAAAAAAA change bullet");
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
        deferredSystem.update();

        LevelPacket level = l.levelData;

        float timeSec = (System.currentTimeMillis() - startTimeMillis) / 1000f;

        if (l.editorNavigation.enabled.get()) {
            view.setCameraMatrix(l.editorNavigation.viewTransform);
        } else if(localPlayerIdx >= 0 && !useTopView) {
            //PlayerData player = level.players[localPlayerIdx];
            PlayerPacket player = l.predictedState.getCurrentState();
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
            PlayerPacket player = level.interpolatedState.get(i).getCurrentState();
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
        LevelPacket levelData = level.levelData;
        for (int i = 0; i < levelData.bullets.length; i++) {
            BulletPacket bullet = levelData.bullets[i];
            for (Shape shape : bulletModels[i].getAllShapesInTree()) {
                shape.setVisible(bullet.alive);
            }
            if (bullet.alive) {
                long time = level.interpolatedServerState.getCurrentState().serverTime;
                Vec3 bulletPos = GameLogic.getPositionAtTime(bullet, time);
                bulletModels[i].setTransform(new Mat4().setTranslation_(bulletPos));
            }
        }
    }
}
