package trb.trials4k;



/**
 *
 * @author admin
 */
public class Circle {
	public Tuple center = new Tuple();
	public float radius = 20;

	public Circle() {
		
	}

	public Circle(float x, float y, float r) {
		center.x = x;
		center.y = y;
		radius = r;
	}
}
