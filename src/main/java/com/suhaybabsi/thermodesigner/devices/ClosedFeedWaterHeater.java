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
public class ClosedFeedWaterHeater extends SteadyFlowDevice {

    public static class MassConservationEquation extends ThermalEquation {
        
        private final Property massIn;
        private final Property massOut;
        public MassConservationEquation(Property massIn, Property massOut) {
            
            super(massIn, massOut);
            setName("Mass Conservation Equation");
            this.massIn = massIn;
            this.massOut = massOut;
        }
        
        @Override
        protected double evaluate() {
            
            double m_in = massIn.getValue();
            double m_out = massOut.getValue();
            return m_in - m_out;
        }   
    }
    public static class EnergyBalanceEquation extends ThermalEquation {

        private final Flow pe_in1;
        private final Flow pe_in2;
        private final Flow pe_out1;
        private final Flow pe_out2;
        
        public EnergyBalanceEquation(Flow pe_in1, Flow pe_in2, Flow pe_out1, Flow pe_out2) {
            
            super(pe_in1.getMassRateProp(), pe_in2.getMassRateProp(),
                  pe_in1.getEnthalpyProp(), pe_in2.getEnthalpyProp(), 
                  pe_out1.getEnthalpyProp(), pe_out2.getEnthalpyProp());
            
            setName("Energy Balance Equation");
            this.pe_in1 = pe_in1;
            this.pe_in2 = pe_in2;
            this.pe_out1 = pe_out1;
            this.pe_out2 = pe_out2;
        }
        
        @Override
        protected double evaluate() {
            
            double h_i1 = pe_in1.getEnthalpy();
            double h_i2 = pe_in2.getEnthalpy();
            double h_o1 = pe_out1.getEnthalpy();
            double h_o2 = pe_out2.getEnthalpy();
            double m1 = pe_in1.getMassRate();
            double m2 = pe_in2.getMassRate();
            
            //System.out.println("h_i1: "+h_i1+", h_o1: "+h_o1+", m1: "+m1);
            //System.out.println("h_i2: "+h_i2+", h_o2: "+h_o2+", m2: "+m2);
            return m1 * h_i1 + m2 * h_i2 - m1 * h_o1 - m2 * h_o2;
        }   
    }
    
    private static class ExtractedFlowStateEquation extends ThermalEquation{

        private final Flow flow;
        
        private ExtractedFlowStateEquation(Flow flow){
            
            super();
            this.flow = flow;
            setName("Extracted Flow State Equation");
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
    
    public ClosedFeedWaterHeater() {
        super("Closed Feed Water Heater", "closed_feed_heater");
    }
    
    @Override
    protected void configureEquations() throws ConfigurationException {
        
        insureFluids();
        Flow fi1 = flowManager.getIn(0);
        Flow fi2 = flowManager.getIn(1);
        Flow fo1 = flowManager.getOut(0);
        Flow fo2 = flowManager.getOut(1);
        
        Property massIn1 = fi1.getMassRateProp();
        Property massIn2 = fi2.getMassRateProp();
        Property massOut1 = fo1.getMassRateProp();
        Property massOut2 = fo2.getMassRateProp();
        
        addEquation(new ExtractedFlowStateEquation(fo2));
        addEquation(new MassConservationEquation(massIn1, massOut1));
        addEquation(new MassConservationEquation(massIn2, massOut2));
        addEquation(new EnergyBalanceEquation(fi1, fi2, fo1, fo2));
        
        addEquation(new CommonEquations.PropertyMatchEquation(
                fi1.getPressureProp(), fo1.getPressureProp()));
        addEquation(new CommonEquations.PropertyMatchEquation(
                fi2.getPressureProp(), fo2.getPressureProp()));
        
        addEquation(new Pipe.FlowPropertyEquation(fi1, fi1.getEnthalpyProp()));
        addEquation(new Pipe.FlowPropertyEquation(fi2, fi2.getEnthalpyProp()));
        addEquation(new Pipe.FlowPropertyEquation(fo1, fo1.getEnthalpyProp()));
        addEquation(new Pipe.FlowPropertyEquation(fo2, fo2.getEnthalpyProp()));
    }
    
    @Override
    protected List<FieldResult> getEnergyOutputResults() {
        
        Flow fo1 = flowManager.getOut(0);
        Flow fo2 = flowManager.getOut(1);
        
        List<FieldResult> res = new ArrayList<FieldResult>();
        
        res.add(FieldResult.create("Feed Line Exit Temperature", fo1.getTemperature() ));
        res.add(FieldResult.create("Feed Line Exit Pressure", fo1.getPressure() ));
        res.add(FieldResult.create("Feed Line Exit Vapour Fraction", fo1.getVapourFraction() ));
        
        res.add(FieldResult.create("Extraction Line Exit Temperature", fo2.getTemperature() ));
        res.add(FieldResult.create("Extraction Line Exit Pressure", fo2.getPressure() ));
        res.add(FieldResult.create("Extraction Line Exit Vapour Fraction", fo2.getVapourFraction() ));
        
        return res;
    }
    
    private final Property exergyDestruction = new Property("Exergy Destruction");
    private final Property secondLawEfficiency = new Property("Second Law Efficiency");
    
    @Override
    public void calculateExergy(){
        
        super.calculateExergy();
        
        double Xin = 0.0;
        for(int i = 0; i < flowManager.getInFlowsNum(); i++){
            
            Flow f = flowManager.getIn(i);
            double m = f.getMassRate();
            double x = f.getExergy();
            
            Xin += m * x;
        }
        
        double Xout = 0.0;
        for(int i = 0; i < flowManager.getOutFlowsNum(); i++){
            
            Flow f = flowManager.getOut(i);
            double m = f.getMassRate();
            double x = f.getExergy();
            
            Xout += m * x;
        }
        
        double Xdest = Xin - Xout;
        double Xeff = (Xin - Xdest) / Xin;
        
        exergyDestruction.setValue(Xdest);
        secondLawEfficiency.setValue(Xeff);
    }
    
    @Override
    public List<FieldResult> getExergyResults() {
        
        List<FieldResult> res = new ArrayList<FieldResult>();
        
        res.add(FieldResult.create("Xdest", "Exergy Destruction", exergyDestruction.getValue()) );
        res.add(FieldResult.create("Xeff", "Second Law Efficiency", secondLawEfficiency.getValue()) );
        
        return res; 
    }
}
