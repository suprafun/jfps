package trb.fps;

public class Handshake {

	public final String name;
	public final int playerIdx;

	public Handshake() {
		name = "noname";
		playerIdx = -10;
	}

	public Handshake(String name, int playerIdx) {
		this.name = name;
		this.playerIdx = playerIdx;
	}

	@Override
	public String toString() {
		return "Handshake " + name + " " + playerIdx;
	}
}
