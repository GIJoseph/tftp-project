import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class tftpclient {

	public static void main(String[] args) throws IOException {
		//String fileName = "TFTP.pdf";
		//tftpclient tFTPClientNet = new tftpclient();
		InetAddress net = InetAddress.getByName("10.19.104.44");
		System.out.println(net.toString());
	}
	public void getFile() {
		DatagramPacket dp = new DatagramPacket(null, 0);
	}
}
