package trb.fps;

import trb.fps.model.LevelData;
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
import trb.fps.editor.LevelEditor;
import trb.fps.input.InputManager;
import trb.fps.input.InputState;
import trb.fps.jsg.JsgDeferredRenderer;
import trb.fps.jsg.JsgRenderer;
import trb.fps.model.PlayerData;
import trb.fps.model.ServerData;
import trb.jsg.View;

public class FpsClient {

    public static boolean useTopView = false;

    private final LevelGenerator levelGenerator = new LevelGenerator();
    private final LevelEditor levelEditor = new LevelEditor(levelGenerator);

    public final FpsRenderer orthoRenderer = new OrthoRenderer();
    public final FpsRenderer jsgRenderer = new JsgRenderer();
    public final FpsRenderer jsgDeferredRenderer = new JsgDeferredRenderer(levelGenerator);
    public FpsRenderer renderer = jsgDeferredRenderer;
	private final Client client = new Client();
	private int playerIdx = -1;
    public final Level level = new Level();
    final List<LevelData> in = Collections.synchronizedList(new ArrayList());
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
					if (object instanceof Handshake) {
						playerIdx = ((Handshake) object).playerIdx;
					} else if (object instanceof LevelData) {
						level.levelData = (LevelData) object;
                        in.add(level.levelData);
					}
				}

				@Override
				public void disconnected(Connection connection) {
					System.out.println("client disconnected " + connection);
					playerIdx = -2;
				}
			});
			client.sendTCP(new Handshake(name, -1));

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

                level.predictedState.update(new PlayerUpdator(input, level.character, level.physicsLevel));
                LevelData levelData = null;
                if (!in.isEmpty()) {
                    synchronized (in) {
                        levelData = in.get(in.size()-1);
                        in.clear();
                    }
                    level.predictedState.correct(levelData.players[playerIdx]);
                }

                for (int i = 0; i < level.interpolatedState.size(); i++) {
                    PlayerData fromServer = (levelData == null ? null : levelData.players[i]);
                    level.interpolatedState.get(i).update(now, fromServer);
                }

                level.interpolatedServerState.update(now, levelData == null ? null : new ServerData(levelData.serverTimeMillis));

				renderer.render(level, playerIdx);

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
        if (playerIdx < -1) {
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
				&& playerIdx >= -1;
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
            System.out.println("AAAAAAAAAAAAAAAAAAAAA");
            level.editorNavigation.enabled.set(!level.editorNavigation.enabled.get());
        }
        if (inputState.wasKeyPressed(Keyboard.KEY_V)) {
            View.useFrustumCulling = !View.useFrustumCulling;
            System.out.println("useFrustumCulling = " + View.useFrustumCulling);
        }
        if (inputState.wasKeyPressed(Keyboard.KEY_F1)) {
            renderer = orthoRenderer;
        }
        if (inputState.wasKeyPressed(Keyboard.KEY_F2)) {
            renderer = jsgRenderer;
        }
        boolean mouseButton1pressed = inputState.wasButtonPressed(0);
        if (mouseButton1pressed && level.levelData.players[playerIdx].getHealth() <= 0) {
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


	