import java.io.*;
import java.net.*;
import java.util.*;

public class EClient {
	
     public static void main (String args[]){
     	String ip; int port;
        try{
            
        	ip = args[0];
        	port = Integer.parseInt(args[1]);
            
            
            Socket client = new Socket(ip,port);//"localhost",3000);
            while(true){
		
	        BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
        	String msg = buff.readLine();
            	OutputStream out = client.getOutputStream();
            	DataOutputStream dos = new DataOutputStream(out);
            	dos.writeUTF("Client Says :: " + msg);
            	dos.flush();
            	
                if (msg.equalsIgnoreCase("Bye"))
			break;
		}
	    client.close();
            
          
       } catch (ArrayIndexOutOfBoundsException e){
            	System.out.println("Incorrect Number of Arguments");
       }catch(IOException e){
            System.out.println("IO Error");
        }
        
    }
    
}
