package trb.fps.physics;

import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import java.util.HashMap;
import java.util.Map;
import javax.vecmath.Vector3f;
import trb.fps.entity.Box;
import trb.fps.entity.DeferredSystem;
import trb.fps.entity.EntityList;
import trb.fps.entity.Powerup;
import trb.fps.entity.SpawnPoint;
import trb.fps.net.PlayerPacket;
import trb.jsg.Shape;
import trb.jsg.TreeNode;
import trb.jsg.util.Vec3;

/**
 *
 * @author tomrbryn
 */
public class PhysicsLevel {

    public static final Object globalLock = new Object();

    // contains all the physics objects and performs the simulation
    public final DynamicsWorld dynamicsWorld;
    BroadphaseInterface broadphase;
    private final KinematicCharacter character;

    /**
     * Creates dynamicsWorld and adds RigidBodies
     */
    public PhysicsLevel(EntityList entities) {
        dynamicsWorld = createDynamicsWorld();
        dynamicsWorld.setGravity(new Vector3f(0f, -10f, 0f));
        character = new KinematicCharacter(this);

        Map<TreeNode, Box> nodeBoxMap = new HashMap();
        TreeNode treeNode = DeferredSystem.createGeometry(entities, null, nodeBoxMap, null);
        treeNode.updateTree(true);
        for (TreeNode child : treeNode.getChildren()) {
            Box box = nodeBoxMap.get(child);
            if (box.getComponent(SpawnPoint.class) == null && box.getComponent(Powerup.class) == null) {
                addAsConvexHull(child, false);
            }
        }
    }

    private ConvexHull addAsConvexHull(TreeNode node, boolean isDynamic) {
        Shape shape = node.getShape(0);
        ConvexHull convexHull = new ConvexHull(shape, isDynamic);
        dynamicsWorld.addRigidBody(convexHull.getRigidBody());
        return convexHull;
    }

    public PlayerPacket move(PlayerPacket from, PlayerPacket to, boolean jump) {
        synchronized (globalLock) {
            character.setFromTo(from.getPosition(), to.getPosition());
            MyKinematicCharacterController c = character.character;
            c.verticalVelocity = from.verticalVelocity;
            c.wasJumping = from.wasJumping;
            c.wasOnGround = from.wasOnGround;
            if (jump) {
                character.character.jump();
            }
            long timeDeltaMillis = to.getTime() - from.getTime();
            nextFrame(timeDeltaMillis / 1000f);
            to.verticalVelocity = c.verticalVelocity;
            to.wasJumping = c.wasJumping;
            to.wasOnGround = c.wasOnGround;
            return to.setPosition(character.getTransform().getTranslation());
        }
    }

    /**
     * Creates a dynamics world
     */
    private DynamicsWorld createDynamicsWorld() {
        // collision configuration contains default setup for memory, collision setup
        DefaultCollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
        // calculates exact collision given a list possible colliding pairs
        CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);

        // the maximum size of the collision world. Make sure objects stay
        // within these boundaries. Don't make the world AABB size too large, it
        // will harm simulation quality and performance
        Vector3f worldAabbMin = new Vector3f(-1000, -1000, -1000);
        Vector3f worldAabbMax = new Vector3f(1000, 1000, 1000);
        // maximum number of objects
        final int maxProxies = 1024;
        // Broadphase computes an conservative approximate list of colliding pairs
        broadphase = new AxisSweep3(
                worldAabbMin, worldAabbMax, maxProxies);

        // constraint (joint) solver
        ConstraintSolver solver = new SequentialImpulseConstraintSolver();

        // provides discrete rigid body simulation
        return new DiscreteDynamicsWorld(
                dispatcher, broadphase, solver, collisionConfiguration);
    }

    /**
     * Resets the scene to its start state
     */
    public void resetScene() {
        // iterate rigid bodies
        for (int i = 0; i < dynamicsWorld.getNumCollisionObjects(); i++) {
            CollisionObject colObj = dynamicsWorld.getCollisionObjectArray().get(i);
            RigidBody body = RigidBody.upcast(colObj);
            if (body != null) {
                if (body.getMotionState() instanceof ShapeMotionState) {
                    // reset body to its start position
                    ((ShapeMotionState) body.getMotionState()).reset(body);
                }

                // removed cached contact points
                dynamicsWorld.getBroadphase().getOverlappingPairCache().cleanProxyFromPairs(
                        colObj.getBroadphaseHandle(), dynamicsWorld.getDispatcher());

                // stop the body from moving and spinning
                if (!body.isStaticObject()) {
                    body.setLinearVelocity(new Vector3f(0f, 0f, 0f));
                    body.setAngularVelocity(new Vector3f(0f, 0f, 0f));
                }
            }
        }

        //character.reset();
        //character.warp(CHARACTER_POS);
    }

    /**
     * Updates the simulation.
     * @param frameTimeSec the duration of the last frame in seconds
     */
    private void nextFrame(float frameTimeSec) {

//        for (int i = 0; i < dynamicsWorld.getNumCollisionObjects(); i++) {
//            CollisionObject colObj = dynamicsWorld.getCollisionObjectArray().get(i);
//            RigidBody body = RigidBody.upcast(colObj);
//            if (body != null) {
//                // removed cached contact points
//                dynamicsWorld.getBroadphase().getOverlappingPairCache().cleanProxyFromPairs(
//                        colObj.getBroadphaseHandle(), dynamicsWorld.getDispatcher());
//            }
//        }

        // will increment simulation in 1/60 second steps and interpolate
        // between the two last steps to get smooth animation
        dynamicsWorld.stepSimulation(frameTimeSec, 0, frameTimeSec);
    }

    public void rayTest(Vec3 p1, Vec3 p2, ClosestRayResultCallback rayCallback) {
        synchronized (globalLock) {
            dynamicsWorld.rayTest(p1, p2, rayCallback);
        }
    }
}
