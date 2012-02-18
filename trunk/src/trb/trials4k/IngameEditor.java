package trb.trials4k;

import java.awt.Color;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Point;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class IngameEditor {

	public enum Type {LINE_ARRAY, CIRCLE, CHECKPOINT̋, DELTA_ARRAY};

	Level level;

	Type type = Type.LINE_ARRAY;
	public int selectedLineArray = 0;
	public int selectedVertex = 0;
	public int selectedCircle = 0;
	public int selectedCheckpoint = 0;
	public int selectedDeltaArray = 0;
	public int selectedDeltaVertex = 0;
	public boolean enabled = false;
	public List<Event> events = new CopyOnWriteArrayList();

	public IngameEditor() {
		load();
	}

	void load() {
		try {
			DataInputStream in = new DataInputStream(new FileInputStream(new File("/vrdev/fps/data.bin")));
			level = LevelIO.readLevel(in);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	void save() {
		LevelIO.writeLevel(level, new File("data.bin"));
		System.out.println("save");
	}

	public void next(int delta) {
		switch (type) {
			case LINE_ARRAY:
				if (level.lineArrays.size() > 0) {
					LineArray lineArray = level.lineArrays.get(selectedLineArray);
					selectedVertex = (selectedVertex + delta + lineArray.points.size()) % lineArray.points.size();
				}
				break;
			case CIRCLE:
				selectedCircle = (selectedCircle + delta + level.circles.size()) % level.circles.size();
				break;
			case CHECKPOINT̋:
				selectedCheckpoint = (selectedCheckpoint + delta + level.checkpoints.size()) % level.checkpoints.size();
				break;
			case DELTA_ARRAY:
				if (level.deltaArrays.size() > 0) {
					DeltaArray deltaArray = level.deltaArrays.get(selectedDeltaArray);
					selectedDeltaVertex = (selectedDeltaVertex + delta + deltaArray.points.size() + 1) % (deltaArray.points.size() + 1);
				}
				break;
		}
	}

	public boolean nextLineArray(int delta) {
		switch (type) {
			case LINE_ARRAY:
				selectedLineArray = (selectedLineArray + delta + level.lineArrays.size()) % level.lineArrays.size();
				selectedVertex = 0;
				return false;
			case CIRCLE:
				System.out.println("mutataekj lkajs ødfkljdlsk j");
				level.circles.get(selectedCircle).radius += delta * 5;
				save();
				return true;
			case DELTA_ARRAY:
				selectedDeltaArray = (selectedDeltaArray + delta + level.deltaArrays.size()) % level.deltaArrays.size();
				selectedDeltaVertex = 0;
				return false;
		}
		return false;
	}

	public void move(int dx, int dy) {
		if (type == Type.DELTA_ARRAY) {
			if (level.deltaArrays.isEmpty()) {
				return;
			}
			DeltaArray deltaArray = level.deltaArrays.get(selectedDeltaArray);
			if (selectedDeltaVertex == 0) {
				Tuple t = deltaArray.start;
				t.x += dx * 8;
				t.y += dy * 8;				
			} else {
				DeltaArray.Delta delta = deltaArray.points.get(selectedDeltaVertex-1);
				delta.add(dx, dy);
				if (dy != 0 && selectedDeltaVertex < deltaArray.points.size()) {
					deltaArray.points.get(selectedDeltaVertex).add(0, -dy);
				}
			}
		} else  {
			Tuple t = getSelectedTuple();
			t.x += dx * 8;
			t.y += dy * 8;
		}
		save();
	}

	public void delete() {
		switch (type) {
			case LINE_ARRAY:
				if (!level.lineArrays.isEmpty()) {
					LineArray lineArray = level.lineArrays.get(selectedLineArray);
					lineArray.points.remove(selectedVertex);
					if (lineArray.points.size() <= 0) {
						level.lineArrays.remove(selectedLineArray);
						selectedLineArray = Math.min(selectedLineArray, level.lineArrays.size() - 1);
					} else {
						selectedVertex = Math.min(selectedVertex, lineArray.points.size() - 1);
					}
				}
				break;
			case CIRCLE:
				if (level.circles.size() > 0) {
					level.circles.remove(selectedCircle);
					selectedCircle = Math.min(selectedCircle, level.circles.size() - 1);
				}
				break;
			case CHECKPOINT̋:
				if (level.checkpoints.size() > 0) {
					level.checkpoints.remove(selectedCheckpoint);
					selectedCheckpoint = Math.min(selectedCheckpoint, level.checkpoints.size() - 1);
				}
				break;
			case DELTA_ARRAY:
				if (!level.deltaArrays.isEmpty() && selectedDeltaVertex > 0) {
					DeltaArray deltaArray = level.deltaArrays.get(selectedDeltaArray);
					deltaArray.points.remove(selectedDeltaVertex-1);
					if (deltaArray.points.isEmpty()) {
						level.deltaArrays.remove(selectedDeltaArray);
						selectedDeltaArray = Math.min(selectedDeltaArray, level.deltaArrays.size() - 1);
					} else {
						selectedDeltaVertex = Math.min(selectedDeltaVertex, deltaArray.points.size() - 1);
					}
					break;
				}
		}
		save();
	}

	public void insert() {
		Tuple t = new Tuple(getSelectedTuple());
		t.x += 10;
		switch (type) {
			case LINE_ARRAY:
				if (level.lineArrays.isEmpty()) {
					level.lineArrays.add(new LineArray(new Tuple(), new Tuple(10, 0)));
				} else {
					LineArray lineArray = level.lineArrays.get(selectedLineArray);
					lineArray.points.add(selectedVertex + 1, t);
					selectedVertex++;
				}
				break;
			case CIRCLE:
				level.circles.add(selectedCircle+1, new Circle(t.x, t.y, level.circles.get(selectedCircle).radius));
				selectedCircle++;
				break;
			case CHECKPOINT̋:
				level.checkpoints.add(selectedCheckpoint+1, new Checkpoint(t));
				selectedCheckpoint++;
				break;
			case DELTA_ARRAY:
				System.out.println("insertDeltaArray "+level.deltaArrays.size());
				if (level.deltaArrays.isEmpty()) {
					level.deltaArrays.add(new DeltaArray(new Tuple(), new DeltaArray.Delta(0)));
				} else {
					DeltaArray deltaArray = level.deltaArrays.get(selectedDeltaArray);
					deltaArray.points.add(selectedDeltaVertex + 1, new DeltaArray.Delta(0));
					selectedDeltaVertex++;
				}
				break;
		}
		save();
	}

	public boolean handleEvents() {
		boolean b = false;
		for (Event event : events) {
			if (event.key == 'e') {
				enabled = !enabled;
			}
			if (enabled) {
				b |= handleEvent(event);
			}
		}
		events.clear();
		return b;
	}

	public boolean handleEvent(Event e) {
		if (e.shiftDown()) {
			if (e.key == Event.RIGHT) {
				return nextLineArray(1);
			}
			if (e.key == Event.LEFT) {
				return nextLineArray(-1);
			}
		} else {
			if (e.key == Event.RIGHT) {
				next(1);
			}
			if (e.key == Event.LEFT) {
				next(-1);
			}
		}
		if (e.key == Event.F1) {
			type = Type.LINE_ARRAY;
		}
		if (e.key == Event.F2) {
			type = Type.CIRCLE;
		}
		if (e.key == Event.F3) {
			type = Type.CHECKPOINT̋;
		}
		if (e.key == Event.F4) {
			type = Type.DELTA_ARRAY;
		}
		if (e.key == Event.DELETE || e.key == Event.BACK_SPACE) {
			delete();
			return true;
		}
		if (e.key == 'w') {
			move(0, -1);
			return true;
		}
		if (e.key == 's') {
			move(0, 1);
			return true;
		}
		if (e.key == 'a') {
			move(-1, 0);
			return true;
		}
		if (e.key == 'd') {
			move(1, 0);
			return true;
		}
		if (e.key == ' ') {
			insert();
			return true;
		}

		return false;
	}

	public void draw(Graphics g) {
		g.setColor(Color.RED);
		Point p = getSelectedPoint();
		g.fillOval(p.x-5, p.y-5, 10, 10);
	}

	Point getSelectedPoint() {
		return getSelectedTuple().point();
	}

	Tuple getSelectedTuple() {
		switch (type) {
			case LINE_ARRAY:
				if (level.lineArrays.isEmpty()) {
					return new Tuple();
				} else {
					LineArray lineArray = level.lineArrays.get(selectedLineArray);
					return lineArray.points.get(selectedVertex);
				}
			case CIRCLE:
				if (level.circles.isEmpty()) {
					return new Tuple();
				} else {
					return level.circles.get(selectedCircle).center;
				}
			case CHECKPOINT̋:
				if (level.checkpoints.isEmpty()) {
					return new Tuple();
				} else {
					return level.checkpoints.get(selectedCheckpoint).position;
				}
			case DELTA_ARRAY:
				if (level.deltaArrays.isEmpty()) {
					return new Tuple();
				} else {
					return level.deltaArrays.get(selectedDeltaArray).getTuple(selectedDeltaVertex);
				}
		}

		System.err.println("getSelectedTuple failed");
		return new Tuple();
	}
}
