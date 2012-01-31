package trb.fps;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import trb.fps.entity.Box;
import trb.fps.entity.Entity;
import trb.fps.entity.EntityList;
import trb.fps.entity.HemisphereLightComp;
import trb.fps.entity.Meta;
import trb.fps.entity.PointLightComp;
import trb.fps.entity.Transform;
import trb.fps.jsg.JsgBox;
import trb.jsg.Shape;
import trb.jsg.State.Material;
import trb.jsg.State.StencilFuncParams;
import trb.jsg.State.StencilOpParams;
import trb.jsg.TreeNode;
import trb.jsg.VertexData;
import trb.jsg.enums.StencilAction;
import trb.jsg.enums.StencilFunc;
import trb.jsg.util.Mat4;
import trb.jsg.util.Vec3;

public class LevelGenerator {

    private final List<TreeNode> nodes = new CopyOnWriteArrayList();
    public boolean nodesChanged = true;

    public LevelGenerator() {
        Random rand = new Random(12345678);
        for (int i = 0; i < 30; i++) {
            Vec3 pos = new Vec3(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
            pos.sub_(new Vec3(0.5f, 0.5f, 0.5f)).scale(200f, 5f, 200f);
            Vec3 size = new Vec3(10, 10, 10);
            nodes.add(createNode(JsgBox.createFromPosSize(pos, size), false));
        }

        nodes.add(createNode(JsgBox.createFromMinMax(-100, -1, -100, 100, 0, 100), false));
        nodes.add(createNode(JsgBox.createFromMinMax(-0.1f, 0, -0.1f, 0.1f, 100, 0.1f), false));
    }

    public static TreeNode createNode(VertexData vertexData, boolean isDynamic) {
        Shape shape = new Shape();
        shape.getState().setCullEnabled(true);
        shape.getState().setMaterial(new Material());
        shape.getState().setStencilTestEnabled(true);
        shape.getState().setStencilFunc(new StencilFuncParams(StencilFunc.ALWAYS, 1, 1));
        shape.getState().setStencilOp(new StencilOpParams(StencilAction.KEEP, StencilAction.KEEP, StencilAction.REPLACE));
        shape.setVertexData(vertexData);
        return new TreeNode(shape);
    }

    public void replace(List<TreeNode> newNodes) {
        synchronized (nodes) {
            nodes.clear();
            nodes.addAll(newNodes);
            nodesChanged = true;
        }
    }

    public List<TreeNode> get() {
        nodesChanged = false;
        return new ArrayList(nodes);
    }

//    public static EntityList createEntityList() {
//        EntityList list = new EntityList();
//        Random rand = new Random(12345678);
//        for (int i = 0; i < 30; i++) {
//            Vec3 pos = new Vec3(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
//            pos.sub_(new Vec3(0.5f, 0.5f, 0.5f)).scale(200f, 5f, 200f);
//            Vec3 size = new Vec3(10, 10, 10);
//            list.add(Box.fromPosSize("Generated Box", pos, size));
//        }
//
//        rand = new Random(988231);
//        for (float y = -100; y < 100; y += 20) {
//            for (float x = -100; x < 100; x += 20) {
//                Vec3 pos = new Vec3(x, 3, y);
//                Vec3 color = new Vec3(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()).scale_(0.8f).add(0.2, 0.2, 0.2);
//                Entity e = Entity.create(Meta.class, Transform.class, PointLightComp.class);
//                Transform t = e.getComponent(Transform.class);
//                t.positionx.set(pos.x);
//                t.positionx.set(pos.y);
//                t.positionx.set(pos.z);
//                PointLightComp pointLigh = e.getComponent(PointLightComp.class);
//                pointLigh.color.set(color.getColor());
//                pointLigh.radius.set(15f);
//            }
//        }
//        Entity e = Entity.create(Meta.class, Transform.class, HemisphereLightComp.class);
//        HemisphereLightComp light = e.getComponent(HemisphereLightComp.class);
//        light.skyColor.set(new Vec3(0.35, 0.3, 0.4).getColor());
//        light.groundColor.set(new Vec3().getColor());
//        e.getComponent(Transform.class).set(new Mat4().lookAtDir(new Vec3(-1, 0.25f, 0f)));
//        return list;
//    }
}
