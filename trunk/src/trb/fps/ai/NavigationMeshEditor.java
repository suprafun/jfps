package trb.fps.ai;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;
import org.critterai.math.Vector3;
import org.critterai.nav.DistanceHeuristicType;
import org.critterai.nav.MasterNavRequest;
import org.critterai.nav.MasterNavigator;
import org.critterai.nav.MasterPath.Path;
import org.critterai.nav.NavUtil;
import org.critterai.nmgen.NavmeshGenerator;
import org.critterai.nmgen.TriangleMesh;
import trb.jsg.Shape;
import trb.jsg.State.StencilFuncParams;
import trb.jsg.State.StencilOpParams;
import trb.jsg.TreeNode;
import trb.jsg.VertexData;
import trb.jsg.enums.PolygonMode;
import trb.jsg.enums.SortOrder;
import trb.jsg.enums.StencilAction;
import trb.jsg.enums.StencilFunc;
import trb.jsg.renderer.Renderer;
import trb.jsg.util.SGUtil;

public class NavigationMeshEditor {
    public final NavmeshParameters parameters = new NavmeshParameters();
    private MasterNavigator navigator;
    public final TreeNode treeNode = new TreeNode();
    private final NavigationMeshCreator creator;

    public NavigationMeshEditor(NavigationMeshCreator creator) {
        this.creator = creator;

        parameters.listeners.addListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                generateNavMesh();
            }
        });
    }


    public void generateNavMesh() {
        Renderer.invokeLater(new Runnable() {

            public void run() {
                NavmeshGenerator generator = parameters.create();
                TriangleMesh triMesh = creator.create(generator);
                treeNode.removeAllShapes();
                treeNode.addShape(createMeshGeometry(triMesh.vertices, triMesh.indices, false));
                navigator = NavUtil.getNavigator(
                        triMesh.vertices, triMesh.indices,
                        5, 0.5f, 0.05f, DistanceHeuristicType.MANHATTAN,
                        1000000, 60000, 2, 20);

                Vector3f start = new Vector3f(-2, 0, 0);
                Vector3f goal = new Vector3f(8, -8f, 0);
                findPath(start, goal);
            }
        });

    }

    private void findPath(Tuple3f start, Tuple3f goal) {
        MasterNavRequest<Path>.NavRequest pathRequest = navigator.navigator().getPath(
                start.x, start.y, start.z, goal.x, goal.y, goal.z);

        MasterNavRequest<Vector3>.NavRequest nearest = navigator.navigator().getNearestValidLocation(start.x, start.y, start.z);
        navigator.processAll(true);
        System.out.println(pathRequest.data());
        System.out.println("" + nearest.data());

        Path path = pathRequest.data();
        if (path == null) {
            System.out.println("findPath no data");
            return;
        }
        int[] pathIndices = new int[path.pathPolyCount() * 3];
        float[] pathVerts = new float[path.pathVertCount() * 3];
        path.getPathPolys(pathVerts, pathIndices);
        //treeNode.addShape(createMeshGeometry(pathVerts, pathIndices, true));
        System.out.println("index count "+pathIndices.length);

        List<Point3f> pathLineCoords = new ArrayList();
        Vector3 pos = new Vector3(start.x, start.y, start.z);
        Vector3 nextPos = new Vector3();
        int maxIter = 100;
        while (!pos.sloppyEquals(goal.x, goal.y, goal.z, 0.1f)) {
            pathLineCoords.add(new Point3f(pos.x, pos.y+0.1f, pos.z));
            path.getTarget(pos.x, pos.y, pos.z, nextPos);
            if (pos.sloppyEquals(nextPos, 0.1f)) {
                break;
            }
            pos.set(nextPos);
            if (maxIter-- < 0) {
                break;
            }
        }
        pathLineCoords.add(new Point3f(pos.x, pos.y, pos.z));
        if (pathLineCoords.size() > 1) {
            int[] indices = new int[(pathLineCoords.size()-1)*2];
            for (int i=0; i<pathLineCoords.size()-1; i++) {
                indices[i*2] = i;
                indices[i*2+1] = i+1;
            }
            float[] coords = SGUtil.toFloats(pathLineCoords);
            float[] colors = new float[coords.length];
            for (int i=0; i<colors.length; i+=3) {
                colors[i] = 0f;
                colors[i+1] = 1f;
                colors[i+2] = 0f;
            }

            VertexData vertexData = new VertexData(coords, null, colors, 2, null, indices);
            vertexData.mode = VertexData.Mode.LINES;
            Shape shape = new Shape(vertexData);
            shape.getState().setStencilTestEnabled(true);
            shape.getState().setStencilFunc(new StencilFuncParams(StencilFunc.ALWAYS, 1, 1));
            shape.getState().setStencilOp(new StencilOpParams(StencilAction.KEEP, StencilAction.KEEP, StencilAction.REPLACE));
            shape.getState().setLineWidth(5);
            treeNode.addShape(shape);
        }
    }

    public static Shape createMeshGeometry(float[] vertices, int[] indices, boolean filled) {
        VertexData vertexData = new VertexData(vertices, null, null, 2, null, indices);
        Shape shape = new Shape(vertexData);
        shape.setSortOrder(SortOrder.BACK_TO_FRONT);
        shape.getState().setStencilTestEnabled(true);
        shape.getState().setStencilFunc(new StencilFuncParams(StencilFunc.ALWAYS, 1, 1));
        shape.getState().setStencilOp(new StencilOpParams(StencilAction.KEEP, StencilAction.KEEP, StencilAction.REPLACE));
        if (filled) {
            shape.getState().setDepthTestEnabled(false);
        } else {
            shape.getState().setPolygonMode(PolygonMode.LINE);
            shape.getState().setLineWidth(0.5f);
            shape.getState().setLineSmooth(true);
        }
        return shape;
    }
//
//    public static Appearance createLineAppearance() {
//        Appearance lineApp = new Appearance();
//        lineApp.setColoringAttributes(new ColoringAttributes(0.4f, 0.4f, 0.4f, ColoringAttributes.NICEST));
//        lineApp.setPolygonAttributes(new PolygonAttributes(PolygonAttributes.POLYGON_LINE, PolygonAttributes.CULL_NONE, 0f));
//        return lineApp;
//    }
}
