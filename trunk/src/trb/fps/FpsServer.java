package trb.fps;

import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;
import com.bulletphysics.dynamics.RigidBody;
import trb.fps.model.LevelData;
import trb.fps.model.PlayerData;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import trb.fps.model.BulletData;
import trb.fps.physics.PhysicsLevel;
import trb.fps.physics.KinematicCharacter;
import trb.jsg.util.Vec3;

public class FpsServer {

    private ServerRunnable serverRunnable = new ServerRunnable();
    private Thread thread;
	private Server server = new Server();
	private boolean running = true;

	private LevelData level = new LevelData();
    private ConnectionInfo[] playerConnections = new ConnectionInfo[LevelData.MAX_PLAYERS];

    private PhysicsLevel physicsLevel = new PhysicsLevel();
    private KinematicCharacter character = physicsLevel.addCharacter();

    private final long startTimeMillis = currentTimeMillis();
    private long prevTime = 0;
    private long time = 0;

    private static final float PLAYER_RADIUS = 3f;
    private static final long BULLET_TIMEOUT_MILLIS = 5000;
    public static final BulletStats[] bulletStats = {
        new BulletStats(10, 50f)
    };
    private static final Vec3 BULLET_SPAWN_OFFSET = new Vec3(0, 1.4f, 0);

    private long currentTimeMillis() {
        return System.nanoTime() / 1000000l;
    }

