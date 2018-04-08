import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

public class Tftpclient {

	static byte opcode;
	static String fileName = "testfile.txt";
	static String serverAddress = "192.168.1.25";
	static DatagramPacket packetToSend;
	static InetAddress InetServerAddress;
	static DatagramPacket packetToRecieve;
	static DatagramSocket udpPacketSender;
	static ByteArrayOutputStream byteStream;
	static Boolean noError;
	static String mode;
	
	public static void main(String[] args) throws IOException {
		noError = true;
		mode = "octet";
		InetServerAddress = InetAddress.getByName(serverAddress);
		udpPacketSender = new DatagramSocket();
		Scanner scan = new Scanner(System.in);
		System.out.println("What would you like to do? \n 1) Read File \n 2) Write File");
		int choice = scan.nextInt();
		switch (choice) {
		case 1:readFile();break;
		case 2:writeFile();break;
		default: break;
		}
		
	}
	public static void writeFile() throws IOException{
		opcode = 2;
		byte[] packet =  requestPacketByteArray(opcode, fileName);
		packetToSend = new DatagramPacket(packet, packet.length, InetServerAddress, 69);
		udpPacketSender.send(packetToSend);
		
		InputStream inputStream = new FileInputStream(fileName);
		System.out.println(inputStream.available());
		
		while (inputStream.available() > 0 && noError) {
			recieveAck();
			opcode = 3;
			byte[] tempByteArray = new byte[4 + Math.min(512, inputStream.available())];
			
			tempByteArray =  makeDataPacket(tempByteArray.length, packetToRecieve.getData());
			inputStream.read(tempByteArray, 4, tempByteArray.length - 4);
			packetToSend = new DatagramPacket(tempByteArray, tempByteArray.length, InetServerAddress, packetToRecieve.getPort());
			udpPacketSender.send(packetToSend);
			System.out.println(inputStream.available());
			if (packetToRecieve.getData()[1] == 5)
			{
				showError();
				noError = false;
			}
		}
		inputStream.close();
	}
	public static void recieveAck() throws IOException {
		byte[] rpacket = new byte[516];
		packetToRecieve = new DatagramPacket(rpacket, rpacket.length, InetServerAddress, udpPacketSender.getLocalPort());
		udpPacketSender.receive(packetToRecieve);
	}
	public static void readFile() throws IOException {
		opcode = 1;
		byte[] packet =  requestPacketByteArray(opcode, fileName);
		packetToSend = new DatagramPacket(packet, packet.length, InetServerAddress, 69);
		udpPacketSender.send(packetToSend);
		
		byteStream = recieveFile();
		OutputStream outputStream = new FileOutputStream(fileName);
		byteStream.writeTo(outputStream);
		byteStream.close();
	}
	public static byte[] requestPacketByteArray(byte opcode, String fileName) {
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
	public static byte[] makeDataPacket(int dataSize, byte[] blockNum) {
		byte[] result = new byte[dataSize];
		blockNum = incrementBlock(blockNum);
		int i = 0;
		
		result[i] = 0;
		i++;
		
		result[i] = 3;
		i++;
		
		result[i] = blockNum[0];
		i++;
		
		result[i] = blockNum[1];
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
			
			
			if(packetToRecieve.getData()[1] == 5) {
				//System.out.println("Error Code: " + packetToRecieve.getData()[3]);
				showError();
				noError = false;
			}
			else if(packetToRecieve.getData()[1] == 3){
				DataOutputStream dataOutputStream = new DataOutputStream(result);
				//dataOutputStream.write(packetToRecieve.getData());
				dataOutputStream.write(packetToRecieve.getData(), 4, packetToRecieve.getLength() - 4);
				System.out.println(Arrays.toString(packetToRecieve.getData()));
				sendAck(packetToRecieve.getData());
			}
		}while (packetToRecieve.getLength() > 512 && noError);
		return result;
	}
	public static byte[] incrementBlock(byte[] currentBlock) {
		byte[] result = new byte[2];
		currentBlock[3]++;
		if(currentBlock[3] < 0) {
			currentBlock[2]++;
			currentBlock[3] = 0;
		}
		result[0] = currentBlock[2];
		result[1] = currentBlock[3];
		return result;
	}
	public static void showError() {
		switch(packetToRecieve.getData()[3]) {
		case 0:System.out.println("Not defined, see error message (if any).");break;
		case 1:System.out.println("File not found.");break;
		case 2:System.out.println("Access violation.");break;
		case 3:System.out.println("Disk full or allocation exceeded.");break;
		case 4:System.out.println("Illegal TFTP operation.");break;
		case 5:System.out.println("Unknown transfer ID.");break;
		case 6:System.out.println("File already exists.");break;
		case 7:System.out.println("No such user.");break;
		default: break;
		}
	}
	
}
