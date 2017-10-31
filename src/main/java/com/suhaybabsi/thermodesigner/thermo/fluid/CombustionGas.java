/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.thermo.fluid;

import com.suhaybabsi.thermodesigner.thermo.equations.CombustionGasHeatEquation;

/**
 *
 * @author suhaybal-absi
 */
public class CombustionGas extends Gas {
    
    private double fuelAirRatio;
    private final CombustionGasHeatEquation heatEquation;
    
    public double getFuelAirRatio() {
        return fuelAirRatio;
    }
    public void setFuelAirRatio(double f) {
        this.fuelAirRatio = f;
        this.heatEquation.setFuelAirRatio(f);
    }
    
    public CombustionGas(double fuelRatio) {
        super("combustion gases");
        this.heatEquation = new CombustionGasHeatEquation();
        this.fuelAirRatio = fuelRatio;
    }
    
    @Override
    public double specificHeatCp(double temperature) {
        return heatEquation.getHeatAtConstPressure(temperature);
    } 

    @Override
    public double specificHeatRatio(double temperature) {
        return heatEquation.getHeatRatio(temperature);
    }
    
    @Override
    public double gasConstant() {
        return heatEquation.getGasConstant();
    }

    @Override
    public double entropy(FlowState state) {
        return Air.getInstance().entropy(state);
    }

    @Override
    public double enthalpy(FlowState state) {
        
        double cp = specificHeatCp(state.temperature());
        double h = cp * state.temperature();
        return h;
    }
}