	public void start() {
		try {
			Main.initKryo(server.getKryo());
            thread = new Thread(serverRunnable);
            thread.start();
			server.bind(54555, 54777);
			server.addListener(serverRunnable);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private int getPlayerIndex(Connection connection) {
		for (int i = 0; i < playerConnections.length; i++) {
			if (playerConnections[i] != null && playerConnections[i].connection == connection) {
				return i;
			}
		}

		return -3;
	}

    private ConnectionInfo getConnectionInfo(Connection connection) {
        int playerIdx = getPlayerIndex(connection);
        return (playerIdx >= 0) ? playerConnections[playerIdx] : null;
    }

	public boolean isRunning() {
		return running;
	}

	void stop() {
		running = false;
		server.stop();
	}

	private void updateLevel() {
        prevTime = time;
        time = currentTimeMillis() - startTimeMillis;
        level.serverTimeMillis = time;

        for (int i=0; i<playerConnections.length; i++) {
            ConnectionInfo info = playerConnections[i];
            if (info != null) {
                info.updatePlayer();
            }
        }
        updateBullets();
	}

    private void updateBullets() {
        int liveBullets = 0;
        for (BulletData bullet : level.bullets) {
            if (bullet.alive) {
                liveBullets++;
                if ((time - bullet.spawnTime) > BULLET_TIMEOUT_MILLIS) {
                    bullet.alive = false;
                } else {
                    CollisionInfo collision = collideBullet(bullet);
                    if (collision.type == CollisionInfo.Type.Player) {
                        PlayerData player = level.players[collision.playerIdx];
                        player = player.takeDamage(bulletStats[bullet.bulletType].damage);
                        if (player.getHealth() <= 0) {
                            player = player.incDeaths();
                            level.players[bullet.shooterPlayerIdx] = level.players[bullet.shooterPlayerIdx].incKills();
                        }
                        bullet.alive = false;
                        level.players[collision.playerIdx] = player;
                        System.out.println("bullet from " + bullet.shooterPlayerIdx
                                + " collided with " + collision.playerIdx);
                    } else if (collision.type == CollisionInfo.Type.World) {
                        bullet.alive = false;
                    }
                    // TODO: send bullet hit something event to clients for visualisation
                }
            }
        }
    }

    /**
     * Returns bullets collision with world or players the during the last
     * update.
     */
    private CollisionInfo collideBullet(BulletData bullet) {
        Vec3 p1 = getPositionAtTime(bullet, Math.max(bullet.spawnTime, prevTime));
        Vec3 p2 = getPositionAtTime(bullet, time);
        return collideBullet(bullet, p1, p2);
    }

    private CollisionInfo collideBullet(BulletData bullet, Vec3 p1, Vec3 p2) {
        float shortestDistance = Float.MAX_VALUE;
        CollisionInfo collisionInfo = null;
        for (PlayerData player : level.players) {
            if (player.isConnected() && player.getHealth() > 0 && player.getIndex() != bullet.shooterPlayerIdx) {
                LineDistance distanceToPlayer = new LineDistance(player.getPosition(), p1, p2);
                //System.out.println("bullet distance to " + player.index + " = " + distanceToPlayer.distance);
                if (distanceToPlayer.distance < PLAYER_RADIUS) {
                    float dist = distanceToPlayer.d.distance(p1);
                    if (dist < shortestDistance) {
                        shortestDistance = dist;
                        collisionInfo = new CollisionInfo(CollisionInfo.Type.Player, distanceToPlayer.d, player.getIndex());
                    }
                }
            }
        }

        // this code works but we want to use a bigger object than what is used for collision detection
//        Transform rayFromTrans = new Transform();
//        Transform rayToTrans = new Transform();
//        rayFromTrans.setIdentity();
//        rayFromTrans.origin.set(p1);
//        rayToTrans.setIdentity();
//        rayToTrans.origin.set(p2);
//        for (PlayerData player : level.players) {
//            if (player.connected && player.health > 0 && player.index != bullet.shooterPlayerIdx) {
//                Character c = characters[player.index];
//                ClosestRayResultCallback rayCallback = new ClosestRayResultCallback(p1, p2);
//                Transform worldTransform = new Transform();
//                c.ghostObject.getWorldTransform(worldTransform);
//                CollisionWorld.rayTestSingle(rayFromTrans, rayToTrans
//                        , c.ghostObject, c.ghostObject.getCollisionShape()
//                        , worldTransform
//                        , rayCallback);
//                if (rayCallback.hasHit()) {
//                    float distance = p1.distance(rayCallback.hitPointWorld);
//                    if (distance < shortestDistance) {
//                        System.out.println("Hit player!! " + rayCallback.collisionObject.getCollisionShape());
//                        shortestDistance = distance;
//                        collisionInfo = new CollisionInfo(CollisionInfo.Type.Player
//                                , new Vec3(rayCallback.hitPointWorld), player.index);
//                    }
//                }
//            }
//        }

        ClosestRayResultCallback rayCallback = new ClosestRayResultCallback(p1, p2);
        physicsLevel.dynamicsWorld.rayTest(p1, p2, rayCallback);
        if (rayCallback.hasHit()) {
            RigidBody body = RigidBody.upcast(rayCallback.collisionObject);
            if (body != null) {
                float distance = p1.distance(rayCallback.hitPointWorld);
                if (distance < shortestDistance) {
                    System.out.println("Hit world!! "
							+ p1 + " "
							+ p2 + " "
							+ rayCallback.hitPointWorld + " "
							+ ((RigidBody) rayCallback.collisionObject).getCollisionShape());
                    shortestDistance = distance;
                    collisionInfo = new CollisionInfo(CollisionInfo.Type.World
                            , new Vec3(rayCallback.hitPointWorld), -1);
                }
            }
        }

        if (collisionInfo == null) {
            collisionInfo = new CollisionInfo();
        }
        return collisionInfo;
    }

    public static Vec3 getPositionAtTime(BulletData bullet, long time) {
        float timeDeltaSec = (time - bullet.spawnTime) / 1000f;
        float t = bulletStats[bullet.bulletType].speed * timeDeltaSec;
        return new Vec3().scaleAdd_(t, new Vec3(bullet.startDirection), new Vec3(bullet.startPosition));
        
    }

    /**
     * @param fireServerTime server time as seen by shooter
     */
    private void fireBullet(PlayerData player, long fireServerTime) {
        System.out.println("fireBullet " + player.getIndex());
        for (BulletData bullet : level.bullets) {
            if (!bullet.alive) {
                bullet.alive = true;
                bullet.shooterPlayerIdx = player.getIndex();
                bullet.setStartPosition(player.getPosition().add_(BULLET_SPAWN_OFFSET));
                bullet.setStartDirection(player.getTransform().transformAsVector(new Vec3(0, 0, -1)));
                System.out.println("fireBullet time diff: " + (fireServerTime - time));
                bullet.spawnTime = fireServerTime;
                return;
            }
        }

        System.err.println("No more bullets available");
    }

	class ServerRunnable extends Listener implements Runnable {
		public void run() {
			try {
                while (isRunning()) {
                    server.update(0);
					updateLevel();
                    for (ConnectionInfo c : playerConnections) {
                        if (c != null) {
                            c.connection.sendTCP(level);
                        }
                    }
                    server.update(0);
					Thread.sleep(100);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
        
        @Override
        public void received(Connection connection, Object object) {
            //System.out.println("Server received: " + object + " from " + connection.getID());
            int playerIdx = getPlayerIndex(connection);
            PlayerData player = level.players[playerIdx];
            ConnectionInfo info = getConnectionInfo(connection);
            if (object instanceof Handshake) {
                Handshake handshake = (Handshake) object;
                if (playerIdx >= 0) {
                    player.init(handshake.name);
                }
                connection.sendTCP(new Handshake(handshake.name, playerIdx));
            } else if (object instanceof Input) {
                if (info != null) {
                    info.inputList.add((Input) object);
                } else {
                    System.err.println("Input from unconnected player");
                }
            } else if (object instanceof String) {
                System.out.println("got string from client: " + object);
                if ("respawn".equals(object)) {
                    if (player.getHealth() <= 0) {
                        player.respawn();
                    }
                }
            }
        }

        @Override
        public void connected(Connection connection) {
            System.out.println("server connected " + connection);
            for (int i = 0; i < playerConnections.length; i++) {
                if (playerConnections[i] == null) {
                    playerConnections[i] = new ConnectionInfo(connection, i);
                    break;
                }
            }
        }

        @Override
        public void disconnected(Connection connection) {
            System.out.println("server disconnected " + connection);
            int idx = getPlayerIndex(connection);
            if (idx >= 0) {
                playerConnections[idx].destroy();
            }
        }
	}

    class ConnectionInfo {
        Connection connection;
        int playerIdx;
        Input prevInput = null;
        List<Input> inputList = new ArrayList();

        public ConnectionInfo(Connection connection, int playerIdx) {
            this.connection = connection;
            this.playerIdx = playerIdx;
        }

        private void updatePlayer() {
            PlayerData player = level.players[playerIdx];
            if (player.getHealth() > 0) {
                for (Input input : inputList) {
                    player = new PlayerUpdator(input, character, physicsLevel).update(player);
                }
                level.players[playerIdx] = player;

                long fireServerTime = -1;
                boolean fire = false;
                for (Input input : inputList) {
                    fire |= input.fire;
                    if (input.fire) {
                        fireServerTime = input.serverTime;
                    }
                }
                if (fire) {
                    fireBullet(player, fireServerTime);
                }
            }
            inputList.clear();
        }

        private void destroy() {
            if (playerConnections[playerIdx] == this) {
                level.players[playerIdx] = level.players[playerIdx].setConnected(false);
                playerConnections[playerIdx] = null;
            } else {
                System.err.println("ERROR");
                Thread.dumpStack();
            }
        }
    }
}
