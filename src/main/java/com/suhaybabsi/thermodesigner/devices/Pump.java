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
import com.suhaybabsi.thermodesigner.core.Work;
import com.suhaybabsi.thermodesigner.core.WorkConsumingDevice;
import com.suhaybabsi.thermodesigner.core.WorkType;
import com.suhaybabsi.thermodesigner.thermo.Flow;
import com.suhaybabsi.thermodesigner.thermo.fluid.FlowState;
import com.suhaybabsi.thermodesigner.thermo.fluid.Fluid;
import com.suhaybabsi.thermodesigner.thermo.fluid.Water;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author suhaybal-absi
 */
public class Pump extends TurboMachine implements WorkConsumingDevice {
    
    private double massFlowRate = Double.NaN;
    private double exitPressure = Double.NaN;
    private final Property isentropicEfficiency = new Property("Isentropic Efficiency");

    public void defineMassFlowRate(double value) {
        this.massFlowRate = value;
    }
    public void defineExitPressure(double value) {
        this.exitPressure = value;
    }
    
    public void defineIsentropicEfficiency(double value) {
        this.isentropicEfficiency.define(value);
    }
    
    public void defineWorkConsumed(double value) {
        this.work.define(value);
    }

    public double getIsentropicEfficiency() {
        return isentropicEfficiency.getValue();
    }

    @Override
    public double getWorkConsumed() {
        return work.getValue();
    }

    public double getMassFlowRate() {
        return flowManager.getIn(0).getMassRate();
    }

    public double getExitPressure() {
        return flowManager.getOut(0).getPressure();
    }
    
    private static class WorkEquation extends ThermalEquation {
      
        private final Property wp_p;
        private final Flow pe_in;
        private final Flow pe_out;
        private final Property m_p;
        
        private WorkEquation(Property wp_p, Flow pe_in, Flow pe_out, Property m_p){
            super(wp_p, pe_in.getEnthalpyProp(), pe_out.getEnthalpyProp(), m_p);
            this.wp_p = wp_p;
            this.pe_in = pe_in;
            this.pe_out = pe_out;
            this.m_p = m_p;
        }
        
        @Override
        protected double evaluate() {
            
            double m = m_p.getValue();
            double h1 = pe_in.getEnthalpy();
            double h2 = pe_out.getEnthalpy();
            
            double wp = wp_p.getValue();
            return wp - m * (h2 - h1);
        }
    }
    
    private static class TemperatureEquation extends ThermalEquation {

        private final Flow pe_in;
        private final Flow pe_out;
        private final FlowState state;
        private final Property np_p;

        private TemperatureEquation(Flow pe_in, Flow pe_out, Property np_p){
            super(pe_in.getPressureProp(),
                    pe_in.getEnthalpyProp(),
                    pe_out.getPressureProp(),
                    pe_out.getEnthalpyProp(),
                    np_p );
            this.pe_in = pe_in;
            this.pe_out = pe_out;
            this.np_p = np_p; 
            this.state = new FlowState();
        }
        
        @Override
        protected double evaluate() {
            
            Fluid fluid = pe_in.getFluid();
            
            double h1 = pe_in.getEnthalpy();
            double h2 = pe_out.getEnthalpy();
            double np = np_p.getValue();
            
            double s1 = pe_in.calculateEntropy();
            double p2 = pe_out.getPressure();

            state.reset();
            state.pressure(p2);
            state.entropy(s1);

            double h2s = fluid.enthalpy(state);
            
            return (h2 - h1) - (h2s - h1) / np;
        }
    }

    public Pump() {
        super("Pump", "pump");
        work = new Work(WorkType.CONSUMED);
    }    
    
    @Override
    protected void configureEquations() throws ConfigurationException {
        
        insureFluids(Water.getInstance());
        Flow f1 = flowManager.getIn(0);
        Flow f2 = flowManager.getOut(0);
        
        if(Double.isNaN(exitPressure) == false){
            f2.definePressure(exitPressure);
        }
        
        Property temp_out = f2.getTemperatureProp();
        temp_out.setMin(273.15);
        temp_out.setMax(2000);
        
        addEquation(new WorkEquation(
            work,
            f1, f2, 
            f1.getMassRateProp()) );
        addEquation(new TemperatureEquation(f1, f2, isentropicEfficiency) );
        
        if(Double.isNaN(massFlowRate) == false){
            f1.defineMassRate(massFlowRate);
            f2.defineMassRate(massFlowRate);
        }else{
            
            addEquation(new CommonEquations.MassConservationEquation(
                        f1.getMassRateProp(), 
                        f2.getMassRateProp()) );
        }
    }

    @Override
    public List<FieldResult> getEnergyResults() {
        
        List<FieldResult> res = new ArrayList<FieldResult>();
        
        res.add(FieldResult.create("mp", "Mass Rate", getMassFlowRate()) );
        res.add(FieldResult.create("np", "Isentropic Efficiency", getIsentropicEfficiency()) );
        res.add(FieldResult.create("wp", "Work Consumed", getWorkConsumed()) );
        res.add(FieldResult.create("ep", "Exit Pressure", getExitPressure()) );
        
        return res;
    }
    
    private final Property reversibleWork = new Property("Reversible Work");
    private final Property exergyDestruction = new Property("Exergy Destruction");
    private final Property secondLawEfficiency = new Property("Second Law Efficiency");
    
    @Override
    public void calculateExergy(){
        
        super.calculateExergy();
        
        Flow f1 = flowManager.getIn(0);
        Flow f2 = flowManager.getOut(0);
        
        double m1 = f1.getMassRate();
        double x1 = f1.getExergy();
        double x2 = f2.getExergy();
        
        double Wc = work.getValue();
        double Wrev = m1 * (x2 - x1);
        double Xdest = Wc - Wrev;
        double Xeff = Wrev / Wc;
        
        reversibleWork.setValue(Wrev);
        exergyDestruction.setValue(Xdest);
        secondLawEfficiency.setValue(Xeff);
    }
    
    @Override
    public List<FieldResult> getExergyResults() {
        
        List<FieldResult> res = new ArrayList<FieldResult>();
        
        res.add(FieldResult.create("Wrev","Reversible Work", reversibleWork.getValue()) );
        res.add(FieldResult.create("Xdest","Exergy Destruction", exergyDestruction.getValue())  );
        res.add(FieldResult.create("Xeff","Second Law Efficiency", secondLawEfficiency.getValue()) );
        
        return res;
    }
}
