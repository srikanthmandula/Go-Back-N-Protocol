import java.io.*;
import java.net.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.Queue;

public class GBN_Receiver{
	InetAddress IPAddress1;
	int network_port;
	int receiverPort;
	String destination_file;
	DatagramSocket receiver_socket;
	int expected_Seqnum;
	int char_received = -1;
	Writer arrival_Writer;
	Writer file_writer;
	Queue<packet> received_packets = new LinkedList<packet>();
	
	
	GBN_Receiver(String ipaddress, int network_port, int receiverPort, String destination_file){
		try{
		this.IPAddress1 = InetAddress.getByName(ipaddress);
		this.receiver_socket = new DatagramSocket(receiverPort);
		//System.out.println(receiver_socket.isConnected());
		} catch (Exception e){
			System.err.println(e);
		}
		this.network_port = network_port;
		this.receiverPort = receiverPort;
		this.destination_file = destination_file;
				
	}

	public void receive(String destination_file) throws Exception {
		
		byte[] ack_data = new byte[packet.createACK(0).getUDPdata().length];
		DatagramPacket receiver_packet = new DatagramPacket(ack_data, packet.createACK(0).getUDPdata().length);
		while(true){
			//System.out.println(receiver_socket.isConnected());
			receiver_socket.receive(receiver_packet);
			packet received_Packet = packet.parseUDPdata(receiver_packet.getData());
			System.out.println("Received packet with seq_num: " + received_Packet.getSeqNum());
			System.out.println("Sending acknowledgement: " + received_Packet.getSeqNum());
			arrival_Writer.write(received_Packet.getSeqNum() + "\n");
			
			if(received_Packet.getSeqNum() == expected_Seqnum){
				char_received = received_Packet.getSeqNum();
				packet acknowledgement = packet.createACK(received_Packet.getSeqNum());
				byte[] sendData = acknowledgement.getUDPdata();
				DatagramPacket send_Packet = new DatagramPacket(sendData, sendData.length, IPAddress1, network_port);
				receiver_socket.send(send_Packet);
				received_packets.add(received_Packet);
//				System.out.println("Sending acknowledgement: " + received_Packet.getSeqNum());
				expected_Seqnum = (expected_Seqnum + 1)% 16;
			}
			else if((received_Packet.getType() == 2)){
			                    byte[] sendData = received_Packet.getUDPdata();
                                DatagramPacket send_Packet = new DatagramPacket(sendData, sendData.length, IPAddress1, network_port);
				receiver_socket.send(send_Packet);
				return;
			}
			else{
								packet acknowledgement = packet.createACK(char_received);
                                byte[] sendData = acknowledgement.getUDPdata();
                                DatagramPacket send_Packet = new DatagramPacket(sendData, sendData.length, IPAddress1, network_port);
                                receiver_socket.send(send_Packet);
                                
			}
			
			//System.out.print("end of while loop");
	}

	}
	
	void generate_file(String destination_file) throws Exception{
		for (packet receivedPacket : received_packets){
			String s = new String(receivedPacket.getData());
			file_writer.write(s);
		}
	}
	public static void main(String[] args){
		if(args.length != 4){
			System.err.println("User has not passed required number of run time arguments.Pls try again");
			System.exit(1);
		}
		GBN_Receiver r = new GBN_Receiver(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3]);
		try{
			r.arrival_Writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("arrival.log"), "utf-8"));
			r.file_writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[3]), "utf-8"));
			r.receive(args[3]);
			r.generate_file(args[3]);
			
		}catch (Exception e){
			System.err.print(e.getMessage());
	
		}finally {
   		try {r.arrival_Writer.close(); r.file_writer.close();} catch (Exception ex) {}
		}
	}

}
