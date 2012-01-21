package trb.fps;

import trb.jsg.util.Vec3;


public class LineDistance {

    public float t;
    public Vec3 d = new Vec3();
    public float distance;

    public LineDistance(Vec3 c, Vec3 a, Vec3 b) {
        Vec3 ab = new Vec3(b).sub_(a);
        Vec3 ac = new Vec3(c).sub_(a);
        t = ac.dot(ab) / ab.dot(ab);
        if (t < 0f) {
            t = 0;
        }
        if (t > 1) {
            t = 1;
        }

        d.scaleAdd(t, ab, a);
        distance = d.distance(c);
    }
}
