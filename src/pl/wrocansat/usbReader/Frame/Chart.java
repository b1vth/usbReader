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
	private static XYSeries firstGas;
	private static XYSeries secondGas;
	private static XYSeries humidity;
	private static XYSeries pressureMid;
	private static XYSeries pressureFirst;
	private static XYSeries pressureSecond;
	private static XYSeries pressureThird;
	private static XYSeries pressureFourth;

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
		humidity = new XYSeries("humidity");
		firstGas = new XYSeries("methan");
		secondGas = new XYSeries("secondGas");
		pressureMid = new XYSeries("pressureMid");
		pressureFirst = new XYSeries("humidity 1");
		pressureSecond = new XYSeries("humidity 2");
		pressureThird = new XYSeries("humidity 3");
		pressureFourth = new XYSeries("humidity 4");

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
		gasList.add(humidity);

		pressureList.add(pressureMid);
		pressureList.add(pressureFirst);
		pressureList.add(pressureSecond);
		pressureList.add(pressureThird);
		pressureList.add(pressureFourth);

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
		    serialPort.setParams(115200, 8, 1, 0);
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
						int pressMid = 0;
						int pressFirst = 0;
						int pressSecond = 0;
						int pressThird = 0;
						int pressFourth = 0;
						int firstG = 0;
						int secondG = 0;
						int humid = 0;

						if(line.charAt(0) == 'd') {
							String[] dataSplitted = line.split(";");
							if(dataSplitted[1] != null && Util.isFloat(dataSplitted[1])) {
								temp = Float.parseFloat(dataSplitted[1]);
							}
							if(dataSplitted[2] != null && Util.isInteger(dataSplitted[2])) {
								pressMid = Integer.parseInt(dataSplitted[2]);
							}
							if(dataSplitted[3] != null && Util.isInteger(dataSplitted[3])) {
								pressFirst = Integer.parseInt(dataSplitted[3]);
							}
							if(dataSplitted[4] != null && Util.isInteger(dataSplitted[4])) {
								pressSecond = Integer.parseInt(dataSplitted[4]);
							}
							if(dataSplitted[5] != null && Util.isInteger(dataSplitted[5])) {
								pressThird = Integer.parseInt(dataSplitted[5]);
							}
							if(dataSplitted[6] != null && Util.isInteger(dataSplitted[6])) {
								pressFourth = Integer.parseInt(dataSplitted[6]);
							}
							if(dataSplitted[7] != null && Util.isInteger(dataSplitted[7])) {
								humid = Integer.parseInt(dataSplitted[7]);
							}
							if(dataSplitted[8] != null && Util.isInteger(dataSplitted[8])) {
								firstG = Integer.parseInt(dataSplitted[8]);
							}
							if(dataSplitted[9] != null && Util.isInteger(dataSplitted[9])) {
								secondG = Integer.parseInt(dataSplitted[9]);
							}

							temperature.add((x++)/4, temp);
							humidity.add((x++)/4, humid);
							firstGas.add((x++)/4, firstG);
							secondGas.add((x++)/4, secondG);
							pressureMid.add((x++)/4, pressMid);
							pressureFirst.add((x++)/4, pressFirst);
							pressureSecond.add((x++)/4, pressSecond);
							pressureThird.add((x++)/4, pressThird);
							pressureFourth.add((x++)/4, pressFirst);
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