/*
 * File:    Main.java
 * Package: PACKAGE_NAME
 * Author:  Zachary Gill
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    
    public static void main(String[] args) throws IOException {
        System.out.println(RegexpTrie.buildRegex(Arrays.asList("WHO", "WHAT", "WHEN", "WHERE", "WHY")));
//        final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//        try {
//            final ArrayList<String> lines = new ArrayList<String>();
//            while (true) {
//                final String line = in.readLine();
//                if (line == null) {
//                    break;
//                }
//                lines.add(line);
//            }
//            System.out.println(RegexpTrie.buildRegex(lines));
//        } finally {
//            in.close();
//        }
    }
}