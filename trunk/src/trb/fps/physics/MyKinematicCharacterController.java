/*
 * Java port of Bullet (c) 2008 Martin Dvorak <jezek2@advel.cz>
 *
 * Bullet Continuous Collision Detection and Physics Library
 * Copyright (c) 2003-2008 Erwin Coumans  http://www.bulletphysics.com/
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from
 * the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

package trb.fps.physics;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.collision.broadphase.BroadphasePair;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.dynamics.ActionInterface;
import com.bulletphysics.linearmath.IDebugDraw;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

/**
 * btKinematicCharacterController is an object that supports a sliding motion in
 * a world. It uses a ghost object and convex sweep test to test for upcoming
 * collisions. This is combined with discrete collision detection to recover
 * from penetrations.
 * <p>
 * Interaction between btKinematicCharacterController and dynamic rigid bodies
 * needs to be explicity implemented by the user.
 *
 * @author tomrbryn
 */
public class MyKinematicCharacterController extends ActionInterface {

    static Vector3f[] upAxisDirection = new Vector3f[] {
        new Vector3f(1.0f, 0.0f, 0.0f),
        new Vector3f(0.0f, 1.0f, 0.0f),
        new Vector3f(0.0f, 0.0f, 1.0f),
    };

    protected float halfHeight;

    protected PairCachingGhostObject ghostObject;

    // is also in ghostObject, but it needs to be convex, so we store it here
    // to avoid upcast
	protected ConvexShape convexShape;

    protected float verticalVelocity;
    protected float verticalOffset;
    protected float fallSpeed;
    protected float jumpSpeed;
    protected float maxJumpHeight;
    protected float maxSlopeRadians; // Slope angle that is set (used for returning the exact value)
    protected float maxSlopeCosine;  // Cosine equivalent of m_maxSlopeRadians (calculated once when set, for optimization)
    protected float gravity;

    protected float turnAngle;

    protected float stepHeight;

    protected float addedMargin;//@todo: remove this and fix the code

    ///this is the desired walk direction, set by the user
    protected Vector3f walkDirection = new Vector3f();
    protected Vector3f normalizedDirection = new Vector3f();

    //some internal variables
    protected Vector3f currentPosition = new Vector3f();
    protected float currentStepOffset;
    protected Vector3f targetPosition = new Vector3f();

    ///keep track of the contact manifolds
    ObjectArrayList<PersistentManifold> manifoldArray = new ObjectArrayList<PersistentManifold>();

    protected boolean touchingContact;
    protected Vector3f touchingNormal = new Vector3f();

    public boolean wasOnGround;
    public boolean wasJumping;

    protected boolean useGhostObjectSweepTest;
    protected boolean useWalkDirection;
    protected float velocityTimeInterval;
    protected int upAxis;

    protected CollisionObject me;

    public MyKinematicCharacterController(PairCachingGhostObject ghostObject
            , ConvexShape convexShape, float stepHeight) {
        this(ghostObject, convexShape, stepHeight, 1);
    }

    public MyKinematicCharacterController(PairCachingGhostObject ghostObject
            , ConvexShape convexShape, float stepHeight, int upAxis) {

        this.upAxis = upAxis;
        this.addedMargin = 0.02f;
        this.walkDirection.set(0, 0, 0);
        this.useGhostObjectSweepTest = true;
        this.ghostObject = ghostObject;
        this.stepHeight = stepHeight;
        this.turnAngle = 0.0f;
        this.convexShape = convexShape;
        this.useWalkDirection = true;
        this.velocityTimeInterval = 0.0f;
        this.verticalVelocity = 0.0f;
        this.verticalOffset = 0.0f;
        this.gravity = 9.8f * 3; // 3G acceleration
        this.fallSpeed = 55.0f; // Terminal velocity of sky diver in m/s
        this.jumpSpeed = 10.0f; // ?
        this.wasOnGround = false;
        this.wasJumping = false;
        setMaxSlope((float) Math.toRadians(45.0));
    }

    PairCachingGhostObject getGhostObject() {
        return ghostObject;
    }

    // ActionInterface interface
    public void updateAction(CollisionWorld collisionWorld, float deltaTime) {
        preStep(collisionWorld);
        playerStep(collisionWorld, deltaTime);
    }

    // ActionInterface interface
    public void debugDraw(IDebugDraw debugDrawer) {
    }

    public void setUpAxis(int axis) {
        if (axis < 0) {
            axis = 0;
        }
        if (axis > 2) {
            axis = 2;
        }
        upAxis = axis;
    }

