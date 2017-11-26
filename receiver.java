import java.io.*;
import java.net.*;
import java.util.*;


public class receiver{
	InetAddress sender_IPAddress;
	int networkEmuPort;
	int receiverPort;
	String filename;
	DatagramSocket receiverSocket;
	int expectedSeqnum;
	int lastReceived = -1;
	Writer arrivalWriter;
	Writer fileWriter;
	Queue<packet> receivedPackets = new LinkedList<packet>();
	
	



	//PrintWriter writer = new PrintWriter("");
	//constrctor
	receiver(String networkHostname, int networkEmuPort, int receiverPort, String filename){
		//must be thrown
		try{
		this.sender_IPAddress = InetAddress.getByName(networkHostname);
		this.receiverSocket = new DatagramSocket(receiverPort);
		//this.receiverSocket.connect(InetAddress.getByName(networkHostname),receiverPort);
		//System.out.println(receiverSocket.isConnected());
		} catch (Exception e){
			System.err.println(e.getMessage());
		}
		this.networkEmuPort = networkEmuPort;
		this.receiverPort = receiverPort;
		this.filename = filename;
		//System.out.print(networkHostname + " " + networkEmuPort + " " + receiverPort + " " + filename + "\n");
		
	}

	public void receive(String filename) throws Exception {
		//int packetSize = packet.createPacket(0, new String( new char[500]) ).getUDPdata().length;
		byte[] ACKdata = new byte[packet.createACK(0).getUDPdata().length];
		DatagramPacket receiverPacketDatagram = new DatagramPacket(ACKdata, packet.createACK(0).getUDPdata().length);
		//receiverSocket.setSoTimeout(1000);
		//System.out.print(packet.createACK(0).getUDPdata().length);
		receiverSocket.setSoTimeout(3000000);
		while(true){
			
			
			
			//System.out.println("entered while loop");
			//System.out.println(receiverSocket.isConnected());
			receiverSocket.receive(receiverPacketDatagram);
				//System.out.println("first mid of while loop");
			packet receiverPacket = packet.parseUDPdata(receiverPacketDatagram.getData());
			System.out.println("Received packet with seqnum: " + receiverPacket.getSeqNum());
			arrivalWriter.write(receiverPacket.getSeqNum() + "\n");
			//System.out.print(expectedSeqnum);
			if((receiverPacket.getType() == 2)){
				//packet EOTPacket = packet.createACK(lastReceived);
                                byte[] sendData = receiverPacket.getUDPdata();
                                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, sender_IPAddress, networkEmuPort);
				receiverSocket.send(sendPacket);
				//System.out.println("sent EOT packet back");
				return;
			}
			if(receiverPacket.getSeqNum() == expectedSeqnum){
				//System.out.println("received expected seqnum " + receiverPacket.getSeqNum());
				lastReceived = receiverPacket.getSeqNum();
				packet ACK = packet.createACK(receiverPacket.getSeqNum());
				byte[] sendData = ACK.getUDPdata();
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, sender_IPAddress, networkEmuPort);
				receiverSocket.send(sendPacket);
				receivedPackets.add(receiverPacket);
				int tm=receiverPacket.getSeqNum()+1;
				if(tm==2)
					tm=0;
				System.out.println("Sending ACK: " + tm);
				
				expectedSeqnum = (expectedSeqnum + 1)% 2;
			}
			 
			/*else{
				//System.out.println("expected " + expectedSeqnum + "but got " + receiverPacket.getSeqNum());
				packet ACK = packet.createACK(lastReceived);
                                byte[] sendData = ACK.getUDPdata();
                                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, sender_IPAddress, networkEmuPort);
                                receiverSocket.send(sendPacket);
                                //System.out.println("send ack: " + lastReceived);
                                //expectedSeqnum++;
			}*/
			
			
			//expectedSeqnum++;
			//System.out.print("hello");
			//System.out.print("end of while loop");
	}

	}
	
	void writeToFile(String filename) throws Exception{
		for (packet receivedPacket : receivedPackets){
			String s = new String(receivedPacket.getData());
			fileWriter.write(s);
		}
	}
	public static void main(String[] args){
		if(args.length != 4){
			System.err.println("Usage:\n\tsender \t<host address of the network emulator>\n" +
					"\t\t<UDP port number used by the emulator to receive data from the sender>\n" +
					"\t\t<UDP port number used by the sender to receive ACKs from the emulator>\n" +
			"\t\t<name of the file to be transferred>\n");
			System.exit(1);
		}
		receiver r = new receiver(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3]);
		try{
			//arrivalWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("arrival.log"), "utf-8"));
		
			//receiver r = new receiver(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3]);
			r.arrivalWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("arrival.log"), "utf-8"));
			r.fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[3]), "utf-8"));
			r.receive(args[3]);
			System.out.println("-------------------------------");
			System.out.println("-------------------------------");
			System.out.println("writing output file is initated");
			r.writeToFile(args[3]);
			System.out.println("writing output file is completed");
			System.out.println();
			System.out.println("------END of Process------------");
			
		}catch (Exception e){
			System.err.print(e.getMessage());
	
		}finally {
   		try {r.arrivalWriter.close(); r.fileWriter.close();} catch (Exception ex) {}
		}
	}

}
