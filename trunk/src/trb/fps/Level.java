/*
 * HALDEN VR PLATFORM
 *
 * RADIATION MODULE
 *
 * $RCSfile: $
 *
 * Author :
 * Date   :
 * Version: $Revision: $ ($Date: $)
 *
 * (c) 2000-2011 Halden Virtual Reality Centre <http://www.ife.no/vr/>,
 * Institutt for energiteknikk. All rights reserved.
 *
 * This code is the property of Halden VR Centre <vr-info@hrp.no> and may
 * only be used in accordance with the terms of the license agreement
 * granted.
 */

package trb.fps;

import java.util.ArrayList;
import java.util.List;
import trb.fps.model.LevelData;
import trb.fps.model.PlayerData;
import trb.fps.model.ServerData;
import trb.fps.physics.KinematicCharacter;
import trb.fps.physics.PhysicsLevel;
import trb.fps.predict.DelayedInterpolatedState;
import trb.fps.predict.PredictedState;

/**
 *
 * @author tomrbryn
 */
public class Level {
    public LevelData levelData = new LevelData();
    public PhysicsLevel physicsLevel = new PhysicsLevel();
    public KinematicCharacter character;
    public PredictedState<PlayerData, PlayerUpdator> predictedState;
    public List<DelayedInterpolatedState<PlayerData>> interpolatedState = new ArrayList();
    public DelayedInterpolatedState<ServerData> interpolatedServerState =
            new DelayedInterpolatedState<ServerData>(new ServerData());

    public Level() {
        character = physicsLevel.addCharacter();
        PlayerData initialState = new PlayerData();
        predictedState = new PredictedState(initialState);
        for (int i = 0; i < LevelData.MAX_PLAYERS; i++) {
            interpolatedState.add(new DelayedInterpolatedState<PlayerData>(initialState));
        }
    }
}
