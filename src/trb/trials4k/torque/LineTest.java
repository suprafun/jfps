package trb.trials4k.torque;

import java.applet.Applet;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tomrbryn
 */
public class LineTest extends Applet implements Runnable {

    static final int TARGET_FPS = 50;
    static final float FRICTION = 1f;//0.996f;
    static final float GRAVITY = 0f;//0.2f;
    static final boolean USE_DAMPING = false;
    static final int STROKE_WIDTH = 10;
    final int V_CNT = 21;
    final float WHEEL_RADIUS = 50;
    final int EDGE_ITER = 1;
    final float STIFF = 0.099f;
    final float damp = 0.001f;

    final List<Vertex> vertices = new ArrayList();
    final List<Edge> edges = new ArrayList();
    final List<Line> lines = new ArrayList();
    static float gravity = GRAVITY;

    boolean[] k = new boolean[0x10000];

    @Override
    public void start() {
        new Thread(this).start();
    }

    public void run() {
        createWheel();

        setSize(800, 600); // For AppletViewer, remove later.

        // Set up the graphics stuff, double-buffering.
        BufferedImage screen = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) screen.getGraphics();
        Graphics appletGraphics = getGraphics();
        requestFocusInWindow();

        long lastTime = System.nanoTime();

        int yieldCnt = 0;
        do {
            if (k['r']) {
                createWheel();
            }
            if (k[Event.LEFT]) {
                System.out.println("left");
            }
            if (k[Event.RIGHT]) {
                System.out.println("right");
            }
            gravity = GRAVITY;
            if (k[Event.UP]) {
                gravity = -GRAVITY;
            }
            if (k[Event.DOWN]) {
                gravity += GRAVITY;
            }
            long now = System.nanoTime();
            lastTime = now;
            update();
            draw(g);
            g.drawString("yieldCnt: " + yieldCnt, 10, 20);
            appletGraphics.drawImage(screen, 0, 0, null);
            yieldCnt = 0;
            do {
                yieldCnt++;
                Thread.yield();
                //try {Thread.sleep(5);} catch (Exception ex) {}
            } while (System.nanoTime() - lastTime < (1000000000 / TARGET_FPS));
        } while (isActive());
    }

    @Override
    public boolean handleEvent(Event e) {
        switch (e.id) {
            case Event.KEY_ACTION:
            case Event.KEY_PRESS:
                k[e.key] = true;
                break;
            case Event.KEY_ACTION_RELEASE:
            case Event.KEY_RELEASE:
                k[e.key] = false;
                break;
        }
        return false;
    }

    void createWheel() {
        vertices.clear();
        vertices.add(new Vertex(400, 300, 10, 1f, 0xff000000));
        vertices.add(new Vertex(400, 350, 10, 100f, 0xff000000));
        edges.clear();
        edges.add(new Edge(vertices.get(0), vertices.get(1), STIFF, damp));
        edges.get(0).length = 80;
        lines.clear();
        lines.add(new Line(0, 400, 1000, 400));
    }

    void update() {
        for (Vertex v : vertices) {
            v.update();
        }
        for (Edge e : edges) {
            e.update();
        }
        for (Vertex v : vertices) {
            v.collide(lines);
        }
    }

    void draw(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 800, 600);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (Line l : lines) {
            l.draw(g);
        }
        g.setStroke(new BasicStroke(1));
        for (Vertex v : vertices) {
            v.draw(g);
        }
        for (Edge e : edges) {
            e.draw(g);
        }
    }

    static float length(Vertex v1, Vertex v2) {
        return length(v1.x, v1.y, v2.x, v2.y);
    }

    static float length(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return length(dx, dy);
    }

    static float length(float dx, float dy) {
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    static float dot(float dx1, float dy1, float dx2, float dy2) {
        return dx1 * dx2 + dy1 * dy2;
    }

    static class Vertex {
        float x;
        float y;
        float oldx;
        float oldy;
        float r;
        float mass;
        Color color;

        public Vertex(float x, float y, float r, float mass, int color) {
            this.x = x;
            this.y = y;
            this.oldx = x;
            this.oldy = y;
            this.r = r;
            this.mass = mass;
            this.color = new Color(color);
        }

        void update() {
            float tempX = x;
            float tempY = y;
            x += FRICTION * (x - oldx);
            y += FRICTION * (y - oldy + gravity);
            oldx = tempX;
            oldy = tempY;
        }

        void collide(List<Line> lines) {
            boolean foundCollision = false;
            float normalx = 0;
            float normaly = 1;
            float closestx = 0;
            float closesty = 0;
            float closestDistance = 10000000f;
            float ballx = x;
            float bally = y;
            float vradius = r + STROKE_WIDTH / 2;

//            if (USE_CIRCLES) {
//                for (int circleIdx = 0; circleIdx < f[CIRCLE_CNT_OFF]; circleIdx++) {
//                    float circlex = f[CIRCLE_OFF + OBJECT_STRIDE * circleIdx + X1];
//                    float circley = f[CIRCLE_OFF + OBJECT_STRIDE * circleIdx + Y1];
//                    float radius = f[CIRCLE_OFF + OBJECT_STRIDE * circleIdx + CIRCLE_RADIUS];
//                    float dx = ballx - circlex;
//                    float dy = bally - circley;
//                    float length = length(dx, dy);
//                    float dist = length - radius;
//                    boolean intersected = dist < vradius;
//                    if (dist < closestDistance) {
//                        closestDistance = dist;
//                        foundCollision = intersected;
//                        closestx = circlex + (dx / length * radius);
//                        closesty = circley + (dy / length * radius);
//                        normalx = v[POS_X] - closestx;
//                        normaly = v[POS_Y] - closesty;
//                        float normalLength = length(normalx, normaly);
//                        normalx /= normalLength;
//                        normaly /= normalLength;
//                    }
//                }
//            }
            //for (int lineIdx = 0; lineIdx < f[LINE_CNT_OFF]; lineIdx++) {
            for (Line l : lines) {
                float linex1 = l.x1;
                float liney1 = l.y1;
                float linex2 = l.x2;
                float liney2 = l.y2;

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
                if (dist < closestDistance) {
                    closestDistance = dist;
                    foundCollision = intersected;
                    closestx = tempProjectedx;
                    closesty = tempProjectedy;
                    normalx = x - closestx;
                    normaly = y - closesty;
                    float normalLength = length(normalx, normaly);
                    normalx /= normalLength;
                    normaly /= normalLength;
                }
            }

            // ---------------- end collision detection --------------------
            if (foundCollision) {
//                float velx = x - oldx;
//                float vely = y - oldy;
//                float normalDotVel = dot(velx, vely, normalx, normaly);
//                oldx += normalx * normalDotVel * 2;
//                oldy += normaly * normalDotVel * 2;
//                velx = x - oldx;
//                vely = y - oldy;


//                if (length(closestx - x, closesty - y) < vradius * 0.99f) {
//                    x = (normalx * vradius * 0.99f + closestx);
//                    y = (normaly * vradius * 0.99f + closesty);
//                }

                x = oldx;
                y = oldy;
            }
        }

        void draw(Graphics g) {
            g.setColor(color);
            g.fillOval(Math.round(x-r), Math.round(y-r), Math.round(r*2), Math.round(r*2));
        }
    }

    static class Edge {
        Vertex v1;
        Vertex v2;
        float stiff = 1f;
        float length = 1f;
        float damp = 0f;
        float minLength = 1;
        float maxLength = 1;
        float oo_totalMass = 1f;

        public Edge(Vertex v1, Vertex v2, float stiff, float damp) {
            this.v1 = v1;
            this.v2 = v2;
            this.stiff = stiff;
            this.damp = damp;
            this.length = length(v1, v2);
            this.minLength = length / 2f;
            this.maxLength = length * 2f;
            this.oo_totalMass = 1 / (v1.mass * v2.mass);
        }

        void update() {
            float v1v2x = v2.x - v1.x;
            float v1v2y = v2.y - v1.y;

            // fast square root
            float x = v1v2x * v1v2x + v1v2y * v1v2y;
            float guess = length;
            float v1v2Length = (guess + x / guess) * 0.5f;

            float diff = v1v2Length - length;
            float adjustment = diff * stiff;
            if (v1v2Length - adjustment < minLength) {
                adjustment = v1v2Length - minLength;
                damp = 0;
            }
            if (v1v2Length - adjustment > maxLength) {
                adjustment = v1v2Length - maxLength;
                damp = 0;
            }

            float ooLength = 1f / v1v2Length;
            v1v2x *= ooLength;
            v1v2y *= ooLength;

            float mf1 = v2.mass * oo_totalMass;
            float mf2 = v1.mass * oo_totalMass;

            // Push both vertices apart by half of the difference respectively so the distance between them equals the original length
            v1.x += v1v2x * adjustment * mf1 * 0.5f;
            v1.y += v1v2y * adjustment * mf1 * 0.5f;
            v2.x -= v1v2x * adjustment * mf2 * 0.5f;
            v2.y -= v1v2y * adjustment * mf2 * 0.5f;

            if (USE_DAMPING) {
                float velDiffx = (v2.x - v2.oldx) - (v1.x - v1.oldx);
                float velDiffy = (v2.y - v2.oldy) - (v1.y - v1.oldy);

                velDiffx *= (damp * 0.5f);
                velDiffy *= (damp * 0.5f);
                v1.x += velDiffx;
                v1.y += velDiffy;
                v2.x -= velDiffx;
                v2.y -= velDiffy;
            }
        }

        void draw(Graphics g) {
            g.drawLine(Math.round(v1.x), Math.round(v1.y), Math.round(v2.x), Math.round(v2.y));
        }
    }

    static class Line {
        float x1;
        float y1;
        float x2;
        float y2;

        public Line(float x1, float y1, float x2, float y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        void draw(Graphics g) {
            g.drawLine(Math.round(x1), Math.round(y1), Math.round(x2), Math.round(y2));
        }
    }
}
