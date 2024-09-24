import java.io.*;
import java.net.*;

public class EServerM
{
 	public static void main(String[] args) 
 	{	try{  	ServerSocket serverSocket = new ServerSocket(3000);
			int count = 0;

  			while(true)
  			{
   				Socket socket = serverSocket.accept();

				EServerMWorker worker = new EServerMWorker(socket, count++);
		
				worker.start();
				//worker.join();
				//System.out.println(java.lang.Thread.activeCount());
				//BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
        			//String condition = buff.readLine(); 
        			//if (condition.equalsIgnoreCase("Quit"))
				//	break;

  			}
  		  //}catch (InterruptedException ex) {
			//System.out.println(ex.getMessage());
		  }catch(IOException e){
	           	System.out.println("IO Error");
      		  }finally {
      		  	System.out.println("Done");
      		  }
      		  
  		  

 	}
}
