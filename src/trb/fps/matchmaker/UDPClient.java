package trb.fps.matchmaker;

import java.net.*;

class UDPClient {

	public static void main(String args[]) throws Exception {
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName("localhost");
		byte[] data = "Hello World".getBytes();
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, 9876);
		clientSocket.send(sendPacket);
		byte[] receiveData = new byte[1024];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(receivePacket);
		System.out.println("FROM SERVER:" + new String(receivePacket.getData()));
		clientSocket.close();
	}
}
