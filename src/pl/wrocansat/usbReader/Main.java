package pl.wrocansat.usbReader;

import jssc.SerialPortException;
import pl.wrocansat.usbReader.Frame.Chart;
import pl.wrocansat.usbReader.Frame.Window;
import pl.wrocansat.usbReader.Threads.DataSaveThread;
import pl.wrocansat.usbReader.Utils.Logger;

import javax.swing.*;
import java.io.IOException;
import java.util.Scanner;

public class Main {

	private static Window window;
	private static Thread chart;
	private static Thread dataSave;

	public static void main(String[] args) throws IOException {
		int width;
		int height;
		Scanner input = new Scanner(System.in);

		System.out.print("Enter an width: ");
		width = input.nextInt();
		System.out.print("Enter an height: ");
		height = input.nextInt();

		init(width, height);
	}

	static void init(int width, int height) {
		registerShutDownListener();
		createFrame(width, height);
		Logger.sendInfo("Registring threads!");
		chart = new Thread(new Chart());
		dataSave = new Thread(new DataSaveThread());

		Logger.sendInfo("Starting threads!");
		chart.start();
		dataSave.start();
	}
	
	private static void registerShutDownListener() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
		    public void run() {
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

	static void createFrame(int width, int height) {
		JFrame frame = new JFrame("Painter from USB");
		window = new Window(width, height);
		frame.add(window);
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static Window getWindow() {
		return window;
	}
}