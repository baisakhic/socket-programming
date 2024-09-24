import java.io.*;
import java.net.*;

class ChatThread implements Runnable{  
	//@override	
	public void run(){  
		//System.out.println("Chat is running...");  
	}  
  
	public static void main(String args[]){  
	String portno = args[0];
	ChatThread e1=new ChatThread();  
	Thread t1 =new Thread(){
		public void run(){
		EServer();
		}
	 };  

	ChatThread e2=new ChatThread();  
	Thread t2 =new Thread(){
		public void run(){
		EClient("abc");
		}
	 };  
 

	try{
		t1.start();  
		t2.start();
		t1.join();
		t2.join();
		}
	catch (InterruptedException e){
		System.out.println("Interrupted");
	}

        
 	}  

	public static void EServer(){
		try{
            		ServerSocket serv = new ServerSocket(6000);
       			Socket client = serv.accept(); 
       			while(true){
                		
                		InputStream in = client.getInputStream();
                		DataInputStream dis = new DataInputStream(in);
				String msg = dis.readUTF();   
				//dis.flush();       
				System.out.println(msg);
                		
				if (msg.equalsIgnoreCase("Client says :: Bye"))
					break;
		            }
			client.close();
        
            
	         } catch(IOException e){
	            System.out.println("IO Error");
      		  }
       }

	public static void EClient(String port){
		try{	Socket client = new Socket("localhost",3000);
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
            		}
          
       		 catch(IOException e){
            		System.out.println("IO Error");
       		 }
  }
    
   
}
