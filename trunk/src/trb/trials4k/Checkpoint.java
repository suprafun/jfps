package trb.trials4k;



/**
 *
 * @author admin
 */
public class Checkpoint {
	public Tuple position = new Tuple();
	public boolean passed = false;

	public Checkpoint() {
	}

	public Checkpoint(float x, float y) {
		position.set(x, y);
	}

    public Checkpoint(Tuple t) {
        position.set(t);
    }
}
