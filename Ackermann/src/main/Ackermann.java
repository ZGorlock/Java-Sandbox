/*
 * File:    Ackermann.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

public class Ackermann {
    
    public static void main(String[] args) {
        for (int m = 0; m < 5; m++) {
            for (int n = 0; n < 5; n++) {
                System.out.println("Ackermann (" + m + "," + n + ") = " + ack(m, n));
            }
        }
    }
    
    private static int ack(int m, int n) {
        int ans;
        if (m == 0) {
            ans = n + 1;
        } else if (n == 0) {
            ans = ack(m - 1, 1);
        } else {
            ans = ack(m - 1, ack(m, n - 1));
        }
        return ans;
    }
    
}
