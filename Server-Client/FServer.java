import java.io.*;
import java.net.*;

public class FServer
{
	// proper exception handling required
 	public static void main(String[] args)throws IOException,InterruptedException
 	{
		// port number to be read in as command line param
  		ServerSocket serverSocket = new ServerSocket(3000); 
		
		String[] secret = {"ZERO", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN"};
		try
		{

			while(true)
			{
				Socket client = serverSocket.accept();

				// get input stream from socket
				InputStream in = client.getInputStream();
				DataInputStream dis = new DataInputStream(in);
				
				// get output stream from socket
				OutputStream out = client.getOutputStream();
				DataOutputStream dos = new DataOutputStream(out); 

				int count = 0;
				String msg;

				while (count<secret.length) {
					msg = dis.readUTF();
					System.out.println(msg);
					
					if (msg.contains("Hello")) {
						// Server sends SECRET only when Client says Hello
						dos.writeUTF(secret[count]);
						count++;
					}
						
				}	
				
				dos.writeUTF("END");
				Thread.sleep(1000);

				client.close();
			}
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
		catch(InterruptedException i)
		{
			System.out.println(i);
		}

 	}
}
