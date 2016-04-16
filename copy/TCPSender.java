import java.net.*;
import java.io.*;
import java.lang.*;

public class TCPSender implements Runnable {
	private File theFile;
	private long pointer;
	private FileInputStream fileReader;
	private Socket socket;
	private long fileLength, currentPos, bytesRead;
	private int toPort, length;
	private byte[] msg, buffer;
	private String toHost,initReply;
	private InetAddress toAddress;
	private OutputStream theOutput;
	private InputStream theInput;
	
	public TCPSender(InetAddress address, int port, File file, long pointer, long fileLength) throws IOException {
		this.theFile = file;
		this.pointer = pointer;
		this.fileLength = fileLength;
		this.toAddress = address;
		toPort = port;
		msg = new byte[8192];
		buffer = new byte[8192];
		socket = new Socket(toAddress, toPort);
		theOutput = socket.getOutputStream();
		theInput = socket.getInputStream();
	}
	public void close() throws IOException{
		socket.close();
	}
	public void ShakeHand() throws IOException {
		theOutput.write((theFile.getName() + "::" + theFile.length()).getBytes());
		theOutput.flush();
		length = 0;
		while (length <= 0) {
			length = theInput.read(buffer);// read from input stream and and store it into the buffer
			if (length > 0) {
				initReply = (new String(buffer, 0, length));// decode the buffer;
			}
		}
		if (initReply == "GOT_IT") {
			System.out.println("File already received");
		}
		socket.close();
	}
	public void sendFile() throws IOException {
		fileReader = new FileInputStream(theFile);
		fileReader.skip(pointer);// skip pinter to read the file
		theOutput.write((theFile.getName() + "::" + fileLength + "::" + pointer ).getBytes());
		theOutput.flush();
		
		length = 0;
		while (length <= 0) {
			length = theInput.read(buffer);
			if (length > 0) {
				initReply = (new String(buffer, 0, length));
			}
		}
		if (initReply == "OK") {
			System.out.println("Sending file...");
			while (currentPos < fileLength) {
				bytesRead = fileReader.read(msg);//msg the bytes in which the data is read
				theOutput.write(msg);
				currentPos += bytesRead;
			}
			socket.close();
		} else {
			System.out.println("Something wrong happens can't start to send file although get reply from the sender");
		}
	}
	@Override
	public void run() {
		try {
			sendFile();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
}

