package pl.wrocansat.usbReader.Frame;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import pl.wrocansat.usbReader.Listener.PortListener;
import pl.wrocansat.usbReader.Main;
import pl.wrocansat.usbReader.Utils.Logger;
import pl.wrocansat.usbReader.Utils.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Chart implements Runnable{
	
	static int x = 0;
	
	public static SerialPort serialPort;
	public static long refreshTimeX;
	
	private static JComboBox<String> portList;
	private static JFrame window;
	private static JButton connectButton;

	private static XYSeries temperature;
	private static XYSeries pressure;
	private static XYSeries firstGas;
	private static XYSeries secondGas;
	private static XYSeries thirdGas;
	private static XYSeries humidity;
	private static XYSeries airClear;

	private static JLabel timeRefreshLabel;
	private static JTextField timeRefreshText;
	//Thanks for upgrdman from YouTube for chart look

	public Chart() {
		createWindow();
	}
	
	private void createWindow() {
		window = new JFrame();
		window.setTitle("USBReader");
		window.setSize(600, 400);
		window.setLayout(new BorderLayout());
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		timeRefreshText = new JTextField();
		timeRefreshText.setPreferredSize(new Dimension(40, 25));
		timeRefreshText.setText("250");
		timeRefreshText.setFont(timeRefreshText.getFont().deriveFont(timeRefreshText.getFont().getStyle() | Font.BOLD));

		timeRefreshLabel = new JLabel("Refresh time:  ");
		timeRefreshLabel.setFont(timeRefreshLabel.getFont().deriveFont(timeRefreshLabel.getFont().getStyle() | Font.BOLD));

		portList = new JComboBox<String>();
		connectButton = new JButton("Connect");
		JPanel topPanel = new JPanel();
		topPanel.add(portList);
		topPanel.add(connectButton);
		topPanel.add(timeRefreshLabel);
		topPanel.add(timeRefreshText);
		window.add(topPanel, BorderLayout.NORTH);
		
		String[] portNames = SerialPortList.getPortNames();
		for(int i = 0; i < portNames.length; i++)
			portList.addItem(portNames[i]);

		temperature = new XYSeries("temperature");
		pressure = new XYSeries("pressure");
		firstGas = new XYSeries("firstGas");
		secondGas = new XYSeries("secondGas");
		thirdGas = new XYSeries("thirdGas");
		humidity = new XYSeries("humidity");
		airClear = new XYSeries("air");

		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(temperature);
		dataset.addSeries(pressure);
		dataset.addSeries(firstGas);
		dataset.addSeries(secondGas);
		dataset.addSeries(thirdGas);
		dataset.addSeries(humidity);
		dataset.addSeries(airClear);

		JFreeChart chart = ChartFactory.createXYLineChart("SerialPort Readings", "Time", "Data", dataset, PlotOrientation.VERTICAL, false, false, false);
		window.add(new ChartPanel(chart), BorderLayout.CENTER);
	}
	
	private void init() {
		try {
			if(!(Util.isInteger(timeRefreshText.getText()))) {
				Logger.sendError("Refresh Time is not Integer!");
				return;
			}

			if(serialPort == null) {
				Logger.sendError("Serial port is null!");
				return;
			}

		    serialPort.openPort();
		    serialPort.setParams(9600, 8, 1, 0);
		    serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
		    serialPort.addEventListener(new PortListener(), SerialPort.MASK_RXCHAR);
		    Logger.sendInfo("Everyting is ok...");
		    Logger.sendInfo("Starting new Thread...");
		}
		catch (SerialPortException ex) {
		    Logger.sendError("There are an error on writing string to port т: " + ex);
		}
	}

	@Override
	public void run() {
		final long[] refreshTime = new long[1];
		final String[] refreshTimeString = new String[1];

		connectButton.addActionListener(new ActionListener(){
			@Override public void actionPerformed(ActionEvent arg0) {
				if(connectButton.getText().equals("Connect")) {
					serialPort = new SerialPort(portList.getSelectedItem().toString());
					init();

					refreshTimeString[0] = timeRefreshText.getText().replace(" ", "");
					refreshTime[0] = Integer.parseInt(refreshTimeString[0]);
					refreshTimeX = refreshTime[0];

					Logger.sendInfo("Selected port: " + portList.getSelectedItem().toString());
					connectButton.setText("Disconnect");
					portList.setEnabled(false);
					Thread thread = new Thread(){
						@Override public void run() {
							String lastLine = "";
							boolean running=true;
							while (!portList.isEnabled()) {
								String line = PortListener.getData().replace("\n", "").replace("\r", "");
								//d - dane /obrot x/obrot y/obrot z/temperatura/cisnienie/1 gaz/2 gaz/dlugosc/szerokosc/3 gaz/wilgoc/jakosc powietrza
								//p - zdjecie

								if(line.charAt(0) == 'd') {
									String[] dataSplitted = line.split(",");
									float temp = Float.parseFloat(dataSplitted[3]);
									int press = Integer.parseInt(dataSplitted[4]);
									int firstG = Integer.parseInt(dataSplitted[5]);
									int secondG = Integer.parseInt(dataSplitted[6]);
									int thirdG = Integer.parseInt(dataSplitted[9]);
									int humid = Integer.parseInt(dataSplitted[10]);
									int air = Integer.parseInt(dataSplitted[11]);

									temperature.add((x++)/4, temp);
									pressure.add((x++)/4, press);
									firstGas.add((x++)/4, firstG);
									secondGas.add((x++)/4, secondG);
									thirdGas.add((x++)/4, thirdG);
									humidity.add((x++)/4, humid);
									airClear.add((x++)/4, air);
								}

								if(line.charAt(0) == 'p') {
									boolean canPaint = lastLine.equals(line);
									if(!canPaint) {
										Main.getWindow().fillNextPixel(line);
										lastLine = line;
									}
									window.repaint();
								}

								String[] lineSplitted = line.split(",");
								Logger.sendInfo("Data from port " + serialPort.getPortName() + line);

							}
							try {
								Thread.sleep(refreshTime[0]);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					};
					thread.start();
				} else {
					portList.setEnabled(true);
					connectButton.setText("Connect");
					temperature.clear();
					pressure.clear();
					firstGas.clear();
					secondGas.clear();
					thirdGas.clear();
					humidity.clear();
					airClear.clear();
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