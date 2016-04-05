import java.net.*;
import java.nio.*;
import java.util.*;
import java.io.*;

class TCPReceiver implements Runnable{
	int length;
	ServerSocket listener;
	Socket s;
	String filename, initString;
	byte[] buffer;
	ByteBuffer largeBuffer;
	FileOutputStream fileWriter;
	RandomAccessFile randOutput;
	Long startPoint, bytesReceived, bytesToReceive;
	OutputStream theOutput;
	InputStream theInput;
	
	public TCPReceiver(int port)throws IOException {
		buffer = new byte[8192];
		listener = new ServerSocket(port);
		System.out.println(" -- Ready to receive information on port: "+port);
		s = listener.accept();
		
		theInput = s.getInputStream();
		theOutput = s.getOutputStream();
		length = theInput.read(buffer);
		initString = "Received-" + new String(buffer, 0, length);
		StringTokenizer t = new StringTokenizer(initString, "::");
		filename = t.nextToken();
		bytesToReceive = new Long(t.nextToken()).longValue();
		theOutput.write((new String("GOT_IT")).getBytes()); 
	}
	public TCPReceiver(int port, ByteBuffer bb)throws IOException {
		this.largeBuffer = bb;
		listener = new ServerSocket(port);
		buffer = new byte[8192];
		System.out.println(" -- Ready to receive file on port: "+port);
	}
	public void close()throws IOException {
		s.close();
	}
	public void receive()throws IOException{
		s = listener.accept();
		theInput = s.getInputStream();
		theOutput = s.getOutputStream();
		length = theInput.read(buffer);
		initString = "Recieved-"+s.getLocalPort()+new String(buffer, 0, length);
		StringTokenizer t = new StringTokenizer(initString, "::");
		filename = t.nextToken();
		bytesToReceive = new Long(t.nextToken()).longValue();
		startPoint = new Long(t.nextToken()).longValue();
		theOutput.write((new String("OK")).getBytes());
		while (bytesReceived < bytesToReceive - 8192) {
			length = theInput.read(buffer);
			largeBuffer.put(buffer, 0, length);
			bytesReceived = bytesReceived + length;
		}
		byte[] temp = new byte[(int)(bytesToReceive - bytesReceived)];
		length = theInput.read(temp);
		largeBuffer.put(temp);// contrast to before not understand offset of last function
		s.close();
	}
	@Override
	public void run() {
		try {
			receive();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
