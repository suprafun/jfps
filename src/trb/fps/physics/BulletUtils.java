package trb.fps.physics;

import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.IntArrayList;
import javax.media.j3d.Transform3D;
import trb.jsg.util.Vec3;

public class BulletUtils {
//    public static Vec3 moveToCenterOfMass(float[] coords) {
//        Vec3 offset = new Vec3();
//        if (coords.length > 0) {
//            // center of mass in bullet is always center of bullet shape
//            offset.set(new BBox(coords).getCenter());
//            for (int off = 0; off < coords.length; off += 3) {
//                coords[off + 0] -= offset.x;
//                coords[off + 1] -= offset.y;
//                coords[off + 2] -= offset.z;
//            }
//        }
//        return offset;
//    }

    public static int[] getInts(IntArrayList list) {
        int[] ints = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            ints[i] = list.get(i);
        }
        return ints;
    }

    public static Transform toBulletTransform(Transform3D t3d) {
        return toBulletTransform(t3d, new Transform());
    }

    public static Transform toBulletTransform(Transform3D t3d, Transform out) {
        t3d.get(out.origin);
        t3d.get(out.basis);
        return out;
    }
}
