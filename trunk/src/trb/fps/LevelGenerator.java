package trb.fps;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import trb.fps.jsg.JsgBox;
import trb.jsg.Shape;
import trb.jsg.State.Material;
import trb.jsg.State.StencilFuncParams;
import trb.jsg.State.StencilOpParams;
import trb.jsg.VertexData;
import trb.jsg.enums.StencilAction;
import trb.jsg.enums.StencilFunc;
import trb.jsg.util.Vec3;

public class LevelGenerator {

    private final List<Shape> shapes = new CopyOnWriteArrayList();
    public boolean shapesChanged = true;

    public LevelGenerator() {
        Random rand = new Random(12345678);
        for (int i = 0; i < 30; i++) {
            Vec3 pos = new Vec3(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
            pos.sub_(new Vec3(0.5f, 0.5f, 0.5f)).scale(200f, 5f, 200f);
            Vec3 size = new Vec3(10, 10, 10);
            shapes.add(createShape(JsgBox.createFromPosSize(pos, size), false));
        }

        shapes.add(createShape(JsgBox.createFromMinMax(-100, -1, -100, 100, 0, 100), false));
        shapes.add(createShape(JsgBox.createFromMinMax(-0.1f, 0, -0.1f, 0.1f, 100, 0.1f), false));
    }

    public static Shape createShape(VertexData vertexData, boolean isDynamic) {
        Shape shape = new Shape();
        shape.getState().setCullEnabled(true);
        shape.getState().setMaterial(new Material());
        shape.getState().setStencilTestEnabled(true);
        shape.getState().setStencilFunc(new StencilFuncParams(StencilFunc.ALWAYS, 1, 1));
        shape.getState().setStencilOp(new StencilOpParams(StencilAction.KEEP, StencilAction.KEEP, StencilAction.REPLACE));
        shape.setVertexData(vertexData);
        return shape;
    }

    public void replace(List<Shape> newShapes) {
        synchronized (shapes) {
            shapes.clear();
            shapes.addAll(newShapes);
            shapesChanged = true;
        }
    }

    public List<Shape> get() {
        shapesChanged = false;
        return new ArrayList(shapes);
    }
}
