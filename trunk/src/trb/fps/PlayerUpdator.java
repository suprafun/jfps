package trb.fps;

import trb.fps.model.PlayerData;
import trb.fps.physics.KinematicCharacter;
import trb.fps.physics.PhysicsLevel;
import trb.fps.predict.TimedInput;
import trb.jsg.util.Vec3;

public class PlayerUpdator implements TimedInput {

    public Input input;
    public KinematicCharacter character;
    private PhysicsLevel physicsLevel;

    public PlayerUpdator(Input input, KinematicCharacter character, PhysicsLevel physicsLevel) {
        this.input = input;
        this.character = character;
        this.physicsLevel = physicsLevel;
    }

    public long getTime() {
        return input.getTime();
    }

    public PlayerData update(PlayerData player) {
        long deltaTime = getTime() - player.getTime();
        if (deltaTime <= 0 || player.getHealth() <= 0) {
            return player.setTime(getTime());
        }
        if (deltaTime > 1000) {
            System.out.println("CCCCCCCCCCCCCCCC " + deltaTime);
            deltaTime = 1000;
        }
        PlayerData tempPlayer = player.setTime(getTime());
        tempPlayer.rotateAndMove(deltaTime, input.mouseDx, input.mouseDy, input.moveX, input.moveY);
        Vec3 characterPos = PhysicsLevel.move(player.getPosition(), tempPlayer.getPosition());
        return tempPlayer.setPosition(characterPos);
    }
}
