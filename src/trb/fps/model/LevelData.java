package trb.fps.model;

public class LevelData {

	public static final int MAX_PLAYERS = 4;
    public static final int MAX_BULLETS = MAX_PLAYERS * 4;
    
    public long serverTimeMillis = 0l;
    public PlayerData[] players = new PlayerData[MAX_PLAYERS];
    public BulletData[] bullets = new BulletData[MAX_BULLETS];

	public LevelData() {
        for (int i = 0; i < players.length; i++) {
            players[i] = new PlayerData(i);
        }
        for (int i = 0; i < bullets.length; i++) {
            bullets[i] = new BulletData();
        }
	}
}
