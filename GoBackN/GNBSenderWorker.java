import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.nio.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

////Author Baisakhi
public class GNBSenderWorker extends Thread {

	public static int CONSIGNMENT = 512;
    	public static int HEX_PER_LINE = 10;
    	public static byte[] RDT = new byte[] { 0x52, 0x44, 0x54 };
    	public static byte[] SEQ_0=new byte[1];// = new byte[] { 0x30 };
    	public static byte[] END = new byte[] { 0x45, 0x4e, 0x44 };
    	public static byte[] CRLF = new byte[] { 0x0a, 0x0d };
    	


	// Maximum Segment Size - Quantity of data from the application layer in the segment
	public static final int MSS = 512;

	// Probability of loss during packet sending
	public static final double PROBABILITY = 0.0;

	// Window size - Number of packets sent without acking
	public static final int WINDOW_SIZE = 4;
	
	// Time (ms) before REsending all the non-acked packets
	public static final int TIMER = 30;

	DatagramSocket toReceiver;//socket;
	//DatagramPacket start = null;
	InetAddress clientip;
	int clientport ;
	String[] args;
	int count;
	String file_string = "";

	public GNBSenderWorker(DatagramSocket socket, InetAddress ip, int port, String[] args, String filename, int count) throws Exception{
		this.clientip=ip;
		this.clientport=port;
		this.args=args;
		this.file_string = filename;
		this.count = count;
	}	

	public void run(){
	  try
	  {	String acknow = "";
		System.out.println("Starting Thread " + count);
		System.out.println();
		
		int fgt=0,flag=1;
		int[] forget=new int[4];
		if(args.length>1)
		{
			for(int i=1;i<args.length;i++)
				forget[i-1]=Integer.parseInt(args[i]);
		}
		// Sequence number of the last packet sent (rcvbase)
		int lastSent = -1;
		
		// Sequence number of the last acked packet
		int waitingForAck = -1;

		DatagramSocket toReceiver = new DatagramSocket();


		System.out.println("Received request for " + file_string + " from " + clientip + " port " + clientport);
		System.out.println();

		// Data to be sent (you can, and should, use your own Data-> byte[] function here)
		File file = new File(file_string);//"demoPDF.pdf");		
		byte[] fileBytes = new byte[(int)file.length()];
		(new FileInputStream(file)).read(fileBytes);

		System.out.println("Data size: " + fileBytes.length + " bytes");

		// Last packet sequence number
		int lastSeq = (int) Math.ceil( (double) fileBytes.length / MSS);

		System.out.println("Number of packets to send: " + lastSeq);	
		System.out.println();
		
		// List of all the packets sent
		ArrayList<byte[]> packs = new ArrayList<byte[]>();
		
		
		makeFrame(packs,file_string);			

		while(true){

			// Sending loop
			while(lastSent - waitingForAck < WINDOW_SIZE && (lastSent<lastSeq-1)){
				
				// Array to store part of the bytes to send
				byte[] filePacketBytes = new byte[MSS];

				
									
				byte[] sendData = packs.get(lastSent+1);
								
				
				// Create the packet
				DatagramPacket packet = new DatagramPacket(sendData, sendData.length, clientip, clientport);//start.getAddress(), start.getPort());// receiverAddress, 3000 );

				
							
				// Send with some probability of loss
				fgt=0;
				for(int i=0;i<args.length-1;i++){
					
					if((lastSent+1)==forget[i])
					{
						//System.out.print(lastSent+1);
						fgt=1;
						forget[i]=-1;
						break;
					}
				}
				if(fgt==1){//&&flag==1){
					System.out.println("Forgot CONSIGNMENT " + (lastSent+1));
					System.out.println();
					//flag=0;
				}else{
					System.out.println("Sent CONSIGNMENT " + (lastSent+1));// +  " and size " + sendData.length + " bytes");
					toReceiver.send(packet);
					//flag=1;
					
				}

				// Increase the last sent
				lastSent++;
				System.out.println();

			} // End of sending while ONE MORE
			
			// Byte array for the ACK sent by the receiver
			byte[] ackBytes = new byte[40];
			
			// Creating packet for the ACK
			DatagramPacket ack = new DatagramPacket(ackBytes, ackBytes.length);
			
			try{	while(waitingForAck<lastSent){
					// If an ACK was not received in the time specified (continues on the catch clausule)
					toReceiver.setSoTimeout(TIMER);
				
					// Receive the packet
					toReceiver.receive(ack);

				
					acknow=new String(ack.getData(), "UTF-8").substring(4,6);
					System.out.println("Received ACK " + (Integer.valueOf( acknow.trim() )+1) );//acknow);
				
				
					// If this ack is for the last packet, stop the sender (Note: gbn has a cumulative acking)
				
					if(waitingForAck < (Integer.valueOf(acknow.trim())  ))
						{//System.out.println("waiting = " + waitingForAck);
						waitingForAck=Integer.valueOf(acknow.trim());//++; 
						//System.out.println("waitingforAck = " + waitingForAck);
						}
				}
				if(Integer.parseInt(acknow.trim())==lastSeq-1 && lastSent == lastSeq-1)//ackObject.getPacket() == lastSeq){
					break;
				//System.out.println("Last sent = " + lastSent);
				
			}catch(SocketTimeoutException e){
				// then send all the sent but non-acked packets
				//System.out.println("Timeout");
				for(int i = waitingForAck; i < lastSent; i++){
					
					// Serialize the RDTPacket object
					byte[] sendData = packs.get(i+1); //Serializer.toBytes(sent.get(i));

					// Create the packet
					DatagramPacket packet = new DatagramPacket(sendData, sendData.length, clientip, clientport);//start.getAddress(), start.getPort());// receiverAddress, 3000 );
					
					toReceiver.send(packet);
					System.out.println();
					System.out.println("Sent CONSIGNMENT " +  (i+1));
				}
			}
			
		
		}
		System.out.println();
		System.out.println("Finished transmission");
	 }catch (ArrayIndexOutOfBoundsException e){
            		System.out.println("Incorrect Number of Arguments");
        	}catch(Exception e){
           		System.out.println("IO Error");
 		}

	}

