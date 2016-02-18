package protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**

 */
public class LeapProtocol extends Thread implements IMACProtocol {
	private int currentSlot = 1;
	private int nextSlot = 0;
	
	private static final int EMPTYLEAP = 10;
	private static final int FULLLEAP = 1;
	private static final int TRANSMISSIONPERCENTAGE = 60;
	
	private List<Integer> takenSlots = new ArrayList<Integer>();
	private boolean waitingForNextSlot = false;
	
	
	@Override
	public TransmissionInfo TimeslotAvailable(MediumState previousMediumState, int controlInformation, int localQueueLength) {
		currentSlot++;
		try {
			this.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (waitingForNextSlot && controlInformation != nextSlot - currentSlot) {
			nextSlot = 0;
		}
		waitingForNextSlot = false;
		takenSlots.add(controlInformation + currentSlot);
		System.out.println(controlInformation);
		if (nextSlot < currentSlot) {
			System.out.println("sorry jerre... :'-(");
			if (new Random().nextInt(100) < TRANSMISSIONPERCENTAGE) {
				nextSlot = nextSlot(localQueueLength);
				waitingForNextSlot = true;
				return new TransmissionInfo(TransmissionType.Data, nextSlot - currentSlot);
			} else {
				return new TransmissionInfo(TransmissionType.Silent, 10101337);
			}
			
		} else if (currentSlot == nextSlot) {
			if (localQueueLength > 0) {
				nextSlot = nextSlot(localQueueLength);
				waitingForNextSlot = true;
				return new TransmissionInfo(TransmissionType.Data, nextSlot - currentSlot);
			} else {
				nextSlot = nextSlot(localQueueLength);
				waitingForNextSlot = true;
				return new TransmissionInfo(TransmissionType.NoData, nextSlot - currentSlot);
			}
		} else {
			return new TransmissionInfo(TransmissionType.Silent, nextSlot - currentSlot);
		}
	}
	
	private int nextSlot(int localQueueLength) {
		int result = 0;
		if (localQueueLength > 1) {
			result = currentSlot- 1 + FULLLEAP;
		} else {
			result = currentSlot- 1 + EMPTYLEAP;
		}
		while (takenSlots.contains(result)) {
			result++;
		}
		takenSlots.add(result);
		return result;
	}
}
