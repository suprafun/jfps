package trb.trials4k;



import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class Level {
	public static final int X1 = 0;
	public static final int Y1 = 1;
	public static final int X2 = 2;
	public static final int Y2 = 3;
	public static final int CIRCLE_RADIUS = 2;
	public static final int CHECKPOINT_PASSED = 2;
	public static final int LEVEL_STRIDE = 0x1000;
	public static final int OBJECT_STRIDE = 0x10;
	public static final int LINE_OFF = OBJECT_STRIDE * 1024;
	public static final int CIRCLE_OFF = OBJECT_STRIDE * 1024 * 2;
	public static final int CHECKPOINT_OFF = OBJECT_STRIDE * 1024 * 3;
	public static final int LINE_CNT_OFF = 0;
	public static final int CIRCLE_CNT_OFF = 1;
	public static final int CHECKPOINT_CNT_OFF = 2;
	public static final int LEVEL_CNT_OFF = LEVEL_STRIDE * 16;

	public final List<LineArray> lineArrays = new ArrayList();
	public final List<Circle> circles = new ArrayList();
	public final List<Checkpoint> checkpoints = new ArrayList();

    public void addLevel(Level level, float xoffset, float yoffset) {
        for (LineArray lineArray : level.lineArrays) {
            LineArray newLineArray = new LineArray();
            for (Tuple p : lineArray.points) {
                newLineArray.points.add(new Tuple(p).add(xoffset, yoffset));
            }
            lineArrays.add(newLineArray);
        }

        for (Circle c : level.circles) {
            Circle newCircle = new Circle();
            newCircle.center.set(c.center).add(xoffset, yoffset);
            newCircle.radius = c.radius;
            circles.add(newCircle);
        }

        for (Checkpoint checkpoint : level.checkpoints) {
            checkpoints.add(new Checkpoint(new Tuple(checkpoint.position).add(xoffset, yoffset)));
        }
    }
	
	public void draw(Graphics g) {
		g.setColor(Color.WHITE);
		for (LineArray lineArray : lineArrays) {
			for (Line l : lineArray.createLines()) {
				g.drawLine(Math.round(l.v1.x), Math.round(l.v1.y), Math.round(l.v2.x), Math.round(l.v2.y));
			}
		}
		for (Circle c : circles) {
			int r = (int) c.radius;
			int x = (int) c.center.x;
			int y = (int) c.center.y;
			g.drawOval(x-r, y-r, r*2, r*2);
		}
		for (Checkpoint c : checkpoints) {
			g.setColor(c.passed ? Color.GREEN : Color.RED);
			g.drawLine(Math.round(c.position.x), Math.round(c.position.y),
					Math.round(c.position.x), Math.round(c.position.y) - 100);
		}
	}

	public Intersection collide(float[] v) {
		Tuple ball = getPosition(v);
		float radius = v[A.RADIUS];
		
		// Collision result variables 
		Intersection intersection = new Intersection();

		if (v[A.COLLIDABLE] == 0) {
			return intersection;
		}

		// detect collision between objects and ball
		for (Circle circle : circles) {
			float dx = ball.x - circle.center.x;
			float dy = ball.y - circle.center.y;
			float length = A.length(dx, dy);
			float dist = length - circle.radius;
			boolean intersected = dist < v[A.RADIUS];
			if (!intersection.foundCollision || dist < intersection.closestDistance) {
				intersection.closestDistance = dist;
				intersection.foundCollision = intersected;
				intersection.closest.x = circle.center.x + (dx / length * circle.radius);
				intersection.closest.y = circle.center.y + (dy / length * circle.radius);
				intersection.normal.x = v[A.POS_X] - intersection.closest.x;
				intersection.normal.y = v[A.POS_Y] - intersection.closest.y;
				intersection.normal.normalize();
			}
		}
		for (LineArray lineArray : lineArrays) {
			for (Line line : lineArray.createLines()) {

				// the closest point on line if inside ball radius
				float tempProjectedx = 0;
				float tempProjectedy = 0;
				float dist = 0;
				boolean intersected = false;

				// dot line with (ball - line endpoint)
				float rrr = (ball.x - line.v1.x) * (line.v2.x - line.v1.x) + (ball.y - line.v1.y) * (line.v2.y - line.v1.y);
				float len = A.length(line.v2.x - line.v1.x, line.v2.y - line.v1.y);
				float t = rrr / len / len;
				if (t >= 0 && t <= 1) {
					tempProjectedx = line.v1.x + (t * (line.v2.x - line.v1.x));
					tempProjectedy = line.v1.y + (t * (line.v2.y - line.v1.y));

					dist = A.length(ball.x - tempProjectedx, ball.y - tempProjectedy);
					intersected = (dist <= radius);
				} else {
					// center of ball is outside line segment. Check end points.
					dist = A.length(ball.x - line.v1.x, ball.y - line.v1.y);
					float distance2 = A.length(ball.x - line.v2.x, ball.y - line.v2.y);
					if (dist < radius) {
						intersected = true;
						tempProjectedx = line.v1.x;
						tempProjectedy = line.v1.y;
					}
					if (distance2 < radius && distance2 < dist) {
						intersected = true;
						tempProjectedx = line.v2.x;
						tempProjectedy = line.v2.y;
						dist = distance2;
					}
				}

				// store closest hit
				if (!intersection.foundCollision || dist < intersection.closestDistance) {
					intersection.closestDistance = dist;
					intersection.foundCollision = intersected;
					intersection.closest.x = tempProjectedx;
					intersection.closest.y = tempProjectedy;
					intersection.normal.x = v[A.POS_X] - intersection.closest.x;
					intersection.normal.y = v[A.POS_Y] - intersection.closest.y;
					intersection.normal.normalize();
				}
			}
		}
		
		return intersection;
	}

	public static Tuple getPosition(float[] d) {
		return new Tuple(d[A.POS_X], d[A.POS_Y]);
	}
}
