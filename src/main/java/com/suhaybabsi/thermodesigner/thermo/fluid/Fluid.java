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
public abstract class Fluid {
    
    private final String name;
    public String getName() {
        return name;
    }
    protected Fluid(String name) {
        this.name = name;
    }
    public static Fluid getFluid(String string) {
        
        if(string == null){return null;}
        
        if("air".equalsIgnoreCase(string.trim())){
            return Air.getInstance();
        }
        if("steam".equalsIgnoreCase(string.trim())){
            return Water.getInstance();
        }
        if("water".equalsIgnoreCase(string.trim())){
            return Water.getInstance();
        }
        if("nitrogen".equalsIgnoreCase(string.trim())){
            return Nitrogen.getInstance();
        }
        
        return null;
    }
    
    public double specificHeatCp(double temperature){return -1;}
    public double specificHeatCv(double temperature){return -1;}
    public double specificHeatRatio(double temperature){return -1;}
    
    public double averageHeatCp(double t1, double t2) {
        
        
        double cp1 = specificHeatCp(t1);
        double cp2 = specificHeatCp(t2);
        
        if (Double.isNaN(cp1)) {
            cp1 = cp2;
        }
        if (Double.isNaN(cp2)) {
            cp2 = cp1;
        }
        return (cp1+cp2)/2.0; 
    }
    public double averageHeatRatio(double t1, double t2, double p) {
        
        double g1 = specificHeatRatio(t1);
        double g2 = specificHeatRatio(t2);
        
        //System.out.println("g1: "+g1+", g2: "+g2);
        if (Double.isNaN(g1)) {
            g1 = g2;
        }
        if (Double.isNaN(g2)) {
            g2 = g1;
        }

        return (g1+g2)/2.0; 
    }

    public double enthalpy(FlowState state){return -1;}
    public double internalEnergy(FlowState state){return -1;}
    public double specificVolume(FlowState state){return -1;}
    public double temperature(FlowState state){return -1;}
    public double pressure(FlowState state){return -1;}
    
    public double exergy(FlowState state, double To, double Po) {
        
        double h = state.enthalpy();
        double s = state.entropy();
        h = Double.isNaN(h) ? enthalpy(state) : h;
        s = Double.isNaN(s) ? entropy(state) : s;
        
        FlowState st_o = new FlowState();
        st_o.temperature(To);
        st_o.pressure(Po);
        
        double ho = enthalpy(st_o);
        double so = entropy(st_o);
        
        double x = (h - ho) - To * (s - so);
        return x;
    }
    
    public double entropy(FlowState state) {return -1;}
    public double vapourFraction(FlowState state) {return -1;}
    public double saturatedTemperature(double pressure) {return -1;}
    public double saturatedPressure(double temperature) {return -1;}
}
