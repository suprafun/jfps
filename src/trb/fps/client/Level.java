package trb.fps.client;

import java.util.ArrayList;
import java.util.List;
import trb.fps.PlayerUpdator;
import trb.fps.editor.EditorNavigation;
import trb.fps.entity.EntityList;
import trb.fps.net.LevelPacket;
import trb.fps.net.PlayerPacket;
import trb.fps.net.ServerPacket;
import trb.fps.physics.PhysicsLevel;
import trb.fps.predict.DelayedInterpolatedState;
import trb.fps.predict.PredictedState;

public class Level {
    public LevelPacket levelData = new LevelPacket();
    public PhysicsLevel physicsLevel = new PhysicsLevel(new EntityList());
    public PredictedState<PlayerPacket, PlayerUpdator> predictedState;
    public List<DelayedInterpolatedState<PlayerPacket>> interpolatedState = new ArrayList();
    public DelayedInterpolatedState<ServerPacket> interpolatedServerState =
            new DelayedInterpolatedState<ServerPacket>(new ServerPacket());
    public EditorNavigation editorNavigation = new EditorNavigation();

    public Level() {
        PlayerPacket initialState = new PlayerPacket();
        predictedState = new PredictedState(initialState);
        for (int i = 0; i < LevelPacket.MAX_PLAYERS; i++) {
            interpolatedState.add(new DelayedInterpolatedState<PlayerPacket>(initialState));
        }
    }

    public void changeLevel(EntityList entities) {
        physicsLevel = new PhysicsLevel(entities);
    }
}
