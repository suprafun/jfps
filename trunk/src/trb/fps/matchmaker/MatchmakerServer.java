package trb.fps.matchmaker;

import com.esotericsoftware.kryo.Kryo;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import javax.swing.JFrame;

public class MatchmakerServer {
	public static void main(String args[]) throws Exception {
		final DatagramSocket serverSocket = new DatagramSocket(9876);
		new Thread(new Runnable() {

			public void run() {
				Kryo kryo = new Kryo();
				kryo.register(byte[].class);
				kryo.register(ClientInfo.class);
				byte[] data = new byte[1024 * 4];
				ByteBuffer buffer = ByteBuffer.wrap(data);
				while (true) {
					try {
						DatagramPacket receivePacket = new DatagramPacket(data, data.length);
						serverSocket.receive(receivePacket);

						buffer.rewind();
//						String message = kryo.readObject(buffer, String.class);
						ClientInfo message = kryo.readObject(buffer, ClientInfo.class);

						System.out.println("RECEIVED: " + message);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}).start();

		Kryo kryo = new Kryo();
		kryo.register(byte[].class);
		kryo.register(ClientInfo.class);
		byte[] data = new byte[1024 * 4];
		ByteBuffer buffer = ByteBuffer.wrap(data);

		for (int i=0; i<10; i++) {
			buffer.rewind();
			kryo.writeObject(buffer, new ClientInfo());
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName("localhost");
			DatagramPacket sendPacket = new DatagramPacket(data, buffer.position(), IPAddress, 9876);
			clientSocket.send(sendPacket);
		}


		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public static class ClientInfo {
		long time;
		byte[] address;
		int port;

		public ClientInfo() {

		}
	}
}
