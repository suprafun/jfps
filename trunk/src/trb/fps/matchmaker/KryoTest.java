package trb.fps.matchmaker;

import com.esotericsoftware.kryo.Kryo;
import java.nio.ByteBuffer;

public class KryoTest {

	byte[] address = {1, 2, 3, 4};

	public static void main(String[] args) {
		byte[] bytes = new byte[1024];
		ByteBuffer buffer = ByteBuffer.wrap(bytes);

		Kryo kryo = new Kryo();
		kryo.register(KryoTest.class);
		kryo.register(byte[].class);
		kryo.writeObject(buffer, "A");
		buffer.rewind();
		String someObject = kryo.readObject(buffer, String.class);
		buffer.rewind();
		System.out.println(someObject);
		kryo.writeObject(buffer, "Hello world");
		buffer.rewind();
		System.out.println(kryo.readObject(buffer, String.class));
	}
}
