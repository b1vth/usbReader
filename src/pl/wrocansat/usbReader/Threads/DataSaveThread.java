package pl.wrocansat.usbReader.Threads;

import pl.wrocansat.usbReader.Frame.Chart;
import pl.wrocansat.usbReader.Listener.PortListener;
import pl.wrocansat.usbReader.Main;
import pl.wrocansat.usbReader.Utils.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataSaveThread implements Runnable {

    static PrintWriter pw;
    private volatile boolean running = true;

    public DataSaveThread() {
        pw = Main.pw;
        try {
            pw = new PrintWriter("Data.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String lastLine = "";
        while (running) {
            String line = PortListener.getData().replace("\n", "").replace("\r", "");
            boolean canPaint = lastLine.equals(line);
            if (!canPaint) {
                pw.println(PortListener.getData());
                lastLine = line;
            }
            try {
                Thread.sleep(Chart.refreshTimeX);
            } catch (InterruptedException e) {
                pw.close();
                Logger.sendInfo("SaveThread stopped!");
            }
        }
    }

    public void terminate() {
        running = false;
    }
}
