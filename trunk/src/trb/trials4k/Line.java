package trb.trials4k;



public class Line {
	Tuple v1;
	Tuple v2;
	
	public Line(Tuple v1, Tuple v2) {
		this.v1 = v1;
		this.v2 = v2;
	}
	
	public Line(float x1, float y1, float x2, float y2) {
		v1 = new Tuple(x1, y1);
		v2 = new Tuple(x2, y2);
	}
	
	public Tuple normal() {
		return new Tuple(v2).sub(v1).normalize().cross();
	}
	
	public float distance(Tuple t) {
		Tuple v1t = new Tuple(t).sub(v1);
		return normal().dot(v1t);
	}
}
