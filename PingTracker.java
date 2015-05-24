package pingTracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class PingTracker implements Runnable {
	int dest = -1;
	int time = -1; 
	int end = -1; 
	boolean shouldRun = true;
	long interval = -1; 
	boolean systemOut= true; 
	FileOutputStream fs = null ;
	Scanner scanner = new Scanner(System.in);
	List<InetAddress> list;
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Timer timer = null;
	boolean busy =false;
	long stime = 0;
	private static final class Lock { }
	private final Object lock = new Lock() ;
	
	public PingTracker() throws FileNotFoundException { 
		File logFile = new File("PingTracker_log"+System.currentTimeMillis() + ".log");
		fs = new FileOutputStream(logFile);
		list = new ArrayList<InetAddress>();
		timer = new Timer();
	}
	public synchronized boolean isBusy() {
		return busy;
	}
	private void getDest() {
		String input = "" ;
		InetAddress address = null;
		
		while(true){
			write("Which servers do you want me to track? \n Type \" END \" to stop "); 
			
			input = scanner.nextLine(); 
			write("Input: "+input);
			if (input.equalsIgnoreCase("END")){ 
				write("END request accepted ");
				return; 
			}
			
			try{ 
				write("Validating: "+input);
				address = InetAddress.getByName(input); 
				address.isReachable(3000);
			}catch( UnknownHostException e ){ 
				write("The host could not be found"); 
				continue;
			} catch (IOException e) {
				write("host could not be reached" ) ; 
				write( e.getMessage());
				continue;
			}
			
			
			list.add(address);
			write("Input accepted \nenter next address");
			
			
		}
		
		
	}
	
	private void ping (){ 
		busy = true; 
		BufferedReader is = null ; 
		for (InetAddress ad : list) { 
			String a = ad.getHostAddress();
			ProcessBuilder pb = new ProcessBuilder("ping",a);
			Process proc;
			try {
				proc = pb.start();

				is = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				proc.waitFor();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			StringBuilder builder = new StringBuilder(); 
			String line = ""; 
			
			try {
				while ( (line = is.readLine()) != null) {
					   builder.append(line);
					   builder.append(System.getProperty("line.separator"));
					}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String blah = builder.toString();
			parsePing(blah ,ad);
			
		}
		busy = false;
	}
	
	
	
	private void parsePing(String blah,InetAddress ad) {
		// TODO Auto-generated method stub
		String result[] = blah.split(" ");
		for (int i = 0; i< result.length; i++ ) {
			String word = result[i];
			if( word.contains("time=")){ 
				write("["+dateFormat.format(new Date()) + "]: for : " +ad.getHostAddress()+" = "+ word.substring(word.indexOf('=')+1));
				
			}else if (word.contains("Average")) { 
				
				write("average: "+result[i+2]);
			}
		}
	}
	public void time() { 
		write("Remaining time: " + (int)((interval -(System.currentTimeMillis()- stime))/1000));
	}
	protected void write(String string) { 
		try{
			
			System.out.println(string);
		
			string = dateFormat.format(new Date()) + string +"\n";
			fs.write(string.getBytes());
		}catch(IOException e ) { 
			e.printStackTrace();
		}
	}
	private void setInterval() { 
		write("Give me an interval in minutes to ping"); 
		String input ;
		double in =(double) -1;
		while(shouldRun) { 
			input = scanner.nextLine();
			if(input.equalsIgnoreCase("END")){ 
				break;
			}
			try{ 
				in = Double.parseDouble(input);
				break;
			}catch (IllegalArgumentException e) {
				write(input + "  is not a valid number");
				write("Try again or type \"END\' to use the default value");
				
			}
		}
		//convert from minutes to ms 
		if( in < 0 ) {
			write("Using default of 15 mins");
			interval = 15 * 60000; 
		}else { 
			write("Using "+in+" minutes");
			interval = (long)(in * 60000 );
		}
		
		
		
	}
	public void start() { 
		write("restarting service");
		shouldRun =true;
		this.startFunction();
	}
	public void stop()  {
		timer.cancel();
		shouldRun = false; 
		write("Stopped pinging");
	}
	public void printAd () { 
		write("Current addresses are:");
		for(InetAddress a : list) {
			write(a.getHostName() + " | " + a.getHostAddress());
		}
	}
	public void exit() { 
		write("Stopping service.. "); 
		System.exit(0);
	}
	public void startFunction() { 
		class PingTimer extends TimerTask { 
			public void run() { 
				ping();
				
			}
		}
		(new Thread(new inputHandler(this))).start();
		timer = new Timer();
		timer.scheduleAtFixedRate(new PingTimer(), 0,interval);
		while(shouldRun) { 
			//wait til the ping command is finished running
			while(isBusy()){
				
				
			}

			this.stime = System.currentTimeMillis();
			write(dateFormat.format(new Date()) + ": Waiting for " + interval/60000 + " minutes");
			write("Avaiable commands are ADDRESSES/EXIT "); 
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
		

			
			
		}
	}
	@Override
	public void run() {
		
		inputHandler ih = new inputHandler(this);
		getDest();
		setInterval();
		write("Starting requests...");
		ping();
		
		startFunction();
		
		
	}

	
}



