package trb.trials4k;

import java.applet.Applet;
import java.awt.BasicStroke;
import java.awt.Event;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 16604 - removed wheel class
 * 15446 - remove Body class
 * 14834 - remove Vertex class
 * 13807 - remove Rider class
 * 12586 - removed Bike
 * 13951 - inlined collision detection in Level and added Intersection class
 * 13207 - read levels from file
 * 13898 - inlined level parsing code
 * 13729 - inlined methods
 * 7640 - removed Intersection class
 * 6802 - removed Tuple class
 * 10791 (3811) - removed Edge class
 * 10660 (3782) - inlined code and removed test code
 * ? (3685) - removed System.outs
 * 2808 - commented out create bike
 *
 * vertices: 5*54 = 270
 * edges: 173 * 6 = 1038
 * 
 * @author admin
 */
public class C extends Applet implements Runnable {

	public static final boolean DEBUG = false;
	public static final boolean USE_CIRCLES = false;
	public static final boolean USE_DAMPING = false;

	// Level
	public static final int X1 = 0;
	public static final int Y1 = 1;
	public static final int X2 = 2;
	public static final int Y2 = 3;
	public static final int CIRCLE_RADIUS = 2;
	public static final int CHECKPOINT_PASSED = 2;
	public static final int OBJECT_STRIDE = 0x10;
	public static final int LINE_OFF = 100 + OBJECT_STRIDE * 1024;
	public static final int CIRCLE_OFF = 100 + OBJECT_STRIDE * 1024 * 2;
	public static final int CHECKPOINT_OFF = 100 + OBJECT_STRIDE * 1024 * 3;
	public static final int LINE_CNT_OFF = 0;
	public static final int CIRCLE_CNT_OFF = 1;
    public static final int STROKE_WIDTH = 6;
    public static final int TARGET_FPS = 60;

	// vertex
	public static final int POS_X = 0;
	public static final int POS_Y = 1;
	public static final int OLD_X = 2;
	public static final int OLD_Y = 3;
	public static final int ACC_X = 4;
	public static final int ACC_Y = 5;
	public static final int RADIUS = 6;
	public static final int MASS = 7;

	static final int STATE_DEAD = 0;
	static final int STATE_PLAYING = 1;
	static final int STATE_FINISHED = 2;

	static final int BACKGROUND_COLOR = 0xff2f174f;
	static final float[] f = new float[1000000];
    public int checkpointCnt = 0;
	public int currentCheckpoint = 0;
	public int tries = 0;
	public long startTime = System.currentTimeMillis();

    static final float BIKE_TORQUE = 0.004f;
    static final float WHEEL_TORQUE = 0.3f;
    static final float GRAVITY = 0.25f;

	boolean[] changed = new boolean[0x10000];
	boolean[] k = new boolean[0x10000];

	// body
	public final List<float[]> vertices = new ArrayList();
	public final List<float[]> constraints = new ArrayList();
	public boolean collidedWithRider = false;

	// rider
	float[] shoulders;
	List<float[]> riderEdges;
	List<float[]> riderEdges1;
	List<float[]> riderEdges2;
	List<float[]> riderVertices;
	float riderT = 0;

	// bike
	float[] backWheel;
	float[] frontWheel;
	float[] stearing;
	float[] chain;

	// Edge
	static final int EDGE_LENGTH = 0;
	static final int EDGE_STIFFNESS = 1;
	static final int EDGE_DAMPING = 2;
	static final int EDGE_MIN_LENGTH = 3;
	static final int EDGE_MAX_LENGTH = 4;
	static final int EDGE_OO_TOTAL_MASS = 5;
	static final int EDGE_V1 = 6;
	static final int EDGE_V2 = 7;

	IngameEditor editor;

	@Override
    public void start() {
        new Thread(this).start();
    }

    public void run() {
		editor = new IngameEditor();

        setSize(800, 600); // For AppletViewer, remove later.

        // Set up the graphics stuff, double-buffering.
        BufferedImage screen = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) screen.getGraphics();
        Graphics appletGraphics = getGraphics();

