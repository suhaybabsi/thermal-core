/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.devices;

import com.suhaybabsi.thermodesigner.core.CommonEquations;
import com.suhaybabsi.thermodesigner.core.ConfigurationException;
import com.suhaybabsi.thermodesigner.core.HeatProducingDevice;
import com.suhaybabsi.thermodesigner.core.Property;
import com.suhaybabsi.thermodesigner.core.ThermalEquation;
import com.suhaybabsi.thermodesigner.thermo.Flow;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author suhaybal-absi
 */
public class Boiler extends SteadyFlowDevice implements HeatProducingDevice {
    
    
    private double exitTemperature = Double.NaN;
    private final Property pressureLoss = new Property("Pressure Loss");
    private final Property heatProduced = new Property("Heat Produced");
    
    private static class HeatEquation extends ThermalEquation {

        private final Property q_p;
        private final Flow pe_in;
        private final Flow pe_out;
        private final Property m_p;
        

        private HeatEquation(Property q_p, Flow pe_in, Flow  pe_out,Property  m_p){
            
            super(q_p, 
                pe_in.getEnthalpyProp(),
                pe_out.getEnthalpyProp(),
                m_p);
            
            this.q_p = q_p;
            this.pe_in = pe_in;
            this.pe_out = pe_out;
            this.m_p = m_p;
        }

        @Override
        protected double evaluate() {
            
            double m1 = m_p.getValue();
            double h1 = pe_in.getEnthalpy();
            double h2 = pe_out.getEnthalpy();
            double q = q_p.getValue();
            
            //System.out.println("m1: "+m1+", h1:"+h1+", h2:"+h2+", q:"+q);
            
            return q - m1 * (h2 - h1);
        }
    }

    public Boiler() {
        super("Boiler", "boiler");
    }
    
    @Override
    protected void configureEquations() throws ConfigurationException {
        
        insureSingleFlowFluids();
        Flow f1 = flowManager.getIn(0);
        Flow f2 = flowManager.getOut(0);
        
        if(Double.isNaN(exitTemperature) == false){
            f2.defineTemperature(exitTemperature);
        }
        
        addEquation(new HeatEquation(heatProduced, f1, f2, f1.getMassRateProp()) );
        addEquation(new CommonEquations.MassConservationEquation(
                        f1.getMassRateProp(), 
                        f2.getMassRateProp()) );
        addEquation(new CommonEquations.PressureLossEquation(
                        f1.getPressureProp(), 
                        f2.getPressureProp(), pressureLoss));
        
        addEquation(new Pipe.FlowPropertyEquation(f1,  f1.getEnthalpyProp()));
        addEquation(new Pipe.FlowPropertyEquation(f2, f2.getEnthalpyProp()));
    }

    public void defineExitTemperature(double value) {
        this.exitTemperature = value;
    }

    public double getExitTemperature() {
        
        Flow f2 = flowManager.getOut(0);        
        return f2.getTemperature();
    }
    
    @Override
    public double getHeatProduced() {
        return heatProduced.getValue();
    }
    public void defineHeatProduced(double value) {
        this.heatProduced.setValue(value);
    }

    public void definePressureLoss(double value) {
        this.pressureLoss.setValue(value);
    }
    public double getPressureLoss() {
        return pressureLoss.getValue();
    }

    @Override
    public List<FieldResult> getEnergyResults() {
        
        List<FieldResult> res = new ArrayList<FieldResult>();
        
        res.add(FieldResult.create("hp", "Heat Produced", getHeatProduced()) );
        res.add(FieldResult.create("pl", "Pressure Loss", getPressureLoss()) );
        res.add(FieldResult.create("et", "Exit Temperature", getExitTemperature()) );
        
        return res;
    }
}
