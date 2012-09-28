package trb.trials4k.torque;

import java.applet.Applet;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tomrbryn
 */
public class Torque extends Applet implements Runnable {

    enum Type {

        NORMAL, DEATH
    };

    static final int TARGET_FPS = 50;
    static final float FRICTION = 0.996f;
    static final float GRAVITY = 0.2f;
    static final boolean USE_DAMPING = true;
    static final int STROKE_WIDTH = 10;
    final int V_CNT = 21;
    final float WHEEL_RADIUS = 50;
    final int EDGE_ITER = 7;
    final float STIFF = 0.75f;
    final float DAMP = 0.001f;

    final List<Vertex> vertices = new ArrayList();
    final List<Edge> edges = new ArrayList();
    final List<Line> lines = new ArrayList();
    final List<ForceRect> forceRects = new ArrayList();
    static float gravity = GRAVITY;

    boolean[] k = new boolean[0x10000];

    static BufferedImage tile = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
    static TexturePaint paint;

    int frameIdx = 0;

    @Override
    public void start() {
        new Thread(this).start();
    }

    public void run() {
        createLevel1();
        createWheel();

        Graphics tileg = tile.getGraphics();
        tileg.setColor(Color.LIGHT_GRAY);
        tileg.fillOval(10, 10, tile.getWidth()-20, tile.getHeight()-20);
        paint = new TexturePaint(tile, new Rectangle2D.Float(0, 0, 32, 32));

        setSize(800, 600); // For AppletViewer, remove later.

        // Set up the graphics stuff, double-buffering.
        BufferedImage screen = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) screen.getGraphics();
        Graphics appletGraphics = getGraphics();
        requestFocusInWindow();

        long lastTime = System.nanoTime();

