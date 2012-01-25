package trb.fps.input;

public class MouseEvent {
    public final boolean state;
    public final int button;

    public MouseEvent(boolean state, int button) {
        this.state = state;
        this.button = button;
    }
}
