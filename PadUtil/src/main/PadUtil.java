/*
 * File:    PadUtil.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import main.data.cache.DataMirror;
import main.data.parser.DataParser;

public class PadUtil {
    
    //Constants
    
    public static final boolean MIRROR = false;
    
    public static final boolean PARSE = false;
    
    
    //Main Method
    
    public static void main(String[] args) {
        if (MIRROR) {
            DataMirror.sync();
        }
        
        if (PARSE) {
            DataParser.parse();
        }
        
    }
    
}
