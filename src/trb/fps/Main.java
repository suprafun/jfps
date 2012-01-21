package trb.fps;

import trb.fps.model.LevelData;
import trb.fps.model.PlayerData;
import com.esotericsoftware.kryo.Kryo;
import javax.swing.JOptionPane;
import trb.fps.model.BulletData;

public class Main {

	public static void main(String[] args) {
		ConnectPanel connectPanel = new ConnectPanel();
		if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
                null, connectPanel, "Title", JOptionPane.OK_CANCEL_OPTION) ) {
            System.out.println("" + connectPanel.serverBtn.isSelected());
            FpsServer server = null;
            if (connectPanel.serverBtn.isSelected()) {
                server = startServer();
            }
            FpsClient fpsClient = new FpsClient();
            fpsClient.gameLoop(connectPanel.nameTxt.getText());
            if (server != null) {
                server.stop();
            }
        }
	}

	private static FpsServer startServer() {
		FpsServer server = new FpsServer();
		server.start();
		return server;
	}

	public static void initKryo(Kryo kryo) {
        kryo.register(float[].class);
        kryo.register(Input.class);
        kryo.register(PlayerData.class);
        kryo.register(PlayerData[].class);
        kryo.register(BulletData.class);
        kryo.register(BulletData[].class);
        kryo.register(LevelData.class);
		kryo.register(Handshake.class);
	}
}
