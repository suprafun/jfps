package trb.fps.net;

import java.util.ArrayList;
import java.util.List;

public class LevelPacket {

	public static final int MAX_PLAYERS = 4;
    
    public long serverTimeMillis = 0l;
    public int killLimit = 5;
    public PlayerPacket[] players = new PlayerPacket[MAX_PLAYERS];
	public List<BulletPacket> bullets = new ArrayList();
	public long[] powerupsPickupTime = {};

	public LevelPacket() {
        for (int i = 0; i < players.length; i++) {
            players[i] = new PlayerPacket(-1, i, "");
        }
	}

    public PlayerPacket getPlayer(int id) {
        for (int i=0; i<players.length; i++) {
            if (players[i].getId() == id) {
                return players[i];
            }
        }
        return null;
    }

    public void setPlayer(int id, PlayerPacket player) {
        for (int i = 0; i < players.length; i++) {
            if (players[i].getId() == id) {
                players[i] = player;
                return;
            }
        }
    }

    public int getPlayerIndex(int id) {
        for (int i = 0; i < players.length; i++) {
            if (players[i].getId() == id) {
                return i;
            }
        }
        return -1;
    }

    public List<PlayerPacket> getConnectedPlayers() {
        List<PlayerPacket> p = new ArrayList();
        for (int i=0; i<players.length; i++) {
            if (players[i].isConnected()) {
                p.add(players[i]);
            }
        }
        return p;
    }

    public boolean isGameOver() {
        for (PlayerPacket p : getConnectedPlayers()) {
            if (p.getKills() >= killLimit) {
                return true;
            }
        }
        return false;
    }
}
