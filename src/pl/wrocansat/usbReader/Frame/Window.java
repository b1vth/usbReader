package pl.wrocansat.usbReader.Frame;

import pl.wrocansat.usbReader.Utils.Logger;
import pl.wrocansat.usbReader.Utils.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Window extends JPanel {

    int x, y;

    private BufferedImage canvas;

    public Window(int width, int height) {
        createCanvas(width, height);
        x = 0;
        y = 0;
    }

    void createCanvas(int width, int height) {
        canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        fillCanvas(new Color(0, 0, 0));
    }

    public void fillCanvas(Color c) {
        int color = c.getRGB();
        Logger.sendInfo("Creating basic canvas!");
        for (int x = 0; x < canvas.getWidth(); x++) {
            for (int y = 0; y < canvas.getHeight(); y++) {
                canvas.setRGB(x, y, color);
            }
        }
        repaint();
        Logger.sendInfo("Basic canvas created!");
    }

    public void fillPixel(int x, int y, Color c) {
        int color = c.getRGB();
        canvas.setRGB(x, y, color);
        Logger.sendLog("Pixel  " + x + " " + y + " has been set to [" + c.getRed() + "," + c.getGreen() + "," + c.getBlue() + "]");
        repaint();
    }

    public void fillNextPixel(String s) {
        for(Color c : Util.getColorFromString(s)){
            if (x < this.getWidth()) {
                fillPixel(x, y, c);
                x++;
            } else {
                y++;
                x = 0;
                fillPixel(x, y, c);
            }
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(canvas.getWidth(), canvas.getHeight());
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(canvas, null, null);
    }
}
