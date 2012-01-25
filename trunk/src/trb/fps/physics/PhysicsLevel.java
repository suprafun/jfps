package trb.fps.physics;

import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import javax.vecmath.Vector3f;
import trb.fps.LevelGenerator;
import trb.jsg.Shape;
import trb.jsg.TreeNode;
import trb.jsg.util.Vec3;

/**
 *
 * @author tomrbryn
 */
public class PhysicsLevel {

    static PhysicsLevel instance;
    static KinematicCharacter character;

    public static synchronized Vec3 move(Vec3 from, Vec3 to) {
        if (instance == null) {
            instance = new PhysicsLevel();
            character = instance.addCharacter();
        }

        character.setFromTo(from, to);
        instance.nextFrame(1/30f);
        return character.getTransform().getTranslation();
    }

    // contains all the physics objects and performs the simulation
    public DynamicsWorld dynamicsWorld = null;
    BroadphaseInterface broadphase;

    /**
     * Creates dynamicsWorld and adds RigidBodies
     */
    public PhysicsLevel() {
        dynamicsWorld = createDynamicsWorld();
        dynamicsWorld.setGravity(new Vector3f(0f, -10f, 0f));

        // add shape to the renderpass tree
        for (TreeNode node : new LevelGenerator().get()) {
            addAsConvexHull(node, false);
        }
    }

    public ConvexHull addAsConvexHull(TreeNode node, boolean isDynamic) {
        Shape shape = node.getShape(0);
        ConvexHull convexHull = new ConvexHull(shape, isDynamic);
        dynamicsWorld.addRigidBody(convexHull.getRigidBody());
        return convexHull;
    }

    public KinematicCharacter addCharacter() {
        return new KinematicCharacter(this);
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
        dynamicsWorld.stepSimulation(frameTimeSec);
    }
}
