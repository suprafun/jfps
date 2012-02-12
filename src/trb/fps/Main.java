package trb.fps;

import trb.fps.client.FpsClient;
import trb.fps.server.FpsServer;
import trb.fps.net.HandshakePacket;
import trb.fps.net.LevelPacket;
import trb.fps.net.PlayerPacket;
import com.esotericsoftware.kryo.Kryo;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import trb.fps.editor.LevelEditor;
import trb.fps.net.BulletPacket;
import trb.fps.net.ChangeLevelPacket;

public class Main {

	public static void main(String[] args) {
		ConnectPanel connectPanel = new ConnectPanel();
		if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
                null, connectPanel, "Title", JOptionPane.OK_CANCEL_OPTION) ) {
            System.out.println("" + connectPanel.hostBtn.isSelected());
            FpsServer server = null;
            FpsClient client = new FpsClient();
            if (connectPanel.hostBtn.isSelected()) {
                server = startServer();
                server.gameLogic.level.killLimit = (Integer) connectPanel.killLimit.getValue();
                if (connectPanel.startEditor.isSelected()) {
                    LevelEditor.instance = new LevelEditor(server, client);
                    server.changeLevel(LevelEditor.instance.entities);
                }
            }
            client.gameLoop(connectPanel.nameTxt.getText());
            if (server != null) {
                server.stop();
            }
        }
	}

	private static FpsServer startServer() {
		FpsServer server = new FpsServer();
		server.start();
		server.botManager.addBot("Bot01");
		return server;
	}

	public static void initKryo(Kryo kryo) {
        kryo.register(float[].class);
        kryo.register(Input.class);
        kryo.register(PlayerPacket.class);
        kryo.register(PlayerPacket[].class);
        kryo.register(BulletPacket.class);
        kryo.register(BulletPacket[].class);
        //kryo.register(LevelPacket.class, new DeltaCompressor(kryo, kryo.newSerializer(LevelPacket.class)));
        kryo.register(LevelPacket.class);
        kryo.register(HandshakePacket.class);
        kryo.register(ArrayList.class);
        kryo.register(ChangeLevelPacket.class);
	}
}
