/*
 * File:    PiBot.java
 * Package:
 * Author:  Zachary Gill
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Calculates Pi.
 */
public class PiBot {
    
    //Constants
    
    /**
     * The file that stores the digits of Pi currently calculated.
     */
    public static final File PI_FILE = new File("data/pi.txt");
    
    /**
     * The list of benchmark decimal places.
     */
    public static final List<Integer> benchmarks = new ArrayList<>();
    
    /**
     * A flag indicating whether or not to enable decimal conversion of benchmark decimal places.
     */
    public static final boolean enableBenchmarks = true;
    
    /**
     * The run length of the program per session.
     */
    private static final int RUN_LENGTH = -1;
    
    /**
     * The chunk size for calculating digits.
     */
    private static final int CHUNK_SIZE = 10;
    
    /**
     * The size of the buffer to fill before outputting.
     */
    private static final int BUFFER_SIZE = CHUNK_SIZE * 100;
    
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
        //TODO verify against pi-verify
        //TODO fix calculation issue
        
        if (!initializeFiles()) {
            System.out.println("Could not initialize program files");
            return;
        }
        if (!readIndex()) {
            System.out.println("Could not acquire index");
            return;
        }
        initializeTwoPowers();
        initializeBenchmarks();
        
        System.out.println(index + " (start)");
        
        int c = 0;
        int k = 0;
        while (true) {
            k += BUFFER_SIZE;
            if ((RUN_LENGTH > 0) && (k > RUN_LENGTH)) {
                break;
            }
            
            IntStream.range(0, BUFFER_SIZE / CHUNK_SIZE).boxed().parallel().forEach(e -> {
                String n = calculatePiDigit(index + (e * CHUNK_SIZE));
                for (int i = 0; i < n.length(); i++) {
                    buffer[i + (e * CHUNK_SIZE)] = n.charAt(i);
                }
            });
            
            writeBuffer();
            checkBenchmarks();
            index += BUFFER_SIZE;
            System.out.println(index);
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
        for (long k = 0; k < n; k++) {
            double denom = 8 * k + m;
            double pow = n - k;
            double term = modPow16(pow, denom);
            sum += (term / denom);
            sum -= (int) sum;
        }
        
        for (long k = n; k <= n + 100; k++) {
            double denom = 8 * k + m;
            double term = Math.pow(16.0, (double) (n - k)) / denom;
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
            sb.append(hexChars.charAt((int) y));
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
     * Initializes the array of benchmark digit counts.
     */
    private static void initializeBenchmarks() {
        benchmarks.add(100);
        benchmarks.add(250);
        benchmarks.add(500);
        benchmarks.add(1000);
        benchmarks.add(2500);
        benchmarks.add(5000);
        benchmarks.add(10000);
        benchmarks.add(20000);
        benchmarks.add(50000);
        benchmarks.add(100000);
        benchmarks.add(250000);
        benchmarks.add(500000);
        benchmarks.add(1000000);
        benchmarks.add(2500000);
        benchmarks.add(5000000);
        benchmarks.add(10000000);
        benchmarks.add(25000000);
        benchmarks.add(50000000);
        benchmarks.add(100000000);
        benchmarks.add(250000000);
        benchmarks.add(500000000);
        benchmarks.add(1000000000);
    }
    
    /**
     * Initializes the files for the program.
     *
     * @return Whether the initialization was completed successfully or not.
     */
    private static boolean initializeFiles() {
        if (!PI_FILE.exists()) {
            try {
                if (!PI_FILE.createNewFile()) {
                    throw new IOException();
                }
                buffer = new char[2];
                buffer[0] = '3';
                buffer[1] = '.';
                writeBuffer();
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
        if (PI_FILE.exists()) {
            index = PI_FILE.length() - "3.".length();
            return true;
        }
        return false;
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
        return true;
    }
    
    /**
     * Checks for benchmark digit counts.
     */
    private static void checkBenchmarks() {
        if (!enableBenchmarks) {
            return;
        }
        
        for (Integer benchmark : benchmarks) {
            File benchmarkFile = new File("data/pi-" + benchmark + ".txt");
            if (benchmarkFile.exists()) {
                continue;
            }
            if (PI_FILE.length() > benchmark + 2 + 20) {
                try {
                    System.out.println("Generating benchmark: " + benchmark);
                    String hex = new String(Files.readAllBytes(PI_FILE.toPath())).substring(0, (benchmark + 2 + 20));
                    String decimal = hexToDecimal(hex);
                    
                    BufferedWriter piBenchmarkWriter = new BufferedWriter(new FileWriter(benchmarkFile, false));
                    piBenchmarkWriter.write(decimal);
                    piBenchmarkWriter.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
    
    /**
     * Converts a hex number string to a decimal number string.
     *
     * @param hex      The hex number string.
     * @param accuracy The number of decimal places to return in the decimal number string.
     * @return The decimal number string.
     */
    public static String hexToDecimal(String hex, int accuracy) {
        if (!hex.matches("[0-9A-Fa-f.\\-]+") || (accuracy < 0)) {
            return "";
        }
        
        boolean negative = hex.startsWith("-");
        hex = hex.replaceAll("^-", "");
        String integral = hex.contains(".") ? hex.substring(0, hex.indexOf(".")) : hex;
        String fraction = hex.contains(".") ? hex.substring(hex.indexOf(".") + 1) : "";
        if (integral.contains("-") || fraction.contains(".") || fraction.contains("-")) {
            return "";
        }
        
        StringBuilder decimal = new StringBuilder();
        decimal.append(negative ? "-" : "");
        decimal.append(integral.isEmpty() ? "0" : new BigDecimal(new BigInteger(integral, 16)).toPlainString());
        if (fraction.isEmpty() || (accuracy == 0)) {
            return decimal.toString();
        }
        decimal.append(".");
        
        int numberDigits = accuracy;
        int length = Math.min(fraction.length(), numberDigits);
        int[] hexDigits = new int[numberDigits];
        Arrays.fill(hexDigits, 0);
        IntStream.range(0, length).boxed().parallel().forEach(i -> hexDigits[i] = Integer.parseInt(String.valueOf(fraction.charAt(i)), 16));
        
        while ((numberDigits != 0)) {
            int carry = 0;
            for (int i = length - 1; i >= 0; i--) {
                int value = hexDigits[i] * 10 + carry;
                carry = value / 16;
                hexDigits[i] = value % 16;
            }
            decimal.append(carry);
            numberDigits--;
        }
        return decimal.toString();
    }
    
    /**
     * Converts a hex number string to a decimal number string.
     *
     * @param hex The hex number string.
     * @return The decimal number string.
     * @see #hexToDecimal(String, int)
     */
    public static String hexToDecimal(String hex) {
        String fraction = hex.contains(".") ? hex.substring(hex.indexOf(".") + 1) : "";
        return hexToDecimal(hex, fraction.length());
    }
    
}

