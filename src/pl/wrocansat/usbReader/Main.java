package pl.wrocansat.usbReader;

import jssc.SerialPortException;
import pl.wrocansat.usbReader.Frame.Chart;
public class Main {
	
	public static void main(String[] args) {
		registerListeners();
		Thread t = new Thread(new Chart());
		t.start();
	}
	
	private static void registerListeners() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
		    public void run() {
		        try {
		        	if(Chart.getSerialPort() == null) {
		        		System.exit(0);
		        	}
		        	if(Chart.getSerialPort().isOpened()) Chart.getSerialPort().closePort();
				} catch (SerialPortException e) {
					e.printStackTrace();
				}
		    }
		}));
	}
}
