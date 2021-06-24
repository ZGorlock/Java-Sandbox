/*
 * File:    Dsd24.java
 * Package: Dsd24
 * Author:  Zachary Gill
 */

package Dsd24;

import java.io.File;

public class Dsd24 {
    
    public static void main(String[] args) {
        File a = new File("C:\\Instrument Data");
        File b = new File(a.getAbsolutePath().replaceAll("^[A-Z]:", "").replace("\\", "/"));
        System.out.println(b.toPath().isAbsolute());
    }
    
}
