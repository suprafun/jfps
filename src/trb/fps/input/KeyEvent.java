package trb.fps.input;

public class KeyEvent {

    public final boolean state;
    public final int key;

    public KeyEvent(boolean state, int key) {
        this.state = state;
        this.key = key;
    }
}
