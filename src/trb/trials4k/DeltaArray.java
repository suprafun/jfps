package trb.trials4k;

import java.util.ArrayList;
import java.util.List;

public class DeltaArray {

	public static final float TO_RAD = (float) Math.PI / 0x7f;
	public static final float LENGTH_SCALE = 8f;
	public Tuple start = new Tuple();
	public List<Delta> points = new ArrayList();

	public DeltaArray() {
	}

	public DeltaArray(Tuple start, Delta delta) {
		this.start.set(start);
		points.add(delta);
	}

	public List<Line> createLines() {
		List<Line> lines = new ArrayList();
		Tuple prev = start;
		float prevAngle = 0f;
		for (Delta delta : points) {
			Tuple t = new Tuple(prev);
			prevAngle += delta.getAngleRad();
			float length = delta.getLength();
			t.x += Math.cos(prevAngle) * length;
			t.y += Math.sin(prevAngle) * length;
			lines.add(new Line(prev, t));
			prev = t;
		}
		return lines;
	}

	public Tuple getTuple(int idx) {
		if (idx == 0) {
			return start;
		}
		Tuple prev = start;
		float prevAngle = 0f;
		for (Delta delta : points) {
			Tuple t = new Tuple(prev);
			prevAngle += delta.getAngleRad();
			float length = delta.getLength();
			t.x += Math.cos(prevAngle) * length;
			t.y += Math.sin(prevAngle) * length;
			idx--;
			if (idx == 0) {
				return t;
			}
			prev = t;
		}

		Thread.dumpStack();
		return new Tuple();
	}

	public static float toLength(int lengthAngle) {
		return (float) Math.pow(1.33f, ((lengthAngle >> 5) & 7) + 17);
//		return 8 << ((lengthAngle >> 5) & 7);
	}

	public static float toRad(int lengthAngle) {
//		return (lengthAngle & 0x1f) * (float) Math.PI * 2.0 / (0x1f + 1);
		return (lengthAngle & 0x1f) * (float) Math.PI / (0x1f - 5) - ((float) Math.PI / 2);
	}

	public static class Delta {
		byte lengthAngle;

		public Delta(int lengthAngle) {
			this.lengthAngle = (byte) lengthAngle;
		}

		float getAngleRad() {
			return toRad(lengthAngle);
		}

		float getLength() {
			return toLength(lengthAngle);
		}

		public void add(int dx, int dy) {
			int angle = ((lengthAngle & 0x1f) + dy) & 0x1f;
			int length = (((lengthAngle >>> 5) + dx) & 7) << 5;
			lengthAngle = (byte) (angle | length);
		}
	}

	public static void main(String[] args) {
		Delta delta = new Delta(0);
		System.out.println(Math.toDegrees(delta.getAngleRad()) + " "+ delta.getLength());
		for (int i = 0; i < 33; i++) {
			delta.add(0, 1);
			System.out.println(Math.toDegrees(delta.getAngleRad()) + " " + delta.getLength());
		}
		System.out.println("-------");
		for (int i = 0; i < 16; i++) {
			delta.add(1, 0);
			System.out.println(Math.toDegrees(delta.getAngleRad()) + " " + delta.getLength());
		}
		for (int i = 0; i < 8; i++) {
			System.out.println(Math.pow(1.33f, i+17));
		}
	}
}
