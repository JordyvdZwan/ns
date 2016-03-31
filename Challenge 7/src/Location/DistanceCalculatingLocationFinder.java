package Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Utils.*;

//knownLocations.put("f4:cf:e2:54:e3:30", new Position(28,6));	//ap2700-0094
//knownLocations.put("f4:cf:e2:2c:1b:40", new Position(9,27));	//ap2700-0093
//knownLocations.put("f4:cf:e2:2c:0f:20", new Position(28,47));	//ap2700-0092	

/**
 * Simple Location finder that returns the first known APs location from the list of received MAC addresses
 * @author
 *
 */
public class DistanceCalculatingLocationFinder implements LocationFinder{
	
	private HashMap<String, Position> knownLocations; //Contains the known locations of APs. The long is a MAC address.
	
	public DistanceCalculatingLocationFinder(){
		knownLocations = Utils.getKnownLocations(); //Put the known locations in our hashMap
	}
//2.857142857
	public static final double DISTANCECONSTANT = 1.2;
	
	List<Circle> circles = new ArrayList<Circle>();
	Position lastPos = new Position(0,0);
	@Override
	public Position locate(MacRssiPair[] data) {
		Position pos = lastPos;
		List<MacRssiPair> knownMacRssiPairs = new ArrayList<MacRssiPair>();
		
		
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
//			pos = getLocationByTrilateration(keys.get(0), map.get(keys.get(0)), keys.get(1), map.get(keys.get(1)), keys.get(2), map.get(keys.get(2)));
//			pos = realify(pos);
//			pos = new Position((((pos.getX() - 29) / 5) * 15) + 15, (((pos.getY() - 25) / 10) * 26) + 26);
			
			
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
	        Circle c = circles.get(i);
	        Circle c2, c3;
	        if (i==0) {
	            c2 = circles.get(1);
	            c3 = circles.get(2);
	        }
	        else if (i==1) {
	            c2 = circles.get(0);
	            c3 = circles.get(2);
	        }
	        else {
	            c2 = circles.get(0);
	            c3 = circles.get(1);
	        }

	        int d = c2.x - c3.x;

	        int v1 = (c.x * c.x + c.y * c.y) - (c.r * c.r);
	        top += d*v1;

	        int v2 = c.y * d;
	        bot += v2;

	    }

	    int y = top / (2*bot);
	    Circle c1 = circles.get(0);
	    Circle c2 = circles.get(1);
	    top = c2.r*c2.r+c1.x*c1.x+c1.y*c1.y-c1.r*c1.r-c2.x*c2.x-c2.y*c2.y-2*(c1.y-c2.y)*y;
	    bot = c1.x-c2.x;
	    int x = top / (2*bot);

	    return new Position(x,y);

	}
	
	public class Circle {
		public int x;
		public int y;
		public int r;
		
		public Circle(int x, int y, int r) {
			this.x = x;
			this.y = y;
			this.r = r;
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
	
	public List<MacRssiPair> getKnownMacRssiPairs(MacRssiPair[] data) {
		List<MacRssiPair> result = new ArrayList<MacRssiPair>();
		for (MacRssiPair pair : data) {
//			if (pair.getMacAsString() == "f4:cf:e2:54:e3:30" || pair.getMacAsString() == "f4:cf:e2:2c:0f:20") {
//				result.add(pair);
//			}
			result.add(pair);
		}
		return result;
	}

	public Position getLocationByTrilateration(
			Position location1, double distance1,
			Position location2, double distance2,
			Position location3, double distance3){

	    //DECLARE VARIABLES

	    double[] P1   = new double[2];
	    double[] P2   = new double[2];
	    double[] P3   = new double[2];
	    double[] ex   = new double[2];
	    double[] ey   = new double[2];
	    double[] p3p1 = new double[2];
	    double jval  = 0;
	    double temp  = 0;
	    double ival  = 0;
	    double p3p1i = 0;
	    double triptx;
	    double tripty;
	    double xval;
	    double yval;
	    double t1;
	    double t2;
	    double t3;
	    double t;
	    double exx;
	    double d;
	    double eyy;

	    //TRANSALTE POINTS TO VECTORS
	    //POINT 1
	    P1[0] = location1.getX();
	    P1[1] = location1.getY();
	    //POINT 2
	    P2[0] = location2.getX();
	    P2[1] = location2.getY();
	    //POINT 3
	    P3[0] = location3.getX();
	    P3[1] = location3.getY();

	    for (int i = 0; i < P1.length; i++) {
	        t1   = P2[i];
	        t2   = P1[i];
	        t    = t1 - t2;
	        temp += (t*t);
	    }
	    d = Math.sqrt(temp);
	    for (int i = 0; i < P1.length; i++) {
	        t1    = P2[i];
	        t2    = P1[i];
	        exx   = (t1 - t2)/(Math.sqrt(temp));
	        ex[i] = exx;
	    }
	    for (int i = 0; i < P3.length; i++) {
	        t1      = P3[i];
	        t2      = P1[i];
	        t3      = t1 - t2;
	        p3p1[i] = t3;
	    }
	    for (int i = 0; i < ex.length; i++) {
	        t1 = ex[i];
	        t2 = p3p1[i];
	        ival += (t1*t2);
	    }
	    for (int  i = 0; i < P3.length; i++) {
	        t1 = P3[i];
	        t2 = P1[i];
	        t3 = ex[i] * ival;
	        t  = t1 - t2 -t3;
	        p3p1i += (t*t);
	    }
	    for (int i = 0; i < P3.length; i++) {
	        t1 = P3[i];
	        t2 = P1[i];
	        t3 = ex[i] * ival;
	        eyy = (t1 - t2 - t3)/Math.sqrt(p3p1i);
	        ey[i] = eyy;
	    }
	    for (int i = 0; i < ey.length; i++) {
	        t1 = ey[i];
	        t2 = p3p1[i];
	        jval += (t1*t2);
	    }
	    xval = (Math.pow(distance1, 2) - Math.pow(distance2, 2) + Math.pow(d, 2))/(2*d);
	    yval = ((Math.pow(distance1, 2) - Math.pow(distance3, 2) + Math.pow(ival, 2) + Math.pow(jval, 2))/(2*jval)) - ((ival/jval)*xval);

	    t1 = location1.getX();
	    t2 = ex[0] * xval;
	    t3 = ey[0] * yval;
	    triptx = t1 + t2 + t3;

	    t1 = location1.getY();
	    t2 = ex[1] * xval;
	    t3 = ey[1] * yval;
	    tripty = t1 + t2 + t3;

	    return new Position(triptx,tripty);

	}
	
}
