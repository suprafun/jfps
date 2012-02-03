package trb.fps;

import trb.fps.predict.TimedInput;

public class Input implements TimedInput {

    public final long time;
    public final long serverTime;
	public final int moveX;
	public final int moveY;
	public final int mouseDx;
	public final int mouseDy;
	public final boolean fire;
	public final boolean jump;

    public Input() {
        time = 0;
        serverTime = 0;
        moveX = 0;
        moveY = 0;
        mouseDx = 0;
        mouseDy = 0;
        fire = false;
        jump = false;
    }

	public Input(long time, long serverTime, int moveX, int moveY
            , int mouseDx, int mouseDy, boolean fire, boolean jump) {
        this.time = time;
        this.serverTime = serverTime;
        this.moveX = moveX;
        this.moveY = moveY;
        this.mouseDx = mouseDx;
        this.mouseDy = mouseDy;
        this.fire = fire;
        this.jump = jump;
	}

    public long getTime() {
        return time;
    }

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
        str.append("time=").append(time);
        str.append(" serverTime=").append(serverTime);
        str.append(" moveX=").append(moveX).append(" moveY=").append(moveY);
		if (fire) {
			str.append(" fire");
		}
        if (jump) {
            str.append(" jump");
        }
		str.append(" dx=").append(mouseDx).append(" dy=").append(mouseDy);
		return str.toString();
	}
}
