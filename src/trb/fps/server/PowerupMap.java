package trb.fps.server;

import trb.fps.entity.EntityList;
import trb.fps.entity.Powerup;
import trb.fps.util.TwoWayMap;

public class PowerupMap {
    private final TwoWayMap<Powerup, Integer> powerupIndexes = new TwoWayMap();

    public PowerupMap(EntityList entityList) {
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

    public int getIndex(Powerup powerup) {
        return powerupIndexes.getForward(powerup);
    }
}
