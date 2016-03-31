package Location;

import java.util.HashMap;
import Utils.*;

/**
 * Simple Location finder that returns the first known APs location from the list of received MAC addresses
 * @author Bernd
 *
 */
public class StrongestLocationFinder implements LocationFinder{
	
	private HashMap<String, Position> knownLocations; //Contains the known locations of APs. The long is a MAC address.
	
	public StrongestLocationFinder(){
		knownLocations = Utils.getKnownLocations(); //Put the known locations in our hashMap
	}

	@Override
	public Position locate(MacRssiPair[] data) {
		printMacs(data); //print all the received data
		printSelected(data);
		return getStrongestRssi(data); //return the first known APs location
	}
	
	/**
	 * Returns the position of the first known AP found in the list of MacRssi pairs
	 * @param data
	 * @return
	 */
	private Position getStrongestRssi(MacRssiPair[] data){
		Position pos = new Position(0,0);
		MacRssiPair pair = data[0];
		for(int i = 0; i < data.length; i++){
			if(data[i].getRssi() > pair.getRssi() && knownLocations.get(data[i].getMacAsString()) != null){
				pair = data[i];
			}
		}
		if (knownLocations.get(pair.getMacAsString()) != null) {
			pos = knownLocations.get(pair.getMacAsString());
		}
		return pos;
	}
	
	private MacRssiPair getStrongestPair(MacRssiPair[] data){
		Position pos = new Position(0,0);
		MacRssiPair pair = data[0];
		for(int i = 1; i < data.length; i++){
			if(data[i].getRssi() > pair.getRssi()){
				pair = data[i];
				break;
			}
		}
		if (knownLocations.get(pair.getMacAsString()) != null) {
			pos = knownLocations.get(pair.getMacAsString());
		}
		return pair;
	}
	
	/**
	 * Outputs all the received MAC RSSI pairs to the standard out
	 * This method is provided so you can see the data you are getting
	 * @param data
	 */
	private void printMacs(MacRssiPair[] data) {
		for (MacRssiPair pair : data) {
			System.out.println(pair);
		}
	}
	
	private void printSelected(MacRssiPair[] data) {
		System.out.println("Our Awnser... : " + getStrongestPair(data));
	}

}
