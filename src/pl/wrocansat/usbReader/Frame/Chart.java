package pl.wrocansat.usbReader.Frame;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import pl.wrocansat.usbReader.Listener.PortReader;

public class Chart implements Runnable{
	
	static int x = 0;
	static int y = 0;
	
	public static SerialPort serialPort;
	
	private static JComboBox<String> portList;
	private static JFrame window;
	private static JButton connectButton;
	private static XYSeries series;
	
	public Chart() {
		createWindow();
	}
	
	private void createWindow() {
		window = new JFrame();
		window.setTitle("USBReader");
		window.setSize(600, 400);
		window.setLayout(new BorderLayout());
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		portList = new JComboBox<String>();
		connectButton = new JButton("Connect");
		JPanel topPanel = new JPanel();
		topPanel.add(portList);
		topPanel.add(connectButton);
		window.add(topPanel, BorderLayout.NORTH);
		
		String[] portNames = SerialPortList.getPortNames();
		for(int i = 0; i < portNames.length; i++)
			portList.addItem(portNames[i]);
		
		series = new XYSeries("SerialPort Readings");
		XYSeriesCollection dataset = new XYSeriesCollection(series);
		JFreeChart chart = ChartFactory.createXYLineChart("SerialPort Readings", "Time", "Data", dataset, PlotOrientation.VERTICAL, false, false, false);
		window.add(new ChartPanel(chart), BorderLayout.CENTER);
	}
	
	private void init() {
		try {
		    serialPort.openPort();
		    serialPort.setParams(9600, 8, 1, 0);
		    serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
		    serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);
		}
		catch (SerialPortException ex) {
		    System.out.println("There are an error on writing string to port т: " + ex);
		}
	}

	@Override
	public void run() {
		connectButton.addActionListener(new ActionListener(){
			@Override public void actionPerformed(ActionEvent arg0) {
				if(connectButton.getText().equals("Connect")) {
					serialPort = new SerialPort(portList.getSelectedItem().toString());
					System.out.println(portList.getSelectedItem().toString());
					init();
					connectButton.setText("Disconnect");
					portList.setEnabled(false);
					Thread thread = new Thread(){
						@Override public void run() {
							while (!portList.isEnabled()) {
									String line = PortReader.getData().replace("\n", "").replace("\r", "");;
									double number = Double.parseDouble(line);
									System.out.println(number);
									series.add((x++)/4, number);
									window.repaint();
									try {
										Thread.sleep(250);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
							}
						}
					};
					thread.start();
				} else {
					portList.setEnabled(true);
					connectButton.setText("Connect");
					series.clear();
					x = 0;
				}
			}
		});
		window.setVisible(true);
	}

	public static SerialPort getSerialPort() {
		return serialPort;
	}
}