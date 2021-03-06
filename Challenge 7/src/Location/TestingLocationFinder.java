package Location;

import java.util.HashMap;
import Utils.*;

public class TestingLocationFinder implements LocationFinder{
	
	private HashMap<String, Position> knownLocations; //Contains the known locations of APs. The long is a MAC address.
	
	public TestingLocationFinder(){
		knownLocations = Utils.getKnownLocations(); //Put the known locations in our hashMap
	}

	int total = 0;
	int average = 0;
	int counter = 0;
	
	@Override
	public Position locate(MacRssiPair[] data) {
		if (getpair(data) != null) {
			counter++;
			total = total + getpair(data).getRssi();
			average = total / counter;
			System.out.println("Counter : " + counter);
			System.out.println("Total : " + total);
			System.out.println("RSSI : " + getpair(data).getRssi());
			System.out.println("Average : " + average);
		}
		return new Position(0, 0);
	}
	
	/**
	 * Returns the position of the first known AP found in the list of MacRssi pairs
	 * @param data
	 * @return
	 */
	private MacRssiPair getpair(MacRssiPair[] data){
		MacRssiPair pair = data[0];
		for (MacRssiPair p : data) {
			if (pair.getMacAsString().equals("f4:cf:e2:2c:0f:20")) {
				System.out.println(p);
				pair = p;
			}
		}
		return pair;
	}
}
