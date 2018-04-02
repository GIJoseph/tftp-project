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
import java.util.Arrays;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

public class tftpclient {

	static byte opcode;
	static String fileName = "testfile.txt";
	static String serverAddress = "192.168.0.6";
	static DatagramPacket packetToSend;
	static InetAddress InetServerAddress;
	static DatagramPacket packetToRecieve;
	static DatagramSocket udpPacketSender;
	static ByteArrayOutputStream byteStream;
	
	public static void main(String[] args) throws IOException {
		//String fileName = "TFTP.pdf";
		//tftpclient tFTPClientNet = new tftpclient();
		readFile();
		
	}
	public static void readFile() throws IOException {
		opcode = 1;
			InetServerAddress = InetAddress.getByName(serverAddress);
			udpPacketSender = new DatagramSocket();
			byte[] packet = requestPacketByteArray(opcode, fileName);
			packetToSend = new DatagramPacket(packet, packet.length, InetServerAddress, 69);
			udpPacketSender.send(packetToSend);
		
			byteStream = recieveFile();
	}
	public static byte[] requestPacketByteArray(byte opcode, String fileName) {
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
		i++;
		for (int k = 0; k < mode.length(); k++,i++)
			result[i] = (byte) mode.charAt(k);
		
		result[i] = 0;
		i++;
		return result;
	}
	public static void sendAck(byte[] blockNum) throws IOException {
		byte[] something = new byte[4];
		something[0] = 0;
		something[1] = 4;
		something[2] = blockNum[2];
		something[3] = blockNum[3];
		
		packetToSend = new DatagramPacket(something, something.length, InetServerAddress, packetToRecieve.getPort());
		udpPacketSender.send(packetToSend);
	}
	public static ByteArrayOutputStream recieveFile() throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		do {
			byte[] rpacket = new byte[516];
			packetToRecieve = new DatagramPacket(rpacket, rpacket.length, InetServerAddress, udpPacketSender.getLocalPort());
			udpPacketSender.receive(packetToRecieve);
		
			System.out.println(Arrays.toString(packetToRecieve.getData()));
			sendAck(packetToRecieve.getData());
			
		}while (packetToRecieve.getLength() > 512);
		return result;
	}
	
}
