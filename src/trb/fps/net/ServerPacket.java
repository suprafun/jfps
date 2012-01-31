package trb.fps.net;

import trb.fps.predict.TimedInput;
import trb.fps.predict.TimedState;

public class ServerPacket implements TimedState<ServerPacket, TimedInput> {

    public long serverTime = 0;
    public long time = System.currentTimeMillis();

    public ServerPacket() {

    }

    public ServerPacket(long serverTime) {
        this.serverTime = serverTime;
    }

    public ServerPacket(ServerPacket other) {
        this.time = other.time;
        this.serverTime = other.serverTime;
    }

    /** TimedState */
    public ServerPacket setTime(long time) {
        ServerPacket serverData = new ServerPacket(this);
        serverData.time = time;
        return serverData;
    }

    /** TimedState */
    public long getTime() {
        return time;
    }

    /** TimedState */
    public boolean withinPredictThreshold(ServerPacket state) {
        return Math.abs(serverTime - state.serverTime) < 100;
    }

    /** TimedState */
    public ServerPacket update(TimedInput serverUpdator) {
        throw new RuntimeException();
    }

    /** TimedState */
    public ServerPacket interpolate(float t, ServerPacket s2) {
        ServerPacket d = new ServerPacket(s2);
        d.time = (long) (time + t * (s2.time - time));
        d.serverTime = (long) (serverTime + t * (s2.serverTime - serverTime));
        return d;
    }

}