    /**
     * This should probably be called setPositionIncrementPerSimulatorStep. This
     * is neither a direction nor a velocity, but the amount to increment the
     * position each simulation iteration, regardless of dt.
     * <p>
     * This call will reset any velocity set by setVelocityForTimeInterval().
     */
	public void	setWalkDirection(Vector3f walkDirection) {
        useWalkDirection = true;
        this.walkDirection.set(walkDirection);
        normalizedDirection.set(getNormalizedVector(walkDirection, new Vector3f()));
    }

    /**
     * Caller provides a velocity with which the character should move for the
     * given time period. After the time period, velocity is reset to zero.
     * This call will reset any walk direction set by setWalkDirection().
     * Negative time intervals will result in no motion.
     */
	public void setVelocityForTimeInterval(Vector3f velocity, float timeInterval) {
        useWalkDirection = false;
        walkDirection.set(velocity);
        normalizedDirection.set(getNormalizedVector(walkDirection, new Vector3f()));
        velocityTimeInterval = timeInterval;
    }

	public void reset() {

    }

	public void warp(Vector3f origin) {
        Transform xform = new Transform();
        xform.setIdentity();
        xform.origin.set(origin);
        ghostObject.setWorldTransform(xform);
    }

    public void preStep(CollisionWorld collisionWorld) {
        int numPenetrationLoops = 0;
        touchingContact = false;
        while (recoverFromPenetration(collisionWorld)) {
            numPenetrationLoops++;
            touchingContact = true;
            if (numPenetrationLoops > 4) {
    //			printf("character could not recover from penetration = %d\n", numPenetrationLoops);
                break;
            }
        }

        currentPosition.set(ghostObject.getWorldTransform(new Transform()).origin);
        targetPosition.set(currentPosition);
    //	printf("m_targetPosition=%f,%f,%f\n",m_targetPosition[0],m_targetPosition[1],m_targetPosition[2]);
    }

    public void playerStep(CollisionWorld collisionWorld, float dt) {
        // quick check...
        if (!useWalkDirection && velocityTimeInterval <= 0.0f) {
            return;		// no motion
        }

        wasOnGround = onGround();

        // Update fall velocity.
        verticalVelocity -= gravity * dt;
        if (verticalVelocity > 0.0 && verticalVelocity > jumpSpeed) {
            verticalVelocity = jumpSpeed;
        }
        if (verticalVelocity < 0.0 && Math.abs(verticalVelocity) > Math.abs(fallSpeed)) {
            verticalVelocity = -Math.abs(fallSpeed);
        }
        verticalOffset = verticalVelocity * dt;

        Transform xform = ghostObject.getWorldTransform(new Transform());

        stepUp(collisionWorld);
        if (useWalkDirection) {
            stepForwardAndStrafe(collisionWorld, walkDirection);
        } else {
            //printf("  time: %f", m_velocityTimeInterval);
            // still have some time left for moving!
            float dtMoving = (dt < velocityTimeInterval) ? dt : velocityTimeInterval;
            velocityTimeInterval -= dt;

            // how far will we move while we are moving?
            Vector3f move = new Vector3f();
            move.scale(dtMoving, walkDirection);

            // okay, step
            stepForwardAndStrafe(collisionWorld, move);
        }
        stepDown(collisionWorld, dt);

        // printf("\n");

        xform.origin.set(currentPosition);
        ghostObject.setWorldTransform(xform);
    }

	public void setFallSpeed(float fallSpeed) {
        this.fallSpeed = fallSpeed;
    }

    public void setJumpSpeed(float jumpSpeed) {
        this.jumpSpeed = jumpSpeed;
    }

	public void setMaxJumpHeight(float maxJumpHeight) {
        this.maxJumpHeight = maxJumpHeight;
    }

    public boolean canJump() {
        return onGround();
    }

