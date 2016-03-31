package Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Utils.*;

public class DistanceCalculatingLocationFinder implements LocationFinder{
	
	private HashMap<String, Position> knownLocations; //Contains the known locations of APs. The long is a MAC address.
	
	public DistanceCalculatingLocationFinder(){
		knownLocations = Utils.getKnownLocations(); //Put the known locations in our hashMap
	}

	public static final double DISTANCECONSTANT = 1.2;
	
	List<Circle> circles = new ArrayList<Circle>();
	Position lastPos = new Position(0,0);
	@Override
	public Position locate(MacRssiPair[] data) {
		Position pos = lastPos;
		List<MacRssiPair> knownMacRssiPairs = new ArrayList<MacRssiPair>();
		
		//Printing the mac addresses and the RSSI
		//Also adding them to the knownMacRssiPairs list.
		if (getPair("f4:cf:e2:54:e3:30", data) != null) {		
			MacRssiPair AP94 = getPair("f4:cf:e2:54:e3:30", data);
			knownMacRssiPairs.add(AP94);
			System.out.println("AP94 RSSI : " + AP94.getRssi());
		} else {
			System.out.println("AP94 RSSI : No Connection");
		}
		if (getPair("f4:cf:e2:2c:1b:40", data) != null) {	
			MacRssiPair AP93 = getPair("f4:cf:e2:2c:1b:40", data);
			knownMacRssiPairs.add(AP93);
			System.out.println("AP93 RSSI : " + AP93.getRssi());
		} else {
			System.out.println("AP93 RSSI : No Connection");
		}
		if (getPair("f4:cf:e2:2c:0f:20", data) != null) {	
			MacRssiPair AP92 = getPair("f4:cf:e2:2c:0f:20", data);
			knownMacRssiPairs.add(AP92);
			System.out.println("AP92 RSSI : " + AP92.getRssi());
		} else {
			System.out.println("AP92 RSSI : No Connection");
		}
		
		//If enough nodes are found to use trilateration then compute a new Position else use old position.
		if (knownMacRssiPairs.size() == 3) {
			circles.clear();
			Map<Position, Double> map = new HashMap<Position, Double>();
			List<Position> keys = new ArrayList<Position>();
			for (MacRssiPair pair : knownMacRssiPairs) {
				keys.add(knownLocations.get(pair.getMacAsString()));
				Position loc = knownLocations.get(pair.getMacAsString());
				circles.add(new Circle((int) loc.getX(), (int) loc.getY(), (int) Math.sqrt(Math.pow(DISTANCECONSTANT * pair.getRssi(), 2) - Math.pow(9, 2))));
				map.put(loc, DISTANCECONSTANT * pair.getRssi());
			}
			
			pos = findCenter();

			lastPos = pos;
		}
		return pos;
	}
	
	public Position realify(Position position) {
		boolean again  = true;
		while (again) {
			if (position.getX() < 3) {
				position = new Position(position.getX() + 1, position.getY());
			} else if (position.getX() > 36) {
				position = new Position(position.getX() - 1, position.getY());
			} else if (position.getY() < 0) {
				position = new Position(position.getX(), position.getY() + 1);
			} else if (position.getY() > 53) {
				position = new Position(position.getX(), position.getY() - 1);
			} else {
				again = false;
			}
		}
		return position;
	}
	
	private Position findCenter() {
	    int top = 0;
	    int bot = 0;
	    for (int i=0; i<3; i++) {
	        Circle circle = circles.get(i);
	        Circle circle2, circle3;
	        if (i==0) {
	            circle2 = circles.get(1);
	            circle3 = circles.get(2);
	        }
	        else if (i==1) {
	            circle2 = circles.get(0);
	            circle3 = circles.get(2);
	        }
	        else {
	            circle2 = circles.get(0);
	            circle3 = circles.get(1);
	        }

	        int d = circle2.x - circle3.x;

	        int v1 = (circle.x * circle.x + circle.y * circle.y) - (circle.radius * circle.radius);
	        top += d*v1;

	        int v2 = circle.y * d;
	        bot += v2;

	    }

	    int y = top / (2*bot);
	    Circle circleA = circles.get(0);
	    Circle circleB = circles.get(1);
	    top = circleB.radius*circleB.radius+circleA.x*circleA.x+circleA.y*circleA.y-circleA.radius*circleA.radius-circleB.x*circleB.x-circleB.y*circleB.y-2*(circleA.y-circleB.y)*y;
	    bot = circleA.x-circleB.x;
	    int x = top / (2*bot);

	    return new Position(x,y);

	}
	
	public class Circle {
		public int x;
		public int y;
		public int radius;
		
		public Circle(int x, int y, int r) {
			this.x = x;
			this.y = y;
			this.radius = r;
		}
	}
	
	public MacRssiPair getPair(String address, MacRssiPair[] knownMacRssiPairs) {
		MacRssiPair res = null;
		for (MacRssiPair pair : knownMacRssiPairs) {
			if (pair.getMacAsString().equals(address)) {
				res = pair;
				break;
			}
		}
		return res;
	}
}
