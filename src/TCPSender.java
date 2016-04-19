import java.net.*;
import java.io.*;

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

    public void sendFile()throws IOException{
    	fileReader = new FileInputStream(theFile);
    	fileReader.skip(pointer);
	    theOutstream.write((theFile.getName()+"::"+fileLength+"::"+pointer).getBytes());
	    theOutstream.flush();

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
		    bytesRead = fileReader.read(msg);
		    theOutstream.write(msg);
		    currentPos = currentPos + bytesRead;
		}
		
		s.close();
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