package trb.trials4k;



public class Intersection {

	public boolean foundCollision = false;
	public Tuple closest = new Tuple();
	public Tuple normal = new Tuple(0, 1);
	public float closestDistance = 0;
}
