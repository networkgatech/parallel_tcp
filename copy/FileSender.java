import java.util.*;
import java.lang.*;
import java.net.*;
import java.io.*;

public class FileSender {
	public static void main(String[] args) throws Exception {
		int thePort = 0;
		File theFile;
		InetAddress theAddress = null;
		ArrayList<String> parameter = new ArrayList<String>();
		Scanner sc = new Scanner(System.in);
		System.out.println("Parallel tcp sender test");
		System.out.println("IP:");
		String temp = sc.next();
		parameter.add(temp);
		System.out.println("File path and name:");
                temp = sc.next();
                parameter.add(temp);
		System.out.println("Parallel path number:");
                temp = sc.next();
                parameter.add(temp);
		int portNumber = Integer.parseInt(parameter.get(2));
		int[] ports = new int[portNumber];
		for (int i = 0; i < portNumber; i++) {
			ports[i] = 8081 + i;
		}
		try {
			theAddress = InetAddress.getByName(parameter.get(0));
			
		} catch (UnknownHostException e){
			printError("the ip address can't be found on the network");
		}
		theFile = new File(parameter.get(1));
		if (!theFile.canRead()) {
			printError("Can't open specific file");
		}
		long length = theFile.length();
		long currentSize = 0;
		long packet = length / 8192;
		if (length % 8192 != 0) {
			packet++;
		}
		long packetPerTCP = packet / portNumber;
		if (packet % portNumber != 0) {
			packetPerTCP++;
		}
		long pieceSize = packetPerTCP * 8192;
		long Pointer = 0L;
		TCPSender sender = new TCPSender(theAddress, 8080, theFile, 0L, 0L);
		sender.ShakeHand();
		Thread thread = null;
		for (int i = 0; i < portNumber; i++) {
			if (i == portNumber - 1) {
				thread = new Thread(new TCPSender(theAddress, ports[i], theFile, Pointer, length - Pointer));
			} else {
				thread = new Thread(new TCPSender(theAddress, ports[i], theFile, Pointer, pieceSize));
				Pointer += pieceSize;
			}
			thread.start();
		}
	} 
	public static void printError(String e) {
		System.out.println("Error "+e);
		System.exit(1);
	}
}
