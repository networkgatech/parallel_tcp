import java.net.*;
import java.io.*;

/**
 * UDPSender is an implementation of the Sender interface, using UDP as the transport protocol.
 * The object is bound to a specified receiver host and port when created, and is able to 
 * send the contents of a file to this receiver.
 *
 * @author Alex Andersen (alex@daimi.au.dk)
 */
public class TCPSender implements Runnable{
	
    private File theFile;
    private long pointer;
    private FileInputStream fileReader;
    private Socket s;
    private long fileLength, currentPos, bytesRead;
    private int toPort, length;
    private byte[]  msg, buffer;
    private String toHost,initReply;
    private InetAddress toAddress;
    private OutputStream theOutstream; 
    private InputStream theInstream;

    /**
     * Class constructor.
     * Creates a new UDPSender object capable of sending a file to the specified address and port.
     *
     * @param address  the address of the receiving host
     * @param port    the listening port on the receiving host
     */
    public TCPSender(InetAddress address, int port, File file, long pointer, long filelength) throws IOException{
    	this.theFile = file;
    	this.pointer = pointer;
    	this.fileLength = filelength;
    	toPort = port;
    	toAddress = address;
    	msg = new byte[100];
    	buffer = new byte[100];
    	s = new Socket(toAddress, toPort);
    	theOutstream = s.getOutputStream();
    	theInstream = s.getInputStream();
    }
    
    public void close() throws IOException {
    	s.close();
    }
    
    public void shakehand() throws IOException {
    	theOutstream.write((theFile.getName()+"::"+theFile.length()).getBytes());
    	theOutstream.flush();
    	
    	length = 0;
    	while (length <= 0){
    	    length = theInstream.read(buffer);
    	    if (length>0) 
    	    	initReply = (new String(buffer, 0, length));
    	}
    	if (initReply.equals("GOT_IT"))
    		System.out.println("  -- Got file information");
    	s.close();
    }

    /**
     * Sends a file to the bound host.
     * Reads the contents of the specified file, and sends it via UDP to the host 
     * and port specified at when the object was created.
     *
     * @param theFile  the file to send
     */
    public void sendFile()throws IOException{
    	fileReader = new FileInputStream(theFile);
    	fileReader.skip(pointer);
//    	fileLength = theFile.length();
//    	fileLength = fileLength/(long)2;
//    	System.out.println("-----"+fileLength);
//	System.out.println(" -- Filename: "+theFile.getName());
//	System.out.println(" -- Bytes to send: "+fileLength);

	// 1. Send the filename and length to the receiver
	theOutstream.write((theFile.getName()+"::"+fileLength+"::"+pointer).getBytes());
	theOutstream.flush();


	// 2. Wait for a reply from the receiver	
//	System.out.println(" -- Waiting for OK from the receiver");
	length = 0;
	while (length <= 0){
	    length = theInstream.read(buffer);
	    if (length>0) 
	    	initReply = (new String(buffer, 0, length));
	}
	
	
	// 3. Send the content of the file
	if (initReply.equals("OK"))
	    {
		System.out.println("  -- Got OK from receiver - sending the file ");

		
		while (currentPos<fileLength){
//		    System.out.println("Will read at pos: "+currentPos);
		    bytesRead = fileReader.read(msg);
//		    System.out.println(s.getLocalPort()+"---"+bytesRead);
		    theOutstream.write(msg);
		    //System.out.println("Bytes read: "+bytesRead);
		    currentPos = currentPos + bytesRead;
		}
		
		s.close();
		
		
//		System.out.println("  -- File transfer complete...");
	    }
	else{System.out.println("  -- Recieved something other than OK... exiting");}
    }


	@Override
	public void run() {
		try {
			sendFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
	}
}