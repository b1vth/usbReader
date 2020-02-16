package pl.wrocansat.usbReader;


import jssc.SerialPortException;
import pl.wrocansat.usbReader.Frame.Chart;
import pl.wrocansat.usbReader.Listener.PortListener;
import pl.wrocansat.usbReader.Threads.DataSaveThread;
import pl.wrocansat.usbReader.Utils.Logger;
import pl.wrocansat.usbReader.Frame.Window;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class Main {

	private static Window window;
	private static Window window2;
	private static Thread chart;
	private static Thread dataSave;

	public static PrintWriter pw;

	public static void main(String[] args) throws IOException {
		registerListeners();
		createFrame();
		createDebugFrame();
		Logger.sendInfo("Registring threads!");
		chart = new Thread(new Chart());
		dataSave = new Thread(new DataSaveThread());

		Logger.sendInfo("Starting threads!");
		chart.start();
		dataSave.start();
	}
	
	private static void registerListeners() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
		    public void run() {
		    	pw.close();
		        try {
					if(Chart.getSerialPort() != null && Chart.getSerialPort().isOpened()) {
						Logger.sendInfo("Port " + Chart.getSerialPort().getPortName() + " closed!");
						Chart.getSerialPort().closePort();
					}
					chart.stop();
					dataSave.stop();
				} catch (SerialPortException e) {
					e.printStackTrace();
				}
		    }
		}));
	}

	static void createFrame() {
		int width = 800;
		int height = 600;
		JFrame frame = new JFrame("Painter from USB");
		window = new Window(width, height);
		frame.add(window);
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	static void createDebugFrame() {
		int width = 800;
		int height = 600;
		JFrame frame = new JFrame("Painter from savedData");
		window2 = new Window(width, height);
		frame.add(window2);
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static Window getWindow() {
		return window;
	}

	public static Window getWindow2() { return window2; }
}
