/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.thermo.tables.water;

/**
 *
 * @author suhaybal-absi
 */
public interface SaturatedTable {
    
    public double getLiquidVolume(double value);
    public double getEvapVolume(double value);
    public double getVaporVolume(double value);
    public double getLiquidInternalEnergy(double value);
    public double getEvapInternalEnergy(double value);
    public double getVaporInternalEnergy(double value);
    public double getLiquidEnthalpy(double value);
    public double getEvapEnthalpy(double value);
    public double getVaporEnthalpy(double value);
    public double getLiquidEntropy(double value);
    public double getEvapEntropy(double value);
    public double getVaporEntropy(double value);
    public double getVolume(double value, double x);
    public double getInternalEnergy(double value, double x);
    public double getEnthalpy(double value, double x);
    public double getEntropy(double value, double x);
    public double getSaturationRatio_volume(double value, double volume);
    public double getSaturationRatio_enthalpy(double value, double enthalpy);
    public double getSaturationRatio_energy(double value, double energy);
    public double getSaturationRatio_entropy(double value, double entropy);
}
