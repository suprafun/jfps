package trb.fps.net;

import trb.jsg.util.Vec3;

public class BulletPacket {

    public boolean alive = false;
    public int shooterPlayerId = 0;
    public int bulletType = 0;
    public float[] startPosition = new float[3];
    public float[] startDirection = new float[3];
    public long spawnTime = 0;
    //public Vec3 hitPosition = new Vec3();
    //public long hitTime = 0;

    public void setStartPosition(Vec3 p) {
        System.arraycopy(p.toFloats(), 0, startPosition, 0, 3);
    }

    public void setStartDirection(Vec3 p) {
        System.arraycopy(p.toFloats(), 0, startDirection, 0, 3);
    }
}
