/*
 * File:    SineWaves.java
 * Package:
 * Author:  Zachary Gill
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class SineWaves {
    
    private static int screenDim = 1400;
    
    private static int screenX = screenDim;
    
    private static int screenY = screenDim;
    
    private static double xGraphWidth = 12;
    
    private static double xGraphStep = Math.PI / 8;
    
    private static double yGraphWidth = 12;
    
    private static double yGraphStep = Math.PI / 8;
    
    private static double currentWidth = xGraphWidth;
    
    private static double currentHeight = yGraphWidth;
    
    private static boolean axes = true;
    
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());
        
        // slider to control zoom
        JSlider zoomSlider = new JSlider(0, 10000, 1000);
        pane.add(zoomSlider, BorderLayout.SOUTH);
        
        // panel to display render results
        JPanel renderPanel = new JPanel() {
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                
                currentWidth = ((double) zoomSlider.getValue() / 1000) * xGraphWidth;
                currentHeight = ((double) zoomSlider.getValue() / 1000) * yGraphWidth;
                g2.setColor(Color.WHITE);
                
                //axes
                if (axes) {
                    g2.drawLine(0, screenY / 2, screenX, screenY / 2);
                    g2.drawLine(screenX / 2, 0, screenX / 2, screenY);
                    
                    for (double xGraphMark = 0; xGraphMark <= (screenX / 2); xGraphMark += (xGraphStep / (currentWidth / 2) * (screenX / 2))) {
                        g2.drawLine((int) ((screenX / 2) + xGraphMark), (screenY / 2) + 10, (int) ((screenX / 2) + xGraphMark), (screenY / 2) - 10);
                        g2.drawLine((int) ((screenX / 2) - xGraphMark), (screenY / 2) + 10, (int) ((screenX / 2) - xGraphMark), (screenY / 2) - 10);
                    }
                    for (double yGraphMark = 0; yGraphMark <= (screenY / 2); yGraphMark += (yGraphStep / (currentHeight / 2) * (screenY / 2))) {
                        g2.drawLine((screenX / 2) + 10, (int) ((screenY / 2) + yGraphMark), (screenX / 2) - 10, (int) ((screenY / 2) + yGraphMark));
                        g2.drawLine((screenX / 2) + 10, (int) ((screenY / 2) - yGraphMark), (screenX / 2) - 10, (int) ((screenY / 2) - yGraphMark));
                    }

//                    g2.drawLine(0, 0, 0, screenY);
//                    g2.drawLine(0, (screenY / 2), screenX, (screenY/ 2));
//
//                    for (double xGraphMark = 0; xGraphMark <= screenX; xGraphMark += (xGraphStep / (currentWidth / 2) * (screenX / 2))) {
//                        g2.drawLine((int) xGraphMark, (screenY / 2) + 10, (int) xGraphMark, (screenY / 2) - 10);
//                    }
//                    for (double yGraphMark = 0; yGraphMark <= (screenY / 2); yGraphMark += (yGraphStep / (currentHeight / 2) * (screenY / 2))) {
//                        g2.drawLine(0, (int) ((screenY / 2) + yGraphMark), 10, (int) ((screenY / 2) + yGraphMark));
//                        g2.drawLine(0, (int) ((screenY / 2) - yGraphMark), 10, (int) ((screenY / 2) - yGraphMark));
//                    }
                }


//                List<Color> colors = new ArrayList<>(Arrays.asList(Color.MAGENTA, Color.MAGENTA, Color.MAGENTA, Color.MAGENTA, Color.MAGENTA));
//                List<Color> colors = new ArrayList<>(Arrays.asList(Color.WHITE, Color.CYAN, Color.PINK, Color.MAGENTA, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.RED, Color.LIGHT_GRAY, Color.GRAY, Color.DARK_GRAY));
//                List<Color> colors = new ArrayList<>(Arrays.asList(Color.WHITE, Color.WHITE, Color.CYAN, Color.CYAN, Color.PINK, Color.PINK, Color.MAGENTA, Color.MAGENTA, Color.BLUE, Color.BLUE, Color.GREEN, Color.GREEN, Color.YELLOW, Color.YELLOW, Color.ORANGE, Color.ORANGE, Color.RED, Color.RED, Color.LIGHT_GRAY, Color.LIGHT_GRAY, Color.GRAY, Color.GRAY, Color.DARK_GRAY, Color.DARK_GRAY));
                List<Color> colors = new ArrayList<>(Arrays.asList(getCurrentColor(), getCurrentAntiColor(), getCurrentColor(), getCurrentAntiColor()));
                
                
                //graph
                java.util.List<Polygon> ps = function();
                for (int i = 0; i < ps.size(); i++) {
                    Polygon p = ps.get(i);
                    g2.setColor(colors.get(ps.indexOf(p)));
                    g2.drawPolyline(p.xpoints, p.ypoints, p.npoints);
                }
                
                
                g2.drawImage(img, 0, 0, null);
            }
        };
        pane.add(renderPanel, BorderLayout.CENTER);
        
        frame.setSize(screenX, screenY);
        frame.setVisible(true);
        
        
        while (true) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            renderPanel.repaint();
        }
    }
    
    private static long draw = System.currentTimeMillis();
    
    private static java.util.List<Polygon> function() {
        List<Polygon> ps = new ArrayList<>();

//        ps.add(sinWave(1, 1));


//        for (int n = 1; n < 5; n++) {
//            ps.add(sinWave(n, 1));
//        }


//        ps.add(subSinWave(1, 1, 3, 2));

//
//        ps.add(addSinWave(1, 1, 4, 1));
//        ps.add(subSinWave(1, 1, 4, 1));


//        ps.add(rotatingSinWave(2));


//        if (System.currentTimeMillis() - draw >= 500) {
//            continuousHz++;
//            System.out.println(continuousHz);
//            draw = System.currentTimeMillis();
//        }
//        ps.add(rotatingSinWave(continuousHz));


//        ps.add(lissajousCurve(5, 4, Math.PI / 2, Math.PI, 120 * Math.PI));
        
        
        ps.add(shiftLissajousCurve(16 * Math.PI, 30000));

//        SineWaves.period = 2000;
//        ps.add(rotatingSinWave(2));
        
        return ps;


//        return colorSpectrum();
//        return superColorSpectrum();

//        return shiftDoubleLissajousCurve(24 * Math.PI, 120000);
        
    }
    
    private static Polygon lissajousCurve(int a, int b, double w, double r, double maxT) {
        Polygon p = new Polygon();
        
        
        for (double t = 0; t <= maxT; t += .1) {
            double x = a * Math.sin(w * t + r);
            double y = b * Math.sin(t);
            
            int trueX = screenX / 2;
            trueX += (x / (currentWidth / 2)) * (screenX / 2);
            
            int trueY = screenY / 2;
            trueY -= (y / (currentHeight / 2)) * (screenY / 2);
            
            p.addPoint(trueX, trueY);
        }
        
        return p;
    }
    
    private static double rat = 0;
    
    private static double movA = 0;
    
    private static Polygon shiftLissajousCurve(double maxT, long period) {
        Polygon p = new Polygon();
        
        double distance = System.currentTimeMillis() - last;
        last = System.currentTimeMillis();
        
        double offset = 1;
        SineWaves.period = period;
        movA += (distance / period) * offset;
        if (movA > 1) {
            movA = movA % 1 + .0001;
            
        }
        
        System.out.println(movA / 1);
        
        double r = 0;
        for (double t = -maxT; t <= maxT; t += .1) {
            double x = 5 * Math.sin(movA * t + r);
            double y = 5 * Math.sin(t);
            
            int trueX = screenX / 2;
            trueX += (x / (currentWidth / 2)) * (screenX / 2);
            
            int trueY = screenY / 2;
            trueY -= (y / (currentHeight / 2)) * (screenY / 2);
            
            p.addPoint(trueX, trueY);
        }
        
        return p;
    }
    
    private static List<Polygon> shiftDoubleLissajousCurve(double maxT, long period) {
        Polygon p = new Polygon();
        Polygon p2 = new Polygon();
        Polygon p3 = new Polygon();
        Polygon p4 = new Polygon();
        
        double distance = System.currentTimeMillis() - last;
        last = System.currentTimeMillis();
        
        double offset = 1;
        SineWaves.period = period;
        movA += (distance / period) * offset;
        if (movA > 1) {
            movA = movA % 1 + .0001;
            
        }
        
        System.out.println(movA / 1);
        
        double r = 0;
        for (double t = -maxT; t <= maxT; t += .1) {
            double x = 5 * Math.sin(movA * t + r);
            double y = 5 * Math.sin(t);
            
            int trueX = screenX / 2;
            trueX += (x / (currentWidth / 2)) * (screenX / 2);
            
            int trueY = screenY / 2;
            trueY -= (y / (currentHeight / 2)) * (screenY / 2);
            
            p.addPoint(trueX, trueY);

//            double x2 = 2.5 * Math.sin(movA * t + r);
//            double y2 = 2.5 * Math.sin(t);
//
//            int trueX2 = screenX / 2;
//            trueX2 += (x2 / (currentWidth / 2)) * (screenX / 2);
//
//            int trueY2 = screenY / 2;
//            trueY2 -= (y2 / (currentHeight / 2)) * (screenY / 2);
//
//            p2.addPoint(trueX2, trueY2);
//
//
//            double x3 = 1.25 * Math.sin(movA * t + r);
//            double y3 = 1.25 * Math.sin(t);
//
//            int trueX3 = screenX / 2;
//            trueX3 += (x3 / (currentWidth / 2)) * (screenX / 2);
//
//            int trueY3 = screenY / 2;
//            trueY3 -= (y3 / (currentHeight / 2)) * (screenY / 2);
//
//            p3.addPoint(trueX3, trueY3);
//
//            double x4 = 3.75 * Math.sin(movA * t + r);
//            double y4 = 3.75 * Math.sin(t);
//
//            int trueX4 = screenX / 2;
//            trueX4 += (x4 / (currentWidth / 2)) * (screenX / 2);
//
//            int trueY4 = screenY / 2;
//            trueY4 -= (y4 / (currentHeight / 2)) * (screenY / 2);

//            p4.addPoint(trueX4, trueY4);
        }
        
        List<Polygon> ps = new ArrayList<>();
        ps.add(p);
//        ps.add(p4);
//        ps.add(p2);
//        ps.add(p3);
        return ps;
    }
    
    private static Polygon sinWave(double n, double m) {
        Polygon p = new Polygon();
        for (double x = -currentWidth / 2; x <= currentWidth / 2; x += .01) {
            double y = Math.sin(n * x) * m;
            
            int trueX = screenX / 2;
            trueX += (x / (currentWidth / 2)) * (screenX / 2);
            
            int trueY = screenY / 2;
            trueY -= (y / (currentHeight / 2)) * (screenY / 2);
            
            p.addPoint(trueX, trueY);
        }
        return p;
    }
    
    private static Polygon addSinWave(double n, double m, double n2, double m2) {
        Polygon p = new Polygon();
        for (double x = -currentWidth / 2; x <= currentWidth / 2; x += .01) {
            double y = (Math.sin(n * x) * m) + (Math.sin(n2 * x) * m2);
            
            int trueX = screenX / 2;
            trueX += (x / (currentWidth / 2)) * (screenX / 2);
            
            int trueY = screenY / 2;
            trueY -= (y / (currentHeight / 2)) * (screenY / 2);
            
            p.addPoint(trueX, trueY);
        }
        return p;
    }
    
    private static Polygon subSinWave(double n, double m, double n2, double m2) {
        Polygon p = new Polygon();
        for (double x = -currentWidth / 2; x <= currentWidth / 2; x += .01) {
            double y = (Math.sin(n * x) * m) - (Math.sin(n2 * x) * m2);
            
            int trueX = screenX / 2;
            trueX += (x / (currentWidth / 2)) * (screenX / 2);
            
            int trueY = screenY / 2;
            trueY -= (y / (currentHeight / 2)) * (screenY / 2);
            
            p.addPoint(trueX, trueY);
        }
        return p;
    }
    
    private static double rotationOffset = 0;
    
    private static long last = System.currentTimeMillis();
    
    private static int continuousHz = 0;
    
    private static Polygon rotatingSinWave(int hz) {
        double distance = System.currentTimeMillis() - last;
        last = System.currentTimeMillis();
        
        double rotatePerSec = 2 * Math.PI * hz;
        rotationOffset += (distance / 1000) * rotatePerSec;
        
        Polygon p = new Polygon();
        for (double x = -currentWidth / 2; x <= currentWidth / 2; x += .01) {
            double y = Math.sin((5 * x) + rotationOffset);
            
            int trueX = screenX / 2;
            trueX += (x / (currentWidth / 2)) * (screenX / 2);
            
            int trueY = screenY / 2;
            trueY -= (y / (currentHeight / 2)) * (screenY / 2);
            
            p.addPoint(trueX, trueY);
        }
        return p;
    }
    
    private static java.util.List<Polygon> colorSpectrum() {
        double distance = System.currentTimeMillis() - last;
        last = System.currentTimeMillis();
        
        List<Polygon> ps = new ArrayList<>();
        
        List<Double> frequencies = new ArrayList<>(Arrays.asList(30000000.0, 15015000.0, 15215.0, 728.5, 637.0, 566.0, 517.0, 496.0, 442.0, 215.15, .15015, .00015));
        
        for (int i = 0; i < frequencies.size(); i++) {
            int yOffest = (int) (-15 + 3 * i);
            
            double rotatePerSec = 2 * Math.PI * frequencies.get(i);
            rotationOffset += (distance / 1000) * rotatePerSec;
            rotationOffset %= 1000000000000L;
            
            Polygon p = new Polygon();
            Polygon p2 = new Polygon();
            for (double x = -currentWidth / 2; x <= currentWidth / 2; x += .01) {
                double y = yOffest + (Math.sin(x + rotationOffset));
                
                int trueX = screenX / 2;
                trueX += (x / (currentWidth / 2)) * (screenX / 2);
                
                int trueY = screenY / 2;
                trueY -= (y / (currentHeight / 2)) * (screenY / 2);
                
                p.addPoint(trueX, trueY);
            }
            ps.add(p);
        }
        return ps;
    }
    
    private static java.util.List<Polygon> superColorSpectrum() {
        double distance = System.currentTimeMillis() - last;
        last = System.currentTimeMillis();
        
        List<Polygon> ps = new ArrayList<>();
        
        List<Double> frequencies = new ArrayList<>(Arrays.asList(30000000.0, 15015000.0, 15215.0, 728.5, 637.0, 566.0, 517.0, 496.0, 442.0, 215.15, .15015, .00015));
        
        for (int i = 0; i < frequencies.size(); i++) {
            int yOffest = (int) (-15 + 3 * i);
            
            double rotatePerSec = 2 * Math.PI * frequencies.get(i) * 1000000;
            rotationOffset += (distance / 1000) * rotatePerSec;
            rotationOffset %= 1000000000000L;
            
            Polygon p = new Polygon();
            Polygon p2 = new Polygon();
            for (double x = -currentWidth / 2; x <= currentWidth / 2; x += .01) {
                double y1 = yOffest + (Math.sin(x + rotationOffset));
//                double y1 = yOffest + ((Math.sin(x + rotationOffset)) + (Math.sin(4 * x + rotationOffset))) * 2;
//                double y2 = yOffest + ((Math.sin(x + rotationOffset)) - (Math.sin(4 * x + rotationOffset))) * 2;
                
                int trueX = screenX / 2;
                trueX += (x / (currentWidth / 2)) * (screenX / 2);
                
                int trueY1 = screenY / 2;
                trueY1 -= (y1 / (currentHeight / 2)) * (screenY / 2);
//                int trueY2 = screenY / 2;
//                trueY2 -= (y2 / (currentHeight / 2)) * (screenY / 2);
                
                p.addPoint(trueX, trueY1);
//                p2.addPoint(trueX, trueY2);
            }
            ps.add(p);
//            ps.add(p2);
        }
        return ps;
    }
    
    private static long startColorTime = System.currentTimeMillis();
    
    private static long period = 1;
    
    private static Color getCurrentColor() {
        long currentColorTime = System.currentTimeMillis();
        long diffColorTime = currentColorTime - startColorTime;
        diffColorTime %= period;
        
        float hue = (float) diffColorTime / period;
        
        return Color.getHSBColor(hue, 1, 1);
    }
    
    private static Color getCurrentAntiColor() {
//        return getCurrentColor();
        long currentColorTime = System.currentTimeMillis();
        long diffColorTime = currentColorTime - startColorTime;
        diffColorTime %= period;
        
        float hue = 1 - ((float) diffColorTime / period);
        
        return Color.getHSBColor(hue, 1, 1);
    }
    
}
