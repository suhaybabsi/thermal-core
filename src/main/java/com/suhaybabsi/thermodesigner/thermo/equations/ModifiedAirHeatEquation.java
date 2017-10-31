/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.thermo.equations;

import com.suhaybabsi.thermodesigner.thermo.fluid.GasHeatEvaluator;
import com.suhaybabsi.thermodesigner.thermo.tables.GasChemicalTable;

/**
 *
 * @author suhaybal-absi
 */
public class ModifiedAirHeatEquation implements GasHeatEvaluator{
    
    
    private final double[][] c;
    private final double gasConstant;

    public ModifiedAirHeatEquation() {
        
        c = new double[][]{
            {+1.0189134e+3, +7.9865509e+2},
            {-1.3783636e-1, +5.3392159e-1},
            {+1.9843397e-4, -2.2881694e-4},
            {+4.2399242e-7, +3.7420857e-8},
            {-3.7632489e-10, 0.0}
        };
        
        GasChemicalTable.GasChemicalRecord airProps = 
                GasChemicalTable.readGasChemicalRecord("—");
        gasConstant = airProps.getGasConstant();
    }

    public double getGasConstant() {
        return gasConstant;
    }
    
    @Override
    public double getHeatAtConstPressure(double temperature) {
        
        double T = temperature;
        
        if(T > 2200 || T < 200){
            return Double.NaN;
        }
        int j = (T < 800) ? 0 : 1;
        
        double cp = 0;
        for(int i = 0 ; i < c.length; i++){
            
            cp += c[i][j] * Math.pow(T, i);
            
        }
        return cp/1000; // [kJ/kg.K]
    }

    @Override
    public double getHeatAtConstVolume(double temperature) {
        
        return getHeatAtConstPressure(temperature) - gasConstant;
    }

    @Override
    public double getHeatRatio(double temperature) {
        
        double Cp = getHeatAtConstPressure(temperature);
        double Cv = getHeatAtConstVolume(temperature);
        return Cp/Cv;
    }
}
