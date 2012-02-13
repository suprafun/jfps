package trb.fps;

import trb.fps.net.LevelPacket;
import trb.fps.net.PlayerPacket;
import trb.fps.physics.PhysicsLevel;
import trb.fps.predict.TimedInput;

public class PlayerUpdator implements TimedInput {

    public Input input;
    private final PhysicsLevel physicsLevel;
	private final LevelPacket level;

    public PlayerUpdator(Input input, PhysicsLevel physicsLevel, LevelPacket level) {
        this.input = input;
        this.physicsLevel = physicsLevel;
		this.level = level;
    }

    public long getTime() {
        return input.getTime();
    }

    public PlayerPacket update(PlayerPacket player) {
        long deltaTime = getTime() - player.getTime();
        if (deltaTime <= 0 || player.getHealth() <= 0 || level.isGameOver()) {
            return player.setTime(getTime());
        }
        if (deltaTime > 1000) {
            deltaTime = 1000;
        }
        PlayerPacket tempPlayer = player.setTime(getTime());
        tempPlayer.rotateAndMove(deltaTime, input.headingRad, input.tiltRad, input.moveX, input.moveY);
        return physicsLevel.move(player, tempPlayer, input.jump);
    }
}
