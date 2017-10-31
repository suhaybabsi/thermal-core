/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.thermo.equations;

import com.suhaybabsi.thermodesigner.thermo.fluid.GasHeatEvaluator;

/**
 *
 * @author suhaybal-absi
 */
public class IdealGasHeatEquation implements GasHeatEvaluator {
    
    public static class EquationRange{
        
        private double min;
        private double max;
        private EquationRange(double min, double max) {
            
            if(max >= min){
                this.max = max;
                this.min = min;
                
            }else{
                throw new Error("Max value can't be less than the min one.");
            }
        }
        public boolean validate(double v){
            return (v >= min && v <= max);
        }
    }
    
    private double a;
    private double b;
    private double c;
    private double d;
    
    private double percentErrorMax;
    private double percentErrorAvg;
    private EquationRange range;
    private double molarMass;
    private double gasConstant;
    
    public IdealGasHeatEquation(double a, double b, double c, double d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }
    public void setMolarMass(double molarMass) {
        this.molarMass = molarMass;
    }
    public void setGasConstant(double gasConstant) {
        this.gasConstant = gasConstant;
    }
    
    public void setRange(double min, double max){
        range = new EquationRange(min, max);
    }
    public EquationRange getRange() {
        return range;
    }
    public double getPercentErrorMax() {
        return percentErrorMax;
    }
    public void setPercentErrorMax(double percentErrorMax) {
        this.percentErrorMax = percentErrorMax;
    }
    public double getPercentErrorAvg() {
        return percentErrorAvg;
    }
    public void setPercentErrorAvg(double percentErrorAvg) {
        this.percentErrorAvg = percentErrorAvg;
    }
    
    @Override
    public double getHeatAtConstPressure(double temperature) {
        double T = temperature;
        double cp_ = a 
                + b * T 
                + c * Math.pow(T, 2) 
                + d * Math.pow(T, 3);
        
        double cp = cp_ / molarMass;
                
        return cp; // [kJ/kmol.K]
    } 
    @Override
    public double getHeatAtConstVolume(double temperature){
        double Cp = getHeatAtConstPressure(temperature);
        double R = this.gasConstant;
        return Cp - R;
    }
    @Override
    public double getHeatRatio(double temperature){
        double Cp = getHeatAtConstPressure(temperature);
        double Cv = getHeatAtConstVolume(temperature);
        return Cp/Cv;
    }
}
