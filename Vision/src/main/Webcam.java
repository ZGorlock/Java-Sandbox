/*
 * File:    Webcam.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

public class Webcam extends JPanel {
    
    final static int INTERVAL = 50;///you may use interval
    
    final static String DETECTOR_DIR = "resources" + File.separator;
    
    static Mat matrix;
    
    static BufferedImage image;
    
    static final List<CascadeClassifier> detectors = new ArrayList<>();
    
    static final List<Rect[]> detections = new ArrayList<>();
    
    public void loop() throws FileNotFoundException, IOException {
        while (true) {
            try {
                Thread.sleep(INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            captureSnapShot();
            saveImage();
            doDetections();
            this.repaint();
        }
    }
    
    public BufferedImage captureSnapShot() {
        VideoCapture capture = new VideoCapture(0);
        
        if (capture.isOpened()) {
            if (capture.read(matrix)) {
                image = new BufferedImage(matrix.width(),
                        matrix.height(), BufferedImage.TYPE_3BYTE_BGR);
                
                WritableRaster raster = image.getRaster();
                DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
                byte[] data = dataBuffer.getData();
                matrix.get(0, 0, data);
            }
        }
        return image;
    }
    
    static int i = 0;
    
    public static void saveImage() {
        String file = "snapshot.jpg";
        i++;
        Imgcodecs.imwrite(file, matrix);
    }
    
    public void doDetections() {
        for (int i = 0; i < detectors.size(); i++) {
            CascadeClassifier detector = detectors.get(i);
            MatOfRect detected = new MatOfRect();
            detector.detectMultiScale(matrix, detected);
            detections.set(i, detected.toArray());
        }
    }
    
    public static void addDetector(String detector) {
        detectors.add(new CascadeClassifier(new File(DETECTOR_DIR + detector).getAbsolutePath()));
        detections.add(new Rect[] {});
    }
    
    public static void main(String args[]) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
        JFrame window = new JFrame("Webcam");
        final Webcam webcam = new Webcam();
        window.setSize(1080, 860);
        window.add(webcam);
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.pack();
        window.setVisible(true);
        
        matrix = new Mat();
        addDetector("haarcascade_frontalface_alt.xml");
        addDetector("haarcascade_eye.xml");
        
        try {
            webcam.loop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        if (i > 0) {
            //super.paint(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
            
            for (Rect[] rects : detections) {
                for (Rect rect : rects) {
                    Stroke stroke = g2d.getStroke();
                    g2d.setStroke(new BasicStroke(3.0F));
                    g2d.setStroke(stroke);
                    g2d.setColor(Color.RED);
                    g2d.drawRoundRect(rect.x, rect.y, rect.height, rect.width, 5, 5);
                }
            }
            
            g2d.dispose();
        }
    }
}