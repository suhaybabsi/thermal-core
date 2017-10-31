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
public class MixingChamber extends SteadyFlowDevice {
    
    public static class MassProportionEquation extends ThermalEquation {
        
        private final Property massIn;
        private final Property massOut;
        private final Property y_p;
        public MassProportionEquation(Property massIn, Property massOut, Property y_p) {
            
            super(massIn, massOut, y_p);
            setName("Mass Conservation Equation");
            this.massIn = massIn;
            this.massOut = massOut;
            this.y_p = y_p;
        }
        @Override
        protected double evaluate() {
            
            double m_in = massIn.getValue();
            double m_out = massOut.getValue();
            double y = y_p.getValue();
            
            return m_in - y * m_out;
        }   
    }
    
    public static class MassConservationEquation extends ThermalEquation {
        
        private final Property massIn1;
        private final Property massIn2;
        private final Property massOut;
        public MassConservationEquation(Property massIn1, Property massIn2, Property massOut) {
            
            super(massIn1, massIn2, massOut);
            setName("Mass Conservation Equation");
            this.massIn1 = massIn1;
            this.massIn2 = massIn2;
            this.massOut = massOut;
        }
        
        @Override
        protected double evaluate() {
            
            double m_in1 = massIn1.getValue();
            double m_in2 = massIn2.getValue();
            double m_out = massOut.getValue();
            return m_in1 + m_in2 - m_out;
        }   
    }
    public static class EnergyBalanceEquation extends ThermalEquation {

        private final Flow pe_in1;
        private final Flow pe_in2;
        private final Flow pe_out;
        private final Property y_p;
        
        public EnergyBalanceEquation(Flow pe_in1, Flow pe_in2, Flow pe_out, Property y_p) {
            
            super(y_p, 
                    pe_in1.getEnthalpyProp(), 
                    pe_in2.getEnthalpyProp(), 
                    pe_out.getEnthalpyProp());
            
            setName("Energy Balance Equation");
            this.pe_in1 = pe_in1;
            this.pe_in2 = pe_in2;
            this.pe_out = pe_out;
            this.y_p = y_p;
        }
        
        @Override
        protected double evaluate() {
            
            double h1 = pe_in1.getEnthalpy();
            double h2 = pe_in2.getEnthalpy();
            double h3 = pe_out.getEnthalpy();
            double y = y_p.getValue();
            
            //System.out.println("h1: "+h1+", h2: "+h2+", h3: "+h3+", y: "+y);
            return y * h1 + (1 - y) * h2 - h3;
        }   
    }
    
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
    
    private final Property pressureProp = new Property("Pressure");
    
    public void definePressure(double pressure) {
        this.pressureProp.define(pressure);
    }
    
    protected MixingChamber(String name, String jsonType) {
        super(name, jsonType);
    }
    public MixingChamber() {
        super("Mixing Chamber", "mixing_chamber");
    }

    @Override
    protected void configureEquations() throws ConfigurationException {
        insureFluids();
        
        Flow fi1 = flowManager.getIn(0);
        Flow fi2 = flowManager.getIn(1);
        Flow fo = flowManager.getOut(0);
        
        Property massIn1 = fi1.getMassRateProp();
        Property massIn2 = fi2.getMassRateProp();
        Property massOut = fo.getMassRateProp();
        
        Property y_p = new Property("y-proportion");
        y_p.setMin(0);
        y_p.setMax(1.0);
        
        addEquation(new CommonEquations.PropertyMatchEquation(fi1.getPressureProp(), pressureProp));
        addEquation(new CommonEquations.PropertyMatchEquation(fi2.getPressureProp(), pressureProp));
        addEquation(new CommonEquations.PropertyMatchEquation(fo.getPressureProp(), pressureProp));
        
        addEquation(new MassConservationEquation(massIn1, massIn2, massOut));
        addEquation(new EnergyBalanceEquation(fi1, fi2, fo, y_p));
        addEquation(new MassProportionEquation(massIn1, massOut, y_p));
        
        addEquation(new Pipe.FlowPropertyEquation(fi1, fi1.getEnthalpyProp()));
        addEquation(new Pipe.FlowPropertyEquation(fi2, fi2.getEnthalpyProp()));
        addEquation(new Pipe.FlowPropertyEquation(fo, fo.getEnthalpyProp()));
    }
    
    public double getPressure() {
        return pressureProp.getValue();
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
