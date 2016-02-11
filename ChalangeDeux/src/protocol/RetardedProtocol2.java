package protocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import client.*;

public class RetardedProtocol2 extends IRDTProtocol {

	// change the following as you wish:
	static final int HEADERSIZE=2;   // number of header bytes in each packet
	static final int DATASIZE= 600;   // max. number of user data bytes in each packet
	static final int TIMEOUT = 4000;
	
	static final int SWS=8;
	private int LAR = -1;
	private int LFS = 0;
	
	
	static final int RWS=8;
	private int LFR = 0;
	private int LAF = RWS;
	
	Map<Integer, Boolean> acks = new HashMap<Integer, Boolean>();
	
	List<Integer[]> packets = new ArrayList<Integer[]>();
	
	@Override
	public void sender() {
		System.out.println("Sending...");

		// read from the input file
		Integer[] fileContents = Utils.getFileContents(getFileID());

		// keep track of where we are in the data
		int filePointer = 0;
		int counter = 0;
		while (filePointer < fileContents.length) {
			// create a new packet of appropriate size
			int datalen = Math.min(DATASIZE, fileContents.length - filePointer);
			Integer[] pkt = new Integer[HEADERSIZE + datalen];
			// write something random into the header byte
			pkt[0] = counter; 
			pkt[1] = packets.size();
			acks.put(counter, false);
			// copy databytes from the input file into data part of the packet, i.e., after the header
			System.arraycopy(fileContents, filePointer, pkt, HEADERSIZE, datalen);
			packets.add(pkt);
			filePointer += DATASIZE;
			counter++;
		}
		for (Integer[] packet : packets) {
			packet[1] = packets.size();
		}
		System.out.println("current state of data between packet seperation and sending");
		System.out.println("packets size: " + packets.size());
		
		while (LAR + 1 < packets.size()) {
			if (LFS - 1 < LAR + SWS && packets.size() > LFS) {
				getNetworkLayer().sendPacket(packets.get(LFS));
				System.out.println("Sent one packet with header = " + packets.get(LFS)[0]);
				client.Utils.Timeout.SetTimeout(TIMEOUT, this, LFS);
				LFS++;
			}
//			System.out.println("TEST 1");
			Integer[] packet = getNetworkLayer().receivePacket();
//			System.out.println("TEST 2");
			if (packet != null) {
				System.out.println("ACK received: " +  packet[0]);
				Integer ack = packet[0];
				acks.put(ack, true);
				if (LAR < ack) {
					LAR = ack;
				}
			}
//			System.out.println("TEST 3");
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		
		System.out.println("So... did it work..?");
	}

	@Override
	public void TimeoutElapsed(Object tag) {
		int z = (Integer) tag;
		// handle expiration of the timeout:
		if (z > LAR && !acks.get(z)) {
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
				System.out.println("Received packet, length="+packet.length+"  first byte="+packet[0]+"  second byte="+packet[1] );
				if (packet[0] <= LFR + RWS && packet[0] > LFR - 1) {
					packets.add(packet);
					// Sending the ACK
					LFR = getLFR(packets);
					Integer[] ackpkt = new Integer[1];
					ackpkt[0] = LFR;
					System.out.println("Sending packet with ack nr: " + ackpkt[0]);
					getNetworkLayer().sendPacket(ackpkt);
					
					// and let's just see if the file is now complete
					if (LFR == packet[1] - 1) {
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
			
		}
		if (packets.size() > 0) {
			int counter = 0;
			while (counter < packets.get(0)[1]) {
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
				counter++;
			}
			System.out.println("CHECK");
		} else {
			System.out.println("Well this is akward...");
		}
		System.out.println("CHECK");
		System.out.println(fileContents.length);
		// write to the output file
		Utils.setFileContents(fileContents, getFileID());
	}
	
	public int getLFR(List<Integer[]> packets) {
		int result = 0;
		for (Integer[] packet : packets) {
			if (packet[0] == result + 1) {
				result = getLFR(packets, packet[0]);
				break;
			}
		}		
		return result;
	}
	
	public int getLFR(List<Integer[]> packets, int result) {
		for (Integer[] packet : packets) {
			if (packet[0] == result + 1) {
				result = getLFR(packets, packet[0]);
				break;
			}
		}		
		return result;
	}
}
