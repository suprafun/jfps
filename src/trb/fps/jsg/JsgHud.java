package trb.fps.jsg;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.lwjgl.opengl.Display;
import trb.fps.net.LevelPacket;
import trb.fps.net.PlayerPacket;
import trb.jsg.RenderPass;
import trb.jsg.View;

public class JsgHud {

    public RenderPass renderPass;

    private TexturedQuad quad;

    private final Font font = new Font("SansSerif", Font.BOLD, 14);

    public JsgHud() {
        // ortho mode with a 1:1 mapping to the screen
        View view = new View();
        int w = Display.getDisplayMode().getWidth();
        int h = Display.getDisplayMode().getHeight();
        view.ortho(0, w, 0, h, -1000, 1000);

        // create a renderpass that renders to the screen
        renderPass = new RenderPass();
        renderPass.setView(view);

        quad = new TexturedQuad(0, 0, 256, 256);
        renderPass.getRootNode().addShape(quad.shape);
        renderPass.getRootNode().addShape(createCrosshair(w, h).shape);
    }

    private TexturedQuad createCrosshair(int w, int h) {
        TexturedQuad crosshair = new TexturedQuad(w/2-32, h/2-32, 64, 64);

        Graphics2D g = (Graphics2D) crosshair.image.getGraphics();
        Composite originalComposite = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, 0));
        g.fillRect(0, 0, 64, 64);
        g.setComposite(originalComposite);

        g.setColor(Color.GREEN);
        g.drawLine(32, 0, 32, 24);
        g.drawLine(32, 39, 32, 64);
        g.drawLine(0, 32, 24, 32);
        g.drawLine(39, 32, 64, 32);
        
        crosshair.copyImageToTexture();
        return crosshair;
    }

    private void renderImage(BufferedImage image, LevelPacket level, int localPlayerIdx) {
        List<PlayerPacket> sortedPlayers = getPlayersSortedByKills(level);

        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Composite originalComposite = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, 0));
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.setComposite(originalComposite);
        g.setFont(font);
        for (int i=0; i<sortedPlayers.size(); i++) {
            PlayerPacket p = sortedPlayers.get(i);
            if (p.isConnected()) {
                int y = 20 + i * 20;
                if (i == localPlayerIdx) {
                    g.setPaint(new Color(200, 200, 64, 120));
                } else {
                    g.setPaint(new Color(0, 0, 0, 100));
                }
                g.fillRect(3, y - 13, 256, 16);

                g.setPaint(new Color(0xffffffff, true));
                g.drawString("" + p.getKills(), 5, y);
                g.drawString("" + p.getDeaths(), 25, y);
                g.drawString("" + p.getHealth(), 45, y);
                g.drawString(p.getName(), 75, y);
            }
        }


        if (level.isGameOver()) {
            int y = 30 + (level.players.length+1) * 20;
            g.setPaint(new Color(0, 0, 0, 100));
            g.fillRect(3, y - 13, 256, 16);

            g.setPaint(new Color(0xffffffff, true));
            g.drawString("Game Over", 3, y);
        } else {
            if (level.players[localPlayerIdx].getHealth() <= 0) {
                int y = 30 + level.players.length * 20;
                g.setPaint(new Color(0, 0, 0, 100));
                g.fillRect(3, y - 13, 256, 16);

                g.setPaint(new Color(0xffffffff, true));
                g.drawString("Press ENTER to respawn", 3, y);
            }
        }
    }

    private List<PlayerPacket> getPlayersSortedByKills(LevelPacket level) {
        List<PlayerPacket> connectedPlayers = level.getConnectedPlayers();
        Collections.sort(connectedPlayers, new Comparator<PlayerPacket>() {

            public int compare(PlayerPacket o1, PlayerPacket o2) {
                return o1.getKills() - o2.getKills();
            }
        });

        return connectedPlayers;
    }

    public void render(LevelPacket level, int localPlayerIdx) {
        renderImage(quad.image, level, localPlayerIdx);
        quad.copyImageToTexture();
    }
}
