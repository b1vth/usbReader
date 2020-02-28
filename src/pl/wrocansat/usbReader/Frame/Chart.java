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
	private static XYSeries airQuality;

	private static JLabel timeRefreshLabel;
	private static JTextField timeRefreshText;
	//Thanks for upgrdman from YouTube for chart window look

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
		window.add(topPanel, BorderLayout.PAGE_START);
		
		String[] portNames = SerialPortList.getPortNames();
		for(int i = 0; i < portNames.length; i++)
			portList.addItem(portNames[i]);

		temperature = new XYSeries("temperature");
		pressure = new XYSeries("pressure");
		firstGas = new XYSeries("firstGas");
		secondGas = new XYSeries("secondGas");
		thirdGas = new XYSeries("thirdGas");
		humidity = new XYSeries("humidity");
		airQuality = new XYSeries("air");

		XYSeriesCollection data = new XYSeriesCollection();
		data.addSeries(temperature);
		data.addSeries(pressure);
		data.addSeries(humidity);

		XYSeriesCollection gas = new XYSeriesCollection();
		gas.addSeries(firstGas);
		gas.addSeries(secondGas);
		gas.addSeries(thirdGas);
		gas.addSeries(airQuality);


		JFreeChart chart = ChartFactory.createXYLineChart("SerialPort Readings", "Time", "Data", data, PlotOrientation.VERTICAL, false, false, false);
		JFreeChart percentChart = ChartFactory.createXYLineChart("Air", "Time", "data", gas, PlotOrientation.VERTICAL, false ,false , false);
		window.add(new ChartPanel(chart), BorderLayout.EAST);
		window.add(new ChartPanel(percentChart), BorderLayout.WEST);
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

								float temp = 0;
								int press = 0;
								int firstG = 0;
								int secondG = 0;
								int thirdG = 0;
								int humid = 0;
								int air = 0;

								if(line.charAt(0) == 'd') {
									String[] dataSplitted = line.split(",");
									if(dataSplitted[3] != null && Util.isFloat(dataSplitted[3]))  temp = Float.parseFloat(dataSplitted[3]);
									if(dataSplitted[4] != null && Util.isInteger(dataSplitted[4])) press = Integer.parseInt(dataSplitted[4]);
									if(dataSplitted[5] != null && Util.isInteger(dataSplitted[5])) firstG = Integer.parseInt(dataSplitted[5]);
									if(dataSplitted[6] != null && Util.isInteger(dataSplitted[6])) secondG = Integer.parseInt(dataSplitted[6]);
									if(dataSplitted[7] != null && Util.isInteger(dataSplitted[7])) thirdG = Integer.parseInt(dataSplitted[9]);
									if(dataSplitted[8] != null && Util.isInteger(dataSplitted[8])) humid = Integer.parseInt(dataSplitted[10]);
									if(dataSplitted[9] != null && Util.isInteger(dataSplitted[9])) air = Integer.parseInt(dataSplitted[11]);

									temperature.add((x++)/4, temp);
									pressure.add((x++)/4, press);
									firstGas.add((x++)/4, firstG);
									secondGas.add((x++)/4, secondG);
									thirdGas.add((x++)/4, thirdG);
									humidity.add((x++)/4, humid);
									airQuality.add((x++)/4, air);
								}

								if(line.charAt(0) == 'p') {
									boolean canPaint = lastLine.equals(line);
									if(!canPaint) {
										Main.getWindow().fillNextPixel(line);
										lastLine = line;
									}
									window.repaint();
								}
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
					airQuality.clear();
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