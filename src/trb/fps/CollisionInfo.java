package trb.fps;

import trb.jsg.util.Vec3;

public class CollisionInfo {

    public enum Type {None, Player, World};

    public Type type = Type.None;
    public Vec3 intersection = new Vec3();
    public int playerId = -1;

    public CollisionInfo() {

    }

    public CollisionInfo(Type type, Vec3 intersection, int playerId) {
        this.type = type;
        this.intersection.set(intersection);
        this.playerId = playerId;
    }
}
