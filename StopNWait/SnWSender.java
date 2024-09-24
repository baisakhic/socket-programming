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


//Author Baisakhi and Avimita

public class SnWSender {

	public static int CONSIGNMENT = 512;
    	public static int HEX_PER_LINE = 10;
    	public static byte[] RDT = new byte[] { 0x52, 0x44, 0x54 };
    	public static byte[] SEQ_0;// = new byte[] { 0x30 };
    	public static byte[] END = new byte[] { 0x45, 0x4e, 0x44 };
    	public static byte[] CRLF = new byte[] { 0x0a, 0x0d };
    	


	// Maximum Segment Size - Quantity of data from the application layer in the segment
	public static final int MSS = 512;

	// Probability of loss during packet sending
	public static final double PROBABILITY = 0.0;

	
	// Time (ms) before REsending all the non-acked packets
	public static final int TIMER = 30;


	public static void main(String[] args) {
		
		try{
			String clientip=args[0];
			int clientport=Integer.parseInt(args[1]);

			//Taking the consignement to forget as command line arguments CONSIGNMENT_TO_FORGETx
			int con1= Integer.parseInt(args[2]);
			//int con2= Integer.parseInt(args[3]);
			//int con3= Integer.parseInt(args[4]);
			//int con4= Integer.parseInt(args[5]);
			System.out.println("Starting Server");
			System.out.println();
			String init_word="no";
			int flag=1;
			
			// Sequence number of the last packet sent (rcvbase)
			int lastSent = -1;
		
			// Sequence number of the last acked packet
			int waitingForAck = -1;

			
			// Receiver address
			InetAddress receiverAddress = InetAddress.getByName(clientip);
			DatagramSocket toReceiver = new DatagramSocket(clientport);
		
			//Initializing
			//DatagramSocket cs = new DatagramSocket(clientport);//6000);
			String file_string="";
			byte[] rd=new byte[100];
			DatagramPacket start=new DatagramPacket(rd,rd.length);;
			while(true){
			
				if (init_word.contains("REQUEST"))//add file
					break;
				
				
				/*cs*/toReceiver.receive(start);
			 
				String str = new String(start.getData(), 0, start.getLength());
				file_string=str.substring(7);
				init_word=new String(start.getData(),"UTF-8");
			  
						
			
			}
			System.out.println("Received request for " + file_string + " from " + clientip + " port " + clientport);
		
			File file = new File(file_string/*"demoPDF.pdf"*/);
			byte[] fileBytes = new byte[(int)file.length()];
			(new FileInputStream(file)).read(fileBytes);
			System.out.println("Data size: " + fileBytes.length + " bytes");
			int lastSeq = (int) Math.ceil( (double) fileBytes.length / MSS);
	
			System.out.println("Number of packets to send: " + lastSeq);
			System.out.println();

			
		
			// List of all the packets sent
			ArrayList<byte[]> packs = new ArrayList<byte[]>();
			
		
		
			// Create Frames
			makeFrame(packs, file_string);			

			while(true){

				// Sending
				if(lastSent==waitingForAck && lastSent < lastSeq){
					
					byte[] sendData = packs.get(lastSent+1);
								
				
					// Create the packet
					DatagramPacket packet = new DatagramPacket(sendData, sendData.length, start.getAddress(), start.getPort());//receiverAddress, clientport );
			
					// Send a few, forget a few
					if(((lastSent+1)==con1&&flag==1)){ //|| ((lastSent+1)==con2&&flag==1) || ((lastSent+1)==con3&&flag==1) || ((lastSent+1)==con4&&flag==1)){		
						System.out.println("Forgot CONSIGNMENT " + (lastSent+1));
						flag=0;
					}else{	
						System.out.println("Sent CONSIGNMENT " + (lastSent+1));
						toReceiver.send(packet);
						flag=1;
					}

					// Increase the last sent
					lastSent++;

				} // End of sending
				
				// Byte array for the ACK sent by the receiver
				byte[] ackBytes = new byte[40];
				
				// Creating packet for the ACK
				DatagramPacket ack = new DatagramPacket(ackBytes, ackBytes.length);
				
				try{
					// If an ACK was not received in the time specified (continues on the catch clausule)
					toReceiver.setSoTimeout(TIMER);
					
					// Receive the packet
					toReceiver.receive(ack);

					//System.out.println(new String(ack.getData(), "UTF-8"));
					String acknow=new String(ack.getData(), "UTF-8").substring(4,6);
					System.out.println("Received ACK " + acknow);
					System.out.println();

					// If this ack is for the last packet, break
					if(Integer.parseInt(acknow.trim())==lastSeq-1 && lastSent == lastSeq-1)
						break;
					waitingForAck = Integer.valueOf(acknow.trim());
				
				}catch(SocketTimeoutException e){
					// then send all the sent but non-acked packets
					
					// Retreive Frame
					byte[] sendData = packs.get(lastSent);
					
					// Create the packet
					DatagramPacket packet = new DatagramPacket(sendData, sendData.length, start.getAddress(), start.getPort());//receiverAddress, clientport );
					
					// Send Again
					toReceiver.send(packet);
					
					System.out.println("Sent CONSIGNMENT " +  lastSent); 
				
				}
			
		
			}
		
			System.out.println("Finished transmission");
	  	}catch (ArrayIndexOutOfBoundsException e){
            		System.out.println("Incorrect Number of Arguments");
		}catch(FileNotFoundException f){
			System.out.println("File Not Found");
        	}catch(IOException e){
            		System.out.println("IO Error");
 		}catch(Exception E){
			System.out.println("Unknown Exception");
		}

	}

	public static void makeFrame(ArrayList<byte[]> packs, String file) {
		
       
        FileInputStream myFIS = null;//add file
        byte[] myData = new byte[CONSIGNMENT];
        byte[] myLastData;
        byte[] myMsg;
        int bytesRead = 0;
        int i; // counter for copying bytes in array
        
        try {int count = 0;
            myFIS = new FileInputStream(file);
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
			SEQ_0 =  String.format("%02d", count).getBytes();//String.valueOf(count).getBytes();
                        myMsg = concatenateByteArrays(RDT, SEQ_0, myLastData, END, CRLF);
			count++;
                    } else {
			SEQ_0 = String.format("%02d", count).getBytes();//String.valueOf(count).getBytes();
                        myMsg = concatenateByteArrays(RDT, SEQ_0, myData, CRLF);
			count++;
                    }
                
                    packs.add(myMsg);	
                }
            }
                      
        } catch (FileNotFoundException ex1) {
            System.out.println("File Not Found");
            
        } catch (IOException ex) {
            System.out.println("IOError");
            
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


