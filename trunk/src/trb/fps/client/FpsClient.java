package trb.fps.client;

import trb.fps.net.HandshakePacket;
import trb.fps.net.LevelPacket;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JOptionPane;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import trb.fps.Input;
import trb.fps.Main;
import trb.fps.OrthoRenderer;
import trb.fps.PlayerUpdator;
import trb.fps.editor.LevelEditor;
import trb.fps.entity.EntityList;
import trb.fps.entity.IO;
import trb.fps.input.InputManager;
import trb.fps.input.InputState;
import trb.fps.jsg.JsgDeferredRenderer;
import trb.fps.jsg.JsgRenderer;
import trb.fps.net.ChangeLevelPacket;
import trb.fps.net.PlayerPacket;
import trb.fps.net.ServerPacket;
import trb.fps.physics.PhysicsLevel;
import trb.jsg.View;
import trb.xml.XMLElement;

public class FpsClient {

    public static boolean useTopView = false;

    public final FpsRenderer orthoRenderer = new OrthoRenderer();
    public final FpsRenderer jsgRenderer = new JsgRenderer();
    public final JsgDeferredRenderer jsgDeferredRenderer = new JsgDeferredRenderer();
    public FpsRenderer renderer = jsgDeferredRenderer;
	private final Client client = new Client(1024*64, 1024*256);
	private int playerId = -1;
    public final Level level = new Level();
    final List<LevelPacket> in = Collections.synchronizedList(new ArrayList());
    public final InputManager inputManager = new InputManager();
    public final InputState inputState = new InputState();

	public void gameLoop(String name) {
		Main.initKryo(client.getKryo());
		try {
            client.start();
			client.connect(5000, "localhost", 54555, 54777);
			client.addListener(new Listener() {

				@Override
				public void received(Connection connection, Object object) {
					//System.out.println("client received: " + object + " from " + connection.getID());
					if (object instanceof HandshakePacket) {
						playerId = ((HandshakePacket) object).playerId;
                    } else if (object instanceof ChangeLevelPacket) {
                        ChangeLevelPacket changeLevelPacket = (ChangeLevelPacket) object;
                        try {
                            XMLElement xml = new XMLElement(changeLevelPacket.levelXml);
                            System.out.println(xml.toString());
                            EntityList entities = new EntityList(IO.readLevel(xml.getFirstChildWithName("level")));
                            level.changeLevel(entities);
                            if (LevelEditor.instance == null) {
                                jsgDeferredRenderer.deferredSystem.recreate(entities);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else if (object instanceof LevelPacket) {
						level.levelData = (LevelPacket) object;
                        in.add(level.levelData);
					}
				}

				@Override
				public void disconnected(Connection connection) {
					System.out.println("client disconnected " + connection);
					playerId = -2;
				}
			});
			client.sendTCP(new HandshakePacket(name, -1));

	//		Mouse.setGrabbed(true);

            initDisplay();

            orthoRenderer.init(level);
            jsgRenderer.init(level);
            jsgDeferredRenderer.init(level);
			//renderer.init();

            long fpsTime = System.currentTimeMillis();
            int fpsCounter = 0;
            long startTime = System.nanoTime();
			while (isRunning()) {
                long now = (System.nanoTime() - startTime) / 1000000;

                inputState.poll();

                Input input = createInput(now, level.interpolatedServerState.getCurrentState().serverTime);
                sendInput(input);

                if (level.editorNavigation.enabled.get()) {
                    level.editorNavigation.handleMouseEvent(inputState);
                }

                //System.out.println(level.physicsLevel + " " + level.physicsLevel.dynamicsWorld.getCollisionObjectArray().size());
                level.predictedState.update(new PlayerUpdator(input, level.physicsLevel));
                LevelPacket levelData = null;
                if (!in.isEmpty()) {
                    synchronized (in) {
                        levelData = in.get(in.size()-1);
                        in.clear();
                    }
                    level.predictedState.correct(levelData.getPlayer(playerId));
                }

                for (int i = 0; i < level.interpolatedState.size(); i++) {
                    PlayerPacket fromServer = (levelData == null ? null : levelData.players[i]);
                    level.interpolatedState.get(i).update(now, fromServer);
                }

                level.interpolatedServerState.update(now, levelData == null ? null : new ServerPacket(levelData.serverTimeMillis));

				renderer.render(level, level.levelData.getPlayerIndex(playerId));

                fpsCounter++;
                long fpsNow = System.currentTimeMillis();
                if (fpsNow > fpsTime + 1000) {
                    Display.setTitle("" + fpsCounter);
                    fpsTime = fpsNow;
                    fpsCounter = 0;
                }

                //Thread.sleep(15);
				//Display.sync(60);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Something went wrong");
			System.exit(0);
		}
        if (playerId < -1) {
            JOptionPane.showMessageDialog(null, "You were disconnected");
        }
		client.stop();
		Display.destroy();
		System.exit(0);
	}

    private void initDisplay() {
        // Use LWJGL to create a frame
        int windowWidth = 800;
        int windowHeight = 600;
        try {
            Display.setLocation(0, 0);
            Display.setDisplayMode(new DisplayMode(windowWidth, windowHeight));
            Display.create(new PixelFormat().withStencilBits(8));
        } catch (LWJGLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to initialise LWJGL");
            System.exit(0);
        }
    }

	private boolean isRunning() {
		return !Display.isCloseRequested() 
				&& !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)
				&& playerId >= -1;
	}

	private void sendInput(Input input) {
		client.sendTCP(input);
	}

    /**
     * Polls Keyboard and Mouse. Drains Mouse events.
     */
    Input createInput(long time, long serverTime) {
        if (inputState.wasKeyPressed(Keyboard.KEY_RETURN)) {
            client.sendTCP("respawn");
        }
        if (inputState.wasKeyPressed(Keyboard.KEY_HOME)) {
            level.editorNavigation.enabled.set(!level.editorNavigation.enabled.get());
        }
        if (inputState.wasKeyPressed(Keyboard.KEY_V)) {
            View.useFrustumCulling = !View.useFrustumCulling;
        }
        if (inputState.wasKeyPressed(Keyboard.KEY_F1)) {
            renderer = orthoRenderer;
        }
        if (inputState.wasKeyPressed(Keyboard.KEY_F2)) {
            renderer = jsgRenderer;
        }
        boolean mouseButton1pressed = inputState.wasButtonPressed(0);
        if (mouseButton1pressed && level.levelData.getPlayer(playerId).getHealth() <= 0) {
            client.sendTCP("respawn");
        }
        int moveX = 0;
        int moveY = 0;
        moveX += Keyboard.isKeyDown(Keyboard.KEY_A) ? -1 : 0;
        moveX += Keyboard.isKeyDown(Keyboard.KEY_D) ? 1 : 0;
        moveY += Keyboard.isKeyDown(Keyboard.KEY_W) ? 1 : 0;
        moveY += Keyboard.isKeyDown(Keyboard.KEY_S) ? -1 : 0;
        return new Input(
                time,
                serverTime,
                moveX,
                moveY,
                Mouse.isButtonDown(1) ? inputState.mouseDX : 0,
                Mouse.isButtonDown(1) ? inputState.mouseDY : 0,
                mouseButton1pressed);
    }
}


	