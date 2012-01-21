package trb.fps;

public interface FpsRenderer {
    public void init(Level level);
    public void render(Level level, int localPlayerIdx);
}
