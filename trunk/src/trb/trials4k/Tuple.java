package trb.trials4k;



public class Tuple {

	public float x;
	public float y;
	
	public Tuple() {
		
	}
	
	public Tuple(float x, float y) {
		set(x, y);
	}
	
	public Tuple(Tuple t) {
		this.x = t.x;
		this.y = t.y;
	}

	public Tuple set(Tuple t) {
		set(t.x, t.y);
		return this;
	}
	
	public Tuple set(float x, float y) {
		this.x = x;
		this.y = y;
		return this;
	}
	
	public float length() {
		return (float) Math.hypot(x, y);
	}
	
	public float distance(Tuple t) {
		return (float) Math.hypot(t.x-x, t.y-y);
	}
	
	public Tuple normalize() {
		if (x != 0 || y != 0) {
			float length = length();
			x /= length;
			y /= length;
		}
		return this;
	}
	
	public Tuple add(Tuple t) {
		x += t.x;
		y += t.y;
		return this;
	}

	public Tuple add(float addx, float addy) {
		x += addx;
		y += addy;
		return this;
	}

	public Tuple sub(Tuple t) {
		x -= t.x;
		y -= t.y;
		return this;
	}
	
	public Tuple cross() {
		float oldx = x;
		x = -y;
		y = oldx;
		return this;
	}
	
	public float dot(Tuple t) {
		return x*t.x + y*t.y;
	}
	
	public Tuple scale(float amount) {
		x *= amount;
		y *= amount;
		return this;
	}
	
	public Tuple interpolate(Tuple target, float amount) {
		x = x * (1-amount) + target.x * amount;
		y = y * (1-amount) + target.y * amount;
		return this;
	}

	public float angle() {
		Tuple dir = new Tuple(this).normalize();
		return (float) Math.atan2(dir.y, dir.x);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tuple other = (Tuple) obj;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
			return false;
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return x + " " + y;
	}
	
	public static void main(String[] args) {
		System.out.println("" + new Tuple(1, 0).dot(new Tuple(0, 1)));
	}
}
