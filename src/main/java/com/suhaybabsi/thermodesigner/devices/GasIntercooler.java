/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.devices;

import com.suhaybabsi.thermodesigner.core.CommonEquations;
import com.suhaybabsi.thermodesigner.core.ConfigurationException;
import com.suhaybabsi.thermodesigner.core.Property;
import com.suhaybabsi.thermodesigner.core.ThermalEquation;
import com.suhaybabsi.thermodesigner.thermo.Flow;
import com.suhaybabsi.thermodesigner.thermo.fluid.Fluid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author suhaybal-absi
 */
public class GasIntercooler extends SteadyFlowDevice {
    
    private double exitFlowTemperature = Double.NaN;
    private final Property pressureLoss = new Property("Pressure Loss");
    private final Property heatExtracted = new Property("Heat Extracted");

    public void defineExitFlowTemperature(double temperature) {
        this.exitFlowTemperature = temperature;
    }
    public void definePressureLoss(double value) {
        this.pressureLoss.define(value);
    }
    public void defineHeatExtracted(double value) {
        this.heatExtracted.define(value);
    }

    public double getExitFlowTemperature() {
        Flow flowOut = flowManager.getOut(0);
        return flowOut.getTemperature();
    }
    public double getPressureLoss() {
        return pressureLoss.getValue();
    }
    public double getHeatExtracted() {
        return heatExtracted.getValue();
    }
    
    
    private static class HeatLossEquation extends ThermalEquation {
        
        
        private final Property m_p;
        private final Property t1_p;
        private final Property t2_p;
        private final Fluid fluid;
        private final Property q_p;
    
        private HeatLossEquation(Property q_p, Property m_p, Property t1_p, Property t2_p, Fluid f){
            
            super(q_p, m_p, t1_p, t2_p);
            this.q_p = q_p;
            this.m_p = m_p;
            this.t1_p = t1_p;
            this.t2_p = t2_p;
            this.fluid = f; 
        }

        @Override
        protected double evaluate() {
            
            double m = m_p.getValue();
            double t1 = t1_p.getValue();
            double t2 = t2_p.getValue();
            double q = q_p.getValue();
            
            double cp = fluid.averageHeatCp(t1, t2);
            
            return q - m * cp * (t1 - t2);
        }
    
    
    }
    
    public GasIntercooler() {
        super("Intercooler", "intercooler");
        
        pressureLoss.setMin(0);
        pressureLoss.setMax(1.0);
    }
    
    @Override
    protected void configureEquations() throws ConfigurationException {
        
        insureSingleFlowFluids();
        
        Flow flowIn = flowManager.getIn(0);
        Flow flowOut = flowManager.getOut(0);
        
        if(Double.isNaN(exitFlowTemperature) == false){
           flowOut.defineTemperature(exitFlowTemperature);
        }
        
        Property massIn = flowIn.getMassRateProp();
        Property massOut = flowOut.getMassRateProp();
        
        Property p1 = flowIn.getPressureProp();
        Property p2 = flowOut.getPressureProp();
        
        Property t1 = flowIn.getTemperatureProp();
        Property t2 = flowOut.getTemperatureProp();
        
        Fluid f = flowIn.getFluid();
        
        addEquation(new CommonEquations.MassConservationEquation(massIn, massOut));
        addEquation(new CommonEquations.PressureLossEquation(p1, p2, pressureLoss));
        addEquation(new HeatLossEquation(heatExtracted, massIn, t1, t2, f));        
    }

    @Override
    protected List<FieldResult> getEnergyOutputResults() {
        
        Flow flowIn = flowManager.getIn(0);
        Flow flowOut = flowManager.getOut(0);
        List<FieldResult> res = new ArrayList<FieldResult>();
        
        res.add(FieldResult.create("Heat Extracted", getHeatExtracted() ));
        res.add(FieldResult.create("Pressure Loss", getPressureLoss() ));
        res.add(FieldResult.create("Pressure (1)", flowIn.getPressure() ));
        res.add(FieldResult.create("Temperature (1)", flowIn.getTemperature() ));
        res.add(FieldResult.create("Pressure (2)", flowOut.getPressure() ));
        res.add(FieldResult.create("Temperature (2)", flowOut.getTemperature() ));
        
        return res;
    }
    
    @Override
    public List<FieldResult> getEnergyResults() {
        
        List<FieldResult> res = new ArrayList<FieldResult>();
        
        res.add(FieldResult.create("et", "Exit Flow Temperature", getExitFlowTemperature()) );
        res.add(FieldResult.create("he", "Heat Extracted", getHeatExtracted()) );
        res.add(FieldResult.create("pl", "Pressure Loss", getPressureLoss()) );
        
        return res;
    }
}
