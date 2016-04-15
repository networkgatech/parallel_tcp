import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.io.*;
import java.lang.*;
public class FileReceiver {
	public static void main(String[] args) throws Exception{
		int portNumber = 0;
		List<Thread> ls = new ArrayList<Thread>();
		List<ByteBuffer> buffer = new ArrayList<ByteBuffer>();
		Scanner sc = new Scanner(System.in);
		System.out.println("Please type number of lines to transmite the file:");
		portNumber = Integer.parseInt(sc.next());
		int[] ports = new int[portNumber];
		TCPReceiver first = new TCPReceiver(8080);
		String fileName = first.filename;
		long fileSize = first.bytesToReceive;
		long startTime = 0L;
		first.close();
		for (int i = 0; i < portNumber; i++) { 
                        ports[i] = 8081 + i;
                        buffer.add(ByteBuffer.allocate((int)(fileSize / portNumber) + 8192));
			
                }
		Thread thread = null;
		for (int i = 0; i < portNumber; i++) {
			thread = new Thread(new TCPReceiver(ports[i], buffer.get(i)));
			thread.start();
			ls.add(thread);
		}
		startTime = System.nanoTime();
		try {
			for (Thread td :ls) {
				td.join();
			}
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		FileOutputStream out = new FileOutputStream(fileName);
		byte[] bt = new byte[8192];
		for (ByteBuffer b : buffer) {
		 b.flip();
		 while(b.position()<b.limit()) {
			b.get(bt, 0, (b.limit()-b.position()) < bt.length ? (b.limit()-b.position()) : bt.length);// need more attention
			out.write(bt);
		}		
		System.out.println("one line finished");
			
		}
		out.close();
		long endTime = System.nanoTime();
		System.out.println("the time used"+(double)(startTime - endTime) / (long)1000000000);
	} 
}
