import java.io.*;
import java.net.*;
import java.util.*;

//Author Baisakhi

public class GNBServerM
{
 	public static void main(String[] args) 
 	{	try{  	//ServerSocket serverSocket = new ServerSocket(3000);
			String init_word="";

			int clientport=Integer.parseInt(args[0]);
			String file_string="";
			DatagramSocket toReceiver = new DatagramSocket(clientport);
			int count = 0;

			
			byte[] rd=new byte[100];
			DatagramPacket start=new DatagramPacket(rd,rd.length);
			System.out.println("Starting Server");
  			while(true)
  			{	
				toReceiver.receive(start);
				//System.out.println(start.getData());
				init_word=new String(start.getData(),"UTF-8");
				if (init_word.contains("REQUEST")){//add file
					InetAddress ip = start.getAddress();
		 			int port = start.getPort();
					String str = new String(start.getData(), 0, start.getLength());
					System.out.println(str);
					file_string=str.substring(7);

					GNBSenderWorker worker = new GNBSenderWorker(toReceiver, ip, port, args, file_string, count++);
					worker.start();
					init_word = "";
				}

				
  			}
  		  }catch (ArrayIndexOutOfBoundsException ex) {
			System.out.println(ex.getMessage());
		  }catch(IOException e){
	           	System.out.println(e.getMessage());//"IO Error");
      		}catch(Exception e){
	           	System.out.println(e.getMessage());//"IO Error");
		}finally {
      		  	System.out.println("Done");
      		  }
      		  
  		  

 	}
}