	public static void makeFrame(ArrayList<byte[]> packs, String filename)throws Exception {
        
        FileInputStream myFIS = null;
        byte[] myData = new byte[CONSIGNMENT];
        byte[] myLastData;
        byte[] myMsg;
        int bytesRead = 0;
        int i; // counter for copying bytes in array
        
        try {int count = 0;
            myFIS = new FileInputStream(filename);//"demoPDF.pdf");
            while (bytesRead != -1) {
                bytesRead = myFIS.read(myData);
                             
                if (bytesRead > -1) {
                    
                    if (bytesRead < CONSIGNMENT) {
                        // last consignment
			// make a special byte array that exactly fits the number of bytes read 
			// otherwise, the consignment may be padded with junk data
                        myLastData = new byte[bytesRead];
                        for (i=0; i<bytesRead; i++) {
                            myLastData[i] = myData[i];
                        }
			byte b = (byte)count;
			SEQ_0[0] = b; //SEQ_0 =  String.format("%02d", count).getBytes();//String.valueOf(count).getBytes();
                        myMsg = concatenateByteArrays(RDT, SEQ_0, myLastData, END, CRLF);
			count++;
                    } else {
			byte b = (byte)count;
			SEQ_0[0] = b; //SEQ_0 = String.format("%02d", count).getBytes();//String.valueOf(count).getBytes();
                        myMsg = concatenateByteArrays(RDT, SEQ_0, myData, CRLF);
			count++;
                    }
                
                    packs.add(myMsg);	
                }
            }
                      
        } catch (FileNotFoundException ex1) {
            System.out.println(ex1.getMessage());
            
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            
        }finally {
		
		try {
			myFIS.close();
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
		}	
        }    
    }
    
    public static String byteToHex(byte b) {
        int i = b & 0xFF;
        return Integer.toHexString(i);
    }
    
    public static void printBytesAsHex(byte[] bytes) {
        
        int i=0;
        int j=0;
        while (i<bytes.length) {
            while (i<bytes.length && j<HEX_PER_LINE) {
                System.out.print("0x" + byteToHex(bytes[i++]) + " ");
                j++;
            }
            System.out.println(" ");
            j = 0;
        }
        
    }
    
    public static byte[] concatenateByteArrays(byte[] a, byte[] b, byte[] c, byte[] d) {
        byte[] result = new byte[a.length + b.length + c.length + d.length]; 
        System.arraycopy(a, 0, result, 0, a.length); 
        System.arraycopy(b, 0, result, a.length, b.length);
	System.arraycopy(c, 0, result, a.length+b.length, c.length);
        System.arraycopy(d, 0, result, a.length+b.length+c.length, d.length);
        return result;
    }
    
    public static byte[] concatenateByteArrays(byte[] a, byte[] b, byte[] c, byte[] d, byte[] e) {
        byte[] result = new byte[a.length + b.length + c.length + d.length + e.length]; 
        System.arraycopy(a, 0, result, 0, a.length); 
        System.arraycopy(b, 0, result, a.length, b.length);
        System.arraycopy(c, 0, result, a.length+b.length, c.length);
        System.arraycopy(d, 0, result, a.length+b.length+c.length, d.length);
        System.arraycopy(e, 0, result, a.length+b.length+c.length+d.length, e.length);
        return result;
    }
}


