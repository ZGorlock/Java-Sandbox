/*
 * File:    Test.java
 * Package:
 * Author:  Zachary Gill
 */

public class Test {
    
    private static final int NumHexDigits = 16;
    
    private static final double Epsilon = 1e-17;
    
    private static final int NumTwoPowers = 25;
    
    private static double[] twoPowers = new double[NumTwoPowers];
    
    public static void main(String[] args) {
        for (int digitPosition = 0; digitPosition < 50; digitPosition += 10) {
            double pid, s1, s2, s3, s4;
//            int digitPosition = 0;
            String hexDigits;
            
            InitializeTwoPowers();
            
            //  Digits generated follow immediately after digitPosition.
            s1 = Series(1, digitPosition);
            s2 = Series(4, digitPosition);
            s3 = Series(5, digitPosition);
            s4 = Series(6, digitPosition);
            
            pid = 4d * s1 - 2d * s2 - s3 - s4;
            pid = pid - (int) pid + 1d;
            hexDigits = HexString(pid, NumHexDigits);

//            System.out.println("Position = " + digitPosition);
//            System.out.println("Fraction = " + pid);
//            double sum = 0;
//            for (int i = 0; i < hexDigits.substring(0, 10).length(); i++) {
//                sum += Math.pow(1.0 / 16, i + digitPosition + 1) * Long.parseLong(String.valueOf(hexDigits.charAt(i)), 16);
            System.out.println("Hex digits = " + hexDigits.substring(0, 10));
//            }
//            System.out.println(String.format("%.10f", sum));
        }
    }
    
    // Returns the first NumHexDigits hex digits of the fraction of x.
    private static String HexString(double x, int numDigits) {
        String hexChars = "0123456789ABCDEF";
        StringBuilder sb = new StringBuilder(numDigits);
        double y = Math.abs(x);
        
        for (int i = 0; i < numDigits; i++) {
            y = 16d * (y - Math.floor(y));
            sb.append(hexChars.charAt((int) y));
        }
        
        return sb.toString();
    }
    
    // This routine evaluates the series  sum_k 16^(n-k)/(8*k+m) 
    //    using the modular exponentiation technique.
    private static double Series(int m, int n) {
        double denom, pow, sum = 0d, term;
        
        //  Sum the series up to n.
        for (int k = 0; k < n; k++) {
            denom = 8 * k + m;
            pow = n - k;
            term = ModPow16(pow, denom);
            sum = sum + term / denom;
            sum = sum - (int) sum;
        }
        
        //  Compute a few terms where k >= n.
        for (int k = n; k <= n + 100; k++) {
            denom = 8 * k + m;
            term = Math.pow(16d, (double) (n - k)) / denom;
            
            if (term < Epsilon) {
                break;
            }
            
            sum = sum + term;
            sum = sum - (int) sum;
        }
        
        return sum;
    }
    
    // Fill the power of two table
    private static void InitializeTwoPowers() {
        twoPowers[0] = 1d;
        
        for (int i = 1; i < NumTwoPowers; i++) {
            twoPowers[i] = 2d * twoPowers[i - 1];
        }
    }
    
    // ModPow16 = 16^p mod m.  This routine uses the left-to-right binary 
    //  exponentiation scheme.
    private static double ModPow16(double p, double m) {
        int i;
        double pow1, pow2, result;
        
        if (m == 1d) {
            return 0d;
        }
        
        // Find the greatest power of two less than or equal to p.
        for (i = 0; i < NumTwoPowers; i++) {
            if (twoPowers[i] > p) {
                break;
            }
        }
        
        pow2 = twoPowers[i - 1];
        pow1 = p;
        result = 1d;
        
        // Perform binary exponentiation algorithm modulo m.
        for (int j = 1; j <= i; j++) {
            if (pow1 >= pow2) {
                result = 16d * result;
                result = result - (int) (result / m) * m;
                pow1 = pow1 - pow2;
            }
            
            pow2 = 0.5 * pow2;
            
            if (pow2 >= 1d) {
                result = result * result;
                result = result - (int) (result / m) * m;
            }
        }
        
        return result;
    }
    
}