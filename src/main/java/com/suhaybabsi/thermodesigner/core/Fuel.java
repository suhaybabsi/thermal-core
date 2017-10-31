/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.core;

/**
 *
 * @author suhaybal-absi
 */
public enum Fuel {

    DIESEL(42517);
    
    private double higherHeatingValue;
    private Fuel(double hv) {
        this.higherHeatingValue = hv;    //kJ/kg
    }

    public double getHigherHeatingValue() {
        return higherHeatingValue;
    }
}