package trb.fps;

import trb.fps.client.Level;
import trb.fps.client.FpsRenderer;
import java.awt.Color;
import javax.vecmath.Color3f;
import trb.fps.net.LevelPacket;
import trb.fps.net.PlayerPacket;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import trb.fps.net.BulletPacket;
import trb.fps.server.GameLogic;
import trb.jsg.util.Vec3;

public class OrthoRenderer implements FpsRenderer {

	public void init(Level level) {
	}

	public void render(Level l, int localPlayerIdx) {
        LevelPacket level = l.levelData;

        // init OpenGL
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(-400, 400, 300, -300, 100, -100);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glDisable(GL11.GL_CULL_FACE);

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glColor3f(0.5f, 0.5f, 1.0f);

        for (PlayerPacket p : level.players) {
            if (p.isConnected()) {
                drawPlayer(p);
            }
        }

        for (BulletPacket bullet : level.bullets) {
//            if (bullet.alive) {
//                Vec3 bulletPos = GameLogic.getPositionAtTime(bullet, level.serverTimeMillis);
//                fillRect(Color.yellow, bulletPos.x-5, bulletPos.z-5, 10, 10);
//            }
        }

		Display.update();
	}

    private void drawPlayer(PlayerPacket p) {
        Vec3 pos = p.getPosition();
        Vec3 target = new Vec3().scaleAdd_(10, p.getHeadingVector(), pos);

        GL11.glBegin(GL11.GL_LINES);
        GL11.glColor3f(1f, 1f, 1f);
        GL11.glVertex2f(pos.x, pos.z);
        GL11.glVertex2f(target.x, target.z);
        GL11.glEnd();

        drawPlayerHealthBar(p);
    }

    private void drawPlayerHealthBar(PlayerPacket p) {
        Vec3 pos = p.getPosition();
        fillRect(p.getHealth() > 0 ? Color.WHITE : Color.RED, pos.x - 51, pos.z - 21, 102, 12);
        fillRect(Color.GREEN, pos.x - 50, pos.z - 20, 100 * p.getHealth() / 100, 10);
    }

    private void fillRect(Color color, float x, float y, float width, float height) {
        Color3f c = new Color3f(color);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor3f(c.x, c.y, c.z);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x, y + height);
        GL11.glEnd();
    }
}
