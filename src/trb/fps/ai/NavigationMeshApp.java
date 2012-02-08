package trb.fps.ai;

//import com.sun.j3d.utils.pickfast.PickCanvas;
//import com.sun.j3d.utils.universe.SimpleUniverse;
//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.event.KeyAdapter;
//import java.awt.event.KeyEvent;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseWheelEvent;
//import java.beans.PropertyChangeEvent;
//import java.beans.PropertyChangeListener;
//import java.util.ArrayList;
//import java.util.List;
//import javax.media.j3d.Appearance;
//import javax.media.j3d.Background;
//import javax.media.j3d.BoundingSphere;
//import javax.media.j3d.BranchGroup;
//import javax.media.j3d.Canvas3D;
//import javax.media.j3d.ColoringAttributes;
//import javax.media.j3d.Geometry;
//import javax.media.j3d.LineAttributes;
//import javax.media.j3d.LineStripArray;
//import javax.media.j3d.PickInfo;
//import javax.media.j3d.PolygonAttributes;
//import javax.media.j3d.Shape3D;
//import javax.media.j3d.TransformGroup;
//import javax.swing.JFrame;
//import javax.swing.JPanel;
//import javax.vecmath.Color3f;
//import javax.vecmath.Point3f;
//import javax.vecmath.Tuple3f;
//import javax.vecmath.Vector3f;
//import no.hrp.hvrc.navigation.tools.examine.j3d.ExamineBehavior;
//import no.hrp.hvrc.property.PropertyOwnerFactory;
//import no.hrp.hvrc.utils.grid.Grid;
//import no.hrp.hvrc.utils.j3d.GeometryExtractor;
//import no.hrp.hvrc.utils.j3d.Mat4;
//import no.hrp.hvrc.utils.j3d.PickUtil;
//import no.hrp.hvrc.utils.j3d.SGUtils;
//import no.hrp.hvrc.utils.j3d.Vec3;
//import no.hrp.hvrc.utils.j3d.appearance.SolidTransparent;
//import no.hrp.hvrc.utils.j3d.geometry.IndexedTrianglesGeometry;
//import no.hrp.hvrc.utils.swing.Split;
//import org.critterai.math.Vector3;
//import org.critterai.nav.DistanceHeuristicType;
//import org.critterai.nav.MasterNavRequest;
//import org.critterai.nav.MasterNavigator;
//import org.critterai.nav.MasterPath.Path;
//import org.critterai.nav.NavUtil;
//import org.critterai.nmgen.NavmeshGenerator;
//import org.critterai.nmgen.TriangleMesh;
//import org.jdesktop.j3d.loaders.vrml97.VrmlScene;

