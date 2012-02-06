package trb.fps.server;

import trb.fps.net.HandshakePacket;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import java.io.IOException;
import trb.fps.Input;
import trb.fps.Main;
import trb.fps.entity.EntityList;
import trb.fps.entity.IO;
import trb.fps.net.ChangeLevelPacket;

public class FpsServer {

    private final KryonetListener kryonetListener = new KryonetListener();
	private final Server server = new Server(1024*256, 1024*256);
    public final GameLogic gameLogic = new GameLogic();
    private boolean running = true;
    private EntityList newLevel;

	public void start() {
		try {
			Main.initKryo(server.getKryo());
            Thread thread = new Thread(new Runnable() {

                public void run() {
                    serverGameLoop();
                }
            });
            thread.start();
			server.bind(54555, 54777);
			server.addListener(kryonetListener);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

    private void serverGameLoop() {
        try {
            while (isRunning()) {
                sendChangeLevel();
                server.update(0);
                gameLogic.update();
                for (Connection connection : server.getConnections()) {
                    connection.sendTCP(gameLogic.level);
                    //System.out.println("" + connection.sendTCP(gameLogic.level));
                }
                server.update(0);
                Thread.sleep(100);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void sendChangeLevel() {
        if (newLevel == null) {
            return;
        }

        gameLogic.changeLevel(newLevel);
        ChangeLevelPacket changeLevelPacket = createChangeLevelPacket(newLevel);
        for (Connection connection : server.getConnections()) {
            connection.sendTCP(changeLevelPacket);
        }
        newLevel = null;
    }

    private ChangeLevelPacket createChangeLevelPacket(EntityList entities) {
        String levelStr = IO.writeLevel(entities.getAll()).toString();
        return new ChangeLevelPacket(levelStr);
    }

    public void changeLevel(EntityList level) {
        newLevel = level;
    }

	public boolean isRunning() {
		return running;
	}

	public void stop() {
		running = false;
		server.stop();
	}

	class KryonetListener extends Listener {
        
        @Override
        public void received(Connection connection, Object object) {
            if (object instanceof HandshakePacket) {
                HandshakePacket handshake = (HandshakePacket) object;
                boolean success = gameLogic.addPlayer(connection.getID(), handshake.name);
                handshake = new HandshakePacket(handshake.name, success ? connection.getID() : -3);
                connection.sendTCP(handshake);
                connection.sendTCP(createChangeLevelPacket(gameLogic.entityList));
            } else if (object instanceof Input) {
                gameLogic.addInput(connection.getID(), (Input) object);
            } else if (object instanceof String) {
                if ("respawn".equals(object)) {
                    gameLogic.respawn(connection.getID());
                }
            }
        }

        @Override
        public void connected(Connection connection) {
        }

        @Override
        public void disconnected(Connection connection) {
            gameLogic.removePlayer(connection.getID());
        }
	}
}
