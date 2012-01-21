package trb.fps.editor;

import java.beans.PropertyChangeEvent;
import trb.fps.LevelGenerator;
import trb.fps.jsg.JsgBox;
import trb.fps.property.Property;
import trb.fps.property.PropertyOwner;
import trb.jsg.Shape;
import trb.jsg.VertexData;
import trb.jsg.renderer.Renderer;

public class BoxProps extends PropertyOwner {

    public static BoxProps fromMinMax(String name, float minx, float miny, float minz, float maxx, float maxy, float maxz) {
        BoxProps box = new BoxProps();
        box.name.set(name);
        box.x.set((minx + maxx) / 2);
        box.y.set((miny + maxy) / 2);
        box.z.set((minz + maxz) / 2);
        box.width.set(maxx - minx);
        box.height.set(maxy - miny);
        box.depth.set(maxz - minz);
        return box;
    }

    public final Property<String> name = add("Name", String.class, "Name");
    public final Property<Float> x = add("X", Float.class, 0f);
    public final Property<Float> y = add("Y", Float.class, 0f);
    public final Property<Float> z = add("Z", Float.class, 0f);
    public final Property<Float> width = add("Width", Float.class, 1f);
    public final Property<Float> height = add("Height", Float.class, 1f);
    public final Property<Float> depth = add("Depth", Float.class, 1f);

    private Shape shape;

    @Override
    protected void propertyChanged(PropertyChangeEvent e) {
        if (shape != null) {
            Renderer.invokeLater(new Runnable() {

                public void run() {
                    System.out.println("AAAAAAA");
                    VertexData newVertexData = createVertexData();
                    shape.getVertexData().coordinates.rewind();
                    shape.getVertexData().coordinates.put(newVertexData.coordinates);
                    shape.getVertexData().coordinates.rewind();
                    shape.getVertexData().changed();
                }
            });
        }
    }

    @Override
    public String toString() {
        return name.get();
    }

    public Shape getShape() {
        if (shape == null) {
            shape = LevelGenerator.createShape(createVertexData(), false);
        }
        return shape;
    }

    private VertexData createVertexData() {
        return JsgBox.createFromPosSize(x.get(), y.get(), z.get(), width.get(), height.get(), depth.get());
    }
}
