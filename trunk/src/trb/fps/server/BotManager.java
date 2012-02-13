package trb.fps.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.critterai.math.Vector3;
import org.critterai.nav.DistanceHeuristicType;
import org.critterai.nav.MasterNavRequest;
import org.critterai.nav.MasterNavigator;
import org.critterai.nav.MasterPath.Path;
import org.critterai.nav.NavUtil;
import org.critterai.nmgen.TriangleMesh;
import trb.fps.Input;
import trb.fps.ai.NavmeshParameters;
import trb.fps.editor.NavigationMeshEditorUser;
import trb.fps.entity.Box;
import trb.fps.entity.DeferredSystem;
import trb.fps.entity.Powerup;
import trb.fps.entity.Transform;
import trb.fps.net.ChangeLevelPacket;
import trb.fps.net.PlayerPacket;
import trb.jsg.TreeNode;
import trb.jsg.util.Vec3;

/**
 * Manages a list of bots.
 */
public class BotManager {

    public final NavmeshParameters parameters = new NavmeshParameters();
	private TriangleMesh triangleMesh = null;
	private MasterNavigator navigator;
	private final GameLogic gameLogic;
	private final List<String> botsToAdd = Collections.synchronizedList(new ArrayList());
	private int nextId = -10;
	private List<Bot> bots = new ArrayList();

	public BotManager(GameLogic gameLogic) {
		this.gameLogic = gameLogic;
	}

	public void addBot(String botName) {
		botsToAdd.add(botName);
	}

	public void levelChanged(ChangeLevelPacket changeLevelPacket) {
		Map<Box, TreeNode> boxNodeMap = new HashMap();
		DeferredSystem.createGeometry(gameLogic.entityList, boxNodeMap, null);
		NavigationMeshEditorUser creator = new NavigationMeshEditorUser(boxNodeMap);
		triangleMesh = creator.create(parameters.create());
		navigator = NavUtil.getNavigator(triangleMesh.vertices, triangleMesh.indices,
				5, 0.5f, 0.05f, DistanceHeuristicType.MANHATTAN, 1000000, 60000, 2, 20);
	}

	public void addInputToGameLogic() {
		synchronized (botsToAdd) {
			for (String name : botsToAdd) {
				Bot bot = new Bot(nextId--, name);
                if (gameLogic.addPlayer(bot.id, bot.name)) {					
					bots.add(bot);
				} else {
					System.err.println("Failed to add bot " + name);
				}
			}
			botsToAdd.clear();
		}

		for (Bot bot : bots) {
			bot.update();
		}
	}

	public class Bot {

		int id;
		String name;
		Path path = null;
		Vec3 target = new Vec3(8, -7, 0);

		Bot(int id, String name) {
			this.id = id;
			this.name = name;
		}

		void update() {
			PlayerPacket player = gameLogic.level.getPlayer(id);
			if (player == null) {
				Thread.dumpStack();
				return;
			}

			long now = System.currentTimeMillis();
			if (player.getHealth() <= 0) {
				gameLogic.respawn(id);
			} else {
				if (path == null && navigator != null) {
					List<Powerup> powerups = gameLogic.entityList.getComponents(Powerup.class);
					if (powerups.size() > 0) {
						Powerup powerup = powerups.get((int) (Math.random() * powerups.size()));
						target = powerup.getComponent(Transform.class).get().getTranslation();
					} 
					path = findPath(player.getBottomPosition(), target);
				}

				if (path != null) {
					Vec3 startPos = player.getBottomPosition();
					Vec3 pos = new Vec3(startPos);
					int maxIter = 10;
					while (!pos.epsilonEquals(target, 0.5f) && pos.epsilonEquals(startPos, 0.5f) && maxIter-- > 0) {
						//System.out.println("   "+pos);
						Vector3 nextPos = new Vector3();
						if (!path.getTarget(pos.x, pos.y, pos.z, nextPos)) {
							MasterNavRequest<Path>.NavRequest pathRequest = navigator.navigator().getPath(
									startPos.x, startPos.y, startPos.z, target.x, target.y, target.z);
							navigator.processAll(true);
							path = pathRequest.data();
							System.out.println("getTarget failed "+path);
							break;
						}
						pos.set(nextPos.x, nextPos.y, nextPos.z);
					}
					Vec3 dir = new Vec3(pos).sub_(startPos);
					float heading = (float) Math.atan2(-dir.x, -dir.z);
					//System.out.println(startPos + " " + pos + " " + dir + " " + heading);

					gameLogic.addInput(id, new Input(now, now, 0, 1, heading, 0, false, false));

					if (startPos.epsilonEquals(target, 1f)) {
						System.out.println("Reached target " + target);
						path = null;
					}
				} else {
					gameLogic.addInput(id, new Input(now, now, 0, 0, 0, 0, false, false));
				}
			}
		}

