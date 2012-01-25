package trb.fps.editor;

import java.beans.PropertyChangeEvent;
import trb.fps.LevelGenerator;
import trb.fps.jsg.JsgBox;
import trb.fps.property.Property;
import trb.jsg.TreeNode;
import trb.jsg.VertexData;
import trb.jsg.renderer.Renderer;

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

    public final Property<Float> width = add("Width", 1f);
    public final Property<Float> height = add("Height", 1f);
    public final Property<Float> depth = add("Depth", 1f);

    private TreeNode node;

    @Override
    protected void entityPropertyChanged(PropertyChangeEvent e) {
        if (node != null) {
            Renderer.invokeLater(new Runnable() {

                public void run() {
                    updateNode();
                }
            });
        }
    }

    public TreeNode getNode() {
        if (node == null) {
            node = LevelGenerator.createNode(createVertexData(), false);
        }
        updateNode();
        return node;
    }

    private void updateNode() {
        VertexData newVertexData = createVertexData();
        VertexData vertexData = node.getShape(0).getVertexData();
        vertexData.coordinates.rewind();
        vertexData.coordinates.put(newVertexData.coordinates);
        vertexData.coordinates.rewind();
        vertexData.changed();
        node.setTransform(getComponent(Transform.class).get());
    }

    private VertexData createVertexData() {
        return JsgBox.createFromPosSize(0, height.get()/2, 0, width.get(), height.get(), depth.get());
    }
}
