/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.thermo.fluid;

/**
 *
 * @author suhaybal-absi
 */
public interface GasHeatEvaluator {
    
    public double getHeatAtConstPressure(double temperature);
    public double getHeatAtConstVolume(double temperature);
    public double getHeatRatio(double temperature);
}
