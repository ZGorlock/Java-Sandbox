/*
 * File:    Md5Cracker.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;

import org.apache.commons.io.FileUtils;

public class Md5Cracker {
    
    private static final File SAVE = new File("log/save.txt");
    private static final int SAVE_EVERY = 10000000;
    
    private static final String SEARCH_1 = " ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String SEARCH_2 = SEARCH_1 + "abcdefghijklmnopqrstuvwxyz";
    private static final String SEARCH_3 = SEARCH_2 + "0123456789";
    private static final String SEARCH_4 = SEARCH_3 + "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
    
    private static final String VALID_CHARS = SEARCH_3;
    private static final char FIRST_CHAR = VALID_CHARS.charAt(0);
    private static final char LAST_CHAR = VALID_CHARS.charAt(VALID_CHARS.length() - 1);
    
    
    public static void main(String[] args) throws Exception {
        final String text = crack("75170fc230cd88f82e475ff4087f81d9");
        System.out.println(text);
        writeSave(text);
    }
    
    private static String crack(String hash) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("MD5");
        String test = readSave();
    
        while (true) {
            long startBatch = System.currentTimeMillis();
            for (int i = 0; i < SAVE_EVERY; i++) {
                final String md5 = md5(digest, test);
                if (md5.equalsIgnoreCase(hash)) {
                    return test;
                }
                test = nextTest(test);
            }
            long endBatch = System.currentTimeMillis();
            System.out.println(((long) (SAVE_EVERY / ((endBatch - startBatch) / 1000.0)) / 1000000.0) + " Mh/s"  );
            writeSave(test);
        }
    }
    
    private static String md5(MessageDigest digest, String test) throws Exception {
        digest.reset();
        digest.update(test.getBytes(), 0, test.length());
        return new BigInteger(1, digest.digest()).toString(16);
    }
    
    private static String nextTest(String test) throws Exception {
        boolean hit = false;
        char[] chars = test.toCharArray();
        for (int i = chars.length - 1; i >= 0; i--) {
            if (chars[i] != LAST_CHAR) {
                chars[i] = VALID_CHARS.charAt(VALID_CHARS.indexOf(chars[i]) + 1);
                for (int j = i + 1; j < chars.length; j++) {
                    chars[j] = FIRST_CHAR;
                }
                hit = true;
                break;
            }
        }
        return hit ? new String(chars) : String.valueOf(FIRST_CHAR).repeat(test.length() + 1);
    }
    
    private static String readSave() throws Exception {
        return SAVE.exists() ? FileUtils.readFileToString(SAVE, "UTF-8") : "";
    }
    
    private static void writeSave(String save) throws Exception {
        FileUtils.writeStringToFile(SAVE, save, "UTF-8", false);
    }
    
}
