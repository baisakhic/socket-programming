import java.io.*;
import java.net.*;
import java.util.*;

public class FClient
{
	// proper exception handling required
 	public static void main(String[] args)// throws IOException, InterruptedException 
 	{String ip; int port;
	 try{   ip = args[0];
        	port = Integer.parseInt(args[1]);
  		Socket client = new Socket(ip,port);/*;"localhost",3000);*/ // to be read in as command line params
  		
		// get output stream from socket
		OutputStream out = client.getOutputStream();
		DataOutputStream dos = new DataOutputStream(out); 

		// get input stream from socket
		InputStream in = client.getInputStream();	
		DataInputStream dis = new DataInputStream(in); 

		String message;
		boolean quit = false;

		while (!quit) {
	  		
			dos.writeUTF("Hello");
	  		dos.flush();

			message = dis.readUTF();
			System.out.println(message);
					
			if (message.contains("END")) {
				// Server signals end of SECRET by saying END
				quit = true;
				Thread.sleep(50);
			}
		}

		
  		client.close();
	}catch (ArrayIndexOutOfBoundsException e){
            	System.out.println("Incorrect Number or Arguments");
        }catch(IOException e){
            System.out.println("IO Error");
 	}catch(InterruptedException  e){
            System.out.println("Interrupted");
	}
   }
}
