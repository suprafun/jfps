package trb.fps.physics;

import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
import trb.jsg.Shape;
import trb.jsg.util.Mat4;

/**
 * Custom MotionState that gets the world transform from a TransformGroup
 */
public class ShapeMotionState extends MotionState {

    private final Mat4 startTransform = new Mat4();
    private final Shape shape;
    private final Mat4 centerOfMassOffset = new Mat4();

    /**
     * The current TransformGroup transform will be used when reset.
     * @param tg the TransformGroup that is wrapped
     */
    public ShapeMotionState(Shape tg) {
        this(tg, new Mat4());
    }

    /**
     * The current TransformGroup transform will be used when reset.
     * @param tg the TransformGroup that is wrapped
     * @param centerOfMassOffset defference between the physics and j3d center
     */
    public ShapeMotionState(Shape tg, Mat4 centerOfMassOffset) {
        this.shape = tg;
        startTransform.set(tg.getModelMatrix());
        this.centerOfMassOffset.set(centerOfMassOffset);
    }

    /**
     * Gets a reference to the wrapped TransformGroup.
     * @return the transformGroup
     */
    public Shape getTransformGroup() {
        return shape;
    }

    /**
     * Gives bullet the world transform of tg.
     */
    @Override
    public Transform getWorldTransform(Transform out) {
        Mat4 t3d = new Mat4(centerOfMassOffset);
        t3d.invert();
        t3d.mul(new Mat4(shape.getModelMatrix()));
        t3d.get(out.origin);
        t3d.get(out.basis);
        return out;
    }

    /**
     * Sets the world transform of tg from the transform provided by bullet.
     */
    @Override
    public void setWorldTransform(Transform worldTrans) {
        Mat4 t3d = new Mat4();
        t3d.setTranslation(worldTrans.origin);
        t3d.setRotationScale(worldTrans.basis);
        t3d.mul(centerOfMassOffset);
        shape.setModelMatrix(t3d);
        System.out.println("BBBB " + worldTrans.origin);
    }

    /**
     * Resets the wrapped TransformGroup and the specified body to where it
     * was when this TGMotionState was created.
     */
    public void reset(RigidBody body) {
        shape.setModelMatrix(startTransform);
        body.setWorldTransform(getWorldTransform(new Transform()));
        body.setInterpolationWorldTransform(getWorldTransform(new Transform()));
        body.activate();
    }
}
