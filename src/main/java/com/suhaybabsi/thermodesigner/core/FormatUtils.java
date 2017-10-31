/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 *
 * @author suhaybal-absi
 */
public class FormatUtils {
    
    public static String format(double number){
        return format(number, 2);
    }
    public static String format(double number, int d){
        
        DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance();
        format.setMaximumFractionDigits(d);
        return format.format(number);
    }
}
