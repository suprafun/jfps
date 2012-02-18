package trb.trials4k;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author admin
 */
public class LevelIO {

    public static void write(List<Level> levels, File file) {
        try {
            write(levels, new FileOutputStream(file));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void writeLevel(Level level, File file) {
        try {
            writeLevel(level, new DataOutputStream(new FileOutputStream(file)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

	public static void write(List<Level> levels, OutputStream outStream) {
		try {
			DataOutputStream out = new DataOutputStream(outStream);
			out.writeByte(levels.size());
			for (Level level : levels) {
				writeLevel(level, out);
			}
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}


	public static void writeLevel(Level level, DataOutput out) throws IOException {
		out.writeByte(level.lineArrays.size());
		for (LineArray la : level.lineArrays) {
			System.out.println("write line array "+la.points.size());
			out.writeByte(la.points.size());
			for (Tuple t : la.points) {
				out.writeShort(Math.round(t.x));
				out.writeShort(Math.round(t.y));
			}
		}
		System.out.println("write circles "+level.circles.size());
		out.writeByte(level.circles.size());
		for (Circle circle : level.circles) {
			out.writeShort(Math.round(circle.center.x));
			out.writeShort(Math.round(circle.center.y));
			out.writeByte(Math.round(circle.radius));
		}
		System.out.println("write checkpoints " + level.checkpoints.size());
		out.writeByte(level.checkpoints.size());
		for (Checkpoint checkpoint : level.checkpoints) {
			out.writeShort(Math.round(checkpoint.position.x));
			out.writeShort(Math.round(checkpoint.position.y));
		}
		out.writeByte(level.deltaArrays.size());
		for (DeltaArray la : level.deltaArrays) {
			System.out.println("write delta array " + la.points.size());
			out.writeByte(la.points.size());
			out.writeShort(Math.round(la.start.x));
			out.writeShort(Math.round(la.start.y));
			for (DeltaArray.Delta t : la.points) {
				out.writeByte(t.lengthAngle);
			}
		}
	}

	public static List<Level> read(File file) {
		try {
			return read(new FileInputStream(file));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new ArrayList();
	}

	public static List<Level> read(InputStream inStream) {
		List<Level> levels = new ArrayList();
		try {
			DataInputStream in = new DataInputStream(inStream);
			int levelCnt = in.readByte();
			for (int levelIdx=0; levelIdx<levelCnt; levelIdx++) {
                levels.add(readLevel(in));
			}

			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return levels;
	}

    public static Level readLevel(DataInputStream in) throws IOException {
        Level level = new Level();
        int cnt = in.readByte();
        for (int i = 0; i < cnt; i++) {
            level.lineArrays.add(readLineArray(in));
        }
        cnt = in.readByte();
		System.out.println("circles " + cnt);
        for (int i = 0; i < cnt; i++) {
            level.circles.add(readCircle(in));
        }
        cnt = in.readByte();
		System.err.println("Checkpoints: " + cnt);
        for (int i = 0; i < cnt; i++) {
            level.checkpoints.add(readCheckpoint(in));
        }
		if (in.available() > 0) {
			cnt = in.readByte();
			for (int i=0; i<cnt; i++) {
				level.deltaArrays.add(readDeltaArray(in));
			}
		}
        return level;
    }

	private static LineArray readLineArray(DataInput in) throws IOException {
		LineArray lineArray = new LineArray();
		int cnt = in.readByte();
		System.out.println("read line array " + cnt);
		for (int i = 0; i < cnt; i++) {
			lineArray.points.add(new Tuple(in.readUnsignedShort(), in.readUnsignedShort()));
		}
		return lineArray;
	}

	private static Circle readCircle(DataInput in) throws IOException {
		Circle circle = new Circle();
		circle.center.x = in.readUnsignedShort();
		circle.center.y = in.readUnsignedShort();
		circle.radius = in.readUnsignedByte();
		return circle;
	}

	private static Checkpoint readCheckpoint(DataInput in) throws IOException {
		Checkpoint checkpoint = new Checkpoint();
		checkpoint.position.x = in.readUnsignedShort();
		checkpoint.position.y = in.readUnsignedShort();
		return checkpoint;
	}

	private static DeltaArray readDeltaArray(DataInput in) throws IOException {
		DeltaArray deltaArray = new DeltaArray();
		int cnt = in.readByte();
		System.out.println("read delta array " + cnt);
		deltaArray.start.set(in.readShort(), in.readShort());
		for (int i = 0; i < cnt; i++) {
			deltaArray.points.add(new DeltaArray.Delta(in.readByte()));
		}
		return deltaArray;
	}

	public static void readOptimized(float[] f, DataInput in) throws IOException {
		int levelCnt = in.readByte();
		System.out.println("Level cnt "+ levelCnt);
		f[Level.LEVEL_CNT_OFF] = levelCnt;
		for (int levelIdx = 0; levelIdx < levelCnt; levelIdx++) {
			int levelOff = levelIdx * Level.LEVEL_STRIDE;
			int lineArrayCnt = in.readByte();
			System.out.println("line array cnt "+ lineArrayCnt);
			for (int lineArrayIdx = 0; lineArrayIdx < lineArrayCnt; lineArrayIdx++) {
				int pointCnt = in.readByte();
				f[levelOff + Level.LINE_CNT_OFF] = pointCnt - 1;
				float lastx = 0;
				float lasty = 0;
				int dstIdx = levelOff + Level.LINE_OFF;
				for (int pointIdx = 0; pointIdx < pointCnt; pointIdx++) {
					float x = in.readUnsignedShort();
					float y = in.readUnsignedShort();

					System.out.println("AAA "+x+" "+y);
					if (pointIdx > 0) {
						f[dstIdx + Level.X1] = lastx;
						f[dstIdx + Level.Y1] = lasty;
						f[dstIdx + Level.X2] = x;
						f[dstIdx + Level.Y2] = y;
						dstIdx+=Level.OBJECT_STRIDE;
						System.out.println("line "+lastx+" "+lasty+" "+x+" "+y);
					}
					lastx = x;
					lasty = y;
				}
			}
			int circleCnt = in.readByte();
			f[levelOff + Level.CIRCLE_CNT_OFF] = circleCnt;
			for (int i = 0; i < circleCnt; i++) {
				f[levelOff + Level.CIRCLE_OFF + i * Level.OBJECT_STRIDE + Level.X1] = in.readUnsignedShort();
				f[levelOff + Level.CIRCLE_OFF + i * Level.OBJECT_STRIDE + Level.Y1] = in.readUnsignedShort();
				f[levelOff + Level.CIRCLE_OFF + i * Level.OBJECT_STRIDE + Level.CIRCLE_RADIUS] = in.readUnsignedByte();
			}
			int checkpointCnt = in.readByte();
			f[levelOff + Level.CHECKPOINT_CNT_OFF] = checkpointCnt;
			for (int i = 0; i < checkpointCnt; i++) {
				f[levelOff + Level.CHECKPOINT_OFF + i * Level.OBJECT_STRIDE + Level.X1] = in.readUnsignedShort();
				f[levelOff + Level.CHECKPOINT_OFF + i * Level.OBJECT_STRIDE + Level.Y1] = in.readUnsignedShort();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		DataInputStream in = new DataInputStream(LevelIO.class.getResourceAsStream("data.bin"));
		Level levels = readLevel(in);
	
	}
}
