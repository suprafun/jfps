package trb.fps.jsg;

import trb.fps.util.FloatList;
import trb.jsg.VertexData;
import trb.jsg.util.Vec3;

public class JsgBox {

    public static VertexData createFromMinMax(float minx, float miny, float minz
            , float maxx, float maxy, float maxz) {
        return new Triangle(minx, maxx, miny, maxy, minz, maxz).createVertexData();
    }

    public static VertexData createFromMinMax(Vec3 min, Vec3 max) {
        return new Triangle(min.x, max.x, min.y, max.y, min.z, max.z).createVertexData();
    }

    public static VertexData createFromHalfWidth(float halfw, float halfh, float halfd) {
        return new Triangle(-halfw, halfw, -halfh, halfh, -halfd, halfd).createVertexData();
    }

    public static VertexData createFromPosSize(float x, float y, float z, float w, float h, float d) {
        return createFromPosSize(new Vec3(x, y, z), new Vec3(w, h, d));
    }

    public static VertexData createFromPosSize(Vec3 pos, Vec3 size) {
        Vec3 min = new Vec3().scaleAdd_(-0.5f, size, pos);
        Vec3 max = new Vec3().scaleAdd_(0.5f, size, pos);
        return createFromMinMax(min, max);
    }

    static class Triangle {
        FloatList coords = new FloatList(3 * 6 * 2);
        FloatList normals = new FloatList(3 * 6 * 2);
        FloatList texCoords = new FloatList(2 * 6 * 2);
        float[] indexedCoords;
        Vec3 a = new Vec3();
        Vec3 b = new Vec3();
        Vec3 c = new Vec3();
        Vec3 ab = new Vec3();
        Vec3 ac = new Vec3();
        Vec3 n = new Vec3();

        Triangle(float x1, float x2, float y1, float y2, float z1, float z2) {
            FloatList ic = new FloatList(8 * 3);
            ic.append(x1, y1, z1);
            ic.append(x1, y1, z2);
            ic.append(x2, y1, z2);
            ic.append(x2, y1, z1);
            ic.append(x1, y2, z1);
            ic.append(x1, y2, z2);
            ic.append(x2, y2, z2);
            ic.append(x2, y2, z1);
            indexedCoords = ic.toArray();
            add();
        }

        void add() {
            // top
            add(4, 7, 6,  true);
            add(6, 5, 4, false);
            // bottom
            add(0, 1, 2, true);
            add(2, 3, 0, false);
            // sides
            for (int i = 0; i < 4; i++) {
                add(i, i + 4, (i + 1) % 4 + 4, true);
                add((i + 1) % 4 + 4, (i + 1) % 4, i, false);
            }
        }

        void add(int idx1, int idx2, int idx3, boolean first) {
            a.set(indexedCoords[idx1*3+0], indexedCoords[idx1 * 3 + 1], indexedCoords[idx1 * 3 + 2]);
            b.set(indexedCoords[idx2*3+0], indexedCoords[idx2 * 3 + 1], indexedCoords[idx2 * 3 + 2]);
            c.set(indexedCoords[idx3*3+0], indexedCoords[idx3 * 3 + 1], indexedCoords[idx3 * 3 + 2]);
            ab.sub(b, a);
            ac.sub(c, a);
            n.cross_(ac, ab);
            if (n.lengthSquared() > 0) {
                n.normalize();
            } else {
                n.set(0, 1, 0);
            }

            append(coords, c);
            append(coords, b);
            append(coords, a);
            append(normals, n);
            append(normals, n);
            append(normals, n);
            if (first) {
                texCoords.append(0, 0,  1, 0,  1, 1);
            } else {
                texCoords.append(1, 1,  0, 1,  0, 0);
            }
        }

        void append(FloatList list, Vec3 v) {
            list.append(v.x, v.y, v.z);
        }

        VertexData createVertexData() {
            return new VertexData(coords.toArray(), normals.toArray(), null
                    , 2, new float[][] {texCoords.toArray()}, createIndices());
        }

        int[] createIndices() {
            int[] indices = new int[coords.size() / 3];
            for (int i=0; i<indices.length; i++) {
                indices[i] = i;
            }
            return indices;
        }
    }
}
