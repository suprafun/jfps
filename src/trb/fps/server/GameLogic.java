package trb.fps.server;

import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;
import com.bulletphysics.dynamics.RigidBody;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import trb.fps.entity.EntityList;
import trb.fps.net.LevelPacket;
import trb.fps.net.PlayerPacket;
import trb.fps.BulletStats;
import trb.fps.CollisionInfo;
import trb.fps.Input;
import trb.fps.util.LineDistance;
import trb.fps.PlayerUpdator;
import trb.fps.entity.SpawnPoint;
import trb.fps.entity.Transform;
import trb.fps.net.BulletPacket;
import trb.fps.physics.PhysicsLevel;
import trb.jsg.util.Mat4;
import trb.jsg.util.Vec3;

public class GameLogic {

    private static final float PLAYER_RADIUS = 3f;
    private static final long BULLET_TIMEOUT_MILLIS = 5000;
    public static final BulletStats[] bulletStats = {
        new BulletStats(10, 50f)
    };

    private static final Vec3 BULLET_SPAWN_OFFSET = new Vec3(0, 1.4f, 0);
    private final long startTimeMillis = currentTimeMillis();
    private long prevTime = 0;
    private long time = 0;

    EntityList entityList = new EntityList();
    PhysicsLevel physicsLevel = new PhysicsLevel(entityList);
	private PowerupManager powerupManager = new PowerupManager(entityList);
    public final LevelPacket level = new LevelPacket();
    private final Map<Integer, Player> players = new HashMap();
    
    public GameLogic() {
    }
    
    public void update() {
        updateTime();
        if (!level.isGameOver()) {
            for (PlayerPacket playerPacket : level.players) {
                Player player = getPlayer(playerPacket);
                if (player != null) {
                    player.update();
                }
            }
            updateBullets();
        }
    }
    
    private void updateTime() {
        prevTime = time;
        time = currentTimeMillis() - startTimeMillis;
        level.serverTimeMillis = time;
    }

    private Player getPlayer(PlayerPacket packet) {
        if (packet.isConnected()) {
            return players.get(packet.getId());
        }
        return null;
    }

    private long currentTimeMillis() {
        return System.nanoTime() / 1000000l;
    }

