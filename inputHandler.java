package pingTracker;

import java.util.Enumeration;
import java.util.Scanner;

public class inputHandler implements Runnable {
	PingTracker pt ;
	Scanner scanner = new Scanner(System.in);
	
	public inputHandler(PingTracker pt ) { 
		this.pt = pt ;
		
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true) { 
			String input = scanner.nextLine();
			if(!pt.isBusy()){
				parseInput(input);
			}
		}
	}
	private void parseInput(String in ) { 
		if( in.equalsIgnoreCase("EXIT")) { 
			
			pt.exit(); 
		}else if( in.equalsIgnoreCase("ADDRESSES")){
			pt.printAd();
		}else if( in.equalsIgnoreCase("STOP")){
			pt.write("chill");
			pt.stop();
		}else if( in.equalsIgnoreCase("TIME")) { 
			pt.time();
		}else if (in.equalsIgnoreCase("START")){
			pt.start();
		}else { 
		
		
			pt.write("invalid input");
		}
	}

}
