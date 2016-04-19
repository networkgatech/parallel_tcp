import java.net.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.io.*;
import java.util.*;

public class TCPReceiver implements Runnable{
    int length;
    ServerSocket listener;
    Socket s;
    String filename, initString;
    byte[] buffer;
    List<byte[]> buffers;
    ByteBuffer largeBuffer;
    
    long startPointer, bytesReceived, bytesToReceive;
    InputStream theInstream;
    OutputStream theOutstream;
    
    public TCPReceiver(int port) throws IOException
    {
	// Init stuff
	listener = new ServerSocket(port);
	buffer = new byte[100];
	System.out.println(" -- Ready to receive information on port: "+port);
	s = listener.accept();
	
	theInstream = s.getInputStream();
	theOutstream = s.getOutputStream();
	length = theInstream.read(buffer);
	initString = "Recieved-"+new String(buffer, 0, length);
	StringTokenizer t = new StringTokenizer(initString, "::");
	filename = t.nextToken();
	bytesToReceive = new Integer(t.nextToken()).intValue();
	theOutstream.write((new String("GOT_IT")).getBytes());
    }

    public TCPReceiver(int port, ByteBuffer lb) throws IOException
    {
	// Init stuff
    this.largeBuffer = lb;
	listener = new ServerSocket(port);
	buffer = new byte[100];
	
	
	System.out.println(" -- Ready to receive file on port: "+port);
    
    }
    
    public void close() throws IOException {
    	s.close();
    }
    
	public void receive() throws IOException {
	s = listener.accept();	
	theInstream = s.getInputStream();
	theOutstream = s.getOutputStream();

	length = theInstream.read(buffer);
	
	initString = "Received-"+s.getLocalPort()+new String(buffer, 0, length);
	StringTokenizer t = new StringTokenizer(initString, "::");
	filename = t.nextToken();
	bytesToReceive = new Integer(t.nextToken()).intValue();
	startPointer = new Long(t.nextToken()).longValue();
	
	theOutstream.write((new String("OK")).getBytes());

	while(bytesReceived < bytesToReceive-100) {

		length = theInstream.read(buffer);
		largeBuffer.put(buffer, 0, length);
		bytesReceived = bytesReceived + length;
	}
	byte[] tmp = new byte[(int)(bytesToReceive-bytesReceived)];
	length = theInstream.read(tmp);
	largeBuffer.put(tmp);

	s.close();
    }

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			receive();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}