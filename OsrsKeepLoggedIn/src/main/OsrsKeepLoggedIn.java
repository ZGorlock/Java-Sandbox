/*
 * File:    OsrsKeepLoggedIn.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.awt.Robot;
import java.awt.event.KeyEvent;

public class OsrsKeepLoggedIn {
    
    public static void main(String[] args) throws Exception {
        Robot robot = new Robot();
        
        while (true) {
            long start = System.currentTimeMillis();
            int wait = (int) (Math.random() * 120000 + 60000);
            long stop = start + wait;
            while (true) {
                Thread.sleep(1);
                long now = System.currentTimeMillis();
                if (now >= stop) {
                    int pressTime = (int) (Math.random() * 50 + 100);
                    robot.keyPress(KeyEvent.VK_SPACE);
                    robot.delay(pressTime);
                    robot.keyRelease(KeyEvent.VK_SPACE);
                    System.out.println(wait + " : " + pressTime);
                    break;
                }
            }
            
        }
    }
    
}
