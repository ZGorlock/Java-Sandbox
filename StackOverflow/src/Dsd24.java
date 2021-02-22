/*
 * File:    Dsd24.java
 * Package: PACKAGE_NAME
 * Author:  Zachary Gill
 */

import java.io.File;

public class Dsd24 {
    
    public static void main(String[] args) {
        File a = new File("C:\\Instrument Data");
        File b = new File(a.getAbsolutePath().replaceAll("^[A-Z]:", "").replace("\\", "/"));
        System.out.println(b.toPath().isAbsolute());
    }
    
}
