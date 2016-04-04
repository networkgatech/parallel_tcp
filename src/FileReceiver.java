import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.io.*;
public class FileReceiver{ ////

    public static void main(String[] args) throws Exception{
	int portNumber = 0;
    List<Thread> ls = new ArrayList<Thread>(); // ls: each member thread is for one port
    List<ByteBuffer> buffer = new ArrayList<>();  // buffer : each memeber ByteBuffer is for one port

	// Checking the arguments
	if (args.length != 2) printError("Wrong number of arguments");
	
	if (!args[0].equals("tcp") && !args[0].equals("udp")) printError("Unrecognized protocol: "+args[0]);

	portNumber = Integer.parseInt(args[1]);  // first parameter tcp/udp, second parameter number of ports
	
	int[] thePorts = new int[portNumber]; // thePorts, port ID
	
	TCPReceiver sh = new TCPReceiver(8080); // -> ?? difference with two parameter situation?
	String filename = sh.filename;  // 1个parameter 应该只是知道了port，新建的thread也只是负责收filename和bytes to receive
	long filesize = sh.bytesToReceive; // 2 个parameter 是指定了接收的部分存在哪个buffer
	sh.close();
	
	long start = 0L;
	
	for (int i=0; i<portNumber; i++) {
		thePorts[i] = 8081+i;
		buffer.add(ByteBuffer.allocate((int)(filesize/portNumber+8192))); // +8192 ensures every port with enough buffer size
	}
	
	Thread t = null;
	for (int i=0; i<portNumber; i++) {
		t = new Thread(new TCPReceiver(thePorts[i], buffer.get(i))); // new thread for each port receiving job
		t.start(); // each thread start
		ls.add(t);
		start = System.nanoTime();
	}
	
	try {  
        for(Thread thread : ls) 
            thread.join();  // this is to handle the thread in a sequence, the code after .join can only be handled after...
    } catch (InterruptedException e) {  // ...the current thread is finished/dead.
        e.printStackTrace();  
    }
	
	FileOutputStream out = new FileOutputStream(filename); // filename recieved from sender
	byte[] bt = new byte[8192]; // bt -> buffer for one packet
	for (ByteBuffer b : buffer) {
		b.flip(); // in this command, limit is set to current write position, read position is set to 0...
		while (b.position()<b.limit()) { // ...to ensure the position READ start from zero to limit
			b.get(bt, 0, (b.limit()-b.position()) < bt.length ? (b.limit()-b.position()) : bt.length);
			out.write(bt); // write the result to output packet by packet /// ------? but why?
		}
		System.out.println("Thread done!"); // Means the content of port receive is written to output file
	}
	out.close(); // close the file
	long end = System.nanoTime();
	System.out.println((double)(start-end)/(long)1000000000);
    }
    
    /////merge 其实没有用到
    public static void merge(FileOutputStream output, int portNumber) throws Exception {
//    	RandomAccessFile ok = new RandomAccessFile(file, "rw");

    	for (int i=0; i<portNumber; i++) {
    		String filename = "Recieved-"+(8081+i)+"a.mkv";
    		FileInputStream fs = new FileInputStream(new File(filename));
    		byte[]b = new byte[81920];
    		int n=0;
    		while((n=fs.read(b)) != -1)
    			output.write(b, 0, n);
    		fs.close();
    	}
////    		RandomAccessFile read = new RandomAccessFile(new File("Recieved-8081a.mkv"), "r");
//    		FileInputStream fs = new FileInputStream(new File("Recieved-8081a.mkv"));
//    		byte[]b = new byte[18192];
//    		int n=0;
//    		while((n=fs.read(b)) != -1)
//    			output.write(b, 0, n);
//    		fs.close();
//    		
////    		read = new RandomAccessFile(new File("Recieved-8082a.mkv"), "r");
//    		fs = new FileInputStream(new File("Recieved-8082a.mkv"));
//    		b = new byte[18192];
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
