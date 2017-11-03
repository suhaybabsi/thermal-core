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
import com.suhaybabsi.thermodesigner.core.WorkProducingDevice;
import com.suhaybabsi.thermodesigner.core.WorkType;
import com.suhaybabsi.thermodesigner.thermo.Flow;
import com.suhaybabsi.thermodesigner.thermo.fluid.FlowState;
import com.suhaybabsi.thermodesigner.thermo.fluid.Fluid;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author suhaybal-absi
 */
public class SteamTurbine extends TurboMachine implements WorkProducingDevice {
    
    
    private double exhaustPressure = Double.NaN;
    private final Property isentropicEfficiency = new Property("Isentropic Efficiency");

    public void defineIsentropicEfficiency(double value) {
        this.isentropicEfficiency.define(value);
    }
    public void defineWorkProduced(double value) {
        this.work.define(value);
    }
    @Override
    public double getWorkProduced() {
        return work.getValue();
    }
    public double getIsentropicEfficiency() {
        return isentropicEfficiency.getValue();
    }
    public void defineExhaustPressure(double value) {
        this.exhaustPressure = value;
    }
    public double getExhaustPressure() {
        Flow f2 = flowManager.getOut(0);
        return f2.getPressure();
    }
    
    private static class WorkEquation extends ThermalEquation {

        private final Property wt_p;
        private final Property nt_p;
        private final Flow pe_in;
        private final Flow pe_out;
        private final Property m_p;

       
        private WorkEquation(Property wt_p, Property nt_p, Flow pe_in, Flow pe_out, Property m_p){
            super(wt_p, nt_p, 
                    pe_in.getPressureProp(),
                    pe_in.getEnthalpyProp(),
                    pe_out.getPressureProp(),
                    m_p );
            setName("Work Equation");
            this.wt_p = wt_p;
            this.nt_p = nt_p;
            this.pe_in = pe_in;
            this.pe_out = pe_out;
            this.m_p = m_p; 
        }
        
        @Override
        protected double evaluate() {
            
            
            double m = m_p.getValue();
            double h1 = pe_in.getEnthalpy();
            double s1 = pe_in.calculateEntropy();
            
            Fluid fluid = pe_out.getFluid();
            FlowState state = new FlowState();
            state.pressure(pe_out.getPressure());
            state.entropy(s1);
            
            double h2s = fluid.enthalpy(state);
            double wt = wt_p.getValue();
            double nt = nt_p.getValue();
            
            
            //System.out.println(state);
            //System.out.println("h1: "+h1+", h2: "+h2s+", wt: "+wt);
            return wt - m * nt * (h1 - h2s);
        }
    }
    private static class TemperatureEquation extends ThermalEquation {

        private final Flow pe_in;
        private final Flow pe_out;
        private final FlowState state = new FlowState();
        private final Property nt_p;
        
        private TemperatureEquation(Flow pe_in, Flow pe_out, Property nt_p){
            super(  pe_in.getPressureProp(),
                    pe_in.getEnthalpyProp(),
                    pe_out.getPressureProp(),
                    pe_out.getEnthalpyProp(),
                    nt_p );
            setName("Temperature Equation");
            this.pe_in = pe_in;
            this.pe_out = pe_out;
            this.nt_p = nt_p; 
        }
        
        @Override
        protected double evaluate() {
            
            double h1 = pe_in.getEnthalpy();
            double h2 = pe_out.getEnthalpy();
            
            double s1 = pe_in.calculateEntropy();
            double p2 = pe_out.getPressure();
            state.pressure(p2);
            state.entropy(s1);
            
            Fluid fluid = pe_out.getFluid();
            double h2s = fluid.enthalpy(state);
            double nt = nt_p.getValue();
            
            //System.out.println("*h1: "+h1+", h2: "+h2);
            //System.out.println("dh: "+(h1 - h2) + ", wt: "+wt);
            
            return (h1 - h2) - nt * (h1 - h2s);
        }
    }

    public SteamTurbine() {
        
        super("Steam Turbine", "steam_turbine");
        work = new Work(WorkType.PRODUCED);
    }
     
    @Override
    protected void configureEquations() throws ConfigurationException {
        
        insureFluids(Fluid.getFluid("water"));
        Flow f1 = flowManager.getIn(0);
        Flow f2 = flowManager.getOut(0);
        
        if(Double.isNaN(exhaustPressure) == false){
            f2.definePressure(exhaustPressure);
        }
        
        addEquation(new CommonEquations.MassConservationEquation(
                        f1.getMassRateProp(), 
                        f2.getMassRateProp()));
        
        addEquation(new WorkEquation(
            work, 
            isentropicEfficiency,
            f1, f2, 
            f1.getMassRateProp() ) );
        
        addEquation(new TemperatureEquation(f1, f2, isentropicEfficiency));
        
        addEquation(new Pipe.FlowPropertyEquation(f1, f1.getEnthalpyProp()));
        addEquation(new Pipe.FlowPropertyEquation(f2, f2.getTemperatureProp()));
        addEquation(new Pipe.FlowPropertyEquation(f2, f2.getEntropyProp()));
    }
    
    @Override
    public List<FieldResult> getEnergyResults() {
        
        List<FieldResult> res = new ArrayList<FieldResult>();
        
        res.add(FieldResult.create("nt", "Isentropic Efficiency", getIsentropicEfficiency()) );
        res.add(FieldResult.create("wt", "Work Produced", getWorkProduced()) );
        res.add(FieldResult.create("ep", "Exhaust Pressure", getExhaustPressure()) );
        
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
        double m2 = f2.getMassRate();
        double x1 = f1.getExergy();
        double x2 = f2.getExergy();
        
        double Wct = work.getValue();
        
        double Xin = m1 * x1;
        double Xout = m2 * x2 + Wct;
        
        double Xdest = Xin - Xout;
        double Wrev = m1 * (x1 - x2);
        double Xeff = Wct / Wrev;
        
        reversibleWork.setValue(Wrev);
        exergyDestruction.setValue(Xdest);
        secondLawEfficiency.setValue(Xeff);
    }
    
    @Override
    public List<FieldResult> getExergyResults() {
        
        List<FieldResult> res = new ArrayList<FieldResult>();        
        
        res.add(FieldResult.create("Wrev", "Reversible Work", reversibleWork.getValue()) );
        res.add(FieldResult.create("Xdest", "Exergy Destruction", exergyDestruction.getValue()) );
        res.add(FieldResult.create("Xeff", "Second Law Efficiency", secondLawEfficiency.getValue()) );
         
        return res;
    }
    
}
