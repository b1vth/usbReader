package pl.wrocansat.usbReader.Threads;

import pl.wrocansat.usbReader.Frame.Chart;
import pl.wrocansat.usbReader.Listener.PortListener;
import pl.wrocansat.usbReader.Utils.Logger;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class DataSaveThread implements Runnable {

    static PrintWriter data;
    static PrintWriter picture;
    private volatile boolean running = true;

    public DataSaveThread() {
        try {
            data = new PrintWriter("data.txt");
            picture = new PrintWriter("picture.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String lastLine = "";
        while (running) {
            String line = PortListener.getData().replace("\n", "").replace("\r", "");

            if(line.isEmpty()) return;

            if(line.charAt(0) == 'd') data.println(line.replace("d", ""));


            if(line.charAt(0) == 'p') {
                boolean canPaint = lastLine.equals(line);
                if (!canPaint) {
                    picture.println(line);
                    lastLine = line;
                }
            }
            try {
                Thread.sleep(Chart.refreshTimeX);
            } catch (InterruptedException e) {
                if(data != null) data.close();
                if(picture != null) picture.close();
                Logger.sendInfo("SaveThread stopped!");
            }
        }
    }
}