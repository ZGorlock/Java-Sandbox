/*
 * File:    BinaryUtil.java
 * Package: main.util
 * Author:  Zachary Gill
 */

package main.util;

import java.io.File;
import java.math.BigInteger;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;

public final class BinaryUtil {
    
    //Static Methods
    
    public static void writeFile(File file, BitSet data) throws Exception {
        FileUtils.writeByteArrayToFile(file, data.toByteArray());
    }
    
    public static BitSet readFile(File file) throws Exception {
        return BitSet.valueOf(FileUtils.readFileToByteArray(file));
    }
    
    public static BitSet toBits(String bitString) {
        final BitSet bitSet = new BitSet(bitString.length());
        final AtomicInteger bitCount = new AtomicInteger(0);
        bitString.chars().forEachOrdered(b ->
                bitSet.set(bitCount.getAndIncrement(), (b == '1')));
        return bitSet;
    }
    
    public static String toBitString(BitSet bitSet) {
        final StringBuilder bitStringBuilder = new StringBuilder("0".repeat(bitSet.length()));
        bitSet.stream().forEach(i ->
                bitStringBuilder.setCharAt(i, '1'));
        return bitStringBuilder.toString();
    }
    
    public static String toBitString(Character ascii) {
        return toBitString((byte) ascii.charValue());
    }
    
    public static String toBitString(byte b) {
        return Integer.toBinaryString((b & 0xFF) + 0x100).substring(1);
    }
    
    public static byte toByte(String bitString) {
        return new BigInteger(bitString, 2).byteValue();
    }
    
    public static String toBitString(int num) {
        return new BigInteger(String.valueOf(num)).toString(2);
    }
    
    public static int toInt(String bitString) {
        return new BigInteger(bitString, 2).intValue();
    }
    
    public static String toBitString(long num) {
        return new BigInteger(String.valueOf(num)).toString(2);
    }
    
    public static long toLong(String bitString) {
        return new BigInteger(bitString, 2).longValue();
    }
    
}
