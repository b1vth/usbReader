package pl.wrocansat.usbReader.Listener;

import java.util.Arrays;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import pl.wrocansat.usbReader.Frame.Chart;

public class PortReader implements SerialPortEventListener {
	
	private static String data = "0";
	private static SerialPort serialPort;
	
    @Override
    public void serialEvent(SerialPortEvent event) {
    	serialPort = Chart.getSerialPort();
        
    	if(event.isRXCHAR() && event.getEventValue() > 10) {
            try {	
            	byte buffer[] = serialPort.readBytes(10);
            	data = new String(buffer); 
            	Arrays.fill(buffer, (byte)0);
            }
            catch (SerialPortException ex) {
                System.out.println("Error in receiving string from COM-port: " + ex);
            }
        }
    }

	public static String getData() {
		return data;
	}    
}
    
