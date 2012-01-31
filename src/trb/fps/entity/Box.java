package trb.fps.entity;

import trb.fps.jsg.JsgBox;
import trb.fps.property.Property;
import trb.jsg.VertexData;
import trb.jsg.util.Vec3;

public class Box extends Component {

    public static Entity fromMinMax(String name, float minx, float miny, float minz, float maxx, float maxy, float maxz) {
        Entity e = Entity.create(Transform.class, Box.class);
        e.getComponent(Meta.class).name.set(name);
        Box box = e.getComponent(Box.class);
        box.width.set(maxx - minx);
        box.height.set(maxy - miny);
        box.depth.set(maxz - minz);
        Transform t = e.getComponent(Transform.class);
        t.positionx.set((minx + maxx) / 2);
        t.positiony.set((miny + maxy) / 2);
        t.positionz.set((minz + maxz) / 2);
        return e;
    }

    public static Entity fromPosSize(String name, Vec3 p, Vec3 s) {
        Entity e = Entity.create(Meta.class, Transform.class, Box.class);
        e.getComponent(Meta.class).name.set(name);
        Box box = e.getComponent(Box.class);
        box.width.set(s.x);
        box.height.set(s.y);
        box.depth.set(s.z);
        Transform t = e.getComponent(Transform.class);
        t.positionx.set(p.x);
        t.positiony.set(p.y);
        t.positionz.set(p.z);
        return e;
    }

    public final Property<Float> width = add("Width", 1f);
    public final Property<Float> height = add("Height", 1f);
    public final Property<Float> depth = add("Depth", 1f);

    public VertexData createVertexData() {
        return JsgBox.createFromPosSize(0, height.get()/2, 0, width.get(), height.get(), depth.get());
    }

    public long getSizeChange() {
        return Math.max(width.lastChange, Math.max(height.lastChange, depth.lastChange));
    }
}
