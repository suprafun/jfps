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
public class Spring extends Applet implements Runnable {

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

    static float gravity = GRAVITY;

    boolean[] k = new boolean[0x10000];

    @Override
    public void start() {
        new Thread(this).start();
    }

    public void run() {
        setSize(800, 600); // For AppletViewer, remove later.

        // Set up the graphics stuff, double-buffering.
        BufferedImage screen = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) screen.getGraphics();
        Graphics appletGraphics = getGraphics();
        requestFocusInWindow();


        float cx = 400;
        float cy = 300;
        float r = 10;

        float y = 200;
        float vy = 0;

        float _y = 400;
        float _oldy = 400;


        long lastTime = System.nanoTime();

        int yieldCnt = 0;
        do {
            if (k['r']) {
                y = 200;
                vy = 0;
            }

            long now = System.nanoTime();
            lastTime = now;

            float t = 1;
            float ay = (cy - y) * 0.01f;
            vy += ay;
            y += vy;

            _y += (cy - _y) * 0.01f;
            float tempY = _y;
            _y += (_y - _oldy);
            _oldy = tempY;


            g.setColor(Color.WHITE);
            g.fillRect(0, 0, screen.getWidth(), screen.getHeight());
            g.setColor(Color.BLACK);
            g.fillOval(Math.round(cx - r), Math.round(cy - r), Math.round(r * 2), Math.round(r * 2));
            g.fillOval(Math.round(cx - r), Math.round(y - r), Math.round(r * 2), Math.round(r * 2));
            g.fillOval(Math.round(cx - r - 100), Math.round(_y - r), Math.round(r * 2), Math.round(r * 2));

            g.drawString("yieldCnt: " + yieldCnt, 10, 20);
            g.drawString("y: " + y, 10, 40);
            g.drawString("vy: " + vy, 10, 60);
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
}
