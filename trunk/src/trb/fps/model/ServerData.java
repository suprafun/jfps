package trb.fps.model;

import trb.fps.predict.TimedInput;
import trb.fps.predict.TimedState;

public class ServerData implements TimedState<ServerData, TimedInput> {

    public long serverTime = 0;
    public long time = System.currentTimeMillis();

    public ServerData() {

    }

    public ServerData(long serverTime) {
        this.serverTime = serverTime;
    }

    public ServerData(ServerData other) {
        this.time = other.time;
        this.serverTime = other.serverTime;
    }

    /** TimedState */
    public ServerData setTime(long time) {
        ServerData serverData = new ServerData(this);
        serverData.time = time;
        return serverData;
    }

    /** TimedState */
    public long getTime() {
        return time;
    }

    /** TimedState */
    public boolean withinPredictThreshold(ServerData state) {
        return Math.abs(serverTime - state.serverTime) < 100;
    }

    /** TimedState */
    public ServerData update(TimedInput serverUpdator) {
        throw new RuntimeException();
    }

    /** TimedState */
    public ServerData interpolate(float t, ServerData s2) {
        ServerData d = new ServerData(s2);
        d.time = (long) (time + t * (s2.time - time));
        d.serverTime = (long) (serverTime + t * (s2.serverTime - serverTime));
        return d;
    }

}
