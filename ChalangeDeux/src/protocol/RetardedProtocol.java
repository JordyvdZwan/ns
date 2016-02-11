package protocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import client.*;

public class RetardedProtocol extends IRDTProtocol {

	// change the following as you wish:
	static final int HEADERSIZE=2;   // number of header bytes in each packet
	static final int DATASIZE=248;   // max. number of user data bytes in each packet
	static final int TIMEOUT = 1000;
	
	static final int SWS=1;
	private int LAR = 0;
	private int LFS = 0;
	
	static final int RWS=1;
	private int LFR = 0;
	private int LAF = RWS;
	
	List<Integer[]> packets = new ArrayList<Integer[]>();
	
	@Override
	public void sender() {
		System.out.println("Sending...");

		// read from the input file
		Integer[] fileContents = Utils.getFileContents(getFileID());

		// keep track of where we are in the data
		int filePointer = 0;
		int counter = 0;
		while (filePointer > 0) {
			// create a new packet of appropriate size
			int datalen = Math.min(DATASIZE, fileContents.length - filePointer);
			Integer[] pkt = new Integer[HEADERSIZE + datalen];
			// write something random into the header byte
			pkt[0] = counter; 
			pkt[1] = packets.size();
			// copy databytes from the input file into data part of the packet, i.e., after the header
			System.arraycopy(fileContents, filePointer, pkt, HEADERSIZE, datalen);
			packets.add(pkt);
			filePointer += DATASIZE;
			counter++;
		}
		
		while (LAR < packets.size()) {
			if (LFS < LAR + SWS && packets.size() > LFS) {
				getNetworkLayer().sendPacket(packets.get(LFS + 1));
				System.out.println("Sent one packet with header = " + packets.get(LFS + 1)[0]);
				client.Utils.Timeout.SetTimeout(TIMEOUT, this, LFS + 1);
				LFS++;
			} else {
				Integer[] packet = getNetworkLayer().receivePacket();
				if (packet != null) {
					Integer ack = packet[0];
					if (LAR < ack) {
						LAR = ack;
					}
				}
			}
		}
		
		
		System.out.println("So... did it work..?");
//		// send the packet to the network layer
//		getNetworkLayer().sendPacket(pkt);
//		System.out.println("Sent one packet with header="+pkt[0]);
//
//		// schedule a timer for 1000 ms into the future, just to show how that works:
//		client.Utils.Timeout.SetTimeout(1000, this, 28);
//
//		// and loop and sleep; you may use this loop to check for incoming acks...
//		boolean stop = false;
//		while (!stop) {
//			Integer[] packet = getNetworkLayer().receivePacket();
//			if (packet != null) {
//
//				// tell the user
//				System.out.println("Received packet, length="+packet.length+"  first byte="+packet[0] );
//				if (packet[0] == 123) {
//					System.out.println("ACK was received correctly");
//				} else {
//					System.out.println("ACK was received incorrectly");
//				}
//				
//				stop=true;
//			}else{
//				// wait ~10ms (or however long the OS makes us wait) before trying again
//				try {
//					Thread.sleep(10);
//				} catch (InterruptedException e) {
//					stop = true;
//				}
//			}
//			try {
//				Thread.sleep(10);
//			} catch (InterruptedException e) {
//				stop = true;
//			}
//		}

	}

	@Override
	public void TimeoutElapsed(Object tag) {
		int z = (Integer) tag;
		// handle expiration of the timeout:
		if (z > LAR) {
			System.out.println("Timer expired with tag=" + z);
			getNetworkLayer().sendPacket(packets.get(z));
			System.out.println("Sent one packet with header = " + packets.get(z)[0]);
			client.Utils.Timeout.SetTimeout(TIMEOUT, this, z);
		}
	}

	@Override
	public void receiver() {
		System.out.println("Receiving...");

		// create the array that will contain the file contents
		// note: we don't know yet how large the file will be, so the easiest (but not most efficient)
		//   is to reallocate the array every time we find out there's more data
		Integer[] fileContents = new Integer[0];
		List<Integer[]> packets = new ArrayList<Integer[]>();
		// loop until we are done receiving the file
		boolean stop = false;
		
		while (!stop) {
			// try to receive a packet from the network layer
			Integer[] packet = getNetworkLayer().receivePacket();

			// if we indeed received a packet
			if (packet != null) {
				// tell the user
				System.out.println("Received packet, length="+packet.length+"  first byte="+packet[0] );
				if (packet[0] <= LFR + RWS && packet[0] > LFR) {
					packets.add(packet);
					
					// Sending the ACK
					Integer[] ackpkt = new Integer[HEADERSIZE];
					ackpkt[0] = LFR;
					getNetworkLayer().sendPacket(ackpkt);
					
					LFR = getLFR(packets);
					// and let's just hope the file is now complete
					if (LFR == packet[1]) {
						stop=true;
					}
				}
			}else{
				// wait ~10ms (or however long the OS makes us wait) before trying again
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					stop = true;
				}
			}
			// append the packet's data part (excluding the header) to the fileContents array, first making it larger
			if (packets.size() > 0) {
				int counter = 0;
				while (counter <= packets.get(0)[1]) {
					Integer[] pkt = packets.get(0);
					int i = 1;
					while (pkt[0] != counter) {
						pkt = packets.get(i);
						i++;
					}
					
					int oldlength = fileContents.length;
					int datalen = pkt.length - HEADERSIZE;
					fileContents = Arrays.copyOf(fileContents, oldlength+datalen);
					System.arraycopy(pkt, HEADERSIZE, fileContents, oldlength, datalen);
				}
			} else {
				System.out.println("Well this is akward...");
			}
		}
		

		// write to the output file
		Utils.setFileContents(fileContents, getFileID());
	}
	
	public int getLFR(List<Integer[]> packets) {
		int result = 0;
		for (Integer[] packet : packets) {
			if (packet[0] > result + 1) {
				result = getLFR(packets, packet[0]);
				break;
			}
		}		
		return result;
	}
	
	public int getLFR(List<Integer[]> packets, int result) {
		for (Integer[] packet : packets) {
			if (packet[0] > result + 1) {
				result = getLFR(packets, packet[0]);
				break;
			}
		}		
		return result;
	}
}