    public void jump() {
        if (!canJump()) {
            return;
        }

        verticalVelocity = jumpSpeed;
        wasJumping = true;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    public float getGravity() {
        return gravity;
    }

    public void setMaxSlope(float slopeRadians) {
        this.maxSlopeRadians = slopeRadians;
        this.maxSlopeCosine = (float) Math.cos(slopeRadians);
    }

    public float getMaxSlope() {
        return maxSlopeRadians;
    }

    public boolean onGround() {
        return verticalVelocity == 0.0 && verticalOffset == 0.0;
    }

    // static helper method
    static Vector3f getNormalizedVector(Vector3f v, Vector3f out) {
        out.set(v);
        out.normalize();
        if (out.length() < BulletGlobals.SIMD_EPSILON) {
            out.set(0, 0, 0);
        }
        return out;
    }

    /**
     * Returns the reflection direction of a ray going 'direction' hitting a
     surface with normal 'normal'
     *
     * from: http://www-cs-students.stanford.edu/~adityagp/final/node3.html
     */
    protected Vector3f computeReflectionDirection(Vector3f direction, Vector3f normal, Vector3f out) {
        // return direction - (btScalar(2.0) * direction.dot(normal)) * normal;
        //out.scaleAdd(-2.0f * direction.dot(normal), normal, direction);
        out.set(normal);
        out.scale(-2.0f * direction.dot(normal));
        out.add(direction);
        return out;
    }

    /**
     * Returns the portion of 'direction' that is parallel to 'normal'
     */
	protected Vector3f parallelComponent(Vector3f direction, Vector3f normal, Vector3f out) {
        out.set(normal);
        out.scale(direction.dot(normal));
        return out;
    }

    /**
     * Returns the portion of 'direction' that is perpindicular to 'normal'
     */
    protected Vector3f perpindicularComponent(Vector3f direction, Vector3f normal, Vector3f out) {
        //return direction - parallelComponent(direction, normal);
        Vector3f perpendicular = parallelComponent(direction, normal, out);
        perpendicular.scale(-1);
        perpendicular.add(direction);
        return perpendicular;
    }

	protected boolean recoverFromPenetration(CollisionWorld collisionWorld) {
        boolean penetration = false;

        collisionWorld.getDispatcher().dispatchAllCollisionPairs(
                ghostObject.getOverlappingPairCache()
                , collisionWorld.getDispatchInfo()
                , collisionWorld.getDispatcher());

        currentPosition.set(ghostObject.getWorldTransform(new Transform()).origin);

        float maxPen = 0.0f;
        for (int i=0; i < ghostObject.getOverlappingPairCache().getNumOverlappingPairs(); i++) {
            manifoldArray.clear();

            BroadphasePair collisionPair = ghostObject.getOverlappingPairCache().getOverlappingPairArray().get(i);

            if (collisionPair.algorithm != null) {
                collisionPair.algorithm.getAllContactManifolds(manifoldArray);
            }


            for (int j=0; j<manifoldArray.size(); j++) {
                PersistentManifold manifold = manifoldArray.get(j);
                float directionSign = manifold.getBody0() == ghostObject ? -1.0f : 1.0f;
                for (int p=0; p<manifold.getNumContacts(); p++) {
                    ManifoldPoint pt = manifold.getContactPoint(p);

                    if (pt.getDistance() < 0.0f) {
                        if (pt.getDistance() < maxPen) {
                            maxPen = pt.getDistance();
                            touchingNormal.set(pt.normalWorldOnB);//??
                            touchingNormal.scale(directionSign);
                        }

                        currentPosition.scaleAdd(directionSign * pt.getDistance() * 0.2f
                                , pt.normalWorldOnB, currentPosition);

                        penetration = true;
                    } else {
                        //printf("touching %f\n", pt.getDistance());
                    }
                }

                //manifold->clearManifold();
            }
        }
        Transform newTrans = ghostObject.getWorldTransform(new Transform());
        newTrans.origin.set(currentPosition);
        ghostObject.setWorldTransform(newTrans);
        return penetration;
    }

    protected void stepUp(CollisionWorld world) {
        // phase 1: up
        Transform start = new Transform();
        Transform end = new Transform();
        targetPosition.scaleAdd(stepHeight + (verticalOffset > 0f ? verticalOffset : 0f), upAxisDirection[upAxis], currentPosition);

        start.setIdentity ();
        end.setIdentity ();

        /* FIXME: Handle penetration properly */
        start.origin.scaleAdd(convexShape.getMargin() + addedMargin, upAxisDirection[upAxis], currentPosition);
        end.origin.set(targetPosition);

        Vector3f downAxisDirection = new Vector3f();
        downAxisDirection.scale(-1, upAxisDirection[upAxis]);
        KinematicClosestNotMeConvexResultCallback callback = new KinematicClosestNotMeConvexResultCallback(
                ghostObject, downAxisDirection, 0.7071f);
        callback.collisionFilterGroup = getGhostObject().getBroadphaseHandle().collisionFilterGroup;
        callback.collisionFilterMask = getGhostObject().getBroadphaseHandle().collisionFilterMask;

        if (useGhostObjectSweepTest) {
            ghostObject.convexSweepTest(convexShape, start, end, callback, world.getDispatchInfo().allowedCcdPenetration);
        } else {
            world.convexSweepTest(convexShape, start, end, callback);
        }

        if (callback.hasHit()) {
            // Only modify the position if the hit was a slope and not a wall or ceiling.
            if (callback.hitNormalWorld.dot(upAxisDirection[upAxis]) > 0.0) {
                // we moved up only a fraction of the step height
                currentStepOffset = stepHeight * callback.closestHitFraction;
                currentPosition.interpolate(currentPosition, targetPosition, callback.closestHitFraction);
            }
            verticalVelocity = 0.0f;
            verticalOffset = 0.0f;
        } else {
            currentStepOffset = stepHeight;
            currentPosition.set(targetPosition);
        }
    }

    protected void updateTargetPositionBasedOnCollision (Vector3f hitNormal) {
        updateTargetPositionBasedOnCollision(hitNormal, 0, 1);
    }

    protected void updateTargetPositionBasedOnCollision (Vector3f hitNormal
            , float tangentMag, float normalMag) {
        Vector3f movementDirection = new Vector3f();
        movementDirection.sub(targetPosition, currentPosition);
        float movementLength = movementDirection.length();
        if (movementLength>BulletGlobals.SIMD_EPSILON) {
            movementDirection.normalize();

            Vector3f reflectDir = computeReflectionDirection (movementDirection, hitNormal, new Vector3f());
            reflectDir.normalize();

            Vector3f parallelDir = parallelComponent(reflectDir, hitNormal, new Vector3f());
            Vector3f perpindicularDir = perpindicularComponent(reflectDir, hitNormal, new Vector3f());

            targetPosition.set(currentPosition);
            if (false)//tangentMag != 0.0)
            {
                Vector3f parComponent = new Vector3f();
                parComponent.scale(tangentMag * movementLength, parallelDir);
    //			printf("parComponent=%f,%f,%f\n",parComponent[0],parComponent[1],parComponent[2]);
                targetPosition.add(parComponent);
            }

            if (normalMag != 0.0f) {
                Vector3f perpComponent = new Vector3f();
                perpComponent.scale(normalMag * movementLength, perpindicularDir);
    //			printf("perpComponent=%f,%f,%f\n",perpComponent[0],perpComponent[1],perpComponent[2]);
                targetPosition.add(perpComponent);
            }
        } else
        {
    //		printf("movementLength don't normalize a zero vector\n");
        }
    }

	protected void stepForwardAndStrafe(CollisionWorld collisionWorld, Vector3f walkMove) {
        // phase 2: forward and strafe
        Transform start = new Transform();
        Transform end = new Transform();
        targetPosition.add(currentPosition, walkMove);
        start.setIdentity ();
        end.setIdentity ();

        float fraction = 1.0f;
        Vector3f distance2Vec = new Vector3f();
        distance2Vec.sub(currentPosition, targetPosition);
        float distance2 = distance2Vec.lengthSquared();

        if (touchingContact) {
            if (normalizedDirection.dot(touchingNormal) > 0.0f) {
                updateTargetPositionBasedOnCollision(touchingNormal);
            }
        }

        int maxIter = 10;

        while (fraction > 0.01f && maxIter-- > 0) {
            start.origin.set(currentPosition);
            end.origin.set(targetPosition);
            Vector3f sweepDirNegative = new Vector3f();
            sweepDirNegative.sub(currentPosition, targetPosition);

            KinematicClosestNotMeConvexResultCallback callback = new KinematicClosestNotMeConvexResultCallback(ghostObject, sweepDirNegative, 0f);
            callback.collisionFilterGroup = getGhostObject().getBroadphaseHandle().collisionFilterGroup;
            callback.collisionFilterMask = getGhostObject().getBroadphaseHandle().collisionFilterMask;

            float margin = convexShape.getMargin();
            convexShape.setMargin(margin + addedMargin);

            if (useGhostObjectSweepTest) {
                ghostObject.convexSweepTest(convexShape, start, end, callback, collisionWorld.getDispatchInfo().allowedCcdPenetration);
            } else {
                collisionWorld.convexSweepTest(convexShape, start, end, callback/*, collisionWorld.getDispatchInfo().allowedCcdPenetration*/);
            }

            convexShape.setMargin(margin);


            fraction -= callback.closestHitFraction;

            if (callback.hasHit()) {

                // we moved only a fraction
                Vector3f hitDistanceVec = new Vector3f();
                hitDistanceVec.sub(callback.hitPointWorld, currentPosition);
                float hitDistance = hitDistanceVec.length();

                updateTargetPositionBasedOnCollision(callback.hitNormalWorld);

                Vector3f currentDir = new Vector3f();
                currentDir.sub(targetPosition, currentPosition);
                distance2 = currentDir.lengthSquared();
                if (distance2 > BulletGlobals.SIMD_EPSILON) {
                    currentDir.normalize();
                    /* See Quake2: "If velocity is against original velocity, stop ead to avoid tiny oscilations in sloping corners." */
                    if (currentDir.dot(normalizedDirection) <= 0.0f) {
                        break;
                    }
                } else {
                    break;
                }
            } else {
                // we moved whole way
                currentPosition.set(targetPosition);
            }
        }
    }

	protected void stepDown (CollisionWorld collisionWorld, float dt) {
        Transform start = new Transform();
        Transform end = new Transform();

        // phase 3: down
        float downVelocity = (verticalVelocity < 0.f ? -verticalVelocity : 0.f) * dt;
        if (downVelocity > 0.0 && downVelocity < stepHeight && (wasOnGround || !wasJumping)) {
            downVelocity = stepHeight;
        }

        Vector3f step_drop = new Vector3f();
        step_drop.scale(currentStepOffset + downVelocity, upAxisDirection[upAxis]);
        targetPosition.sub(step_drop);

        start.setIdentity ();
        end.setIdentity ();

        start.origin.set(currentPosition);
        end.origin.set(targetPosition);

        KinematicClosestNotMeConvexResultCallback callback = new KinematicClosestNotMeConvexResultCallback(ghostObject, upAxisDirection[upAxis], maxSlopeCosine);
        callback.collisionFilterGroup = getGhostObject().getBroadphaseHandle().collisionFilterGroup;
        callback.collisionFilterMask = getGhostObject().getBroadphaseHandle().collisionFilterMask;

        if (useGhostObjectSweepTest) {
            ghostObject.convexSweepTest(convexShape, start, end, callback, collisionWorld.getDispatchInfo().allowedCcdPenetration);
        } else {
            collisionWorld.convexSweepTest(convexShape, start, end, callback/*, collisionWorld.getDispatchInfo().allowedCcdPenetration*/);
        }

        if (callback.hasHit()) {
            // we dropped a fraction of the height -> hit floor
            currentPosition.interpolate(currentPosition, targetPosition, callback.closestHitFraction);
            verticalVelocity = 0f;
            verticalOffset = 0f;
            wasJumping = false;
        } else {
            // we dropped the full height
            if (!wasJumping) {
                wasJumping = true;
                wasOnGround = false;
                verticalVelocity = 0f;
                verticalOffset = 0f;
            }

            currentPosition.set(targetPosition);
        }
    }


    static class KinematicClosestNotMeRayResultCallback extends CollisionWorld.ClosestRayResultCallback {
        protected CollisionObject me;
        KinematicClosestNotMeRayResultCallback(CollisionObject me) {
            super(new Vector3f(), new Vector3f());
            this.me = me;
        }

		@Override
		public float addSingleResult(CollisionWorld.LocalRayResult rayResult, boolean normalInWorldSpace) {
            if (rayResult.collisionObject == me) {
                return 1.0f;
            }

            return super.addSingleResult(rayResult, normalInWorldSpace);
        }
    }

    static class KinematicClosestNotMeConvexResultCallback extends CollisionWorld.ClosestConvexResultCallback {
        protected CollisionObject me;
        protected Vector3f up = new Vector3f();
        protected float minSlopeDot;
        KinematicClosestNotMeConvexResultCallback(CollisionObject me, Vector3f up, float minSlopeDot) {
            super(new Vector3f(), new Vector3f());
            this.me = me;
            this.up.set(up);
            this.minSlopeDot = minSlopeDot;
        }

        @Override
        public float addSingleResult(CollisionWorld.LocalConvexResult convexResult, boolean normalInWorldSpace) {
            if (convexResult.hitCollisionObject == me) {
                return 1.0f;
            }

            Vector3f hitNormalWorld = new Vector3f();
            hitNormalWorld.set(convexResult.hitNormalLocal);
            if (!normalInWorldSpace) {
                ///need to transform normal into worldspace
                Matrix3f m3f = convexResult.hitCollisionObject.getWorldTransform(new Transform()).basis;
                m3f.transform(hitNormalWorld);
            }

            float dotUp = up.dot(hitNormalWorld);
            if (dotUp < minSlopeDot) {
                return 1.0f;
            }

            return super.addSingleResult(convexResult, normalInWorldSpace);
        }
    }
}
