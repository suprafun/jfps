package trb.fps.server;

import trb.fps.entity.EntityList;
import trb.fps.entity.Powerup;
import trb.fps.entity.Transform;
import trb.fps.net.PlayerPacket;
import trb.fps.util.TwoWayMap;
import trb.jsg.util.Vec3;

public class PowerupManager {

	private final TwoWayMap<Powerup, Integer> powerupIndexes = new TwoWayMap();

	public PowerupManager(EntityList entityList) {
		int idx = 0;
		for (Powerup powerup : entityList.getComponents(Powerup.class)) {
			powerupIndexes.add(powerup, idx++);
		}
	}

	public int size() {
		return powerupIndexes.size();
	}

	public Powerup getPowerup(int i) {
		return powerupIndexes.getBackward(i);
	}

	public PlayerPacket pickup(PlayerPacket player, long[] powerupsPickupTime, long time) {
		for (int i = 0; i < powerupsPickupTime.length; i++) {
			Powerup powerup = getPowerup(i);
			if (powerupsPickupTime[i] < time - 5000) {
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
