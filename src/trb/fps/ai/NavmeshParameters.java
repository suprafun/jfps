package trb.fps.ai;

//import javax.swing.BorderFactory;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import org.critterai.nmgen.NavmeshGenerator;
import trb.fps.property.Property;
import trb.fps.property.PropertyListPanel;
import trb.fps.property.PropertyOwner;

public class NavmeshParameters extends PropertyOwner {
	public final Property<Boolean> visualise = add("Visualise", true);
    public final Property<Float> cellSize = add("CellSize", 0.1f);
    public final Property<Float> cellHeight = add("CellHeight", 0.1f);
    public final Property<Float> minTraversableHeight = add("MinTraversableHeight", 1.7f);
    public final Property<Float> maxTraversableStep = add("MaxTraversableStep", 0.3f);
    public final Property<Float> maxTraversableSlope = add("MaxTraversableSlope", 45f);
    public final Property<Boolean> clipLedges = add("ClipLedges", true);
    public final Property<Float> traversableAreaBorderSize = add("TraversableAreaBorderSize", 0.5f);
    public final Property<Integer> smoothingTreshold = add("SmoothingTreshold", 0);
    public final Property<Boolean> useConservativeExpansion = add("UseConservativeExpansion", false);
    public final Property<Integer> minUnconnectedRegionSize = add("MinUnconnectedRegionSize", 5000);
    public final Property<Integer> mergeRegionSize = add("MergeRegionSize", 5);
    public final Property<Float> maxEdgeLength = add("MaxEdgeLength", 4f);
    public final Property<Float> edgeMaxDeviation = add("EdgeMaxDeviation", 0.1f);
    public final Property<Integer> maxVertsPerPoly = add("MaxVertsPerPoly", 6);
    public final Property<Float> contourSampleDistance = add("ContourSampleDistance", 0.5f);//0.2
    public final Property<Float> contourMaxDeviation = add("ContourMaxDeviation", 0.1f);

    public NavmeshGenerator create() {
        return new NavmeshGenerator(
                cellSize.get(),
                cellHeight.get(),
                minTraversableHeight.get(),
                maxTraversableStep.get(),
                maxTraversableSlope.get(),
                clipLedges.get(),
                traversableAreaBorderSize.get(),
                smoothingTreshold.get(),
                useConservativeExpansion.get(),
                minUnconnectedRegionSize.get(),
                mergeRegionSize.get(),
                maxEdgeLength.get(),
                edgeMaxDeviation.get(),
                maxVertsPerPoly.get(),
                contourSampleDistance.get(),
                contourMaxDeviation.get());
    }

    public JComponent createUI() {
        return new PropertyListPanel(properties).get();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new NavmeshParameters().createUI(), BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }
}
