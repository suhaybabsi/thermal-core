/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.thermo.tables;

/**
 *
 * @author suhaybal-absi
 */
public class Range {
    
    private double min;
    private double max;

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public Range(double min, double max) {
        this.min = min;
        this.max = max;
    }
    
    
}
