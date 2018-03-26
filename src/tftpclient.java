import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class tftpclient {

	byte opcode;
	String fileName = "beatUpSteven.txt";
	String serverAddress = "10.19.104.44";
	public static void main(String[] args) throws IOException {
		//String fileName = "TFTP.pdf";
		//tftpclient tFTPClientNet = new tftpclient();
		
	}
	public void readFile() throws IOException {
		opcode = 1;
		InetAddress InetServerAddress = InetAddress.getByName(serverAddress);
		DatagramSocket udpPacketSender = new DatagramSocket();
		byte[] packet = requestPacketByteArray(opcode, fileName);
		DatagramPacket packetToSend = new DatagramPacket(packet, packet.length, InetServerAddress, 69);
		udpPacketSender.send(packetToSend);
		
		
		udpPacketSender.get
	}
	public byte[] requestPacketByteArray(byte opcode, String fileName) {
		String mode = "octet";
		byte[] result = new byte[2 + fileName.length() + 1 + mode.length() + 1];
		int i = 0;
		result[i] = 0;
		i++;
		result[i] = opcode;
		i++;
		for(int k = 0; k < fileName.length(); k++,i++) {
			result[i] = (byte) fileName.charAt(k);
		}
		result[i] = 0;
		for (int k = 0; k < mode.length(); k++,i++) {
			result[i] = (byte) mode.charAt(k);
		}
		result[i] = 0;
		
		return result;
	}
}
