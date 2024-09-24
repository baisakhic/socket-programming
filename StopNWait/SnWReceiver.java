import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.net.*;
import java.io.*;

//Author Baisakhi and Avimita

public class SnWReceiver {
	
	public static byte[] CRLF = new byte[] { 0x0a, 0x0d };
	static int HEX_PER_LINE=10;
	
	public static void main(String[] args) {
		//ArrayList<byte[]> received = new ArrayList<byte[]>();
		try{
		
			String serverip=args[0];
			int serverport=Integer.parseInt(args[1]);
			String file=args[2];
			//taking the ack to forget as command line argument in the format: ACK_TO_FORGETx
			int ack1= Integer.parseInt(args[3]);
			//int ack2= Integer.parseInt(args[4]);
			//int ack3= Integer.parseInt(args[5]);
			//int ack4= Integer.parseInt(args[6]);
			
			int seq_no;
			int flag=1;
			DatagramSocket fromSender = new DatagramSocket();//serverport);
			
			System.out.println("Requesting " + file + " from " + serverip + " port " + serverport);
			System.out.println();
		
			//Initializing
			//DatagramSocket cs = new DatagramSocket();	
		 	InetAddress ip=InetAddress.getByName(serverip/*"localhost"*/);
		 	byte[] sd=new byte[100];
			String sp = "REQUEST"+ file;
			sd=sp.getBytes();
			DatagramPacket start=new DatagramPacket(sd,sd.length,ip,serverport);
			/*cs*/fromSender.send(start);
				
			
			
			
			int receiv=0;
			//ArrayList<RDTPacket> received = new ArrayList<RDTPacket>();
			ArrayList<byte[]> received = new ArrayList<byte[]>();
			
			boolean end = false;
		
			while(!end){
				// 7 is the base size (in bytes) of added messages
				byte[] receivedData = new byte[SnWSender.MSS + 7];
			
				System.out.println("Waiting for packet");
				System.out.println();
			
				// Receive packet
				DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
				fromSender.receive(receivedPacket);
				receivedData=receivedPacket.getData();
			
				
				String dat=new String(receivedPacket.getData(), "UTF-8");
				receiv = Integer.parseInt(dat.substring(3,5));
				 System.out.println(received.size());
				System.out.print("Received CONSIGNMENT " + receiv);
				if(received.size()>receiv)
					System.out.println(" duplicate -- discarding");
				else
					{System.out.println(); received.add(receivedData);}
				
				
				String send = "ACK " + String.format("%02d", receiv);//String.valueOf(receiv);
				byte[] ackBytes = concatenateByteArrays(send.getBytes(),CRLF);//send.getBytes();
			
			
				DatagramPacket ackPacket = new DatagramPacket(ackBytes, ackBytes.length, ip, serverport);//receivedPacket.getAddress(), receivedPacket.getPort());
			
				// Send some, forget some
				if((receiv==ack1 && flag==1)){ //|| (receiv==ack2 && flag==1) || (receiv==ack3 && flag==1) || (receiv==ack4 && flag==1)){
					System.out.println("Forgot ACK " + (receiv));
					flag = 0; 
				}else{
					
					flag=1;
					fromSender.send(ackPacket);// and add
					System.out.println("Sent ACK " + (receiv) );
					//received.add(receivedPacket.getData());
					
				}
				System.out.println();

			
				if(dat.contains("END")&&flag==1)
				{	System.out.println("End Of File");
					break;
				}

			}write_file(received, receiv);
			
		
		}catch (ArrayIndexOutOfBoundsException e){
            		System.out.println("Incorrect Number of Arguments");
        	}catch(Exception e){
           		System.out.println("IO Error");
 		}
	
	}

	 public static void write_file(ArrayList<byte[]> packet, int last) {
        
        // given 3 sample messages
        //byte[][] messages = new byte[3][];
        //messages[0] = CONSIGNMENT_0;
        //messages[1] = CONSIGNMENT_1;
        //messages[2] = CONSIGNMENT_2;
	byte[] MESSAGE_START = { 0x52, 0x44, 0x54 }; // "RDT "
    	byte[] MESSAGE_END = { 0x45, 0x4e, 0x44, 0xa, 0xd }; //" END CRLF"
    
   	int MESSAGE_FRONT_OFFSET = 5; //"RDT#"
   	int MESSAGE_BACK_OFFSET = 2; //"CRLF"
    	int MESSAGE_LAST_BACK_OFFSET = 5; //"ENDCRLF"
   
        
        String seqString;
        File myFile;
        FileOutputStream myFOS;
        
        byte[] data = new byte[512]; // each consignment has data length 10 bytes
        int count;                  // for copying / extracting from msg to data

        try {
            myFile = new File("newdemo.pdf");
            myFOS = new FileOutputStream(myFile);
            System.out.println(packet.size());	
            for (int i=0; i<packet.size();i++){//byte[] msg:messages) {
		byte[] msg = packet.get(i);
                // get sequence number
                seqString = String.valueOf(i);//new String(msg, MESSAGE_START.length, 1);
                System.out.println("Sequence Number = " + seqString);

                // get last message
                if (i<packet.size()-1){//!matchByteSequence(msg, msg.length-MESSAGE_END.length , MESSAGE_END.length, MESSAGE_END)) {

                    myFOS.write(msg, MESSAGE_FRONT_OFFSET, msg.length-MESSAGE_FRONT_OFFSET-MESSAGE_BACK_OFFSET);
                    
                    for (count=0; count < msg.length-MESSAGE_FRONT_OFFSET-MESSAGE_BACK_OFFSET; count++) {
                        data[count] = msg[MESSAGE_FRONT_OFFSET+count];
			
                    }
                    System.out.println("Consignment = " + seqString);
             
                } else {
                    myFOS.write(msg, MESSAGE_FRONT_OFFSET, msg.length-MESSAGE_FRONT_OFFSET-MESSAGE_LAST_BACK_OFFSET);
                    System.out.println("Last Consignment");
                }//System.out.println(data.length);
            }
            
            myFOS.close();
            
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        
        // extract data
        // write to file
        
    }
           
    static public boolean matchByteSequence(byte[] input, int offset, int length, byte[] ref) {
        
        boolean result = true;
        
        if (length == ref.length) {
            for (int i=0; i<ref.length; i++) {
                if (input[offset+i] != ref[i]) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }
	public static byte[] concatenateByteArrays(byte[] a, byte[] b) {
        	byte[] result = new byte[a.length + b.length]; 
        	System.arraycopy(a, 0, result, 0, a.length); 
        	System.arraycopy(b, 0, result, a.length, b.length);
		return result;
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
	
	
}
