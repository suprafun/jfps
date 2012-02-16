package trb.fps.bmfont;

public class IntList {

	private int[] data = new int[16];
	private int size = 0;

	public int size() {
		return size;
	}

	public IntList add(int... ints) {
		ensureCapacity(size + ints.length);
		System.arraycopy(ints, 0, data, size, ints.length);
		size += ints.length;
		return this;
	}

	private void ensureCapacity(int newCapacity) {
		if (newCapacity > data.length) {
			int[] newData = new int[Math.max(data.length*2, newCapacity)];
			System.arraycopy(data, 0, newData, 0, data.length);
			data = newData;
		}
	}

	public int[] get() {
		int[] copy = new int[size];
		System.arraycopy(data, 0, copy, 0, size);
		return copy;
	}
}
