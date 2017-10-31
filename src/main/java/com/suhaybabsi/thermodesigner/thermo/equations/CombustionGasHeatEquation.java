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
public class CombustionGasHeatEquation implements GasHeatEvaluator {

    private double fuelAirRatio;
    private final double[][] c;
    private final ModifiedAirHeatEquation airHeatEquation;
    public CombustionGasHeatEquation() {
        
        airHeatEquation = new ModifiedAirHeatEquation();
        c = new double[][]{
            {-3.5949415e+02, +1.0887572e+3},
            {+4.5163996e+00, -1.4158834e-1},
            {+2.8116360e-03, +1.9160159e-3},
            {-2.1708731e-05, -1.2400934e-6},
            {+2.8688783e-08, +3.0669459e-10},
            {-1.2226336e-11, -2.6117109e-14}
        };
    }

    public void setFuelAirRatio(double fuelAirRatio) {
        this.fuelAirRatio = fuelAirRatio;
    }

    @Override
    public double getHeatAtConstPressure(double temperature) {
        
        double T = temperature;
        if(T > 2200 || T < 200){
            return Double.NaN;
        }
        int j = (T < 800) ? 0 : 1;
        
        
        double Cpa = airHeatEquation.getHeatAtConstPressure(T);
        double f = fuelAirRatio;
        
        
        
        double theta = 0;
        for(int i = 0 ; i < c.length ; i++){
            theta += c[i][j] * Math.pow(T, i);
        }
        
        double Cpg = Cpa + ( f/(1+f)*theta )/1000;
        
        return Cpg;
    }
    @Override
    public double getHeatAtConstVolume(double temperature) {
        return getHeatAtConstPressure(temperature) - airHeatEquation.getGasConstant();
    }
    @Override
    public double getHeatRatio(double temperature) {
        double Cp = getHeatAtConstPressure(temperature);
        double Cv = getHeatAtConstVolume(temperature);
        return Cp/Cv;
    }
    public double getGasConstant() {
        return airHeatEquation.getGasConstant();
    }
}
