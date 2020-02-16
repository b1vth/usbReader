package pl.wrocansat.usbReader.Listener;

import java.util.Arrays;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import pl.wrocansat.usbReader.Frame.Chart;
import pl.wrocansat.usbReader.Utils.Logger;

public class PortListener implements SerialPortEventListener {
	
	private static String data = "0";
	private static SerialPort serialPort;
	
    @Override
    public void serialEvent(SerialPortEvent event) {
    	serialPort = Chart.getSerialPort();
        
    	if(event.isRXCHAR() && event.getEventValue() > 41) {
            try {	
            	byte buffer[] = serialPort.readBytes(41);
            	data = new String(buffer);
            	Logger.sendLog("Data comming from " + event.getPortName() + ": " + data);
            	Arrays.fill(buffer, (byte)0);
            }
            catch (SerialPortException ex) {
				Logger.sendError("Error in receiving string from COM-port: " + ex);
            }
        }
    }

	public static String getData() {
		return data;
	}    
}
    
