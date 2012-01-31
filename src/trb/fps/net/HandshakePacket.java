package trb.fps.net;

public class HandshakePacket {

	public final String name;
	public final int playerId;

	public HandshakePacket() {
		name = "noname";
		playerId = -10;
	}

	public HandshakePacket(String name, int playerIdx) {
		this.name = name;
		this.playerId = playerIdx;
	}

	@Override
	public String toString() {
		return "Handshake " + name + " " + playerId;
	}
}
