package trb.fps.util;

import trb.jsg.util.Vec3;


/**
 * Calculates the closest points between two line segments. This class can be
 * reused. However it is not thread safe.
 *
 * Taken from Real Time Collision Detection, page 149.
 * 
 * @author tomrbryn
 */
public class SegmentSegmentDistance {

    private static final float EPSILON = 0.00001f;

    /** closest squared distance between segments */
    public float squaredDistance = 0f;

    /** S1 intersection parameter */
    public float s = 0f;

    /** S2 intersection parameter */
    public float t = 0f;

    /** Closest point on S1*/
    public Vec3 c1 = new Vec3();

    /** Closest point on S2*/
    public Vec3 c2 = new Vec3();

    // temp vars
    private Vec3 d1 = new Vec3();
    private Vec3 d2 = new Vec3();
    private Vec3 r = new Vec3();
    private Vec3 temp = new Vec3();

    /**
     * @return the squared distance between between line segments
     */
    public float calculate(Vec3 p1, Vec3 q1, Vec3 p2, Vec3 q2) {
        d1.sub(q1, p1); // Direction vector of segment S1
        d2.sub(q2, p2); // Direction vector of segment S2
        r.sub(p1, p2);
        float a = d1.dot(d1);   // Squared length of segment S1, always nonnegative
        float e = d2.dot(d2);   // Squared length of segment S2, always nonnegative
        float f = d2.dot(r);

        // Check if either or both segments degenerate into points
        if (a <= EPSILON && e <= EPSILON) {
            // Both segments degenerate into points
            s = 0f;
            t = 0f;
            c1.set(p1);
            c2.set(p2);

            temp.sub(c1, c2);
            return temp.dot(temp);
        }
        if (a <= EPSILON) {
            // First segment degenerates into a point
            s = 0f;
            t = f / e;  // s = 0 => t = (b*s + f) / e = f / e;
            t = clamp(t, 0f, 1f);
        } else {
            float c = d1.dot(r);
            if (e <= EPSILON) {
                // Second segment degenerates into a point
                t = 0f;
                s = clamp(-c / a, 0f, 1f);  // t = 0 => s = (b*t + c) / a = -c / a;
            } else {
                // The general nondegenerate case starts here
                float b = d1.dot(d2);
                float denom = a*e-b*b;  // Always nonnegative

                // If segments not parallel, compute closest point on L1 to L2
                // and clamp to segmnet S1, Else pick arbitrary s (here 0)
                if (denom != 0f) {
                    s = clamp((b*f -c*e) / denom, 0f, 1f);
                } else {
                    s = 0f;
                }
                // Compute point on L2 closest to S1(s) using
                // t = dot((P1 + D1*s) - P2,D2) / dot(D2, D2) = (b*s + f) / e
                float tnom = b*s + f;

                // If t in [0, 1] done. Else clamp t, recompute s for the new
                // value of using s = dot((P2 + D2*t) - P1, D1) / dot(D1, D1) = (t*b - c) / a
                if (tnom < 0f) {
                    t = 0f;
                    s = clamp(-c / a, 0f, 1f);
                } else if (tnom > e) {
                    t = 1f;
                    s = clamp((b - c) / a, 0f, 1f);
                } else {
                    t = tnom / e;
                }
            }
        }

        c1.scaleAdd(s, d1, p1);
        c2.scaleAdd(t, d2, p2);
        temp.sub(c1, c2);
        squaredDistance = temp.dot(temp);
        return squaredDistance;
    }

    private float clamp(float n, float min, float max) {
        if (n < min) return min;
        if (n > max) return max;
        return n;
    }


    public static void main(String[] args) {
        Vec3 p1 = new Vec3(0, 0, -10);
        Vec3 q1 = new Vec3(0, 0, -2);
        Vec3 p2 = new Vec3(0, -10, 0);
        Vec3 q2 = new Vec3(0,  10, 0);

        SegmentSegmentDistance calc = new SegmentSegmentDistance();
        float distance = calc.calculate(p1, q1, p2, q2);
        System.out.println(distance+" "+calc.c1+" "+calc.c2+" "+calc.s+" "+calc.t);
    }
}
