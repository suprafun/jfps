package trb.trials4k;



import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author admin
 */
public class LineArray {

	public List<Tuple> points = new ArrayList();

	public LineArray() {

	}

	public LineArray(Tuple t1, Tuple t2) {
		points.add(t1);
		points.add(t2);
	}

	public List<Line> createLines() {
		List<Line> lines = new ArrayList();
		Tuple prev = null;
		for (Tuple t : points) {
			if (prev != null) {
				lines.add(new Line(prev, t));
			}
			prev = t;
		}
		return lines;
	}
}