        // Some variables to use for the fps.
        int tick = 0, fps = 0, acc = 0;
        long lastTime = System.nanoTime();

		boolean loadLevel = true;
		int state = STATE_PLAYING;

		requestFocusInWindow();

        int yieldCnt = 0;

		double scale = 0.75;

        // Game loop.
        do {
			if (editor.handleEvents()) {
				loadLevel = true;
			}

            if (k[Event.ENTER]) {
                loadLevel = true;
            }

			if (loadLevel) {
				try {
					DataInputStream in = new DataInputStream(new FileInputStream("data.bin"));
//		            DataInputStream in = new DataInputStream(getClass().getResourceAsStream("data.bin"));
					int lineArrayCnt = in.readByte();
					f[LINE_CNT_OFF] = 0;
					int dstIdx = LINE_OFF;
					for (int lineArrayIdx = 0; lineArrayIdx < lineArrayCnt; lineArrayIdx++) {
						int pointCnt = in.readByte();
						f[LINE_CNT_OFF] += pointCnt - 1;
						float lastx = 0;
						float lasty = 0;
						for (int pointIdx = 0; pointIdx < pointCnt; pointIdx++) {
							float x = in.readUnsignedShort();
							float y = in.readUnsignedShort();
							if (pointIdx > 0) {
								f[dstIdx + X1] = lastx;
								f[dstIdx + Y1] = lasty;
								f[dstIdx + X2] = x;
								f[dstIdx + Y2] = y;
								dstIdx += OBJECT_STRIDE;
							}
							lastx = x;
							lasty = y;
						}
					}
					int circleCnt = in.readByte();
					f[CIRCLE_CNT_OFF] = circleCnt;
					for (int i = 0; i < circleCnt; i++) {
						f[CIRCLE_OFF + i * OBJECT_STRIDE + X1] = in.readUnsignedShort();
						f[CIRCLE_OFF + i * OBJECT_STRIDE + Y1] = in.readUnsignedShort();
						f[CIRCLE_OFF + i * OBJECT_STRIDE + CIRCLE_RADIUS] = in.readUnsignedByte();
					}
					checkpointCnt = in.readByte();
					for (int i = 0; i < checkpointCnt; i++) {
						f[CHECKPOINT_OFF + i * OBJECT_STRIDE + X1] = in.readUnsignedShort();
						f[CHECKPOINT_OFF + i * OBJECT_STRIDE + Y1] = in.readUnsignedShort();
					}
					if (in.available() > 0) {
						int deltaArrayCnt = in.readByte();
						for (int deltaArrayIdx = 0; deltaArrayIdx < deltaArrayCnt; deltaArrayIdx++) {
							int deltaCnt = in.readByte();
							float lastx = in.readShort();
							float lasty = in.readShort();
							float lastAngle = 0f;
							System.out.println("load delta arra " + deltaCnt);
							f[LINE_CNT_OFF] += deltaCnt;
							for (int deltaIdx = 0; deltaIdx < deltaCnt; deltaIdx++) {
								f[dstIdx + X1] = lastx;
								f[dstIdx + Y1] = lasty;
								int lengthAngle = in.readByte();
								float angle = DeltaArray.toRad(lengthAngle);
								float length = DeltaArray.toLength(lengthAngle);
								lastAngle += angle;
								lastx += Math.cos(lastAngle) * length;
								lasty += Math.sin(lastAngle) * length;
								f[dstIdx + X2] = lastx;
								f[dstIdx + Y2] = lasty;
								dstIdx += OBJECT_STRIDE;
							}
						}
					}

					in.close();

					currentCheckpoint = 0;
					tries = 0;
					startTime = System.currentTimeMillis();
				} catch (IOException ex) {
					ex.printStackTrace();
				}

				respawn();
				state = STATE_PLAYING;
				loadLevel = false;
			}

			if (DEBUG) {
				if (k['1'] && changed['1']) {
					currentCheckpoint = Math.max(currentCheckpoint - 1, 0);
					changed['1'] = false;
					respawn();
				}

				if (k['2'] && changed['2']) {
					currentCheckpoint = Math.min(currentCheckpoint + 1, checkpointCnt - 1);
					changed['2'] = false;
					respawn();
				}
			}

			if (state != STATE_FINISHED && k[Event.BACK_SPACE] && changed[Event.BACK_SPACE]) {
				// reset to previous checkpoint
				respawn();
				state = STATE_PLAYING;
				changed[Event.BACK_SPACE] = false;
			}

			for (float[] v : vertices) {
				v[ACC_X] = 0;
				v[ACC_Y] = GRAVITY;
			}
			if (state == STATE_PLAYING) {

				float bikeTorque = 0;
				if (k[Event.LEFT] || k['a']) {
					bikeTorque -= BIKE_TORQUE;
					riderT = Math.max(0, riderT - 0.15f);
				}
				if (k[Event.RIGHT] || k['d']) {
					bikeTorque += BIKE_TORQUE;
					riderT = Math.min(1, riderT + 0.15f);
				}
				for (int i = 0; i < riderEdges.size(); i++) {
					riderEdges.get(i)[EDGE_LENGTH] = riderEdges1.get(i)[EDGE_LENGTH]
							* (1 - riderT) + riderEdges2.get(i)[EDGE_LENGTH] * riderT;
				}
				// we dont normalise since we assume the length is baked in amount
				float nx = -(frontWheel[POS_Y] - backWheel[POS_Y]) * bikeTorque;
				float ny =  (frontWheel[POS_X] - backWheel[POS_X]) * bikeTorque;

				frontWheel[POS_X] += nx;
				frontWheel[POS_Y] += ny;
				backWheel[POS_X] -= nx;
				backWheel[POS_Y] -= ny;
			}

			for (int checkpointIdx = 0; checkpointIdx < checkpointCnt; checkpointIdx++) {
				int x = (int) f[CHECKPOINT_OFF + OBJECT_STRIDE * checkpointIdx + X1];
				if (x < vertices.get(0)[POS_X]) {
					f[CHECKPOINT_OFF + OBJECT_STRIDE * checkpointIdx + CHECKPOINT_PASSED] = 1;
					currentCheckpoint = Math.max(currentCheckpoint, checkpointIdx);
					if (currentCheckpoint == checkpointCnt-1) {
						state = STATE_FINISHED;
					}

				}
			}

			// update body
			for (float[] d : vertices) {
				float tempX = d[POS_X];
				float tempY = d[POS_Y];
				d[POS_X] += 0.996f * (d[POS_X] - d[OLD_X] + d[ACC_X]);
				d[POS_Y] += 0.996f * (d[POS_Y] - d[OLD_Y] + d[ACC_Y]);
				d[OLD_X] = tempX;
				d[OLD_Y] = tempY;
			}

			collidedWithRider = false;
			for (int i = 0; i < 10; i++) {
				for (float[] ed : constraints) {
					float[] v1 = vertices.get((int) ed[EDGE_V1]);
					float[] v2 = vertices.get((int) ed[EDGE_V2]);
					float stiff = ed[EDGE_STIFFNESS];
					float v1v2x = v2[A.POS_X] - v1[A.POS_X];
					float v1v2y = v2[A.POS_Y] - v1[A.POS_Y];

					// fast square root
					float x = v1v2x * v1v2x + v1v2y * v1v2y;
					float guess = ed[EDGE_LENGTH];
					float v1v2Length = (guess + x / guess) * 0.5f;

					float diff = v1v2Length - ed[EDGE_LENGTH];
					float adjustment = diff * stiff;
					float damp = ed[EDGE_DAMPING];
					if (v1v2Length - adjustment < ed[EDGE_MIN_LENGTH]) {
						adjustment = v1v2Length - ed[EDGE_MIN_LENGTH];
						damp = 0;
					}
					if (v1v2Length - adjustment > ed[EDGE_MAX_LENGTH]) {
						adjustment = v1v2Length - ed[EDGE_MAX_LENGTH];
						damp = 0;
					}

					float ooLength = 1f / v1v2Length;
					v1v2x *= ooLength;
					v1v2y *= ooLength;

					float mf1 = v2[A.MASS] * ed[EDGE_OO_TOTAL_MASS];
					float mf2 = v1[A.MASS] * ed[EDGE_OO_TOTAL_MASS];

					// Push both vertices apart by half of the difference respectively so the distance between them equals the original length
					v1[A.POS_X] += v1v2x * adjustment * mf1 * 0.5f;
					v1[A.POS_Y] += v1v2y * adjustment * mf1 * 0.5f;
					v2[A.POS_X] -= v1v2x * adjustment * mf2 * 0.5f;
					v2[A.POS_Y] -= v1v2y * adjustment * mf2 * 0.5f;

					if (USE_DAMPING) {
						if (damp != 0) {
							float velDiffx = (v2[A.POS_X] - v2[A.OLD_X])
										   - (v1[A.POS_X] - v1[A.OLD_X]);
							float velDiffy = (v2[A.POS_Y] - v2[A.OLD_Y])
										   - (v1[A.POS_Y] - v1[A.OLD_Y]);

							velDiffx *= (damp * 0.5f);
							velDiffy *= (damp * 0.5f);
							v1[A.POS_X] += velDiffx;
							v1[A.POS_Y] += velDiffy;
							v2[A.POS_X] -= velDiffx;
							v2[A.POS_Y] -= velDiffy;

						}
					}
				}
				for (float[] v : vertices) {
					// ---------------- collision detection --------------------
					boolean foundCollision = false;
					float normalx = 0;
					float normaly = 1;
					float closestx = 0;
					float closesty = 0;
					float closestDistance = 10000000f;
					float ballx = v[POS_X];
					float bally = v[POS_Y];
					float vradius = v[RADIUS] + STROKE_WIDTH / 2;

					if (USE_CIRCLES) {
						for (int circleIdx = 0; circleIdx < f[CIRCLE_CNT_OFF]; circleIdx++) {
							float circlex = f[CIRCLE_OFF + OBJECT_STRIDE * circleIdx + X1];
							float circley = f[CIRCLE_OFF + OBJECT_STRIDE * circleIdx + Y1];
							float radius = f[CIRCLE_OFF + OBJECT_STRIDE * circleIdx + CIRCLE_RADIUS];
							float dx = ballx - circlex;
							float dy = bally - circley;
							float length = length(dx, dy);
							float dist = length - radius;
							boolean intersected = dist < vradius;
							if (/*!foundCollision ||*/ dist < closestDistance) {
								closestDistance = dist;
								foundCollision = intersected;
								closestx = circlex + (dx / length * radius);
								closesty = circley + (dy / length * radius);
								normalx = v[POS_X] - closestx;
								normaly = v[POS_Y] - closesty;
								float normalLength = length(normalx, normaly);
								normalx /= normalLength;
								normaly /= normalLength;
							}
						}
					}
					for (int lineIdx = 0; lineIdx < f[LINE_CNT_OFF]; lineIdx++) {
						float linex1 = f[LINE_OFF + OBJECT_STRIDE * lineIdx + X1];
						float liney1 = f[LINE_OFF + OBJECT_STRIDE * lineIdx + Y1];
						float linex2 = f[LINE_OFF + OBJECT_STRIDE * lineIdx + X2];
						float liney2 = f[LINE_OFF + OBJECT_STRIDE * lineIdx + Y2];

						// the closest point on line if inside ball radius
						float tempProjectedx = 0;
						float tempProjectedy = 0;
						float dist = 0;
						boolean intersected = false;

						// dot line with (ball - line endpoint)
						float rrr = (ballx - linex1) * (linex2 - linex1) + (bally - liney1) * (liney2 - liney1);
						float len = length(linex2 - linex1, liney2 - liney1);
						float t = rrr / len / len;
						if (t >= 0 && t <= 1) {
							tempProjectedx = linex1 + (t * (linex2 - linex1));
							tempProjectedy = liney1 + (t * (liney2 - liney1));

							dist = length(ballx - tempProjectedx, bally - tempProjectedy);
							intersected = (dist <= vradius);
						} else {
							// center of ball is outside line segment. Check end points.
							dist = length(ballx - linex1, bally - liney1);
							float distance2 = length(ballx - linex2, bally - liney2);
							if (dist < vradius) {
								intersected = true;
								tempProjectedx = linex1;
								tempProjectedy = liney1;
							}
							if (distance2 < vradius && distance2 < dist) {
								intersected = true;
								tempProjectedx = linex2;
								tempProjectedy = liney2;
								dist = distance2;
							}
						}

						// store closest hit
						if (/*!foundCollision ||*/ dist < closestDistance) {
							closestDistance = dist;
							foundCollision = intersected;
							closestx = tempProjectedx;
							closesty = tempProjectedy;
							normalx = v[POS_X] - closestx;
							normaly = v[POS_Y] - closesty;
							float normalLength = length(normalx, normaly);
							normalx /= normalLength;
							normaly /= normalLength;
                        }
					}

					// ---------------- end collision detection --------------------
					if (foundCollision) {
						if (length(closestx - v[POS_X], closesty - v[POS_Y]) < vradius * 0.99f) {
							v[POS_X] = (normalx * vradius * 0.99f + closestx);
							v[POS_Y] = (normaly * vradius * 0.99f + closesty);
						}

//						float wheelTorque = 0;
//						if (k[Event.UP] || k['w']) {
//							wheelTorque += WHEEL_TORQUE;
//						}
//						if (k[Event.DOWN] || k['s']) {
//							wheelTorque -= WHEEL_TORQUE;
//						}

						if (v == frontWheel && (k[Event.DOWN] || k['s'])) {
							v[POS_X] -= (v[POS_X] - v[OLD_X]) * 0.1f;
							v[POS_Y] -= (v[POS_Y] - v[OLD_Y]) * 0.1f;
						}

						if (v == backWheel && (k[Event.UP] || k['w'])) {
							v[POS_X] += -normaly * WHEEL_TORQUE;
							v[POS_Y] += normalx * WHEEL_TORQUE;
						}

						if (v == shoulders) {
							collidedWithRider = true;
						}
					}
				}
			}
			// end update body


			if (state == STATE_PLAYING && collidedWithRider) {
				state = STATE_DEAD;
				constraints.clear();
			}

            long now = System.nanoTime();
            acc += now - lastTime;
            tick++;
            if (acc >= 1000000000L) {
                acc -= 1000000000L;
                fps = tick;
                tick = 0;
            }

            // Update
            lastTime = now;

            // Render
			int scrollx = 200;
			int scrolly = 400;
			if (editor.enabled) {
				Point p = editor.getSelectedPoint();
				scrollx = 400-p.x;
				scrolly = 300-p.y;
			} else {
				scrollx -= chain[POS_X];
				scrolly -= chain[POS_Y];
			}

			g.setColor(new Color(BACKGROUND_COLOR));
			g.fillRect(0, 0, 1024, 1024);
			g.scale(scale, scale);
			g.translate(scrollx, scrolly);

			g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			for (int lineIdx = 0; lineIdx < f[LINE_CNT_OFF]; lineIdx++) {
				int linex1 = (int) f[LINE_OFF + OBJECT_STRIDE * lineIdx + X1];
				int liney1 = (int) f[LINE_OFF + OBJECT_STRIDE * lineIdx + Y1];
				int linex2 = (int) f[LINE_OFF + OBJECT_STRIDE * lineIdx + X2];
				int liney2 = (int) f[LINE_OFF + OBJECT_STRIDE * lineIdx + Y2];
				g.drawLine(linex1, liney1, linex2, liney2);
			}
			if (USE_CIRCLES) {
				for (int circleIdx = 0; circleIdx < f[CIRCLE_CNT_OFF]; circleIdx++) {
					int x = (int) f[CIRCLE_OFF + OBJECT_STRIDE * circleIdx + X1];
					int y = (int) f[CIRCLE_OFF + OBJECT_STRIDE * circleIdx + Y1];
					int r = (int) f[CIRCLE_OFF + OBJECT_STRIDE * circleIdx + CIRCLE_RADIUS];
					g.drawOval(x - r, y - r, r * 2, r * 2);
				}
			}
            g.setStroke(new BasicStroke(1));
			for (int checkpointIdx = 0; checkpointIdx < checkpointCnt; checkpointIdx++) {
				int x = (int) f[CHECKPOINT_OFF + OBJECT_STRIDE * checkpointIdx + X1];
				int y = (int) f[CHECKPOINT_OFF + OBJECT_STRIDE * checkpointIdx + Y1];
				g.setColor(f[CHECKPOINT_OFF + OBJECT_STRIDE * checkpointIdx + CHECKPOINT_PASSED] > 0 ? Color.GREEN : Color.RED);
				g.drawLine(x, y, x, y - 100);
			}

			// --------- draw body ------------
			g.setColor(Color.WHITE);
			for (float[] v : vertices) {
				float r = v[RADIUS];
				g.fillOval(Math.round(v[POS_X] - r), Math.round(v[POS_Y] - r), (int) r * 2, (int) r * 2);
			}

			for (float[] edge : constraints) {
				float[] v1 = vertices.get((int) edge[EDGE_V1]);
				float[] v2 = vertices.get((int) edge[EDGE_V2]);
				g.drawLine(Math.round(v1[A.POS_X]), Math.round(v1[A.POS_Y]),
						Math.round(v2[A.POS_X]), Math.round(v2[A.POS_Y]));
			}
			// --------- end draw body ------------

			editor.draw(g);

			g.translate(-scrollx, -scrolly);
			g.scale(1.0/scale, 1.0/scale);
            g.setColor(Color.white);
			g.drawString("FPS " + String.valueOf(fps), 20, 30);

			if (state == STATE_DEAD || state == STATE_FINISHED) {
				g.drawString("ENTER - restart", 20, 70);
			}
			g.drawString("BACKSPACE - last checkpoint", 20, 50);
			if (state == STATE_FINISHED) {
				g.drawString("FINISHED", 20, 130);
			}
			g.drawString("Tries " + tries, 20, 90);
			g.drawString("time " + ((System.currentTimeMillis() - startTime) / 1000) + " " + yieldCnt, 20, 110);

            // Draw the entire results on the screen.
            appletGraphics.drawImage(screen, 0, 0, null);

            yieldCnt = 0;
            do {
                Thread.yield();
                yieldCnt++;
				//try {Thread.sleep(5);} catch (Exception ex) {}
			} while (System.nanoTime() - lastTime < (1000000000 / TARGET_FPS));
        } while (isActive());
    }

	private void respawn() {
		vertices.clear();
		constraints.clear();

		// --------------- create bike ------------------
		float hard = 1f;

        backWheel = createWheel(58, 125, 40);
        frontWheel = createWheel(213, 125, 40);

		stearing = createVertex(170, 40, 12);
		chain = createVertex(130, 120, 17);
		float[] back = createVertex(84, 57, 12);
		List<float[]> vs = new ArrayList();
		vs.add(stearing);
		vs.add(chain);
		vs.add(back);
		vertices.addAll(vs);

		addEdges(createEdges(vertices, vs, hard));

		float[] frontSpring = createEdge(vertices, frontWheel, stearing, 0.1f, 0);
		frontSpring[EDGE_DAMPING] = 0.03f;
		frontSpring[EDGE_MAX_LENGTH] = 120;
		frontSpring[EDGE_MIN_LENGTH] = 80;
		constraints.add(frontSpring);
		constraints.add(createEdge(vertices, frontWheel, chain, hard, 0));
		constraints.add(createEdge(vertices, backWheel, chain, hard, 0));
        float[] e = createEdge(vertices, backWheel, back, .1f, 0);
		e[EDGE_MAX_LENGTH] = 100;
		e[EDGE_MIN_LENGTH] = 65;
		e[EDGE_DAMPING] = 0.03f;
		constraints.add(e);

		// create rider
		shoulders = createVertex(105, -10, 13);
		riderVertices = new ArrayList();
		riderVertices.add(shoulders);

		vertices.addAll(riderVertices);
		riderVertices.add(stearing);
		riderVertices.add(chain);
		riderVertices.add(back);

		List<float[]> vs2 = new ArrayList();
		float[] shoulders2 = createVertex(150, -10, 13);
		vs2.add(shoulders2);
		vs2.add(stearing);
		vs2.add(chain);
		vs2.add(back);

		riderEdges1 = createEdges(vertices, riderVertices, 0.2f);
		riderEdges2 = createEdges(vs2, vs2, 0.2f);
		riderEdges = createEdges(vertices, riderVertices, 0.2f);
		addEdges(riderEdges);

		// checkpoints
		int x = (int) f[CHECKPOINT_OFF + OBJECT_STRIDE * currentCheckpoint + X1];
		int y = (int) f[CHECKPOINT_OFF + OBJECT_STRIDE * currentCheckpoint + Y1];
		for (float[] v : vertices) {
			v[POS_X] += (x - 130);
			v[POS_Y] += (y - 170);
			v[OLD_X] = v[POS_X];
			v[OLD_Y] = v[POS_Y];
			v[ACC_X] = 0;
			v[ACC_Y] = 0;
		}

		tries++;
		if (currentCheckpoint == 0) {
			tries = 0;
			startTime = System.currentTimeMillis();
		}
	}

	public void addEdges(List<float[]> newConstraints) {
		for (float[] c : newConstraints) {
			constraints.add(c);
		}
	}

	public static float[] createVertex(float x, float y, float radius) {
		float[] d = new float[10];
		d[POS_X] = x;
		d[POS_Y] = y;
		d[OLD_X] = x;
		d[OLD_Y] = y;
		d[RADIUS] = radius;
		d[MASS] = (float) Math.PI * radius * radius;
		return d;
	}

	public static float length(float[] d1, float[] d2) {
		float dx = d1[POS_X] - d2[POS_X];
		float dy = d1[POS_Y] - d2[POS_Y];
		return length(dx, dy);
	}

	public List<float[]> createEdges(List<float[]> globalVs, List<float[]> vs, float stiffness) {
		List<float[]> edges = new ArrayList();
		for (int i = 0; i < vs.size(); i++) {
			for (int j = i + 1; j < vs.size(); j++) {
				float[] edge = createEdge(globalVs, vs.get(i), vs.get(j), stiffness, 0);
				edges.add(edge);
			}
		}
		return edges;
	}

	public float[] createWheel(float wheelx, float wheely, float wheelRadius) {
        float[] v = createVertex(wheelx, wheely, wheelRadius);
        v[MASS] = 400;
		vertices.add(v);
		return v;
	}

	/**
	 * Calculates the length of the (x, y) vector.
	 */
	public static float length(float x, float y) {
		return (float) Math.sqrt(y * y + x * x);
	}

	public float[] createEdge(List<float[]> edgeVertices, float[] v1, float[] v2, float stiffness, float damping) {
		float[] ed = new float[9];
		ed[EDGE_STIFFNESS] = stiffness;
		ed[EDGE_LENGTH] = length(v1, v2);
		ed[EDGE_OO_TOTAL_MASS] = 1f / (v1[A.MASS] + v2[A.MASS]);
		ed[EDGE_DAMPING] = damping;
		ed[EDGE_MAX_LENGTH] = 100000f;
		ed[EDGE_V1] = edgeVertices.indexOf(v1);
		ed[EDGE_V2] = edgeVertices.indexOf(v2);
		return ed;
	}

	@Override
	public boolean handleEvent(Event e) {
		switch (e.id) {
			case Event.KEY_ACTION:
			case Event.KEY_PRESS:
				editor.events.add(e);
				changed[e.key] = k[e.key] == false;
				k[e.key] = true;
				break;
			case Event.KEY_ACTION_RELEASE:
			case Event.KEY_RELEASE:
				changed[e.key] = k[e.key] == true;
				k[e.key] = false;
				break;
		}
		return false;
	}
}
