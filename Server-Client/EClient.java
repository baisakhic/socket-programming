import java.io.*;
import java.net.*;
import java.util.*;

public class EClient {
     public static void main (String args[]){
        try{String portno = args[0];
            while(true){
			Socket client = new Socket(portno,3000);
	        BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
        	String msg = buff.readLine();
            	OutputStream out = client.getOutputStream();
            	DataOutputStream dos = new DataOutputStream(out);
            	dos.writeUTF("Client Says :: " + msg);
            	dos.flush();
            	client.close();
                if (msg.equalsIgnoreCase("Bye"))
			break;
		}
            }
          
        catch(IOException e){
            System.out.println("IO Error");
        }
        
    }
    
}
