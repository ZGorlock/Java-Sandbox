/*
 * File:    PiBot.java
 * Package: PACKAGE_NAME
 * Author:  Zachary Gill
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Calculates Pi.
 */
public class PiBot {
    
    //Constants
    
    /**
     * The file that stores the current index of Pi calculated.
     */
    public static final File INDEX_FILE = new File("E:\\Other\\Pi\\index");
    
    /**
     * The file that stores the digits of Pi currently calculated.
     */
    public static final File PI_FILE = new File("E:\\Other\\Pi\\pi");
    
    /**
     * The run length of the program per session.
     */
    private static final int RUN_LENGTH = 1000000;
    
    /**
     * The size of the buffer to fill before outputting.
     */
    private static final int BUFFER_SIZE = 1000;
    
    /**
     * The chunk size for calculating digits.
     */
    private static final int CHUNK_SIZE = 10;
    
    /**
     * The number of powers of 2 to store.
     */
    private static final int NUM_TWO_POWERS = 25;
    
    /**
     * The error margin for double calculations.
     */
    private static final double EPSILON = 0.00000000000000001;
    
    
    //Static Fields
    
    /**
     * The current index.
     */
    private static long index = 0;
    
    /**
     * The buffer to store calculated digits prior to outputting.
     */
    private static char[] buffer = new char[BUFFER_SIZE];
    
    /**
     * A store of powers of two.
     */
    private static double[] twoPowers = new double[NUM_TWO_POWERS];
    
    
    //Methods
    
    /**
     * The main method for calculating Pi.
     * 
     * @param args The arguments for the main method.
     */
    public static void main(String[] args) {
        if (!initializeFiles()) {
            System.out.println("Could not initialize program files");
            return;
        }
        readIndex();
        
        initializeTwoPowers();
        int c = 0;
        for (int k = 0; k <= RUN_LENGTH; k+= CHUNK_SIZE) {
            if (c == BUFFER_SIZE) {
                writeBuffer();
                c = 0;
            }
            
            String n = calculatePiDigit(index);
            for (int i = 0; i < n.length(); i++) {
                buffer[c] = n.charAt(i);
                c++;
            }
            index += CHUNK_SIZE;
        }
    }
    
    /**
     * Calculates the 10 hexadecimal digits of Pi starting at an index.
     * 
     * @param index The index of Pi to start calculation from.
     * @return The 10 hexadecimal digits of Pi starting at the specified index.
     */
    private static String calculatePiDigit(long index) {
        double s1 = series(1, index);
        double s2 = series(4, index);
        double s3 = series(5, index);
        double s4 = series(6, index);
        
        double pid = (4.0 * s1) - (2.0 * s2) - s3 - s4;
        pid = pid - (int) pid + 1.0;
        return hexString(pid).substring(0, CHUNK_SIZE);
    }
    
    /**
     * Determines the series for calculating Pi.
     * 
     * @param m The factor for the denominator.
     * @param n The index of Pi to calculate the series for.
     * @return The series for calculating Pi.
     */
    private static double series(int m, long n) {
        double sum = 0.0;
        for (int k = 0; k < n; k++) {
            double denom = 8 * k + m;
            double pow = n - k;
            double term = modPow16(pow, denom);
            sum += (term / denom);
            sum -= (int) sum;
        }
    
        for (long k = n; k <= n + 100; k++) {
            double denom = 8 * k + m;
            double term = Math.pow(16.0, (double)(n - k)) / denom;
            if (term < EPSILON) {
                break;
            }
            sum += term;
            sum -= (int) sum;
        }
    
        return sum;
    }
    
    /**
     * Determines a fraction in hexadecimal.
     * 
     * @param p The power of the fraction.
     * @param m The denominator of the fraction.
     * @return The fraction in hexadecimal.
     */
    private static double modPow16(double p, double m) {
        if (m == 1.0) {
            return 0.0;
        }
        
        int i;
        for (i = 0; i < NUM_TWO_POWERS; i++) {
            if (twoPowers[i] > p) {
                break;
            }
        }
        
        double pow2 = twoPowers[i - 1];
        double pow1 = p;
        double result = 1.0;
        
        for (int j = 1; j <= i; j++) {
            if (pow1 >= pow2) {
                result *= 16.0;
                result -= (int) (result / m) * m;
                pow1 -= pow2;
            }
            
            pow2 *= 0.5;
            if (pow2 >= 1.0) {
                result *= result;
                result -= (int) (result / m) * m;
            }
        }
        
        return result;
    }
    
    /**
     * Converts the fraction to a hexadecimal string.
     */
    private static String hexString(double x) {
        String hexChars = "0123456789ABCDEF";
        StringBuilder sb = new StringBuilder(16);
        double y = Math.abs(x);
        
        for (int i = 0; i < 16; i++) {
            y = 16.0 * (y - Math.floor(y));
            sb.append(hexChars.charAt((int)y));
        }
        
        return sb.toString();
    }
    
    /**
     * Initializes the array of powers of two.
     */
    private static void initializeTwoPowers() {
        twoPowers[0] = 1d;
        for (int i = 1; i < NUM_TWO_POWERS; i++) {
            twoPowers[i] = 2d * twoPowers[i - 1];
        }
    }
    
    /**
     * Initializes the files for the program.
     * 
     * @return Whether the initialization was completed successfully or not.
     */
    private static boolean initializeFiles() {    
        if (!INDEX_FILE.exists()) {
            try {
                if (!INDEX_FILE.createNewFile()) {
                    throw new IOException();
                }
            } catch (IOException ignored) {
                System.out.println("Could not create index file");
                return false;
            }
        }
    
        if (INDEX_FILE.length() == 0) {
            try {
                BufferedWriter indexWriter = new BufferedWriter(new FileWriter(INDEX_FILE, false));
                indexWriter.write(String.valueOf(index));
                indexWriter.close();
                PI_FILE.delete();
            } catch (IOException ignored) {
                System.out.println("Could not initialize index file");
                return false;
            }
        }
    
        if (!PI_FILE.exists()) {
            try {
                if (!PI_FILE.createNewFile()) {
                    throw new IOException();
                }
            } catch (IOException ignored) {
                System.out.println("Could not create pi file");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Reads the index from the index file.
     * 
     * @return Whether the index was read successfully or not.
     */
    private static boolean readIndex() {
        if (!INDEX_FILE.exists()) {
            System.out.println("Could not find index file");
            return false;
        }
    
        if (INDEX_FILE.length() > 0) {
            try {
                BufferedReader indexReader = new BufferedReader(new FileReader(INDEX_FILE));
                index = Long.valueOf(indexReader.readLine());
                indexReader.close();
            } catch (IOException ignored) {
                System.out.println("Could not read the index file");
                return false;
            }
        } else {
            System.out.println("The index file does not contain the index");
            return false;
        }
        
        return true;
    }
    
    /**
     * Writes the buffer to file.
     * 
     * @return Whether the buffer was successfully written to a file or not.
     */
    private static boolean writeBuffer() {
        try {
            BufferedWriter piWriter = new BufferedWriter(new FileWriter(PI_FILE, true));
            piWriter.write(buffer);
            buffer = new char[BUFFER_SIZE];
            piWriter.close();
        } catch (IOException ignored) {
            System.out.println("Could not write to pi file");
            return false;
        }
        
        try {
            BufferedWriter indexWriter = new BufferedWriter(new FileWriter(INDEX_FILE, false));
            indexWriter.write(String.valueOf(index));
            indexWriter.close();
        } catch (IOException ignored) {
            System.out.println("Could not write to index file");
            return false;
        }
        
        return true;
    }
    
}

