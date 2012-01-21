package trb.fps.model;

import trb.fps.PlayerUpdator;
import trb.fps.predict.TimedState;
import trb.jsg.util.Mat4;
import trb.jsg.util.Vec3;

public class PlayerData implements TimedState<PlayerData, PlayerUpdator> {

    private static final float SPEED = 10f;

    private boolean connected = false;
    private int index = -1;
    private long time = 0;
    private String name = "";
    private float[] position = {0, 0, 0};
    private float headingRad = 0f;
    private float tiltRad = 0f;
    private int health = 0;
    private int kills = 0;
    private int deaths = 0;

    public PlayerData() {

    }

    public PlayerData(PlayerData copy) {
        this.connected = copy.connected;
        this.index = copy.index;
        this.time = copy.time;
        this.name = copy.name;
        this.position = copy.position;
        this.headingRad = copy.headingRad;
        this.tiltRad = copy.tiltRad;
        this.health = copy.health;
        this.kills = copy.kills;
        this.deaths = copy.deaths;
    }

    public PlayerData(int index) {
        this.index = index;
    }

    public boolean isConnected() {
        return connected;
    }

    public PlayerData setConnected(boolean connected) {
        PlayerData d = new PlayerData(this);
        d.connected = connected;
        return d;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public Vec3 getPosition() {
        return new Vec3(position);
    }

    public PlayerData setPosition(Vec3 translation) {
        PlayerData d = new PlayerData(this);
        d.position = translation.toFloats();
        return d;
    }

    public int getHealth() {
        return health;
    }

    public PlayerData takeDamage(int damage) {
        PlayerData d = new PlayerData(this);
        d.health -= damage;
        return d;
    }

    public int getKills() {
        return kills;
    }

    public PlayerData incKills() {
        PlayerData d = new PlayerData(this);
        d.kills++;
        return d;
    }

    public int getDeaths() {
        return deaths;
    }

    public PlayerData incDeaths() {
        PlayerData d = new PlayerData(this);
        d.deaths++;
        return d;
    }

    public void rotateAndMove(long timeDeltaMillis, int mouseDx, int mouseDy, int x, int z) {
        headingRad -= mouseDx * (float) Math.toRadians(360.0 / 400.0);
        tiltRad += mouseDy * (float) Math.toRadians(360.0 / 400.0);
        tiltRad = (float) Math.max(-Math.PI/3, Math.min(Math.PI/3, tiltRad));
        Vec3 headingVec = getHeadingVector();
        Vec3 rightVec = new Vec3().cross_(headingVec, new Vec3(0, 1, 0));
        headingVec.scale(z * SPEED * timeDeltaMillis / 1000f);
        rightVec.scale(x * SPEED * timeDeltaMillis / 1000f);
        Vec3 positionVec = new Vec3(position);
        positionVec.add(headingVec);
        positionVec.add(rightVec);
        position = positionVec.toFloats();
    }

    public Vec3 getHeadingVector() {
        return new Vec3(Math.sin(headingRad), 0, Math.cos(headingRad)).scale_(-1f);
    }

    public Mat4 getTransform() {
        return new Mat4().setTranslation_(new Vec3(position)).setEuler(new Vec3(tiltRad, headingRad, 0));
    }

    public Mat4 getViewTransform() {
        Mat4 viewMat = new Mat4();
        viewMat.setTranslation_(getPosition().add(0, 1.6f, 0));
        viewMat.setEuler(new Vec3(tiltRad, headingRad, 0f));
        viewMat.invert();
        return viewMat;
    }

    public Mat4 getModelTransform() {
        return new Mat4().setTranslation_(getPosition()).setEuler(new Vec3(0, headingRad, 0));
    }

    public void init(String name) {
        this.name = name;
        connected = true;
        kills = 0;
        deaths = 0;
        health = 0;
    }

    public void respawn() {
        position[0] = -50 + (float) Math.random() * 100f;
        position[2] = -50 + (float) Math.random() * 100f;
        headingRad = 0f;
        tiltRad = 0f;
        health = 100;
    }

    /** TimedState */
    public PlayerData setTime(long time) {
        PlayerData d = new PlayerData(this);
        d.time = time;
        return d;
    }

    /** TimedState */
    public long getTime() {
        return time;
    }

    /** TimedState */
    public boolean withinPredictThreshold(PlayerData state) {
        return getPosition().distance(state.getPosition()) < 0.2f;
    }

    /** TimedState */
    public PlayerData update(PlayerUpdator playerUpdator) {
        return playerUpdator.update(this);
    }

    /** TimedState */
    public PlayerData interpolate(float t, PlayerData s2) {
        PlayerData d = new PlayerData(s2);
        d.time = (long) (time + t * (s2.time - time));
        d.position = getPosition().interpolate_(s2.getPosition(), t).toFloats();
        return d;
    }
}