		Path findPath(Vec3 start, Vec3 goal) {
			System.out.println("find path " + start + " " + goal);
			long startTime = System.nanoTime();
			MasterNavRequest<Path>.NavRequest pathRequest = navigator.navigator().getPath(
					start.x, start.y, start.z, goal.x, goal.y, goal.z);

			MasterNavRequest<Vector3>.NavRequest nearestStart = navigator.navigator().getNearestValidLocation(start.x, start.y, start.z);
			MasterNavRequest<Vector3>.NavRequest nearestGoal = navigator.navigator().getNearestValidLocation(goal.x, goal.y, goal.z);
			navigator.processAll(true);
			//System.out.println(pathRequest.data());
			//System.out.println("" + nearest.data());

			long endTime = System.nanoTime();
			double deltaTime = (endTime-startTime) / 1E9;
			//System.out.println("findPath "+ deltaTime);

			if (pathRequest.data() == null) {
				System.err.println("  failed " + nearestStart.data());
				Vector3 newStart = nearestStart.data();
				Vector3 newGoal = nearestGoal.data();
				pathRequest = navigator.navigator().getPath(
						newStart.x, newStart.y, newStart.z, newGoal.x, newGoal.y, newGoal.z);
				navigator.processAll(true);
				if (pathRequest.data() == null) {
					System.err.println("    failed again. Giving up");
				}
			}

			return pathRequest.data();
//			if (path == null) {
//				System.out.println("findPath no data");
//				return;
//			}
//			int[] pathIndices = new int[path.pathPolyCount() * 3];
//			float[] pathVerts = new float[path.pathVertCount() * 3];
//			path.getPathPolys(pathVerts, pathIndices);
//			//treeNode.addShape(createMeshGeometry(pathVerts, pathIndices, true));
//			System.out.println("index count "+pathIndices.length);
//
//			finalPath = "found";
//
//			List<Point3f> pathLineCoords = new ArrayList();
//			Vector3 pos = new Vector3(start.x, start.y, start.z);
//			Vector3 nextPos = new Vector3();
//			int maxIter = 100;
//			while (!pos.sloppyEquals(goal.x, goal.y, goal.z, 0.1f)) {
//				pathLineCoords.add(new Point3f(pos.x, pos.y+0.1f, pos.z));
//				path.getTarget(pos.x, pos.y, pos.z, nextPos);
//				if (pos.sloppyEquals(nextPos, 0.1f)) {
//					break;
//				}
//				pos.set(nextPos);
//				if (maxIter-- < 0) {
//					break;
//				}
//			}
//			pathLineCoords.add(new Point3f(pos.x, pos.y, pos.z));
//			if (pathLineCoords.size() > 1) {
//				int[] indices = new int[(pathLineCoords.size()-1)*2];
//				for (int i=0; i<pathLineCoords.size()-1; i++) {
//					indices[i*2] = i;
//					indices[i*2+1] = i+1;
//				}
//				float[] coords = SGUtil.toFloats(pathLineCoords);
//				float[] colors = new float[coords.length];
//				for (int i=0; i<colors.length; i+=3) {
//					colors[i] = 0f;
//					colors[i+1] = 1f;
//					colors[i+2] = 0f;
//				}
//
//				VertexData vertexData = new VertexData(coords, null, colors, 2, null, indices);
//				vertexData.mode = VertexData.Mode.LINES;
//				Shape shape = new Shape(vertexData);
//				shape.getState().setStencilTestEnabled(true);
//				shape.getState().setStencilFunc(new StencilFuncParams(StencilFunc.ALWAYS, 1, 1));
//				shape.getState().setStencilOp(new StencilOpParams(StencilAction.KEEP, StencilAction.KEEP, StencilAction.REPLACE));
//				shape.getState().setLineWidth(5);
//				treeNode.addShape(shape);
//			}
		}
	}
}