        int yieldCnt = 0;
        do {
            frameIdx++;
            if (k['r']) {
                createWheel();
            }
            if (k['1']) {
                createLevel1();
            }
            if (k['2']) {
                createLevel2();
            }
            if (k['3']) {
                createLevel3();
            }
            if (k['4']) {
                createLevel4();
            }
            if (k[Event.LEFT]) {
                System.out.println("left");
                applyTorque(-1f / WHEEL_RADIUS);
            }
            if (k[Event.RIGHT]) {
                System.out.println("right");
                applyTorque(1f / WHEEL_RADIUS);
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
        float cx = 100;
        float cy = 100;
        for (int i=0; i<V_CNT; i++) {
            double angleRad = 2f * Math.PI * i / (float) V_CNT;
            double x = cx + Math.cos(angleRad) * WHEEL_RADIUS;
            double y = cy + Math.sin(angleRad) * WHEEL_RADIUS;
            int color = 0xff000000 | (int) (Math.abs(Math.cos(angleRad)) * 0xff);
            vertices.add(new Vertex((float) x, (float) y, 10, 1f, color));
        }
        edges.clear();
        for (int i=0; i<vertices.size(); i++) {
            Vertex v = vertices.get(i);
            addEdge(v, i + 1, STIFF, DAMP);
            addEdge(v, i + 7, STIFF, DAMP);
        }
    }

    void createLevel1() {
        lines.clear();
        lines.add(new Line(0, 400, 200, 400, Type.NORMAL));
        lines.add(new Line(300, 500, 500, 500, Type.NORMAL));
        lines.add(new Line(750, 300, 750, 400, Type.NORMAL));
        lines.add(new Line(300, 200, 500, 200, Type.NORMAL));
        lines.add(new Line(300, 0, 300, 200, Type.NORMAL));
    }

    void createLevel2() {
        lines.clear();
        lines.add(new Line(0, 400, 300, 400, Type.NORMAL));
        lines.add(new Line(300, 400, 300, 300, Type.DEATH));
        lines.add(new Line(300, 300, 500, 300, Type.DEATH));
        lines.add(new Line(500, 300, 500, 400, Type.DEATH));
        lines.add(new Line(500, 400, 700, 400, Type.NORMAL));
        lines.add(new Line(0, 10, 1000, 10, Type.DEATH));
    }

    void createLevel3() {
        lines.clear();
        lines.add(new Line(0, 200, 200, 200, Type.NORMAL));
        lines.add(new Line(200, 200, 200, 600, Type.NORMAL));
        lines.add(new Line(600, 0, 375, 400, Type.NORMAL));
        lines.add(new Line(375, 400, 800, 400, Type.DEATH));
        lines.add(new Line(200, 600, 350, 600, Type.DEATH));
        lines.add(new Line(350, 600, 500, 600, Type.NORMAL));
        lines.add(new Line(500, 600, 650, 600, Type.DEATH));
        lines.add(new Line(650, 600, 800, 600, Type.NORMAL));
    }

    void createLevel4() {
        lines.clear();
        lines.add(new Line(0, 200, 200, 200, Type.NORMAL));
        forceRects.add(new ForceRect(0, 0, 800, 300, 0.02f, 0));
    }

    void addEdge(Vertex v, int idx2, float stiff, float damp) {
        if (idx2 >= vertices.size()) {
            idx2 -= V_CNT;
        }
        edges.add(new Edge(v, vertices.get(idx2), stiff, damp));
    }

    void applyTorque(float scale) {
        float[] c = getCenter();
        for (int i=0; i<vertices.size(); i++) {
            Vertex v = vertices.get(i);
            float dx = v.x - c[0];
            float dy = v.y - c[1];
            float tx = -dy;
            float ty = dx;
            v.x += tx * scale;
            v.x += ty * scale;
        }
    }

    float[] getCenter() {
        float[] c = {0, 0};
        for (Vertex v : vertices) {
            c[0] += v.x;
            c[1] += v.y;
        }
        c[0] /= vertices.size();
        c[1] /= vertices.size();
        return c;
    }

    void update() {
        for (ForceRect r : forceRects) {
            r.update(vertices);
        }
        for (Vertex v : vertices) {
            v.update();
        }
        for (int i = 0; i < EDGE_ITER; i++) {
            for (Edge e : edges) {
                e.update();
            }
        }

        for (Vertex v : vertices) {
            if (v.collide(lines) == Type.DEATH) {
                System.out.println("death");
                createWheel();
                break;
            }
        }
    }

    void draw(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 800, 600);
        for (ForceRect r : forceRects) {
            r.draw(g);
        }
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

        Type collide(List<Line> lines) {
            boolean foundCollision = false;
            float normalx = 0;
            float normaly = 1;
            float closestx = 0;
            float closesty = 0;
            float closestDistance = 10000000f;
            float ballx = x;
            float bally = y;
            float vradius = r + STROKE_WIDTH / 2;
            Type type = Type.NORMAL;
            Line closestLine = null;

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
                    closestLine = l;
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
                type = closestLine.type;
            }

            return type;
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
        Type type;

        public Line(float x1, float y1, float x2, float y2, Type type) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.type = type;
        }

        void draw(Graphics g) {
            if (type == Type.NORMAL) {
                g.setColor(Color.BLACK);
            } else {
                g.setColor(Color.RED);
            }
            g.drawLine(Math.round(x1), Math.round(y1), Math.round(x2), Math.round(y2));
        }
    }

    class ForceRect {
        int x;
        int y;
        int w;
        int h;
        float dirx;
        float diry;

        ForceRect(int x, int y, int w, int h, float dirx, float diry) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.dirx = dirx;
            this.diry = diry;
        }

        private void update(List<Vertex> vertices) {
            for (Vertex v : vertices) {
                v.x += dirx;
                v.y += diry;
            }
        }

        void draw(Graphics2D g) {
            g.setColor(Color.BLUE);
            g.setPaint(new TexturePaint(tile, new Rectangle2D.Float(100 * frameIdx * dirx, 100 * frameIdx * diry, 32, 32)));
            g.fillRect(Math.round(x), y, w, h);
        }
    }
}
