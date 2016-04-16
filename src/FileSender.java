import java.net.*;
import java.io.*;
public class FileSender{

public static void main(String[] args) throws Exception
{
	TCPSender theSender1;
	TCPSender theSender2;
	int thePort = 0;
	File theFile;
	InetAddress theAddress = null;

	// Checking the arguments
	if (args.length != 4) printError("Wrong number of arguments");
	
	if (!args[0].equals("tcp") && !args[0].equals("udp")) printError("Unrecognized protocol: "+args[0]);
	
	int portNumber = Integer.parseInt(args[3]);
	int[] thePorts = new int[portNumber];
	
	for (int i=0; i<portNumber; i++)
		thePorts[i] = 8081+i;
	
	try
	    {theAddress = InetAddress.getByName(args[1]);}
	catch(UnknownHostException e)
	    {printError("The specified host could not be found on the network");}

    theFile = new File(args[2]);
	if(!theFile.canRead()) printError("There was an error opening the specified file");
	
	
	// Create the sender object
	long length = theFile.length();
	long currentSize = 0;
	long packet = length / 100;
	if (length%100 != 0)
		packet++;
	long packetPerTCP = packet / portNumber;
	if (packet%portNumber != 0)
		packetPerTCP++;
	long piecesSize = packetPerTCP * 100;
	long Pointer = 0L;
	
	TCPSender sh = new TCPSender(theAddress, 8080, theFile, 0L, 0L);
	sh.shakehand();
	
	Thread t = null;
	for (int i=0; i<portNumber; i++) {
		if (i == portNumber-1)
			t = new Thread(new TCPSender(theAddress, thePorts[i], theFile, Pointer, length-Pointer));
		else
			t = new Thread(new TCPSender(theAddress, thePorts[i], theFile, Pointer, piecesSize));
		Pointer += piecesSize;
		
		t.start();
	}
}

    public static void printError(String error)
    {
	System.out.println(" - Error: "+error);
	System.out.println(" - Usage: FileSender [protocol] [host] [port] [filename]");
	System.exit(1);
    }
}