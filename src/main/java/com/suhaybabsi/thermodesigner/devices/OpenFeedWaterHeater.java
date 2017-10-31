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
import java.util.List;

/**
 *
 * @author suhaybal-absi
 */
public class OpenFeedWaterHeater extends MixingChamber {
    
    private static class ExitFlowStateEquation extends ThermalEquation{

        private final Flow flow;
        
        private ExitFlowStateEquation(Flow flow){
            
            super();
            this.flow = flow;
            setName("Exit Flow State Equation");
        }

        @Override
        protected double evaluate() {
            
            Fluid f = flow.getFluid();
            double p = flow.getPressure();
            double t = flow.getTemperature();
            
            if(Double.isNaN(p) == false){
                
                double tsat = f.saturatedTemperature(p);
                flow.setTemperature(tsat);
                flow.setVapourFraction(0);
                
            }else if(Double.isNaN(t) == false){
                
                double psat = f.saturatedPressure(t);
                flow.setPressure(psat);
                flow.setVapourFraction(0);
            }
            
            p = flow.getPressure();
            t = flow.getTemperature();
            //System.out.println("p:"+p+", t:"+t);
            return (Double.isNaN(t) || Double.isNaN(p)) ? -1 : 0;
        }
    }
    
    public OpenFeedWaterHeater() {
        super("Open Feed Water Heater", "open_feed_heater");
    }

    @Override
    protected void configureEquations() throws ConfigurationException {
        super.configureEquations();
        
        insureFluids();
        Flow fo = flowManager.getOut(0);
        addEquation(new ExitFlowStateEquation(fo) );
    }
    
    @Override
    public List<FieldResult> getEnergyResults() {
        
        List<FieldResult> res = new ArrayList<FieldResult>();
        
        res.add(FieldResult.create("p", "Pressure", getPressure()) );
        
        return res;
    }
    
    @Override
    public List<FieldResult> getEnergyOutputResults() {
        
        Flow fo = flowManager.getOut(0);
        List<FieldResult> res = new ArrayList<FieldResult>();
        
        res.add(FieldResult.create("Exit Temperature", fo.getTemperature() )); 
        res.add(FieldResult.create("Exit Pressure", fo.getPressure() ));
        res.add(FieldResult.create("Exit Vapour Fraction", fo.getVapourFraction() ));
        
        return res;
    }
}
