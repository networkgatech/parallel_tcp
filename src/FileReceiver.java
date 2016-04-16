import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.io.*;
public class FileReceiver{

    public static void main(String[] args) throws Exception{
	int portNumber = 0;
    List<Thread> ls = new ArrayList<Thread>();
    List<ByteBuffer> buffer = new ArrayList<>();

	// Checking the arguments
	if (args.length != 2) printError("Wrong number of arguments");
	
	if (!args[0].equals("tcp") && !args[0].equals("udp")) printError("Unrecognized protocol: "+args[0]);

	portNumber = Integer.parseInt(args[1]);
	
	int[] thePorts = new int[portNumber];
	
	TCPReceiver sh = new TCPReceiver(8080);
	String filename = sh.filename;
	long filesize = sh.bytesToReceive;
	sh.close();
	
	long start = 0L;
	
	for (int i=0; i<portNumber; i++) {
		thePorts[i] = 8081+i;
		buffer.add(ByteBuffer.allocate((int)(filesize/portNumber+100)));
	}
	
	Thread t = null;
	for (int i=0; i<portNumber; i++) {
		t = new Thread(new TCPReceiver(thePorts[i], buffer.get(i)));
		t.start();
		ls.add(t);
		start = System.nanoTime();
	}
	
	try {  
        for(Thread thread : ls) 
            thread.join(); 
    } catch (InterruptedException e) {  
        e.printStackTrace();  
    }
	
	FileOutputStream out = new FileOutputStream(filename);
	byte[] bt = new byte[100];
	for (ByteBuffer b : buffer) {
		b.flip();
		while (b.position()<b.limit()) {
			b.get(bt, 0, (b.limit()-b.position()) < bt.length ? (b.limit()-b.position()) : bt.length);
			out.write(bt);
		}
		System.out.println("Thread done!");
	}
	out.close();
	long end = System.nanoTime();
	System.out.println((double)(start-end)/(long)1000000000);
    }
    
    public static void merge(FileOutputStream output, int portNumber) throws Exception {
//    	RandomAccessFile ok = new RandomAccessFile(file, "rw");

    	for (int i=0; i<portNumber; i++) {
    		String filename = "Recieved-"+(8081+i)+"a.mkv";
    		FileInputStream fs = new FileInputStream(new File(filename));
    		byte[]b = new byte[1000];
    		int n=0;
    		while((n=fs.read(b)) != -1)
    			output.write(b, 0, n);
    		fs.close();
    	}
////    		RandomAccessFile read = new RandomAccessFile(new File("Recieved-8081a.mkv"), "r");
//    		FileInputStream fs = new FileInputStream(new File("Recieved-8081a.mkv"));
//    		byte[]b = new byte[1100];
//    		int n=0;
//    		while((n=fs.read(b)) != -1)
//    			output.write(b, 0, n);
//    		fs.close();
//    		
////    		read = new RandomAccessFile(new File("Recieved-8082a.mkv"), "r");
//    		fs = new FileInputStream(new File("Recieved-8082a.mkv"));
//    		b = new byte[1100];
//    		n=0;
//    		while((n=fs.read(b)) != -1)
//    			output.write(b, 0, n);
//    		fs.close();
//    	ok.close();
    }


    public static void printError(String error){
	System.out.println(" - Error: "+error);
	System.out.println(" - Usage: FileReceiver [protocol] [portnumber]");
	System.exit(1);
    }
}