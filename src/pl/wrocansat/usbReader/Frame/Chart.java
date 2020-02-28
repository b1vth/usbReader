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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class Chart implements Runnable{
	
	static int x = 0;
	
	public static SerialPort serialPort;
	public static long refreshTimeX;
	
	private static JComboBox<String> portList;
	private static JFrame window;
	private static JButton connectButton;

	private static Collection<XYSeries> gasList;
	private static Collection<XYSeries> temperatureList;
	private static Collection<XYSeries> pressureList;

	private static XYSeries temperature;
	private static XYSeries pressure;
	private static XYSeries firstGas;
	private static XYSeries secondGas;
	private static XYSeries humidityFirst;
	private static XYSeries humiditySecond;
	private static XYSeries humidityThird;
	private static XYSeries humidityFourth;
	private static XYSeries airQuality;

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

		JLabel timeRefreshLabel = new JLabel("Refresh time:  ");
		timeRefreshLabel.setFont(timeRefreshLabel.getFont().deriveFont(timeRefreshLabel.getFont().getStyle() | Font.BOLD));

		portList = new JComboBox<>();
		connectButton = new JButton("Connect");
		JPanel topPanel = new JPanel();
		topPanel.add(portList);
		topPanel.add(connectButton);
		topPanel.add(timeRefreshLabel);
		topPanel.add(timeRefreshText);
		window.add(topPanel, BorderLayout.PAGE_START);
		
		String[] portNames = SerialPortList.getPortNames();
		for (String portName : portNames) portList.addItem(portName);

		temperature = new XYSeries("temperature");
		pressure = new XYSeries("pressure");
		firstGas = new XYSeries("firstGas");
		secondGas = new XYSeries("secondGas");
		humidityFirst = new XYSeries("humidity 1");
		humiditySecond = new XYSeries("humidity 2");
		humidityThird = new XYSeries("humidity 3");
		humidityFourth = new XYSeries("humidity 4");
		airQuality = new XYSeries("air");

		preInit();

		XYSeriesCollection temperatureSeries = new XYSeriesCollection();
		for(XYSeries series : temperatureList) temperatureSeries.addSeries(series);


		XYSeriesCollection gasSeries = new XYSeriesCollection();
		for(XYSeries series : gasList) gasSeries.addSeries(series);

		XYSeriesCollection pressureSeries = new XYSeriesCollection();
		for(XYSeries series : pressureList) pressureSeries.addSeries(series);

		JFreeChart temperatureChart = ChartFactory.createXYLineChart("Temperature", "Time", "Temperature", temperatureSeries, PlotOrientation.VERTICAL, true, false, false);
		JFreeChart gasChart = ChartFactory.createXYLineChart("Air", "Time", "Percent", gasSeries, PlotOrientation.VERTICAL, true ,false , false);
		JFreeChart pressureChart = ChartFactory.createXYLineChart("Pressure", "Time", "Pressure", pressureSeries, PlotOrientation.VERTICAL, true, false, false);

		window.add(new ChartPanel(temperatureChart), BorderLayout.EAST);
		window.add(new ChartPanel(gasChart), BorderLayout.CENTER);
		window.add(new ChartPanel(pressureChart), BorderLayout.WEST);
	}

	private void preInit() {

		gasList = new ArrayList<>();
		pressureList = new ArrayList<>();
		temperatureList = new ArrayList<>();

		gasList.add(firstGas);
		gasList.add(secondGas);
		gasList.add(airQuality);
		gasList.add(humidityFirst);
		gasList.add(humiditySecond);
		gasList.add(humidityThird);
		gasList.add(humidityFourth);

		pressureList.add(pressure);

		temperatureList.add(temperature);
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

		connectButton.addActionListener(arg0 -> {
			if(connectButton.getText().equals("Connect")) {
				if(portList.getItemCount() <= 0) {
					Logger.sendError("Serial port is null!");
					return;
				}
				serialPort = new SerialPort(Objects.requireNonNull(portList.getSelectedItem()).toString());
				init();

				refreshTimeString[0] = timeRefreshText.getText().replace(" ", "");
				refreshTime[0] = Integer.parseInt(refreshTimeString[0]);
				refreshTimeX = refreshTime[0];

				Logger.sendInfo("Selected port: " + portList.getSelectedItem().toString());
				connectButton.setText("Disconnect");
				portList.setEnabled(false);
				Thread thread = new Thread(() -> {
					String lastLine = "";
					while (!portList.isEnabled()) {
						String line = PortListener.getData().replace("\n", "").replace("\r", "");
						//d - dane /obrot x/obrot y/obrot z/temperatura/cisnienie/1 gaz/2 gaz/dlugosc/szerokosc/3 gaz/wilgoc/jakosc powietrza
						//p - zdjecie

						float temp = 0;
						int press = 0;
						int firstG = 0;
						//int thirdG = 0;
						int secondG = 0;
						int humid = 0;
						int air = 0;

						if(line.charAt(0) == 'd') {
							String[] dataSplitted = line.split(",");
							if(dataSplitted[3] != null && Util.isFloat(dataSplitted[3])) {
								temp = Float.parseFloat(dataSplitted[3]);
							} else Logger.sendLog("Temperature is null or not Float!");
							if(dataSplitted[4] != null && Util.isInteger(dataSplitted[4])) {
								press = Integer.parseInt(dataSplitted[4]);
							} else Logger.sendLog("Pressure is null or not Integer!");
							if(dataSplitted[5] != null && Util.isInteger(dataSplitted[5])) {
								firstG = Integer.parseInt(dataSplitted[5]);
							} else Logger.sendLog("First Gas is null or not Integer!");
							if(dataSplitted[6] != null && Util.isInteger(dataSplitted[6])) {
								secondG = Integer.parseInt(dataSplitted[6]);
							} else Logger.sendLog("Second Gas is null or not Integer!");
							if(dataSplitted[8] != null && Util.isInteger(dataSplitted[10])) {
								humid = Integer.parseInt(dataSplitted[10]);
							} else Logger.sendLog("Humidity is null or not Integer!");
							if(dataSplitted[9] != null && Util.isInteger(dataSplitted[11])){
								air = Integer.parseInt(dataSplitted[11]);
							} else Logger.sendLog("AirQuality is null or not Integer!");

							temperature.add((x++)/4, temp);
							pressure.add((x++)/4, press);
							firstGas.add((x++)/4, firstG);
							secondGas.add((x++)/4, secondG);
							humidityFirst.add((x++)/4, humid);
							humiditySecond.add((x++)/4, humid);
							humidityThird.add((x++)/4, humid);
							humidityFourth.add((x++)/4, humid);
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
				});
				thread.start();
			} else {
				portList.setEnabled(true);
				connectButton.setText("Connect");
				for(XYSeries series : temperatureList) series.clear();
				for(XYSeries series : gasList) series.clear();
				for(XYSeries series : pressureList) series.clear();
				x = 0;
			}
		});
		window.setVisible(true);
	}

	public static SerialPort getSerialPort() {
		return serialPort;
	}
}