    public void updateBullets() {
        int liveBullets = 0;
        for (BulletPacket bullet : level.bullets) {
            if (bullet.alive) {
                liveBullets++;
                if ((time - bullet.spawnTime) > BULLET_TIMEOUT_MILLIS) {
                    bullet.alive = false;
                } else {
                    CollisionInfo collision = collideBullet(bullet);
                    if (collision.type == CollisionInfo.Type.Player) {
                        PlayerPacket player = level.getPlayer(collision.playerId);
                        player = player.takeDamage(bulletStats[bullet.bulletType].damage);
                        if (player.getHealth() <= 0) {
                            player = player.incDeaths();
                            level.setPlayer(bullet.shooterPlayerId, level.getPlayer(bullet.shooterPlayerId).incKills());
                        }
                        bullet.alive = false;
                        level.setPlayer(collision.playerId, player);
                        System.out.println("bullet from " + bullet.shooterPlayerId
                                + " collided with " + collision.playerId);
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
    private CollisionInfo collideBullet(BulletPacket bullet) {
        Vec3 p1 = getPositionAtTime(bullet, Math.max(bullet.spawnTime, prevTime));
        Vec3 p2 = getPositionAtTime(bullet, time);
        return collideBullet(bullet, p1, p2);
    }

    private CollisionInfo collideBullet(BulletPacket bullet, Vec3 p1, Vec3 p2) {
        float shortestDistance = Float.MAX_VALUE;
        CollisionInfo collisionInfo = null;
        for (PlayerPacket player : level.players) {
            if (player.isConnected() && player.getHealth() > 0 && player.getId() != bullet.shooterPlayerId) {
                LineDistance distanceToPlayer = new LineDistance(player.getPosition(), p1, p2);
                //System.out.println("bullet distance to " + player.index + " = " + distanceToPlayer.distance);
                if (distanceToPlayer.distance < PLAYER_RADIUS) {
                    float dist = distanceToPlayer.d.distance(p1);
                    if (dist < shortestDistance) {
                        shortestDistance = dist;
                        collisionInfo = new CollisionInfo(CollisionInfo.Type.Player, distanceToPlayer.d, player.getId());
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
        physicsLevel.rayTest(p1, p2, rayCallback);
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
                    collisionInfo = new CollisionInfo(CollisionInfo.Type.World, new Vec3(rayCallback.hitPointWorld), -1);
                }
            }
        }

        if (collisionInfo == null) {
            collisionInfo = new CollisionInfo();
        }
        return collisionInfo;
    }

    public static Vec3 getPositionAtTime(BulletPacket bullet, long time) {
        float timeDeltaSec = (time - bullet.spawnTime) / 1000f;
        float t = bulletStats[bullet.bulletType].speed * timeDeltaSec;
        return new Vec3().scaleAdd_(t, new Vec3(bullet.startDirection), new Vec3(bullet.startPosition));

    }

    /**
     * @param fireServerTime server time as seen by shooter
     */
    public void fireBullet(PlayerPacket player, long fireServerTime) {
        System.out.println("fireBullet " + player.getId());
        for (BulletPacket bullet : level.bullets) {
            if (!bullet.alive) {
                bullet.alive = true;
                bullet.shooterPlayerId = player.getId();
                bullet.setStartPosition(player.getPosition().add_(BULLET_SPAWN_OFFSET));
                bullet.setStartDirection(player.getTransform().transformAsVector(new Vec3(0, 0, -1)));
                System.out.println("fireBullet time diff: " + (fireServerTime - time));
                bullet.spawnTime = fireServerTime;
                return;
            }
        }

        System.err.println("No more bullets available");
    }

    public boolean addPlayer(int id, String name) {
        System.out.println("server addPlayer "+name);
        for (int i=0; i<level.players.length; i++) {
            PlayerPacket playerPacket = level.players[i];
            if (!playerPacket.isConnected()) {
                level.players[i] = new PlayerPacket(id, i, name).setConnected(true);
                players.put(id, new Player(id, i));
                return true;
            }
        }
        return false;
    }

    public void removePlayer(int id) {
        level.setPlayer(id, level.getPlayer(id).setConnected(false));
        players.remove(id);
    }

    public void respawn(int id) {
        PlayerPacket playerPacket = level.getPlayer(id);
        if (playerPacket.getHealth() <= 0) {
            playerPacket.respawn(getRandomSpawnPoint());
        }
    }

    private Mat4 getRandomSpawnPoint() {
        Mat4 mat = new Mat4();
        List<SpawnPoint> spawnPoints = entityList.getComponents(SpawnPoint.class);
        if (spawnPoints.size() > 0) {
            int idx = new Random().nextInt(spawnPoints.size());
            return spawnPoints.get(idx).getComponent(Transform.class).get();
        }
        return mat;
    }

    public void addInput(int id, Input input) {
        players.get(id).inputList.add(input);
    }

    public void changeLevel(EntityList entityList) {
        // update physics
        // kill players and reset deaths and kills
        System.out.println("changeLevel "+entityList.getAll().size());
        this.entityList = entityList;
        physicsLevel = new PhysicsLevel(entityList);

        for (int i=0; i<level.players.length; i++) {
            level.players[i] = level.players[i].setHealth(0).setKills(0).setDeaths(0);
        }

		powerupManager = new PowerupManager(entityList);
		level.powerupsPickupTime = new long[powerupManager.size()];
    }

    public class Player {

        int id;
        int slotIdx;
        Input prevInput = null;
        List<Input> inputList = new ArrayList();

        public Player(int id, int slotIdx) {
            this.id = id;
            this.slotIdx = slotIdx;
        }

        private void update() {
            PlayerPacket player = level.getPlayer(id);

			player = powerupManager.pickup(player, level.powerupsPickupTime, time);
			level.setPlayer(id, player);

            if (player.getPosition().y < -100 && player.getHealth() > 0) {
                player = player.setHealth(0).setDeaths(player.getDeaths()+1);
                level.setPlayer(id, player);
            }

            if (player.getHealth() > 0) {
                for (Input input : inputList) {
                    player = new PlayerUpdator(input, physicsLevel).update(player);
                }
                level.setPlayer(id, player);

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
    }
}
