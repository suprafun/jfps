package trb.fps.ai;

//import javax.swing.BorderFactory;
//import javax.swing.JComponent;
//import javax.swing.JPanel;
//import javax.swing.JScrollPane;
//import net.miginfocom.swing.MigLayout;
//import no.hrp.hvrc.property.DefaultPropertyOwner;
//import no.hrp.hvrc.property.Property;
//import no.hrp.hvrc.property.editor.SetDirectly;
//import no.hrp.hvrc.utils.swing.collapse.CollapsableHeader;
//import no.hrp.hvrc.utils.swing.collapse.CollapsablePane;
//import org.critterai.nmgen.NavmeshGenerator;

public class NavmeshParameters /*extends DefaultPropertyOwner*/ {
//    public final Property<Float> cellSize = addObjectProperty("CellSize", 0.1f);
//    public final Property<Float> cellHeight = addObjectProperty("CellHeight", 0.1f);
//    public final Property<Float> minTraversableHeight = addObjectProperty("MinTraversableHeight", 1.7f);
//    public final Property<Float> maxTraversableStep = addObjectProperty("MaxTraversableStep", 0.3f);
//    public final Property<Float> maxTraversableSlope = addObjectProperty("MaxTraversableSlope", 45f);
//    public final Property<Boolean> clipLedges = addObjectProperty("ClipLedges", true);
//    public final Property<Float> traversableAreaBorderSize = addObjectProperty("TraversableAreaBorderSize", 0.1f);
//    public final Property<Integer> smoothingTreshold = addObjectProperty("SmoothingTreshold", 0);
//    public final Property<Boolean> useConservativeExpansion = addObjectProperty("UseConservativeExpansion", false);
//    public final Property<Integer> minUnconnectedRegionSize = addObjectProperty("MinUnconnectedRegionSize", 5);
//    public final Property<Integer> mergeRegionSize = addObjectProperty("MergeRegionSize", 5);
//    public final Property<Float> maxEdgeLength = addObjectProperty("MaxEdgeLength", 1f);
//    public final Property<Float> edgeMaxDeviation = addObjectProperty("EdgeMaxDeviation", 0.1f);
//    public final Property<Integer> maxVertsPerPoly = addObjectProperty("MaxVertsPerPoly", 6);
//    public final Property<Float> contourSampleDistance = addObjectProperty("ContourSampleDistance", 0.2f);
//    public final Property<Float> contourMaxDeviation = addObjectProperty("ContourMaxDeviation", 0.1f);
//
//    public NavmeshGenerator create() {
//        return new NavmeshGenerator(
//                cellSize.get(),
//                cellHeight.get(),
//                minTraversableHeight.get(),
//                maxTraversableStep.get(),
//                maxTraversableSlope.get(),
//                clipLedges.get(),
//                traversableAreaBorderSize.get(),
//                smoothingTreshold.get(),
//                useConservativeExpansion.get(),
//                minUnconnectedRegionSize.get(),
//                mergeRegionSize.get(),
//                maxEdgeLength.get(),
//                edgeMaxDeviation.get(),
//                maxVertsPerPoly.get(),
//                contourSampleDistance.get(),
//                contourMaxDeviation.get());
//    }
//
//    public JComponent createUI() {
//        JPanel panel = new JPanel();
//        panel.setLayout(new MigLayout("fill", "[grow]", ""));
//        for (Property p : this) {
//            JComponent c = p.createEditor(new SetDirectly()).getEditorUI();
//            c.setBorder(BorderFactory.createEmptyBorder());
//            panel.add(c, "growx, wrap");
//        }
//        JScrollPane inspector = new JScrollPane(new CollapsablePane(new CollapsableHeader(toString()), panel, false));
//        inspector.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
//        inspector.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//        return inspector;
//    }
}
