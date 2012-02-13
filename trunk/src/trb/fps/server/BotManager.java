package trb.fps.server;

import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;
import com.bulletphysics.dynamics.RigidBody;
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


			long now = gameLogic.time;
			if (player.getHealth() <= 0) {
				gameLogic.respawn(id);
			} else {
				PlayerPacket visiblePlayer = getClosestVisiblePlayer();
				if (visiblePlayer != null && player.ammo > 0) {
					Vec3 dir = new Vec3(visiblePlayer.getPosition()).sub_(player.getPosition());
					float headingRad = getHeadingRad(dir);
					float tiltRad = (float) Math.asin(dir.y / dir.length());
					gameLogic.addInput(id, new Input(now, now, 0, 0, headingRad, tiltRad, true, false));
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
								System.out.println("getTarget failed " + path);
								break;
							}
							pos.set(nextPos.x, nextPos.y, nextPos.z);
						}
						Vec3 dir = new Vec3(pos).sub_(startPos);
						float heading = getHeadingRad(dir);
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
		}

		private float getHeadingRad(Vec3 dir) {
			return (float) Math.atan2(-dir.x, -dir.z);
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
		}

		public PlayerPacket getClosestVisiblePlayer() {
			PlayerPacket thisPlayer = gameLogic.level.getPlayer(id);
			Vec3 thisPos = thisPlayer.getPosition();
			PlayerPacket closestPlayer = null;
			float closestDist = Float.MAX_VALUE;
			for (PlayerPacket other : gameLogic.level.players) {
				if (other.getId() != thisPlayer.getId() && other.isConnected() && other.getHealth() > 0) {
					Vec3 otherPosition = other.getPosition();
					ClosestRayResultCallback result = new ClosestRayResultCallback(thisPos, otherPosition);
					gameLogic.physicsLevel.rayTest(thisPos, otherPosition, result);
					if (!result.hasHit() || RigidBody.upcast(result.collisionObject) == null) {
						float dist = thisPos.distance(otherPosition);
						if (dist < closestDist) {
							closestPlayer = other;
							closestDist = dist;
						}
					}
				}
			}

			return closestPlayer;
		}
	}
}