public class NavigationMeshApp /*extends JFrame*/ {
//    private SimpleUniverse simpleUniverse;
//    private Canvas3D canvas3D;
//    private ExamineBehavior examine;
//
//    private BranchGroup bg = SGUtils.createBranchGroup();
//    private BranchGroup navMeshBg = SGUtils.createBranchGroup();
//    private BranchGroup pathBg = SGUtils.createBranchGroup();
//    private VrmlScene scene;
//    private final NavmeshParameters parameters = PropertyOwnerFactory.create(NavmeshParameters.class);
//    private GeometryExtractor extractor;
//    private MasterNavigator navigator;
//
//    private Vector3f pickStart = new Vector3f(5, 0, -7);
//
//    private boolean navigate = true;
//
//
//    public NavigationMeshApp() {
//        parameters.addPropertyChangeListener(new PropertyChangeListener() {
//
//            public void propertyChange(PropertyChangeEvent evt) {
//                generateNavMesh();
//            }
//        });
//
//        createUniverse();
//
//        addGrayBackgroundGridAndIndoorLights();
//
//        createPicker();
//
//        initialiseAndShowFrameWithMenuCanvasAndInspector();
//
//        chooseAndLoadFileExitIfCancelled();
//
//        moveViewBackSoTheWholeModelIsVisible();
//
//        bg.addChild(scene.getSceneGroup());
//        bg.addChild(navMeshBg);
//        bg.addChild(pathBg);
//    }
//
//    private void createUniverse() {
//        canvas3D = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
//        simpleUniverse = new SimpleUniverse(canvas3D);
//        simpleUniverse.getViewer().getView().setBackClipDistance(10000.0);
//        simpleUniverse.addBranchGraph(bg);
//    }
//
//    private void createPicker() {
//        MouseAdapter mouseAdapter = new MouseAdapter() {
//
//            @Override
//            public void mousePressed(MouseEvent e) {
//                if (navigate) {
//                    examine.processMouseEvent(e);
//                } else {
//                    pickStart = pickWorldIntersection(e);
//                    if (e.getClickCount() == 2) {
//                        if (pickStart != null) {
//                            Vec3 pickEnd = pickWorldIntersection(e);
//                            if (pickEnd != null) {
//                                findPath(pickStart, pickEnd);
//                            }
//                            pickStart = null;
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void mouseReleased(MouseEvent e) {
//                if (navigate) {
//                    examine.processMouseEvent(e);
//                }
//            }
//
//            @Override
//            public void mouseDragged(MouseEvent e) {
//                if (navigate) {
//                    examine.processMouseEvent(e);
//                } else {
//                    if (pickStart != null) {
//                        Vec3 pickEnd = pickWorldIntersection(e);
//                        if (pickEnd != null) {
//                            findPath(pickStart, pickEnd);
//                        }
//                    }
//                }
//            }
//
//            public void mouseWheelMoved(MouseWheelEvent e) {
//                examine.processMouseEvent(e);
//            }
//        };
//        canvas3D.addMouseListener(mouseAdapter);
//        canvas3D.addMouseMotionListener(mouseAdapter);
//        canvas3D.addMouseWheelListener(mouseAdapter);
//        canvas3D.addKeyListener(new KeyAdapter() {
//
//            public void keyPressed(KeyEvent e) {
//                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
//                    navigate = false;
//                }
//            }
//
//            public void keyReleased(KeyEvent e) {
//                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
//                    navigate = true;
//                }
//            }
//        });
//    }
//
//    private Vec3 pickWorldIntersection(MouseEvent e) {
//        PickCanvas pickCanvas = new PickCanvas(canvas3D, bg);
//        pickCanvas.setFlags(
//                PickInfo.NODE | PickInfo.LOCAL_TO_VWORLD | PickInfo.ALL_GEOM_INFO | PickInfo.SCENEGRAPHPATH);
//        pickCanvas.setMode(PickInfo.PICK_GEOMETRY);
//        pickCanvas.setTolerance(0.0f);
//        pickCanvas.setShapeLocation(e);
//        try {
//            PickInfo pickInfo = PickUtil.getClosestPickResultNotBackface(pickCanvas.pickAllSorted(), canvas3D.getView()).getPickInfo();
//            if (pickInfo != null) {
//                return new Vec3(PickUtil.getIntersectionWorld(pickInfo));
//            }
//        } catch (Exception ex) {
//            // ignore
//        }
//
//        return null;
//    }
//
//    private void addGrayBackgroundGridAndIndoorLights() {
//        Background background = new Background(new Color3f(Color.LIGHT_GRAY));
//        background.setApplicationBounds(SGUtils.infiniteSphere);
//        bg.addChild(SGUtils.wrapInBrancGroup(background));
//        bg.addChild(SGUtils.createIndoorLights());
//        Grid grid = new Grid(80, 80, 1f, 0f, true);
//        grid.setVisible(true);
//        bg.addChild(SGUtils.wrapInBrancGroup(grid));
//
//        examine = new ExamineBehavior(canvas3D,
//                simpleUniverse.getViewingPlatform().getMultiTransformGroup().getTransformGroup(0));
//        examine.setEnable(false);
//        bg.addChild(SGUtils.wrapInBrancGroup(examine));
//    }
//
//    private void chooseAndLoadFileExitIfCancelled() {
////        JFileChooser fileChooser = new JFileChooser();
////        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
////            File file = fileChooser.getSelectedFile();
////            scene = (VrmlScene) SGUtils.loadFile(file.getAbsolutePath());
////        } else {
////            System.exit(0);
////        }
//        scene = (VrmlScene) SGUtils.loadFile("C:\\TEMP\\models\\Stripped_HBWR\\Stripped_HBWR_no_lid.wrl");
//        SGUtils.replaceSharedGroupsWithClones(scene.getSceneGroup());
//        extractor = GeometryExtractor.extractInWorldSpace(scene.getSceneGroup());
//        generateNavMesh();
//    }
//
//    private void moveViewBackSoTheWholeModelIsVisible() {
//        TransformGroup viewTG = simpleUniverse.getViewingPlatform().getMultiTransformGroup().getTransformGroup(0);
//        BoundingSphere boundingSphere = (BoundingSphere) scene.getSceneGroup().getBounds();
//        viewTG.setTransform(new Mat4().translate(SGUtils.getCenter(boundingSphere).scale_(1)).rotateEuler(-0.4f, 0.5f, 0).translate(0, 0, (float) boundingSphere.getRadius() * 4f));
//    }
//
//    private void initialiseAndShowFrameWithMenuCanvasAndInspector() {
//        JPanel canvasWrapper = new JPanel();
//        canvasWrapper.setLayout(new BorderLayout());
//        canvasWrapper.add(canvas3D, BorderLayout.CENTER);
//        canvasWrapper.setMinimumSize(new Dimension(1, 1));
//        Split split = new Split(this, Split.Center.LEFT, canvasWrapper, parameters.createUI());
//        split.setBorderComponentSize(300);
//
//        // init JFrame with toolbar and canvas
//        add(split, BorderLayout.CENTER);
//        setDefaultCloseOperation(EXIT_ON_CLOSE);
//        setSize(1024, 768);
//        setLocationRelativeTo(null);
//        setVisible(true);
//    }
//
//    private void generateNavMesh() {
//        NavmeshGenerator generator = parameters.create();
//        final TriangleMesh triMesh = generator.build(extractor.getCoords(), extractor.getIndices(), null);
//        navMeshBg.removeAllChildren();
//
//        navMeshBg.addChild(createMeshGeometry(triMesh.vertices, triMesh.indices));
//
//        navigator = NavUtil.getNavigator(
//                triMesh.vertices, triMesh.indices,
//                5, 0.5f, 0.05f, DistanceHeuristicType.MANHATTAN,
//                1000000, 60000, 2, 20);
//
//        Vector3f start = new Vector3f(5, 0, -7);
//        Vector3f goal = new Vector3f(23, 0, -5);
//        findPath(start, goal);
//    }
//
//    private void findPath(Tuple3f start, Tuple3f goal) {
//        MasterNavRequest<Path>.NavRequest pathRequest = navigator.navigator().getPath(
//                start.x, start.y, start.z, goal.x, goal.y, goal.z);
//
//        MasterNavRequest<Vector3>.NavRequest nearest = navigator.navigator().getNearestValidLocation(5, 0, -7);
//        navigator.processAll(true);
//        System.out.println(pathRequest.data());
//        System.out.println("" + nearest.data());
//
//        Path path = pathRequest.data();
//        if (path == null) {
//            return;
//        }
////        int[] pathIndices = new int[path.pathPolyCount() * 3];
////        float[] pathVerts = new float[path.pathVertCount() * 3];
////        path.getPathPolys(pathVerts, pathIndices);
////        navMeshBg.addChild(createMeshGeometry(pathVerts, pathIndices));
//
//        List<Point3f> pathLineCoords = new ArrayList();
//        Vector3 pos = new Vector3(start.x, start.y, start.z);
//        Vector3 nextPos = new Vector3();
//        int maxIter = 100;
//        while (!pos.sloppyEquals(goal.x, goal.y, goal.z, 0.1f)) {
//            //System.out.println(pos);
//            pathLineCoords.add(new Point3f(pos.x, pos.y, pos.z));
//            path.getTarget(pos.x, pos.y, pos.z, nextPos);
//            if (pos.sloppyEquals(nextPos, 0.1f)) {
//                break;
//            }
//            pos.set(nextPos);
//            if (maxIter-- < 0) {
//                break;
//            }
//        }
//        //System.out.println("end " + pos);
//        pathLineCoords.add(new Point3f(pos.x, pos.y, pos.z));
//        pathBg.removeAllChildren();
//        if (pathLineCoords.size() > 1) {
//            Appearance lineAppearance = new Appearance();
//            lineAppearance.setLineAttributes(new LineAttributes(4, LineAttributes.PATTERN_SOLID, true));
//            lineAppearance.setColoringAttributes(new ColoringAttributes(0, 0, 0.4f, ColoringAttributes.SHADE_FLAT));
//
//            LineStripArray lineStrip = new LineStripArray(pathLineCoords.size(), LineStripArray.COORDINATES, new int[]{pathLineCoords.size()});
//            lineStrip.setCoordinates(0, pathLineCoords.toArray(new Point3f[0]));
//            pathBg.addChild(SGUtils.wrapInBrancGroup(new Shape3D(lineStrip, lineAppearance)));
//        }
//    }
//
//    public static BranchGroup createMeshGeometry(float[] vertices, int[] indices) {
//        Geometry geom = new IndexedTrianglesGeometry(vertices, indices).geom;
//        return SGUtils.wrapInBrancGroup(
//                new Shape3D(geom, new SolidTransparent()),
//                new Shape3D(geom, createLineAppearance()));
//    }
//
//    public static Appearance createLineAppearance() {
//        Appearance lineApp = new Appearance();
//        lineApp.setColoringAttributes(new ColoringAttributes(0.4f, 0.4f, 0.4f, ColoringAttributes.NICEST));
//        lineApp.setPolygonAttributes(new PolygonAttributes(PolygonAttributes.POLYGON_LINE, PolygonAttributes.CULL_NONE, 0f));
//        return lineApp;
//    }
//
//    public static void main(String[] args) {
//        new NavigationMeshApp();
//    }
}
