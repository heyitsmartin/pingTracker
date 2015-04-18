package pingTracker;

import java.io.FileNotFoundException;

public class Runner {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PingTracker tracker=null;
		try {
			 tracker = new PingTracker();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		tracker.run();
		
	}

}
