/*
 * File:    Webcam.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import static org.bytedeco.opencv.global.opencv_core.CV_8UC1;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_AA;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_CHAIN_APPROX_SIMPLE;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_RETR_LIST;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_THRESH_BINARY;
import static org.bytedeco.opencv.global.opencv_imgproc.approxPolyDP;
import static org.bytedeco.opencv.global.opencv_imgproc.arcLength;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import static org.bytedeco.opencv.global.opencv_imgproc.drawContours;
import static org.bytedeco.opencv.global.opencv_imgproc.findContours;
import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;
import static org.bytedeco.opencv.global.opencv_imgproc.threshold;

public class Webcam {
    
    //Constants
    
    private static final int INTERVAL = 20;
    
    private static final File CLASSIFIER_DIR = new File("resources");
    
    
    //Static Fields
    
    private static final Map<String, CascadeClassifier> classifiers = new LinkedHashMap<>();
    
    private static CanvasFrame frame;
    
    private static FrameGrabber grabber;
    
    private static OpenCVFrameConverter.ToMat converter;
    
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
        try {
            grabber = FrameGrabber.createDefault(0);
            converter = new OpenCVFrameConverter.ToMat();
            
            frame = new CanvasFrame("Webcam", CanvasFrame.getDefaultGamma() / grabber.getGamma());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            addClassifier("haarcascade_frontalface_alt.xml");
            addClassifier("haarcascade_eye.xml");
            
            grabber.start();
            
            new Webcam().loop();
            
        } finally {
            frame.dispose();
            grabber.stop();
        }
    }
    
    
    //Methods
    
    @SuppressWarnings("BusyWait")
    private void loop() throws Exception {
        List<Mat> capture;
        while ((capture = capture()) != null) {
            detect(capture);
            display(capture.get(0));
            
            try {
                Thread.sleep(INTERVAL);
            } catch (InterruptedException ignored) {
            }
        }
    }
    
    private List<Mat> capture() throws Exception {
        final Mat snapshot = converter.convert(grabber.grab());
        if (snapshot == null) {
            return null;
        }
        
        final Mat grayscale = new Mat(snapshot.rows(), snapshot.cols(), CV_8UC1);
        final Mat threshold = grayscale.clone();
        final Mat display = snapshot.clone();
        
        cvtColor(snapshot, grayscale, CV_BGR2GRAY);
        threshold(grayscale, threshold, 64, 255, CV_THRESH_BINARY);
        
        return List.of(display, snapshot, grayscale, threshold);
    }
    
    private Map<String, List<Rect>> detect(List<Mat> capture) {
        final Map<String, List<Rect>> detections = new HashMap<>();
        
        for (Map.Entry<String, CascadeClassifier> classifier : classifiers.entrySet()) {
            final List<Rect> classifierDetections = new ArrayList<>();
            
            final RectVector detected = new RectVector();
            classifier.getValue().detectMultiScale(capture.get(2), detected);
            
            for (long i = 0; i < detected.size(); i++) {
                final Rect detection = detected.get(i);
                classifierDetections.add(detection);
                
                rectangle(capture.get(0), detection, Scalar.RED, 1, CV_AA, 0);
            }
            detections.put(classifier.getKey(), classifierDetections);
        }
        
        final MatVector contours = new MatVector();
        findContours(capture.get(3), contours, CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);
        
        for (long i = 0; i < contours.size(); i++) {
            final Mat contour = contours.get(i);
            
            final Mat points = new Mat();
            approxPolyDP(contours.get(i), points, (arcLength(contour, true) * 0.02), true);
            
            drawContours(capture.get(0), new MatVector(contour), -1, Scalar.BLUE);
        }
        
        return detections;
    }
    
    private void display(Mat display) {
        frame.showImage(converter.convert(display));
    }
    
    
    //Static Methods
    
    private static void addClassifier(String classifier) {
        classifiers.put(classifier, new CascadeClassifier(new File(CLASSIFIER_DIR, classifier).getAbsolutePath()));
    }
    
}
