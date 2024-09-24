import java.io.*;
import java.net.*;

public class EServerMWorker extends Thread
{
	Socket client;
	int count;
	String msg;
	public EServerMWorker(Socket client, int count) {
		this.client = client;
		this.count = count;
	}

 	public void run() 
 	{
		System.out.println("Starting thread");
		
		try{
			while(true){
				InputStream in = client.getInputStream();
				DataInputStream dis = new DataInputStream(in);
				msg = dis.readUTF();
				System.out.println("Thread " + count + " Says :: " + msg);
				if (msg.equalsIgnoreCase("Client Says :: Bye"))
					break;
				}
	   		client.close();

		} catch (IOException ex) {
	
			System.out.println("Exception" + ex.getMessage());
		

		} finally {
	
			System.out.println("Ending thread");
		}
		
 	}
}
