package trb.fps.physics;

import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.convexhull.HullDesc;
import com.bulletphysics.linearmath.convexhull.HullFlags;
import com.bulletphysics.linearmath.convexhull.HullLibrary;
import com.bulletphysics.linearmath.convexhull.HullResult;
import com.bulletphysics.util.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Vector3f;
import trb.jsg.Shape;
import trb.jsg.VertexData;
import trb.jsg.util.Mat4;

public class ConvexHull {

    public enum Type {
        DYNAMIC, STATIC, KINEMATIC;
    }

    public ConvexHullShape convexHullShape = new ConvexHullShape(new ObjectArrayList());
    public RigidBody body;
    private final Shape shape;
    private float[] coords;
    private HullResult hullResult;
    private float mass = 0;
    private Type type = Type.STATIC;

    public ConvexHull(Shape shape, boolean isDynamic) {
        this.shape = shape;
        if (isDynamic) {
            mass = 1f;
            type = Type.DYNAMIC;
        }
        initBody();
        extractCoords();
        moveCoordsToCenterOfMassAndUpdateOffset();
        createOptimizedHull();
        updateConvexHullShapeAndBody();
        applyType();
        updateVisualisation();
    }

    public RigidBody getRigidBody() {
        return body;
    }

    private void initBody() {
        // Create Dynamic Objects
        Vector3f localInertia = new Vector3f(0, 0, 0);

        ShapeMotionState motionState = new ShapeMotionState(shape);
        RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(
                mass, motionState, convexHullShape, localInertia);
        body = new RigidBody(rbInfo);
    }

    private void extractCoords() {
        coords = new float[shape.getVertexData().coordinates.capacity()];
        shape.getVertexData().coordinates.get(coords);
    }

    private void moveCoordsToCenterOfMassAndUpdateOffset() {
        //offset.set(BulletUtils.moveToCenterOfMass(coords));
    }

    private void createOptimizedHull() {
        ObjectArrayList<Vector3f> vertices = new ObjectArrayList(coords.length / 3);
        for (int off = 0; off < coords.length; off += 3) {
            vertices.add(new Vector3f(coords[off + 0], coords[off + 1], coords[off + 2]));
        }
        HullDesc hd = new HullDesc(HullFlags.TRIANGLES, vertices.size(), vertices);
        HullLibrary hullLib = new HullLibrary();
        hullResult = new HullResult();
        hullLib.createConvexHull(hd, hullResult);
    }

    private void updateConvexHullShapeAndBody() {
        List<Vector3f> points = convexHullShape.getPoints();
        points.clear();
        points.addAll(hullResult.outputVertices);
        convexHullShape.recalcLocalAabb();
        applyMassProps();
    }

    private void applyMassProps() {
        Vector3f localInertia = new Vector3f(0, 0, 0);
        // rigidbody is dynamic if and only if mass is non zero, otherwise static
        if ((this.mass != 0f)) {
            convexHullShape.calculateLocalInertia(this.mass, localInertia);
        }

        body.setMassProps(this.mass, localInertia);
    }

    public void setType(Type type, float mass) {
        this.type = type;
        this.mass = mass;
        applyMassProps();
        applyType();
    }

    private void applyType() {
        switch (type) {
            case DYNAMIC:
                body.setCollisionFlags(body.getCollisionFlags() & ~CollisionFlags.KINEMATIC_OBJECT);
                body.forceActivationState(CollisionObject.ACTIVE_TAG);
                break;
            case KINEMATIC:
                body.setCollisionFlags(body.getCollisionFlags() | CollisionFlags.KINEMATIC_OBJECT);
                body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);
                break;
            case STATIC:
                break;
        }
    }

    private void updateVisualisation() {
        if (hullResult.numFaces > 0) {
            VertexData vertexData = new VertexData();
            vertexData.setCoordinates(toFloatArray(hullResult.outputVertices)
                    , null, null, 0, null, BulletUtils.getInts(hullResult.indices));

            Shape s = new Shape();
            s.setVertexData(vertexData);
            s.setModelMatrix(new Mat4(this.shape.getModelMatrix()));
        }
    }

    private float[] toFloatArray(List<Vector3f> vecs) {
        float[] floats = new float[vecs.size() * 3];
        for (int i = 0; i < vecs.size(); i++) {
            Vector3f v = vecs.get(i);
            floats[i*3+0] = v.x;
            floats[i*3+1] = v.y;
            floats[i*3+2] = v.z;
        }
        return floats;
    }
}
