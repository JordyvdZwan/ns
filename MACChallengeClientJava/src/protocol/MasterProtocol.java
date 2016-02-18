package protocol;

import java.util.Random;

/**

 */
public class MasterProtocol implements IMACProtocol {
	private boolean sending = false;
	private boolean waitingForAck = false;
	private int waiting = 0;
	private static final int WAITTIME = 1;
	private static final int MAXBLOCKSIZE = 8;
	private static final int TRANSMISSIONPERCENTAGE = 20;
	private int blockSize = 0;
	
	
	@Override
	public TransmissionInfo TimeslotAvailable(MediumState previousMediumState, int controlInformation, int localQueueLength) {
		if (previousMediumState == MediumState.Succes) {
			waiting = 0;
		} else if (previousMediumState == MediumState.Idle) {
			waiting++;
		} else {
			waiting = 0;
		}
		if (waitingForAck && previousMediumState == MediumState.Succes) {
			sending = true;
		}
		if (localQueueLength == 0) {
			sending = false;
			blockSize = 0;
		}
		if (blockSize > MAXBLOCKSIZE) {
			sending = false;
			blockSize = 0;
		}
		waitingForAck = false;
		if (sending) {
			blockSize++;
			return new TransmissionInfo(TransmissionType.Data, 0);
		} else {
			if (localQueueLength > 0 && waiting > WAITTIME) {
				if (new Random().nextInt(100) < TRANSMISSIONPERCENTAGE) {
					waitingForAck = true;
					return new TransmissionInfo(TransmissionType.Data, 0);
				} else {
					return new TransmissionInfo(TransmissionType.Silent, 0);
				}
			} else {
				return new TransmissionInfo(TransmissionType.Silent, 0);
			}
		}
	}

}
