/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.devices;

import com.hummeling.if97.IF97;
import com.suhaybabsi.thermodesigner.core.CommonEquations;
import com.suhaybabsi.thermodesigner.core.ConfigurationException;
import com.suhaybabsi.thermodesigner.core.Property;
import com.suhaybabsi.thermodesigner.core.ThermalEquation;
import com.suhaybabsi.thermodesigner.thermo.Flow;
import com.suhaybabsi.thermodesigner.thermo.fluid.FlowState;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author suhaybal-absi
 */
public class Condenser extends SteadyFlowDevice {
    
    
    private final Property pressureLoss = new Property("Pressure Loss");
    private final Property subcoolingAmount = new Property("Subcooling Amount");
    private final Property heatExtracted = new Property("Extracted Heat");

    public void defineHeatExtracted(double value) {
        this.heatExtracted.define(value);
    }
    public void definePressureLoss(double value) {
        this.pressureLoss.define(value);
    }
    public double getPressureLoss() {
        return pressureLoss.getValue();
    }
    public void defineSubcoolingAmount(double value) {
        this.subcoolingAmount.define(value);
    }
    public double getSubcoolingAmount() {
        return subcoolingAmount.getValue();
    }
    public double getHeatExtracted() {
        return heatExtracted.getValue();
    }
    
    private static class HeatExtractionEquation extends ThermalEquation {

        private final Property m_p;
        private final Flow pe_in;
        private final Flow pe_out;
        private final Property q_p;
        
        private HeatExtractionEquation(Property q_p, Property m_p, Flow pe_in, Flow pe_out){
            
            super(q_p, m_p, pe_in.getEnthalpyProp(), pe_out.getEnthalpyProp() );
            this.q_p = q_p;
            this.m_p = m_p;
            this.pe_in = pe_in;
            this.pe_out = pe_out;
        }

        @Override
        protected double evaluate() {
            
            double m = m_p.getValue();
            double h1 = pe_in.getEnthalpy();
            double h2 = pe_out.getEnthalpy();
            double q = q_p.getValue();
            //System.out.println("s: "+ pe_in.getEntropy());
            //System.out.println("m: "+m+", h1: "+h1+", h2: "+h2+", q: "+q);
            
            return q - m * (h1 - h2);
        }
    }
    private static class DesaturationEquation extends ThermalEquation {

        private final Flow pe_out;
        private final Property sa_p;
        private final FlowState state;
        
        private DesaturationEquation(Flow pe_out, Property sa_p){
            super(pe_out.getPressureProp(), pe_out.getEnthalpyProp(), sa_p);
            setName("Desaturation Equation");
            this.pe_out = pe_out;
            this.sa_p = sa_p;
            this.state = new FlowState();
        }
        //IF97 if97 = new IF97();
        @Override
        protected double evaluate() {
            
            double p = pe_out.getPressure();
            double h = pe_out.getEnthalpy();
            double sa = sa_p.getValue();
            
            
            double tsat = pe_out.getFluid().saturatedTemperature(p);
            double t = tsat - sa;
            
            this.state.temperature(t);
            this.state.pressure(p);
            this.state.vapourFraction(0.0);
            double h_c = pe_out.getFluid().enthalpy(state);
            
            
//            double hh_c = if97.specificEnthalpyPT(p, t);
            
//            System.out.println("p: "+p+", t: "+t+", sa: "+sa);
//            System.out.println("tsat: "+ tsat);
//            System.out.println("h_c: "+ h_c);
//            System.out.println("hh_c: "+ hh_c);
//            System.out.println("x: "+pe_out.calculateVapourFraction());
            
            return h_c - h;
        }
    }

    public Condenser() {
        super("Condenser", "condenser");
    }
    
    @Override
    protected void configureEquations() throws ConfigurationException {
       
        insureSingleFlowFluids();
        Flow f1 = flowManager.getIn(0);
        Flow f2 = flowManager.getOut(0);
        
        addEquation(new CommonEquations.PressureLossEquation(
                        f1.getPressureProp(), 
                        f2.getPressureProp(), pressureLoss));
        
        addEquation(new CommonEquations.MassConservationEquation(
                        f1.getMassRateProp(), 
                        f2.getMassRateProp() ));
        
        addEquation(new DesaturationEquation(f2, subcoolingAmount));
        addEquation(new HeatExtractionEquation(
                heatExtracted, 
                f1.getMassRateProp(), 
                f1, f2) );
        
        
        addEquation(new Pipe.FlowPropertyEquation(f1, f1.getEnthalpyProp()));
        addEquation(new Pipe.FlowPropertyEquation(f2, f2.getTemperatureProp()));
    }

    @Override
    public List<FieldResult> getEnergyResults() {
        
        List<FieldResult> res = new ArrayList<FieldResult>();
        
        res.add(FieldResult.create("he", "Heat Extracted", getHeatExtracted()) );
        res.add(FieldResult.create("pl", "Pressure Loss", getPressureLoss()) );
        res.add(FieldResult.create("sa", "Subcooling Amount", getSubcoolingAmount()) );
        
        return res;
    }
}
