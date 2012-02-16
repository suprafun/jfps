package trb.fps.bmfont;

public class FloatList {

	private float[] data = new float[16];
	private int size = 0;

	public int size() {
		return size;
	}

	public FloatList add(float... floats) {
		ensureCapacity(size + floats.length);
		System.arraycopy(floats, 0, data, size, floats.length);
		size += floats.length;
		return this;
	}

	private void ensureCapacity(int newCapacity) {
		if (newCapacity > data.length) {
			float[] newData = new float[Math.max(data.length*2, newCapacity)];
			System.arraycopy(data, 0, newData, 0, data.length);
			data = newData;
		}
	}

	public float[] get() {
		float[] copy = new float[size];
		System.arraycopy(data, 0, copy, 0, size);
		return copy;
	}
}
