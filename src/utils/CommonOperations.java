/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

/**
 *
 * @author Juan
 */
public class CommonOperations {

    public static final double EPSILON = 0.000001;

    public static boolean isMultiple(double num1, double num2) {

        boolean multiple;
        double rem = num1 % num2;
        
        multiple = rem <= CommonOperations.EPSILON || num2 - rem <= CommonOperations.EPSILON;
        
        return multiple;
        
    }
    
    public static boolean isEqual(double num1, double num2) {

        boolean equal;
        
        equal = Math.abs(num2 - num1) <= CommonOperations.EPSILON;
        
        return equal;
        
    }    

}
