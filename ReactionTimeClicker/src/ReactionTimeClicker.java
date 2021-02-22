/*
 * File:    ReactionTimeClicker.java
 * Package: PACKAGE_NAME
 * Author:  Zachary Gill
 */

import java.awt.Robot;
import java.awt.event.InputEvent;

public class ReactionTimeClicker {
    
    //https://www.humanbenchmark.com/tests/reactiontime
    public static void main(String[] args) throws Exception {
        Robot robot = new Robot();
        
        int count = 0;
        boolean active = false;
        while (count < 5) {
            int color = robot.getPixelColor(420, 420).getRGB();
            if (color != -11805846) {
                active = true;
            } else if (active) {
                robot.mousePress(InputEvent.BUTTON1_MASK);
                robot.delay(10);
                robot.mouseRelease(InputEvent.BUTTON1_MASK);
                count++;
                if (count < 5) {
                    robot.delay(500);
                    robot.mousePress(InputEvent.BUTTON1_MASK);
                    robot.delay(10);
                    robot.mouseRelease(InputEvent.BUTTON1_MASK);
                }
            }
        }
    }
    
}
