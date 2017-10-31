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
public abstract class Gas extends Fluid {
    protected Gas(String name) {
        super(name);
    }
    
    public abstract double gasConstant();

    @Override
    public double exergy(FlowState state, double To, double Po) {
        
        double T = state.temperature();
        double P = state.pressure();
        
        T = Double.isNaN(T) ? temperature(state) : T;
        P = Double.isNaN(P) ? pressure(state) : P;
        
        double Cpg = averageHeatCp(T, To);
        double R = gasConstant();
        double x = Cpg * (T - To) - To * (Cpg * Math.log(T / To) - R * Math.log(P / Po));
        
        return x;
    }
}
