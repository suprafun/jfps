package trb.trials4k;


import java.awt.Event;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import static java.lang.Math.*;

public class Wheel2 extends Applet implements Runnable {

    public static final int TARGET_FPS = 50;
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

//        List<Level> levels = LevelIO.read(getClass().getResourceAsStream("data.bin"));
//        System.out.println("levelCnt "+levels.size());
//        Level level = levels.get(0);

        LineArray lineArray = new LineArray();
        lineArray.points.add(new Tuple(50, 300));
        lineArray.points.add(new Tuple(200, 400));
        lineArray.points.add(new Tuple(400, 400));
        lineArray.points.add(new Tuple(600, 300));

        Level level = new Level();
        level.lineArrays.add(lineArray);
        level.checkpoints.add(new Checkpoint(0, -1300));

        Tuple start = level.checkpoints.get(0).position;
        System.out.println("start " + start);

        long lastTime = System.nanoTime();

        float[] v = new float[16];
        v[A.POS_X] = 300;
        v[A.POS_Y] = 300;
        v[A.OLD_X] = v[A.POS_X] - .2f;
        v[A.OLD_Y] = v[A.POS_Y];
        v[A.RADIUS] = 50;
        v[A.COLLIDABLE] = 1;

        // Game loop.
        do {
            if (k['r']) {
                v[A.POS_X] = 300;
                v[A.POS_Y] = 300;
                v[A.OLD_X] = v[A.POS_X] - .2f;
                v[A.OLD_Y] = v[A.POS_Y];
            }

            float wheelAcc = 0;

            if (k[Event.LEFT]) {
                wheelAcc -= .005f;
            }
            if (k[Event.RIGHT]) {
                wheelAcc += .005f;
            }


//
//            // ----------- update body ---------------
//            for (float[] v : vertices) {
//                updateVertex(v);
//            }

            v[A.ACC_Y] = A.GRAVITY / 100;

            A.updateVertex(v);


            Intersection intersection = level.collide(v);
            if (intersection.foundCollision) {
                if (Level.getPosition(v).distance(intersection.closest) < v[A.RADIUS] * 0.95f) {
                    v[A.POS_X] = (intersection.normal.x * v[A.RADIUS] * 0.95f + intersection.closest.x);
                    v[A.POS_Y] = (intersection.normal.y * v[A.RADIUS] * 0.95f + intersection.closest.y);
                }
                v[A.POS_X] += (-intersection.normal.y * wheelAcc);
                v[A.POS_Y] += (intersection.normal.x * wheelAcc);
            }

            // Render
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, 1024, 1024);

            g.setColor(Color.WHITE);
            level.draw(g);

            g.setColor(Color.PINK);
            float r = v[A.RADIUS];
            g.fillOval(round(v[A.POS_X] - r), round(v[A.POS_Y] - r), round(r * 2), round(r * 2));
            g.setColor(Color.WHITE);
//            g.drawLine(round(v[A.POS_X]), round(v[A.POS_Y]),
//                    round(v[A.POS_X] + (float) Math.cos(v[ANGLE]) * r),
//                    round(v[A.POS_Y] + (float) Math.sin(v[ANGLE]) * r));

            // Draw the entire results on the screen.
            appletGraphics.drawImage(screen, 0, 0, null);

            do {
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
                //changed[e.key] = k[e.key] == false;
                k[e.key] = true;
                break;
            case Event.KEY_ACTION_RELEASE:
            case Event.KEY_RELEASE:
                //changed[e.key] = k[e.key] == true;
                k[e.key] = false;
                break;
        }
        return false;
    }
}
