package trb.fps.server;

import trb.fps.entity.Powerup;
import trb.fps.entity.Transform;
import trb.fps.net.PlayerPacket;
import trb.jsg.util.Vec3;

public class PowerupManager {

    public static final long HIDE_TIME = 5000;

	public final PowerupMap powerupMap;

	public PowerupManager(PowerupMap powerupMap) {
        this.powerupMap = powerupMap;
	}

	public PlayerPacket pickup(PlayerPacket player, long[] powerupsPickupTime, long time) {
		for (int i = 0; i < powerupsPickupTime.length; i++) {
			Powerup powerup = powerupMap.getPowerup(i);
			if (powerupsPickupTime[i] < time - HIDE_TIME) {
				Vec3 powerupPos = powerup.getComponent(Transform.class).get().getTranslation();
				float distance = powerupPos.distance(player.getPosition());
				if (distance < 2) {
					player = apply(powerup.type.get(), player);
					powerupsPickupTime[i] = time;
				}
			}
		}

		return player;
	}

	private PlayerPacket apply(Powerup.Type type, PlayerPacket player) {
		switch (type) {
			case AMMO_10:
				player.ammo += 10;
				return player;
			case HEALTH_10:
				System.out.println("apply " + type);
				return player.setHealth(player.getHealth() + 10);
		}
		return player;
	}
}
