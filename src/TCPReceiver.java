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
    
    FileOutputStream fileWriter;
    RandomAccessFile randOutput;
    
    long startPointer, bytesReceived, bytesToReceive;
    InputStream theInstream;
    OutputStream theOutstream;
    
    public TCPReceiver(int port) throws IOException
    {
	// Init stuff
	listener = new ServerSocket(port);
	buffer = new byte[8192];
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
	buffer = new byte[8192];
	
	
	System.out.println(" -- Ready to receive file on port: "+port);
    
    }
    
    public void close() throws IOException {
    	s.close();
    }
    
	public void receive() throws IOException {
	s = listener.accept();	
	theInstream = s.getInputStream();
	theOutstream = s.getOutputStream();
	
	// 1. Wait for a sender to transmit the filename

	length = theInstream.read(buffer);
	
	initString = "Recieved-"+s.getLocalPort()+new String(buffer, 0, length);
//	initString = "Recieved-"+new String(buffer, 0, length);
	
	StringTokenizer t = new StringTokenizer(initString, "::");
	filename = t.nextToken();
	bytesToReceive = new Integer(t.nextToken()).intValue();
//	System.out.print(s.getLocalPort()+"---"+bytesToReceive+"-----"+largeBuffer.capacity());
//	dst = new byte[(int)bytesToReceive];
	startPointer = new Long(t.nextToken()).longValue();
	
//	System.out.println("  -- The file will be saved as: "+filename);
//	System.out.println("  -- Expecting to receive: "+bytesToReceive+" bytes");
	
	
	// 2. Send an reply containing OK to the sender
	theOutstream.write((new String("OK")).getBytes());
//	System.out.println("receiving something");
	
	// 3. Receive the contents of the file
	
//	randOutput = new RandomAccessFile(new File(filename), "rw");
//	randOutput.seek(startPointer);
//	fileWriter = new FileOutputStream(filename);

	while(bytesReceived < bytesToReceive-8192) {
//		if (bytesToReceive-bytesReceived < 8192) {
//			byte[] tmp = new byte[(int)(bytesToReceive-bytesReceived)];
//			length = theInstream.read(tmp);
//			largeBuffer.put(tmp);
////			buffers.add((byte[])tmp.clone());
//			System.out.println(s.getLocalPort()+"---"+length);
////			fileWriter.write(tmp, 0,  length);
//		} else {
			length = theInstream.read(buffer);
			largeBuffer.put(buffer, 0, length);
//			buffers.add((byte[])buffer.clone());
//			fileWriter.write(buffer, 0,  length);
//		}
		bytesReceived = bytesReceived + length;
	}
	byte[] tmp = new byte[(int)(bytesToReceive-bytesReceived)];
	length = theInstream.read(tmp);
	largeBuffer.put(tmp);

	s.close();
//	System.out.println(largeBuffer.capacity()+"---"+dst.length);
//	largeBuffer.flip();
//	largeBuffer.get(dst, 0, dst.length);
//	fileWriter.write(dst);
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