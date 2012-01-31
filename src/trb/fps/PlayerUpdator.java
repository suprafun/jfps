package trb.fps;

import trb.fps.net.PlayerPacket;
import trb.fps.physics.PhysicsLevel;
import trb.fps.predict.TimedInput;
import trb.jsg.util.Vec3;

public class PlayerUpdator implements TimedInput {

    public Input input;
    private final PhysicsLevel physicsLevel;

    public PlayerUpdator(Input input, PhysicsLevel physicsLevel) {
        this.input = input;
        this.physicsLevel = physicsLevel;
    }

    public long getTime() {
        return input.getTime();
    }

    public PlayerPacket update(PlayerPacket player) {
        long deltaTime = getTime() - player.getTime();
        if (deltaTime <= 0 || player.getHealth() <= 0) {
            return player.setTime(getTime());
        }
        if (deltaTime > 1000) {
            deltaTime = 1000;
        }
        PlayerPacket tempPlayer = player.setTime(getTime());
        tempPlayer.rotateAndMove(deltaTime, input.mouseDx, input.mouseDy, input.moveX, input.moveY);
        Vec3 characterPos = physicsLevel.move(player.getPosition(), tempPlayer.getPosition());
        return tempPlayer.setPosition(characterPos);
    }
}
