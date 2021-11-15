package jzt.knn.distance;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Distance {
    static public double CalcuDistance(String method, double[] a, double[] b) {
        int length = a.length;
        double result = 0;
        if (method.equals("manhattan")) {
            for(int i = 0; i <length;i++) {
                result = result + Math.abs(a[i]-b[i]) ;
            }
            return result;
        }
        else if (method.equals("chebyshev")) {
            for(int i = 0; i <length;i++) {
                if (Math.abs(a[i]-b[i]) > result)
                    result = Math.abs(a[i]-b[i]);
            }
            return result;
        }
        else {
            for(int i = 0; i <length;i++) {
                result = result + (a[i]-b[i]) * (a[i] - b[i]);
            }
            return Math.sqrt(result);
        }
    }
}
